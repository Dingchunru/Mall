package com.mall.auth.service.impl;

import com.mall.auth.dto.*;
import com.mall.auth.entity.User;
import com.mall.auth.exception.AuthException;
import com.mall.auth.mapper.UserMapper;
import com.mall.auth.service.AuthService;
import com.mall.common.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    // Token过期时间：2小时
    private static final long TOKEN_EXPIRE_TIME = 2 * 60 * 60 * 1000L;
    // RefreshToken过期时间：7天
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;

    @Override
    public LoginResponseDTO login(LoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getUsername());
        
        // 查询用户
        User user = userMapper.findByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new AuthException("用户名或密码错误");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new AuthException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new AuthException("账号已被禁用");
        }
        
        // 生成token
        return generateLoginResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponseDTO register(RegisterDTO registerDTO) {
        log.info("用户注册: {}", registerDTO.getUsername());
        
        // 检查用户名是否已存在
        if (userMapper.findByUsername(registerDTO.getUsername()) != null) {
            throw new AuthException("用户名已存在");
        }
        
        // 检查手机号是否已存在
        if (registerDTO.getPhone() != null && userMapper.findByPhone(registerDTO.getPhone()) != null) {
            throw new AuthException("手机号已被注册");
        }
        
        // 检查邮箱是否已存在
        if (registerDTO.getEmail() != null && userMapper.findByEmail(registerDTO.getEmail()) != null) {
            throw new AuthException("邮箱已被注册");
        }
        
        // 创建新用户
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setStatus(1); // 正常状态
        
        userMapper.insert(user);
        
        // 生成token
        return generateLoginResponse(user);
    }

    @Override
    public LoginResponseDTO refreshToken(String refreshToken) {
        log.info("刷新token");
        
        // 从Redis中获取refreshToken对应的用户ID
        String key = "refresh_token:" + refreshToken;
        Long userId = (Long) redisTemplate.opsForValue().get(key);
        
        if (userId == null) {
            throw new AuthException("无效的刷新令牌");
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new AuthException("用户不存在");
        }
        
        // 删除旧的refreshToken
        redisTemplate.delete(key);
        
        // 生成新的token
        return generateLoginResponse(user);
    }

    @Override
    public void logout(String token) {
        log.info("用户退出登录");
        
        // 从token中获取用户信息
        var claims = jwtUtils.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        
        // 将token加入黑名单
        String blacklistKey = "token_blacklist:" + token;
        redisTemplate.opsForValue().set(blacklistKey, userId, TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        
        // 删除refreshToken
        String refreshTokenKey = "user_refresh:" + userId;
        redisTemplate.delete(refreshTokenKey);
    }

    @Override
    public boolean validateToken(String token) {
        // 检查token是否在黑名单中
        String blacklistKey = "token_blacklist:" + token;
        Boolean isBlacklisted = redisTemplate.hasKey(blacklistKey);
        
        if (Boolean.TRUE.equals(isBlacklisted)) {
            return false;
        }
        
        return jwtUtils.validateToken(token);
    }

    @Override
    public User getUserFromToken(String token) {
        var claims = jwtUtils.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        return userMapper.selectById(userId);
    }

    /**
     * 生成登录响应
     */
    private LoginResponseDTO generateLoginResponse(User user) {
        // 生成访问令牌
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        
        // 生成刷新令牌
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        
        // 保存refreshToken到Redis
        String refreshTokenKey = "refresh_token:" + refreshToken;
        redisTemplate.opsForValue().set(refreshTokenKey, user.getId(), 
                REFRESH_TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        
        // 保存用户refreshToken映射
        String userRefreshKey = "user_refresh:" + user.getId();
        redisTemplate.opsForValue().set(userRefreshKey, refreshToken, 
                REFRESH_TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        
        // 构建用户信息
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        
        return LoginResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(TOKEN_EXPIRE_TIME / 1000)
                .user(userDTO)
                .build();
    }
}