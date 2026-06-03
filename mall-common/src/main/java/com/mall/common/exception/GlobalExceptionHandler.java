package com.mall.common.exception;

import com.mall.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        HttpServletRequest request = getRequest();
        return Result.error(e.getCode(), e.getMessage()).path(request != null ? request.getRequestURI() : "");
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数验证失败: {}", message);
        HttpServletRequest request = getRequest();
        return Result.error(ErrorCode.PARAM_INVALID.getCode(), message).path(request != null ? request.getRequestURI() : "");
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数绑定失败: {}", message);
        HttpServletRequest request = getRequest();
        return Result.error(ErrorCode.PARAM_INVALID.getCode(), message).path(request != null ? request.getRequestURI() : "");
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("约束违反: {}", e.getMessage());
        HttpServletRequest request = getRequest();
        return Result.error(ErrorCode.PARAM_INVALID.getCode(), e.getMessage()).path(request != null ? request.getRequestURI() : "");
    }

    /**
     * 处理唯一键冲突
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<?> handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("数据重复: {}", e.getMessage());
        HttpServletRequest request = getRequest();
        return Result.error(ErrorCode.DATA_EXISTS.getCode(), "数据已存在，请勿重复提交")
                .path(request != null ? request.getRequestURI() : "");
    }

    /**
     * 处理数据库操作异常
     */
    @ExceptionHandler(DataAccessException.class)
    public Result<?> handleDataAccessException(DataAccessException e) {
        log.error("数据库操作异常", e);
        HttpServletRequest request = getRequest();
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "数据操作失败")
                .path(request != null ? request.getRequestURI() : "");
    }

    /**
     * 处理请求体格式错误
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("请求体解析失败: {}", e.getMessage());
        HttpServletRequest request = getRequest();
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), "请求参数格式错误，请检查JSON格式")
                .path(request != null ? request.getRequestURI() : "");
    }


    /**
     * 处理请求方法不正确
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("请求方法不支持: {}", e.getMessage());
        HttpServletRequest request = getRequest();
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), "请求方式不支持: " + e.getMethod())
                .path(request != null ? request.getRequestURI() : "");
    }


    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        HttpServletRequest request = getRequest();
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统繁忙，请稍后重试").path(request != null ? request.getRequestURI() : "");
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}