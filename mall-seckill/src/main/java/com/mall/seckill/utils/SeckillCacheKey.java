package com.mall.seckill.utils;

public class SeckillCacheKey {
    
    private static final String PREFIX = "seckill:";
    
    public static String getStockKey(Long seckillId) {
        return PREFIX + "stock:" + seckillId;
    }
    
    public static String getProductKey(Long seckillId) {
        return PREFIX + "product:" + seckillId;
    }
    
    public static String getUserSeckillKey(Long seckillId, Long userId) {
        return PREFIX + "user:" + seckillId + ":" + userId;
    }
    
    public static String getResultKey(Long seckillId, Long userId) {
        return PREFIX + "result:" + seckillId + ":" + userId;
    }
    
    public static String getCurrentSeckillKey() {
        return PREFIX + "current:list";
    }
}