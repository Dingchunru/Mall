package com.mall.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GatewayIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private String validToken;

    @BeforeEach
    void setUp() {
        // 获取有效token
        validToken = getValidToken();
    }

    @Test
    public void testPublicEndpoint() {
        webTestClient.get()
            .uri("/api/product/list")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    public void testProtectedEndpointWithoutToken() {
        webTestClient.get()
            .uri("/api/user/info")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    public void testProtectedEndpointWithValidToken() {
        webTestClient.get()
            .uri("/api/user/info")
            .header("Authorization", "Bearer " + validToken)
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    public void testRateLimit() {
        for (int i = 0; i < 10; i++) {
            webTestClient.get()
                .uri("/api/product/list")
                .exchange()
                .expectStatus().isOk();
        }
        
        // 第11次请求应该被限流
        webTestClient.get()
            .uri("/api/product/list")
            .exchange()
            .expectStatus().isEqualTo(429); // Too Many Requests
    }

    private String getValidToken() {
        // 调用认证服务获取token
        return "test-valid-token";
    }
}