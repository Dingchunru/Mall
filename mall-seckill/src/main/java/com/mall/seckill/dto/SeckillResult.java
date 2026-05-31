package com.mall.seckill.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class SeckillResult implements Serializable {
    private Long seckillId;
    private String orderNo;
    private Integer status; // 0-处理中 1-成功 2-失败
    private String message;
}