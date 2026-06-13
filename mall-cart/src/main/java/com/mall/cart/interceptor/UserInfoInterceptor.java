package com.mall.cart.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户信息拦截器
 *
 * <p>从网关传递的请求头中提取用户信息，存入ThreadLocal，供后续业务使用。</p>
 *
 * <p>提取的信息：</p>
 * <ul>
 *   <li>X-User-Id - 用户ID</li>
 *   <li>X-Username - 用户名</li>
 *   <li>X-User-Role - 用户角色</li>
 * </ul>
 */
@Component
public class UserInfoInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<UserContext> USER_CONTEXT = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdStr = request.getHeader("X-User-Id");
        String username = request.getHeader("X-Username");
        String role = request.getHeader("X-User-Role");

        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                Long userId = Long.parseLong(userIdStr);
                USER_CONTEXT.set(new UserContext(userId, username, role));
            } catch (NumberFormatException e) {
                // 用户ID格式错误，忽略
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        USER_CONTEXT.remove();
    }

    public static Long getCurrentUserId() {
        UserContext context = USER_CONTEXT.get();
        return context != null ? context.getUserId() : null;
    }

    public static String getCurrentUsername() {
        UserContext context = USER_CONTEXT.get();
        return context != null ? context.getUsername() : null;
    }

    public static String getCurrentUserRole() {
        UserContext context = USER_CONTEXT.get();
        return context != null ? context.getRole() : null;
    }

    public static boolean isAdmin() {
        return "admin".equals(getCurrentUserRole());
    }

    /**
     * 用户上下文信息
     */
    private static class UserContext {
        private final Long userId;
        private final String username;
        private final String role;

        public UserContext(Long userId, String username, String role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }
    }
}
