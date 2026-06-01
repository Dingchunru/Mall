package com.mall.common.filter;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * XSS攻击防护过滤器
 * 对请求参数进行HTML转义
 */
@Component
@Order(1)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
    }

    /**
     * 包装HttpServletRequest，重写获取参数的方法
     */
    static class XssRequestWrapper extends HttpServletRequestWrapper {

        public XssRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return cleanXss(value);
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;
            String[] cleaned = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                cleaned[i] = cleanXss(values[i]);
            }
            return cleaned;
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            return cleanXss(value);
        }

        private String cleanXss(String value) {
            if (value == null) return null;
            // 转义特殊HTML字符
            return value
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;")
                    .replace("&", "&amp;")
                    .replace("(", "&#40;")
                    .replace(")", "&#41;");
        }
    }
}