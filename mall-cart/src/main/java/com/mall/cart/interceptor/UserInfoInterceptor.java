package com.mall.cart.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class UserInfoInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<Long> USER_ID_THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头获取用户ID
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr != null) {
            USER_ID_THREAD_LOCAL.set(Long.parseLong(userIdStr));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        USER_ID_THREAD_LOCAL.remove();
    }

    public static Long getCurrentUserId() {
        return USER_ID_THREAD_LOCAL.get();
    }
}