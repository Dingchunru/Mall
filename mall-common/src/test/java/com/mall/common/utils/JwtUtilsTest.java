package com.mall.common.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", "testSecretKey12345678901234567890");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 3600000L); // 1小时
    }

    @Test
    void testGenerateAndParseToken() {
        // 生成token
        Long userId = 1001L;
        String username = "testUser";
        String token = jwtUtils.generateToken(userId, username);

        assertThat(token).isNotNull();

        // 解析token
        Claims claims = jwtUtils.parseToken(token);
        assertThat(claims.get("userId", Long.class)).isEqualTo(userId);
        assertThat(claims.get("username", String.class)).isEqualTo(username);
    }

    @Test
    void testGetUserIdFromToken() {
        Long userId = 1001L;
        String token = jwtUtils.generateToken(userId, "testUser");

        Long extractedUserId = jwtUtils.getUserIdFromToken(token);
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void testGetUsernameFromToken() {
        String username = "testUser";
        String token = jwtUtils.generateToken(1001L, username);

        String extractedUsername = jwtUtils.getUsernameFromToken(token);
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    void testValidateToken() {
        String token = jwtUtils.generateToken(1001L, "testUser");
        boolean isValid = jwtUtils.validateToken(token);
        assertThat(isValid).isTrue();

        boolean isInvalid = jwtUtils.validateToken("invalid.token.here");
        assertThat(isInvalid).isFalse();
    }
}