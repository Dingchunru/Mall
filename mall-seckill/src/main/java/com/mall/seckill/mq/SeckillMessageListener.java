package com.mall.seckill.mq;

import com.mall.seckill.dto.SeckillMessage;
import com.mall.seckill.entity.SeckillOrder;
import com.mall.seckill.entity.SeckillProduct;
import com.mall.seckill.mapper.SeckillOrderMapper;
import com.mall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = "seckill-topic",
        consumerGroup = "mall-seckill-consumer"
)
public class SeckillMessageListener implements RocketMQListener<SeckillMessage> {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SeckillService seckillService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(SeckillMessage message) {
        log.info("收到秒杀消息: {}", message);

        try {
            SeckillOrder order = createSeckillOrder(message);
            seckillOrderMapper.insert(order);
            seckillService.updateSeckillResult(message.getUserId(), message.getSeckillId(), order.getOrderNo());
            log.info("秒杀订单创建成功: {}", order.getOrderNo());
        } catch (Exception e) {
            log.error("秒杀订单创建失败", e);
            seckillService.rollbackStock(message.getSeckillId());
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
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        return order;
    }

    private String generateOrderNo() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "SK" + timestamp + random;
    }
}