package com.mall.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
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

            // 请求日志
            if (config.isLogHeaders()) {
                log.info("[{}] --> {} {} Headers: {}",
                        traceId,
                        exchange.getRequest().getMethod(),
                        exchange.getRequest().getURI().getPath(),
                        exchange.getRequest().getHeaders());
            } else {
                log.info("[{}] --> {} {}",
                        traceId,
                        exchange.getRequest().getMethod(),
                        exchange.getRequest().getURI().getPath());
            }

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                long duration = System.currentTimeMillis() - startTime;
                int statusCode = exchange.getResponse().getStatusCode() != null ?
                        exchange.getResponse().getStatusCode().value() : 0;

                if (statusCode >= 500) {
                    log.error("[{}] <-- {} {} {} {}ms",
                            traceId, exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI().getPath(),
                            statusCode, duration);
                } else if (statusCode >= 400) {
                    log.warn("[{}] <-- {} {} {} {}ms",
                            traceId, exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI().getPath(),
                            statusCode, duration);
                } else {
                    log.info("[{}] <-- {} {} {} {}ms",
                            traceId, exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI().getPath(),
                            statusCode, duration);
                }
            }));
        };
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