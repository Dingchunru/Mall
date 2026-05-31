package com.mall.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    
    // 系统错误
    SYSTEM_ERROR(500, "系统内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    // 认证授权错误
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    TOKEN_EXPIRED(401001, "Token已过期"),
    TOKEN_INVALID(401002, "Token无效"),
    
    // 参数错误
    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(400001, "缺少必要参数"),
    PARAM_INVALID(400002, "参数无效"),
    
    // 业务错误
    BUSINESS_ERROR(400001, "业务处理失败"),
    DATA_NOT_FOUND(404001, "数据不存在"),
    DATA_EXISTS(409001, "数据已存在"),
    
    // 用户错误
    USER_NOT_FOUND(404101, "用户不存在"),
    USER_PASSWORD_ERROR(400101, "密码错误"),
    USER_LOCKED(403101, "用户已锁定"),
    USER_DISABLED(403102, "用户已禁用"),
    
    // 商品错误
    PRODUCT_NOT_FOUND(404201, "商品不存在"),
    PRODUCT_STOCK_ERROR(400201, "库存不足"),
    PRODUCT_OFF_SHELF(400202, "商品已下架"),
    
    // 订单错误
    ORDER_NOT_FOUND(404301, "订单不存在"),
    ORDER_STATUS_ERROR(400301, "订单状态错误"),
    ORDER_CANNOT_CANCEL(400302, "订单不能取消"),
    
    // 秒杀错误
    SECKILL_NOT_START(400401, "秒杀未开始"),
    SECKILL_ENDED(400402, "秒杀已结束"),
    SECKILL_STOCK_ERROR(400403, "库存不足"),
    SECKILL_REPEAT(400404, "不能重复秒杀"),
    
    // 限流错误
    RATE_LIMIT(429, "请求太频繁"),
    ;
    
    private final Integer code;
    private final String message;
    
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
