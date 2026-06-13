package com.mall.common.filter;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * 请求追踪ID过滤器
 *
 * <p>为每个请求生成唯一的追踪ID（TraceId），用于日志链路追踪。</p>
 *
 * <p>功能：</p>
 * <ul>
 *   <li>如果请求头包含 X-Request-Id，则复用该ID（保证全链路一致）</li>
 *   <li>否则生成新的UUID作为TraceId</li>
 *   <li>将TraceId存入MDC和响应头</li>
 * </ul>
 */
@Component
@Order(0) // 最高优先级，确保最先执行
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String TRACE_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求头获取或生成TraceId
            String traceId = request.getHeader(TRACE_ID_HEADER);
            if (!StringUtils.hasText(traceId)) {
                traceId = generateTraceId();
            }

            // 存入MDC，供日志使用
            MDC.put(TRACE_ID_KEY, traceId);

            // 添加到响应头，方便客户端追踪
            response.setHeader(TRACE_ID_HEADER, traceId);

            filterChain.doFilter(request, response);
        } finally {
            // 清理MDC，防止线程池复用导致污染
            MDC.remove(TRACE_ID_KEY);
        }
    }

    /**
     * 生成追踪ID
     *
     * <p>使用UUID并去除横线，缩短长度。</p>
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取当前请求的TraceId
     */
    public static String getCurrentTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }
}
