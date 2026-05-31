package com.mall.order.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String phone;
    private String email;
    private Integer gender;
    private String avatar;
    private Integer status;
}