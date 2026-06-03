// UserInfoDTO.java
package com.mall.user.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserInfoDTO {
    private Long id;
    private String username;
    private String phone;
    private String email;
    private Integer gender;
    private String avatar;
    private LocalDateTime createTime;
    private String accessToken;
    private String refreshToken;
}