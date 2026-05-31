package com.mall.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String phone;
    private String email;
    private Integer gender;
    private String avatar;
    private LocalDateTime createTime;
}