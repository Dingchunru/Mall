package com.mall.common.exception;

import com.mall.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 [{}] {} - path: {}", e.getCode(), e.getMessage(), request.getRequestURI());
        return Result.error(e.getCode(), e.getMessage()).path(request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败 [{}]: {}", request.getRequestURI(), message);
        return Result.error(ErrorCode.PARAM_INVALID, message).path(request.getRequestURI());
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败 [{}]: {}", request.getRequestURI(), message);
        return Result.error(ErrorCode.PARAM_INVALID, message).path(request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.warn("参数约束违反 [{}]: {}", request.getRequestURI(), e.getMessage());
        return Result.error(ErrorCode.PARAM_INVALID, e.getMessage()).path(request.getRequestURI());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingParamException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少必要参数 [{}]: {}", request.getRequestURI(), e.getMessage());
        return Result.error(ErrorCode.PARAM_MISSING, e.getMessage()).path(request.getRequestURI());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<?> handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        log.warn("数据重复 [{}]: {}", request.getRequestURI(), extractMessage(e));
        return Result.error(ErrorCode.DATA_EXISTS).path(request.getRequestURI());
    }

    @ExceptionHandler(DataAccessException.class)
    public Result<?> handleDataAccessException(DataAccessException e, HttpServletRequest request) {
        log.error("数据库异常 [{}]: {}", request.getRequestURI(), extractMessage(e));
        // 不暴露数据库内部信息给前端
        return Result.error(ErrorCode.SYSTEM_ERROR).path(request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("请求体解析失败 [{}]", request.getRequestURI());
        return Result.error(ErrorCode.PARAM_ERROR, "请求参数格式错误").path(request.getRequestURI());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持 [{}]: {}", request.getRequestURI(), e.getMethod());
        return Result.error(ErrorCode.PARAM_ERROR, "请求方式不支持: " + e.getMethod()).path(request.getRequestURI());
    }
    @ExceptionHandler(Exception.class)
    public Result<?> handleUnknownException(Exception e, HttpServletRequest request) {
        log.error("未知异常 [{}]: {}", request.getRequestURI(), e.getMessage(), e);  // 这里保留堆栈
        // TODO: 接入告警，如钉钉/企微机器人通知
        return Result.error(ErrorCode.SYSTEM_ERROR).path(request.getRequestURI());
    }

    /**
     * 提取异常摘要，避免日志过长
     */
    private String extractMessage(Exception e) {
        return e.getMessage() != null && e.getMessage().length() > 200
                ? e.getMessage().substring(0, 200) + "..."
                : e.getMessage();
    }
}