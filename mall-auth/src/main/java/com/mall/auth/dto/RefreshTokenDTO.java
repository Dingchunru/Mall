package com.mall.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class RefreshTokenDTO {
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}