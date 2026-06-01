package com.mall.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.exception.ErrorCode;
import com.mall.common.response.Result;
import com.mall.common.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationGatewayFilterFactory extends
        AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 白名单路径 - 不需要鉴权
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/auth/captcha",
            "/auth/refresh",
            "/product/list",
            "/product/detail/**",
            "/search/**",
            "/actuator/health",
            "/doc.html",
            "/webjars/**",
            "/v2/api-docs/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/favicon.ico"
    );

    public JwtAuthenticationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public String name() {
        return "JwtAuthentication";
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // 检查白名单
            if (isWhiteListed(path)) {
                log.debug("白名单路径，跳过鉴权: {}", path);
                return chain.filter(exchange);
            }

            // 获取 Token
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("缺少或无效的Authorization头: {}", path);
                return unauthorizedResponse(exchange, ErrorCode.UNAUTHORIZED.getCode(),
                        ErrorCode.UNAUTHORIZED.getMessage());
            }

            String token = authHeader.substring(7);

            // 验证 Token（使用新版 validateAccessToken）
            if (!jwtUtils.validateAccessToken(token)) {
                log.warn("Token验证失败: {}", path);
                return unauthorizedResponse(exchange, ErrorCode.TOKEN_INVALID.getCode(),
                        ErrorCode.TOKEN_INVALID.getMessage());
            }

            // 解析用户信息
            try {
                Long userId = jwtUtils.getUserIdFromToken(token);
                String username = jwtUtils.getUsernameFromToken(token);
                String role = jwtUtils.getRoleFromToken(token);

                log.debug("用户认证通过: userId={}, username={}, role={}, path={}",
                        userId, username, role, path);

                // 将用户信息传递到下游服务
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-Username", username)
                        .header("X-User-Role", role != null ? role : "")
                        .header("X-Trace-Id", exchange.getAttributeOrDefault("traceId", ""))
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                log.error("Token解析失败: {}", e.getMessage());
                return unauthorizedResponse(exchange, ErrorCode.TOKEN_INVALID.getCode(),
                        "Token解析失败");
            }
        };
    }

    /**
     * 判断路径是否在白名单中
     */
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, int code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<?> result = Result.error(code, message)
                .path(exchange.getRequest().getURI().getPath());

        try {
            byte[] bits = objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bits);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("写入响应失败", e);
            return response.setComplete();
        }
    }

    public static class Config {
        // 可扩展配置，如自定义白名单
    }
}