package com.mall.seckill.mq;

import com.mall.seckill.dto.SeckillMessage;
import com.mall.seckill.entity.SeckillOrder;
import com.mall.seckill.entity.SeckillProduct;
import com.mall.seckill.mapper.SeckillOrderMapper;
import com.mall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
public class SeckillMessageListener {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    
    @Autowired
    private SeckillService seckillService;

    @RabbitListener(queues = "seckill.queue")
    @Transactional(rollbackFor = Exception.class)
    public void handleSeckillMessage(SeckillMessage message) {
        log.info("收到秒杀消息: {}", message);
        
        try {
            // 1. 创建秒杀订单
            SeckillOrder order = createSeckillOrder(message);
            
            // 2. 保存订单
            seckillOrderMapper.insert(order);
            
            // 3. 更新秒杀结果到Redis
            seckillService.updateSeckillResult(message.getUserId(), message.getSeckillId(), order.getOrderNo());
            
            log.info("秒杀订单创建成功: {}", order.getOrderNo());
        } catch (Exception e) {
            log.error("秒杀订单创建失败", e);
            // 处理失败，回滚库存
            seckillService.rollbackStock(message.getSeckillId());
            
            // 更新失败结果
            seckillService.updateSeckillResult(message.getUserId(), message.getSeckillId(), "failed");
        }
    }

    private SeckillOrder createSeckillOrder(SeckillMessage message) {
        SeckillOrder order = new SeckillOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(message.getUserId());
        order.setSeckillId(message.getSeckillId());
        
        SeckillProduct product = message.getSeckillProduct();
        order.setProductId(product.getProductId());
        order.setProductName(product.getProductName());
        order.setProductImage(product.getProductImage());
        order.setSeckillPrice(product.getSeckillPrice());
        order.setQuantity(1);
        order.setTotalAmount(product.getSeckillPrice());
        order.setStatus(0); // 待付款
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        return order;
    }

    private String generateOrderNo() {
        // 生成订单号：时间戳 + 随机数
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "SK" + timestamp + random;
    }
}