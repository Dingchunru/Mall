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
 *
 * <p>修复说明：转义顺序至关重要，必须先转义 & 符号，再转义其他字符，
 * 否则会导致已经包含 & 的转义结果被二次转义（如 &lt; 变成 &amp;lt;）。</p>
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

        /**
         * XSS清理 - 正确的转义顺序：先转义 &，再转义其他字符
         *
         * <p>错误顺序（会导致双重转义）：
         * <pre>
         *   "<" -> "&lt;" 然后 "&" -> "&amp;" 结果："&amp;lt;" ❌
         * </pre>
         *
         * <p>正确顺序：
         * <pre>
         *   "&" -> "&amp;" 然后 "<" -> "&lt;" 结果："&lt;" ✅
         * </pre>
         */
        private String cleanXss(String value) {
            if (value == null || value.isEmpty()) {
                return value;
            }

            String cleaned = value;

            // 第一步：先转义 & 符号（必须最先处理，防止后续转义结果被二次处理）
            cleaned = cleaned.replace("&", "&amp;");

            // 第二步：转义其他HTML特殊字符
            cleaned = cleaned.replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;")
                    .replace("(", "&#40;")
                    .replace(")", "&#41;");

            // 第三步：清理可能的JavaScript事件处理器和危险协议
            cleaned = cleaned.replaceAll("(?i)javascript:", "")
                    .replaceAll("(?i)data:", "")
                    .replaceAll("(?i)vbscript:", "")
                    .replaceAll("(?i)on\\w+\\s*=", "")
                    .replaceAll("<script", "&lt;script")
                    .replaceAll("</script", "&lt;/script");

            return cleaned;
        }
    }
}
