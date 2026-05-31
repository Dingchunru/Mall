package com.mall.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Component
public class RateLimitFilter extends AbstractGatewayFilterFactory<RateLimitFilter.Config> {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    public RateLimitFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientIp = exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
                : "unknown";
            
            // 根据IP和路径组合成key
            String key = "rate_limit:" + clientIp + ":" + exchange.getRequest().getPath();
            
            // 使用简单的Redis操作进行计数限流
            return redisTemplate.opsForValue()
                .increment(key)
                .flatMap(count -> {
                    // 如果是第一次访问，设置过期时间
                    if (count == 1) {
                        return redisTemplate.expire(key, Duration.ofSeconds(config.getDuration()))
                            .then(Mono.just(count));
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    // 检查是否超过限制
                    if (count > config.getLimit()) {
                        // 超过限制，返回429状态码
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(config.getLimit()));
                        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");
                        return exchange.getResponse().setComplete();
                    } else {
                        // 添加响应头，告知客户端限流信息
                        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(config.getLimit()));
                        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", 
                            String.valueOf(config.getLimit() - count));
                        // 未超过限制，继续处理请求
                        return chain.filter(exchange);
                    }
                })
                .onErrorResume(e -> {
                    // 发生错误时记录日志并允许请求通过，避免限流故障影响业务
                    System.err.println("Rate limit filter error: " + e.getMessage());
                    return chain.filter(exchange);
                });
        };
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("limit", "duration");
    }

    public static class Config {
        private int limit = 100; // 请求次数限制
        private int duration = 60; // 时间窗口（秒）
        
        public int getLimit() {
            return limit;
        }
        
        public void setLimit(int limit) {
            this.limit = limit;
        }
        
        public int getDuration() {
            return duration;
        }
        
        public void setDuration(int duration) {
            this.duration = duration;
        }
    }
}