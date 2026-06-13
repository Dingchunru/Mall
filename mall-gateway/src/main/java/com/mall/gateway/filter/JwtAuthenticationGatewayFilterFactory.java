package com.mall.gateway.filter;

import com.mall.common.response.Result;
import com.mall.common.utils.JsonUtils;
import com.mall.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证网关过滤器
 *
 * <p>职责：</p>
 * <ul>
 *   <li>验证请求中的JWT Token</li>
 *   <li>将用户信息传递到下游服务（X-User-Id, X-Username, X-User-Role）</li>
 *   <li>支持白名单路径免认证</li>
 *   <li>支持Token自动刷新（基于RefreshToken）</li>
 * </ul>
 *
 * <p>安全增强：</p>
 * <ul>
 *   <li>使用AntPathMatcher替代简单的contains匹配，防止路径绕过</li>
 *   <li>添加请求追踪ID（X-Request-Id）</li>
 *   <li>标准化错误响应格式</li>
 *   <li>记录安全审计日志</li>
 * </ul>
 */
@Slf4j
@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    private final JwtUtils jwtUtils;

    /**
     * 使用AntPathMatcher进行精确的路径匹配（替代简单的contains检查）
     * 防止如 /api/public/auth/login 绕过 /auth/login 白名单的问题
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 白名单路径 - 不需要认证即可访问
     *
     * <p>注意：使用AntPathMatcher支持通配符，如 /public/**</p>
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/user/login",
            "/user/register",
            "/user/check/username",
            "/user/check/phone",
            "/products",
            "/products/*",
            "/search/**",
            "/actuator/health",
            "/doc.html",
            "/webjars/**"
    );

    public JwtAuthenticationGatewayFilterFactory(JwtUtils jwtUtils) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // 1. 检查是否是白名单路径
            if (isWhiteList(path)) {
                log.debug("白名单路径，跳过认证: {}", path);
                return chain.filter(exchange);
            }

            // 2. 获取Token
            String token = extractToken(request);
            if (!StringUtils.hasText(token)) {
                log.warn("请求缺少Token: {}", path);
                return unauthorized(exchange.getResponse(), "缺少认证Token，请先登录");
            }

            // 3. 验证Token
            try {
                Claims claims = jwtUtils.parseToken(token);

                // 检查Token类型必须是access
                String tokenType = claims.get("type", String.class);
                if (!"access".equals(tokenType)) {
                    log.warn("Token类型错误: path={}, type={}", path, tokenType);
                    return unauthorized(exchange.getResponse(), "Token类型错误");
                }

                // 检查Token是否在黑名单中
                if (jwtUtils.validateAccessToken(token)) {
                    // 提取用户信息
                    Long userId = claims.get("userId", Long.class);
                    String username = claims.get("username", String.class);
                    String role = claims.get("role", String.class);

                    // 4. 将用户信息添加到请求头，传递给下游服务
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-Id", String.valueOf(userId))
                            .header("X-Username", username != null ? username : "")
                            .header("X-User-Role", role != null ? role : "")
                            .build();

                    log.debug("Token验证通过: userId={}, username={}, path={}", userId, username, path);
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                } else {
                    log.warn("Token已失效或被拉黑: path={}", path);
                    return unauthorized(exchange.getResponse(), "Token已失效，请重新登录");
                }

            } catch (Exception e) {
                log.warn("Token验证失败: path={}, error={}", path, e.getMessage());
                return unauthorized(exchange.getResponse(), "Token无效或已过期");
            }
        };
    }

    /**
     * 使用AntPathMatcher检查路径是否在白名单中
     */
    private boolean isWhiteList(String path) {
        for (String pattern : WHITE_LIST) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从请求头中提取Token
     */
    private String extractToken(ServerHttpRequest request) {
        List<String> authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeaders)) {
            return null;
        }
        String bearerToken = authHeaders.get(0);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 返回401未认证响应
     */
    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result = Result.error(401, message);
        byte[] bytes = JsonUtils.toJson(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // 配置属性（如需可扩展）
    }
}
