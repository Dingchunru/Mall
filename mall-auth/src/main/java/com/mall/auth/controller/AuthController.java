package com.mall.auth.controller;

import com.mall.auth.dto.*;
import com.mall.auth.entity.User;
import com.mall.auth.service.AuthService;
import com.mall.common.response.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @PostMapping("/register")
    public Result<LoginResponseDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return Result.success(authService.register(registerDTO));
    }

    @PostMapping("/refresh")
    public Result<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        return Result.success(authService.refreshToken(refreshTokenDTO.getRefreshToken()));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("X-User-Id") Long userId) {
        authService.logout(userId);
        return Result.success();
    }

    @PostMapping("/validate")
    public Result<Boolean> validateToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        return Result.success(authService.validateToken(token));
    }

    @GetMapping("/user-info")
    public Result<UserDTO> getUserInfo(@RequestHeader("Authorization") String token) {
        User user = authService.getUserFromToken(token);
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return Result.success(dto);
    }
}