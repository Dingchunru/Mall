package com.mall.common.utils;

import com.mall.common.exception.BusinessException;
import com.mall.common.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtils {

    /**
     * 密钥必须从外部配置注入，禁止硬编码默认值
     * 生产环境应在 application-prod.yml 中配置，并通过环境变量或配置中心注入
     * 密钥生成方式：openssl rand -base64 64
     */
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration:1800}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800}")
    private Long refreshTokenExpiration;

    @Value("${jwt.issuer:mall-server}")
    private String issuer;

    @Autowired(required = false)
    private RedisUtils redisUtils;

    private SecretKey cachedSecretKey;
    private String cachedSecret;

    private SecretKey getSecretKey() {
        if (cachedSecretKey == null || !secret.equals(cachedSecret)) {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 32) {
                throw new IllegalStateException("JWT密钥长度不足，至少需要32字节（256位）");
            }
            cachedSecretKey = Keys.hmacShaKeyFor(keyBytes);
            cachedSecret = secret;
        }
        return cachedSecretKey;
    }

    public String generateAccessToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        claims.put("type", "access");

        return Jwts.builder()
                .claims(claims)
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000))
                .id(generateJti(userId))
                .signWith(getSecretKey())
                .compact();
    }

    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "refresh");

        String token = Jwts.builder()
                .claims(claims)
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration * 1000))
                .id(generateJti(userId))
                .signWith(getSecretKey())
                .compact();

        if (redisUtils != null) {
            redisUtils.set("user:refresh_token:" + userId, token, refreshTokenExpiration);
        }
        return token;
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token解析失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (!"access".equals(claims.get("type"))) {
                log.warn("Token类型错误，期望access，实际: {}", claims.get("type"));
                return false;
            }
            if (isTokenBlacklisted(claims.getId())) {
                log.warn("Token已被拉黑: {}", claims.getId());
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String refreshAccessToken(String refreshToken) {
        Claims claims = parseToken(refreshToken);

        if (!"refresh".equals(claims.get("type"))) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID.getCode(), "Token类型错误");
        }

        Long userId = claims.get("userId", Long.class);
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);

        if (redisUtils != null) {
            String storedToken = (String) redisUtils.get("user:refresh_token:" + userId);
            if (!refreshToken.equals(storedToken)) {
                throw new BusinessException(ErrorCode.TOKEN_EXPIRED.getCode(), "RefreshToken已失效");
            }
        }

        return generateAccessToken(userId, username, role);
    }

    public void logout(Long userId) {
        if (redisUtils != null) {
            redisUtils.del("user:refresh_token:" + userId);
        }
    }

    private boolean isTokenBlacklisted(String jti) {
        if (redisUtils == null) {
            return false;
        }
        return redisUtils.get("user:token_blacklist:" + jti) != null;
    }

    public void addToBlacklist(String token) {
        try {
            Claims claims = parseToken(token);
            String jti = claims.getId();
            Date expiration = claims.getExpiration();
            long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            if (redisUtils != null && ttl > 0) {
                redisUtils.set("user:token_blacklist:" + jti, "1", ttl);
            }
        } catch (Exception e) {
            log.error("Token加入黑名单失败: {}", e.getMessage());
        }
    }

    private String generateJti(Long userId) {
        return userId + "_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }

    public Long getUserIdFromToken(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    public String getUsernameFromToken(String token) {
        return parseToken(token).get("username", String.class);
    }

    public String getRoleFromToken(String token) {
        return parseToken(token).get("role", String.class);
    }

    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (BusinessException e) {
            return true;
        }
    }
}