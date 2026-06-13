package com.mall.common.response;

import com.mall.common.exception.ErrorCode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Getter
@NoArgsConstructor(force = true)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;
    private long timestamp;
    private String path;

    // 国际化占位符
    private transient Object[] messageArgs;

    private Result(int code, String message, T data, Object... args) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
        this.messageArgs = args;
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data, Object... args) {
        return new Result<>(200, message, data, args);
    }

    public static <T> Result<T> error(int code, String message, Object... args) {
        return new Result<>(code, message, null, args);
    }

    public static <T> Result<T> error(ErrorCode errorCode, Object... args) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null, args);
    }

    @JsonIgnore
    public boolean isSuccess() {
        return this.code == 200;
    }

    @JsonIgnore
    public boolean isFail() {
        return !isSuccess();
    }

    public Result<T> path(String path) {
        this.path = path;
        return this;
    }

    public Result<T> data(T data) {
        this.data = data;
        return this;
    }

    private Object readResolve() {
        if (this.messageArgs != null && this.messageArgs.length > 0) {
            this.message = String.format(this.message, this.messageArgs);
        }
        return this;
    }
}