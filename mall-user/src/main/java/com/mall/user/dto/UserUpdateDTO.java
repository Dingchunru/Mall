// UserUpdateDTO.java
package com.mall.user.dto;

import lombok.Data;
import javax.validation.constraints.Pattern;

@Data
public class UserUpdateDTO {
    private String phone;
    private String email;
    private Integer gender;
    private String avatar;
}

