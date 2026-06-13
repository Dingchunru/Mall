package com.mall.common.exception;

import lombok.Getter;

/**
 * 统一错误码规范
 * 编码规则：模块号(3位) + 错误序号(3位)
 *
 * 模块划分：
 * 0xx - 系统通用 (code 范围: 400-599, 以标准HTTP状态码为基础)
 *   001xxx - 参数校验
 *   002xxx - 认证授权
 *   003xxx - 数据操作
 * 1xx - 用户模块
 * 2xx - 商品模块
 * 3xx - 订单模块
 * 4xx - 秒杀模块 (实际错误码为 004xxx)
 * 5xx - 购物车模块
 * 6xx - 搜索模块
 */
@Getter
public enum ErrorCode {

    // 参数校验 001xxx
    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(400001, "缺少必要参数"),
    PARAM_TYPE_ERROR(400002, "参数类型错误"),
    PARAM_INVALID(400003, "参数格式无效"),

    // 认证授权 002xxx
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "无权限访问"),
    TOKEN_EXPIRED(401001, "Token已过期"),
    TOKEN_INVALID(401002, "Token无效"),
    TOKEN_SIGNATURE_ERROR(401003, "Token签名错误"),
    REPEAT_REQUEST(401004, "请求正在处理中，请勿重复提交"),

    // 数据操作 003xxx
    DATA_NOT_FOUND(404, "数据不存在"),
    DATA_EXISTS(409, "数据已存在"),
    DATA_UPDATE_FAILED(500001, "数据更新失败"),
    DATA_DELETE_FAILED(500002, "数据删除失败"),

    // 系统级错误
    SYSTEM_ERROR(500, "系统繁忙，请稍后重试"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    RATE_LIMIT(429, "请求太频繁，请稍后重试"),
    GATEWAY_TIMEOUT(504, "网关超时"),

    USER_NOT_FOUND(100001, "用户不存在"),
    USER_PASSWORD_ERROR(100002, "密码错误"),
    USER_LOCKED(100003, "用户已被锁定"),
    USER_DISABLED(100004, "用户已被禁用"),
    USER_EXISTS(100005, "用户名或手机号已存在"),
    USERNAME_OR_PASSWORD_ERROR(100006, "用户名或密码错误"),

    PRODUCT_NOT_FOUND(200001, "商品不存在"),
    PRODUCT_OFF_SHELF(200002, "商品已下架"),
    PRODUCT_STOCK_INSUFFICIENT(200003, "商品库存不足"),
    CATEGORY_NOT_FOUND(200004, "分类不存在"),

    ORDER_NOT_FOUND(300001, "订单不存在"),
    ORDER_STATUS_ERROR(300002, "订单状态异常"),
    ORDER_CANNOT_CANCEL(300003, "订单不可取消"),
    ORDER_CANNOT_DELETE(300004, "订单不可删除"),

    SECKILL_NOT_START(400001, "秒杀活动未开始"),
    SECKILL_ENDED(400002, "秒杀活动已结束"),
    SECKILL_STOCK_EMPTY(400003, "秒杀库存已空"),
    SECKILL_REPEAT(400004, "您已参与过本次秒杀"),

    CART_ITEM_NOT_FOUND(500001, "购物车商品不存在"),
    CART_ITEM_LIMIT(500002, "购物车商品数量已达上限"),

    SEARCH_KEYWORD_EMPTY(600001, "搜索关键词不能为空"),
    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}