package com.mall.auth.service.impl;

import com.mall.auth.dto.*;
import com.mall.auth.entity.User;
import com.mall.auth.mapper.UserMapper;
import com.mall.auth.service.AuthService;
import com.mall.common.utils.BCryptUtil;
import com.mall.common.utils.JwtUtils;
import com.mall.common.exception.BusinessException;
import com.mall.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;

    @Override
    public LoginResponseDTO login(LoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getUsername());

        User user = userMapper.findByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.USERNAME_OR_PASSWORD_ERROR);
        }

        if (!BCryptUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.USERNAME_OR_PASSWORD_ERROR);
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        return generateLoginResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponseDTO register(RegisterDTO registerDTO) {
        log.info("用户注册: {}", registerDTO.getUsername());

        if (userMapper.findByUsername(registerDTO.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USER_EXISTS);
        }
        if (registerDTO.getPhone() != null && userMapper.findByPhone(registerDTO.getPhone()) != null) {
            throw new BusinessException(ErrorCode.USER_EXISTS);
        }
        if (registerDTO.getEmail() != null && userMapper.findByEmail(registerDTO.getEmail()) != null) {
            throw new BusinessException(ErrorCode.USER_EXISTS);
        }

        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        user.setPassword(BCryptUtil.encode(registerDTO.getPassword()));
        user.setRole("ROLE_USER");
        user.setStatus(1);

        userMapper.insert(user);

        return generateLoginResponse(user);
    }

    @Override
    public LoginResponseDTO refreshToken(String refreshToken) {
        log.info("刷新Token");
        String newAccessToken = jwtUtils.refreshAccessToken(refreshToken);
        return LoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(1800L)
                .build();
    }

    @Override
    public void logout(Long userId) {
        log.info("用户退出登录: userId={}", userId);
        jwtUtils.logout(userId);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtils.validateAccessToken(token);
    }

    @Override
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public User getUserFromToken(String token) {
        Long userId = jwtUtils.getUserIdFromToken(token);
        return getUserById(userId);
    }

    /**
     * 生成登录响应
     */
    private LoginResponseDTO generateLoginResponse(User user) {
        String role = user.getRole() != null ? user.getRole() : "ROLE_USER";

        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), role);
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername(), role);

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(1800L)
                .user(userDTO)
                .build();
    }
}