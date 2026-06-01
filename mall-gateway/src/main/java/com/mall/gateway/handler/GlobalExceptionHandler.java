package com.mall.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Order(-1)
@Configuration
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        if (response.isCommitted()) {
            return Mono.error(ex);
        }
        
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            return handleResponseStatusException(response, responseStatusException);
        }
        
        if (ex instanceof NotFoundException) {
            return handleNotFoundException(response, (NotFoundException) ex);
        }
        
        return handleUnknownException(response, ex);
    }

    private Mono<Void> handleResponseStatusException(ServerHttpResponse response,
                                                     ResponseStatusException ex) {
        HttpStatusCode statusCode = ex.getStatusCode();
        String message = ex.getReason();

        log.error("ResponseStatusException: {} - {}", statusCode, message, ex);

        return writeResponse(response, statusCode.value(), message);
    }

    private Mono<Void> handleNotFoundException(ServerHttpResponse response, 
                                               NotFoundException ex) {
        log.error("NotFoundException: {}", ex.getMessage(), ex);
        
        return writeResponse(response, 
            HttpStatus.SERVICE_UNAVAILABLE.value(), 
            "Service not available: " + ex.getMessage());
    }

    private Mono<Void> handleUnknownException(ServerHttpResponse response, 
                                              Throwable ex) {
        log.error("Unknown exception", ex);
        
        return writeResponse(response, 
            HttpStatus.INTERNAL_SERVER_ERROR.value(), 
            "Internal server error");
    }

    private Mono<Void> writeResponse(ServerHttpResponse response, int code, String message) {
        response.setStatusCode(HttpStatus.valueOf(code));
        
        Result<?> result = Result.error(code, message);
        
        try {
            byte[] bits = objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bits);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error writing response", e);
            return response.setComplete();
        }
    }
}