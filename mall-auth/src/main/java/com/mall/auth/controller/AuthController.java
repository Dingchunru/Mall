package com.mall.auth.controller;

import com.mall.auth.dto.*;
import com.mall.auth.service.AuthService;
import com.mall.common.response.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Api(tags = "认证管理")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @ApiOperation(value = "用户登录")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @PostMapping("/register")
    @ApiOperation(value = "用户注册")
    public Result<LoginResponseDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return Result.success(authService.register(registerDTO));
    }

    @PostMapping("/refresh")
    @ApiOperation(value = "刷新令牌")
    public Result<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        return Result.success(authService.refreshToken(refreshTokenDTO.getRefreshToken()));
    }

    @PostMapping("/logout")
    @ApiOperation(value = "退出登录")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        // 移除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        authService.logout(token);
        return Result.success();
    }

    @GetMapping("/validate")
    @ApiOperation(value = "验证令牌")
    public Result<Boolean> validateToken(@RequestParam String token) {
        return Result.success(authService.validateToken(token));
    }
}