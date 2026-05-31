package com.mall.seckill.service;

import com.mall.seckill.entity.SeckillOrder;
import com.mall.seckill.entity.SeckillProduct;

import java.util.List;

public interface SeckillService {

    /**
     * 获取当前秒杀商品列表
     */
    List<SeckillProduct> getCurrentSeckillProducts();

    /**
     * 获取秒杀商品信息
     */
    SeckillProduct getSeckillProduct(Long seckillId);

    /**
     * 执行秒杀
     */
    SeckillOrder seckill(Long userId, Long seckillId);

    /**
     * 获取秒杀结果
     */
    String getSeckillResult(Long userId, Long seckillId);

    /**
     * 更新秒杀结果
     */
    void updateSeckillResult(Long userId, Long seckillId, String result);

    /**
     * 回滚库存
     */
    void rollbackStock(Long seckillId);
}