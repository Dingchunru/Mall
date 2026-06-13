package com.mall.gateway.filter;

import com.mall.common.response.Result;
import com.mall.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 基于Redis的令牌桶限流过滤器
 *
 * <p>限流策略：</p>
 * <ul>
 *   <li>按用户ID限流：已登录用户按用户ID限流</li>
 *   <li>按IP限流：未登录用户按IP限流</li>
 *   <li>支持配置不同的速率限制（每秒请求数）</li>
 * </ul>
 *
 * <p>使用Redis Lua脚本保证原子性：</p>
 * <pre>
 *   1. 获取当前桶中的令牌数
 *   2. 计算时间间隔内应添加的令牌
 *   3. 如果令牌足够，扣减并返回成功
 *   4. 如果令牌不足，返回失败
 * </pre>
 */
@Slf4j
@Component
public class RateLimitFilter extends AbstractGatewayFilterFactory<RateLimitFilter.Config> {

    private final ReactiveStringRedisTemplate redisTemplate;

    /**
     * 令牌桶限流Lua脚本
     *
     * KEYS[1]: 限流key
     * ARGV[1]: 速率（每秒产生的令牌数）
     * ARGV[2]: 桶容量（最大令牌数）
     * ARGV[3]: 当前时间戳（毫秒）
     */
    private static final String RATE_LIMIT_LUA =
            "local key = KEYS[1]\n" +
            "local rate = tonumber(ARGV[1])\n" +
            "local capacity = tonumber(ARGV[2])\n" +
            "local now = tonumber(ARGV[3])\n" +
            "local interval = 1000 / rate\n" +  // 每个令牌的生成间隔（毫秒）
            "\n" +
            "local bucket = redis.call('hmget', key, 'tokens', 'last_time')\n" +
            "local tokens = tonumber(bucket[1]) or capacity\n" +
            "local last_time = tonumber(bucket[2]) or now\n" +
            "\n" +
            "-- 计算新增的令牌\n" +
            "local elapsed = now - last_time\n" +
            "local added = math.floor(elapsed / interval)\n" +
            "tokens = math.min(capacity, tokens + added)\n" +
            "\n" +
            "-- 判断是否允许通过\n" +
            "if tokens >= 1 then\n" +
            "    tokens = tokens - 1\n" +
            "    redis.call('hmset', key, 'tokens', tokens, 'last_time', now)\n" +
            "    redis.call('pexpire', key, 60000)\n" +  // 60秒过期
            "    return 1\n" +
            "else\n" +
            "    redis.call('hmset', key, 'tokens', tokens, 'last_time', now)\n" +
            "    redis.call('pexpire', key, 60000)\n" +
            "    return 0\n" +
            "end";

    private final RedisScript<Long> rateLimitScript;

    public RateLimitFilter(ReactiveStringRedisTemplate redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = RedisScript.of(RATE_LIMIT_LUA, Long.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // 构建限流key
            String limitKey = buildLimitKey(request);
            long now = System.currentTimeMillis();

            List<String> keys = Collections.singletonList(limitKey);
            List<String> args = Arrays.asList(
                    String.valueOf(config.getRate()),
                    String.valueOf(config.getCapacity()),
                    String.valueOf(now)
            );

            return redisTemplate.execute(rateLimitScript, keys, args)
                    .next()
                    .flatMap(allowed -> {
                        if (allowed != null && allowed == 1L) {
                            // 限流通过
                            return chain.filter(exchange);
                        } else {
                            // 触发限流
                            log.warn("触发限流: key={}, path={}, rate={}, capacity={}",
                                    limitKey, path, config.getRate(), config.getCapacity());
                            return rateLimited(exchange.getResponse(), config);
                        }
                    })
                    .onErrorResume(e -> {
                        log.error("限流检查异常: {}", e.getMessage());
                        // Redis异常时，降级为放行（保证可用性）
                        return chain.filter(exchange);
                    });
        };
    }

    /**
     * 构建限流Key
     *
     * <p>优先级：</p>
     * <ol>
     *   <li>已登录用户：rate_limit:user:{userId}</li>
     *   <li>未登录用户：rate_limit:ip:{clientIp}</li>
     * </ol>
     */
    private String buildLimitKey(ServerHttpRequest request) {
        // 尝试获取用户ID
        List<String> userIdHeaders = request.getHeaders().get("X-User-Id");
        if (userIdHeaders != null && !userIdHeaders.isEmpty()) {
            return "rate_limit:user:" + userIdHeaders.get(0);
        }

        // 获取客户端IP
        String clientIp = getClientIp(request);
        return "rate_limit:ip:" + clientIp;
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(ServerHttpRequest request) {
        // 优先从X-Forwarded-For获取（经过代理时）
        String xff = request.getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }

        // 从X-Real-IP获取
        String xri = request.getHeaders().getFirst("X-Real-IP");
        if (xri != null && !xri.isEmpty()) {
            return xri;
        }

        // 直接连接IP
        return request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    /**
     * 返回429限流响应
     */
    private Mono<Void> rateLimited(ServerHttpResponse response, Config config) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set("X-RateLimit-Limit", String.valueOf(config.getRate()));
        response.getHeaders().set("X-RateLimit-Window", "1");

        Result<Void> result = Result.error(429, "请求过于频繁，请稍后再试");
        byte[] bytes = JsonUtils.toJson(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 配置类
     */
    public static class Config {
        /**
         * 每秒产生的令牌数（默认10）
         */
        private int rate = 10;
        /**
         * 桶容量（最大突发请求数，默认20）
         */
        private int capacity = 20;

        public int getRate() {
            return rate;
        }

        public void setRate(int rate) {
            this.rate = rate;
        }

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }
    }
}
