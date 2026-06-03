package com.mall.gateway.handler;

import cn.hutool.json.JSONUtil;
import com.mall.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Order(-1)
@Component
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result;

        if (ex instanceof ResponseStatusException e) {
            response.setStatusCode(HttpStatus.valueOf(e.getStatusCode().value()));
            result = Result.error(e.getStatusCode().value(), e.getReason());
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            result = Result.error(500, "网关内部错误");
            log.error("Gateway 异常: {}", ex.getMessage(), ex);
        }

        byte[] bytes = JSONUtil.toJsonStr(result).getBytes();

        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            return bufferFactory.wrap(bytes);
        }));
    }
}