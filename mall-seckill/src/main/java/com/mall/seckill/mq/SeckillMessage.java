package com.mall.seckill.mq;

import com.mall.seckill.entity.SeckillProduct;
import lombok.Data;

import java.io.Serializable;

@Data
public class SeckillMessage implements Serializable {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 秒杀商品ID
     */
    private Long seckillId;
    
    /**
     * 秒杀商品信息
     */
    private SeckillProduct seckillProduct;
}