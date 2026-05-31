package com.mall.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    
    // 白名单路径
    private static final List<String> WHITE_LIST = Arrays.asList(
        "/auth/login",
        "/auth/register",
        "/auth/captcha",
        "/product/list",
        "/product/detail/**",
        "/search/**",
        "/actuator/health"
    );

    public JwtAuthenticationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            
            log.info("Request path: {}", path);
            
            // 检查是否是白名单路径
            if (isWhiteListed(path)) {
                log.info("Path is white listed: {}", path);
                return chain.filter(exchange);
            }

            // 获取token
            String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (token == null || !token.startsWith("Bearer ")) {
                log.warn("No valid token found for path: {}", path);
                return unauthorizedResponse(exchange, "Missing or invalid token");
            }

            token = token.substring(7);
            
            // 验证token
            if (!jwtUtils.validateToken(token)) {
                log.warn("Invalid token for path: {}", path);
                return unauthorizedResponse(exchange, "Invalid token");
            }

            try {
                // 解析token并传递用户信息
                var claims = jwtUtils.parseToken(token);
                String userId = claims.get("userId").toString();
                String username = claims.get("username").toString();
                
                log.info("User authenticated: userId={}, username={}", userId, username);
                
                // 将用户信息添加到请求头
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-Username", username)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());
                
            } catch (Exception e) {
                log.error("Token parsing error", e);
                return unauthorizedResponse(exchange, "Token parsing failed");
            }
        };
    }

    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        Result<?> result = Result.error(HttpStatus.UNAUTHORIZED.value(), message);
        
        try {
            byte[] bits = objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bits);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error writing response", e);
            return response.setComplete();
        }
    }

    public static class Config {
        // 配置属性
    }
}