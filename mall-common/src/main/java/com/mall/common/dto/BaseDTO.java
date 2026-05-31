package com.mall.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}