package com.mall.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

@SpringBootTest
public class JwtAuthenticationGatewayFilterFactoryTest {

    @Autowired
    private JwtAuthenticationGatewayFilterFactory filterFactory;

    @Test
    public void testWhiteListPath() {
        GatewayFilter filter = filterFactory.apply(new JwtAuthenticationGatewayFilterFactory.Config());
        
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/auth/login")
            .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        Mono<Void> result = filter.filter(exchange, chain -> Mono.empty());
        
        StepVerifier.create(result)
            .expectComplete()
            .verify(Duration.ofSeconds(5));
    }
}