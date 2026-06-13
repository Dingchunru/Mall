package com.mall.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 请求日志过滤器 - 生产级
 * 记录请求方法、路径、状态码、耗时，并注入TraceId
 */
@Slf4j
@Component
public class LoggingGatewayFilterFactory extends
        AbstractGatewayFilterFactory<LoggingGatewayFilterFactory.Config> {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    public LoggingGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public String name() {
        return "Logging";
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            final long startTime = System.currentTimeMillis();

            // 生成或获取 TraceId
            String headerTraceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);
            final String traceId = (headerTraceId != null && !headerTraceId.isEmpty())
                    ? headerTraceId
                    : UUID.randomUUID().toString().replace("-", "").substring(0, 16);

            // 设置 TraceId 到 exchange 属性（供后续使用）
            exchange.getAttributes().put("traceId", traceId);

            // 将 TraceId 添加到请求头，传递给下游微服务
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(TRACE_ID_HEADER, traceId)
                    .build();
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            // 请求日志
            if (config.isLogHeaders()) {
                log.info("[{}] --> {} {} Headers: {}",
                        traceId,
                        exchange.getRequest().getMethod(),
                        exchange.getRequest().getURI().getPath(),
                        maskSensitiveHeaders(exchange.getRequest().getHeaders()));
            } else {
                log.info("[{}] --> {} {}",
                        traceId,
                        exchange.getRequest().getMethod(),
                        exchange.getRequest().getURI().getPath());
            }

            return chain.filter(mutatedExchange).then(Mono.fromRunnable(() -> {
                long duration = System.currentTimeMillis() - startTime;
                int statusCode = mutatedExchange.getResponse().getStatusCode() != null ?
                        mutatedExchange.getResponse().getStatusCode().value() : 0;

                if (statusCode >= 500) {
                    log.error("[{}] <-- {} {} {} {}ms",
                            traceId, mutatedExchange.getRequest().getMethod(),
                            mutatedExchange.getRequest().getURI().getPath(),
                            statusCode, duration);
                } else if (statusCode >= 400) {
                    log.warn("[{}] <-- {} {} {} {}ms",
                            traceId, mutatedExchange.getRequest().getMethod(),
                            mutatedExchange.getRequest().getURI().getPath(),
                            statusCode, duration);
                } else {
                    log.info("[{}] <-- {} {} {} {}ms",
                            traceId, mutatedExchange.getRequest().getMethod(),
                            mutatedExchange.getRequest().getURI().getPath(),
                            statusCode, duration);
                }
            }));
        };
    }

    /**
     * 脱敏敏感请求头（Authorization、Cookie 等）
     */
    private String maskSensitiveHeaders(org.springframework.http.HttpHeaders headers) {
        if (headers == null || headers.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        headers.forEach((key, value) -> {
            if ("authorization".equalsIgnoreCase(key) || "cookie".equalsIgnoreCase(key)) {
                sb.append(key).append("=***, ");
            } else {
                sb.append(key).append("=").append(value).append(", ");
            }
        });
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("}");
        return sb.toString();
    }

    public static class Config {
        private boolean logHeaders = false;

        public boolean isLogHeaders() {
            return logHeaders;
        }

        public void setLogHeaders(boolean logHeaders) {
            this.logHeaders = logHeaders;
        }
    }
}