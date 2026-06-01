package com.mall.auth.controller;

import com.mall.auth.dto.*;
import com.mall.auth.service.AuthService;
import com.mall.common.response.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

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
        public Result<Void> logout(@RequestHeader("Authorization") String token) {
        // 移除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        authService.logout(token);
        return Result.success();
    }

    @GetMapping("/validate")
        public Result<Boolean> validateToken(@RequestParam String token) {
        return Result.success(authService.validateToken(token));
    }
}