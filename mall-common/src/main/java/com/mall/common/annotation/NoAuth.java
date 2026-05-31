package com.mall.common.annotation;

import java.lang.annotation.*;

/**
 * 免认证注解
 * 添加该注解的方法或类不需要进行token验证
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoAuth {
}