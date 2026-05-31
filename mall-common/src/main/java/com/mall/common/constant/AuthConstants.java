package com.mall.common.constant;

public class AuthConstants {

    /**
     * 认证相关
     */
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 请求头
     */
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USERNAME_HEADER = "X-Username";
    public static final String USER_ROLE_HEADER = "X-User-Role";

    /**
     * 角色
     */
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_SELLER = "ROLE_SELLER";

    /**
     * 权限
     */
    public static final String PERMISSION_ADD = "add";
    public static final String PERMISSION_EDIT = "edit";
    public static final String PERMISSION_DELETE = "delete";
    public static final String PERMISSION_QUERY = "query";

    /**
     * 白名单路径
     */
    public static final String[] WHITE_LIST = {
            "/auth/login",
            "/auth/register",
            "/auth/captcha",
            "/product/list",
            "/product/detail/**",
            "/search/**",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v2/api-docs",
            "/v3/api-docs"
    };
}