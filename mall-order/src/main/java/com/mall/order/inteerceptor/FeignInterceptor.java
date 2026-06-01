// FeignInterceptor.java
package com.mall.order.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class FeignInterceptor implements RequestInterceptor {
    
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) 
                RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // 传递用户ID
            String userId = request.getHeader("X-User-Id");
            if (userId != null) {
                template.header("X-User-Id", userId);
            }
            
            // 传递token
            String token = request.getHeader("Authorization");
            if (token != null) {
                template.header("Authorization", token);
            }
        }
    }
}