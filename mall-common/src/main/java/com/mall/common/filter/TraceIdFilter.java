package com.mall.common.filter;

import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * TraceId 过滤器 - 用于全链路追踪
 * 每个请求生成唯一TraceId，放入MDC和响应头
 */
@Component
@Order(Integer.MIN_VALUE)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class TraceIdFilter extends OncePerRequestFilter {

    /** 请求头中的TraceId Key */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = request.getHeader(TRACE_ID_HEADER);

        // 如果上游没传，自己生成
        if (traceId == null || traceId.isEmpty()) {
            traceId = generateTraceId();
        }

        // 放入MDC，logback可用 %X{traceId} 打印
        MDC.put("traceId", traceId);

        // 放入响应头，方便前端排查问题
        response.setHeader(TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 必须清理，防止内存泄漏
            MDC.clear();
        }
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}