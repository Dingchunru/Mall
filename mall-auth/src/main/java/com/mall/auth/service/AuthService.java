package com.mall.auth.service;

import com.mall.auth.dto.*;
import com.mall.auth.entity.User;

public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponseDTO login(LoginDTO loginDTO);

    /**
     * 用户注册
     */
    LoginResponseDTO register(RegisterDTO registerDTO);

    /**
     * 刷新token
     */
    LoginResponseDTO refreshToken(String refreshToken);

    /**
     * 退出登录
     */
    void logout(Long userId);

    /**
     * 验证token
     */
    boolean validateToken(String token);

    /**
     *
     */
    User getUserById(Long userId);

    /**
     * 从token获取用户信息
     */
    User getUserFromToken(String token);
}