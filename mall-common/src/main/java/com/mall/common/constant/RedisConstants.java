package com.mall.common.constant;

public class RedisConstants {

    /**
     * 用户相关
     */
    public static final String USER_TOKEN_KEY = "user:token:";
    public static final String USER_INFO_KEY = "user:info:";
    public static final long USER_TOKEN_EXPIRE = 86400L; // 24小时

    /**
     * 商品相关
     */
    public static final String PRODUCT_KEY = "product:";
    public static final String PRODUCT_STOCK_KEY = "product:stock:";
    public static final long PRODUCT_EXPIRE = 3600L; // 1小时

    /**
     * 购物车相关
     */
    public static final String CART_KEY = "cart:";
    public static final long CART_EXPIRE = 604800L; // 7天

    /**
     * 秒杀相关
     */
    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String SECKILL_USER_KEY = "seckill:user:";
    public static final String SECKILL_ORDER_KEY = "seckill:order:";
    public static final long SECKILL_EXPIRE = 86400L; // 24小时

    /**
     * 限流相关
     */
    public static final String RATE_LIMIT_KEY = "rate:limit:";
    public static final long RATE_LIMIT_EXPIRE = 60L; // 1分钟

    /**
     * 分布式锁
     */
    public static final String LOCK_KEY = "lock:";
    public static final long LOCK_EXPIRE = 30L; // 30秒

    /**
     * 验证码
     */
    public static final String CAPTCHA_KEY = "captcha:";
    public static final long CAPTCHA_EXPIRE = 300L; // 5分钟
}