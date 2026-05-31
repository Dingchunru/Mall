package com.mall.seckill.service.impl;

import com.mall.common.exception.BusinessException;
import com.mall.seckill.dto.SeckillMessage;
import com.mall.seckill.entity.SeckillOrder;
import com.mall.seckill.entity.SeckillProduct;
import com.mall.seckill.mapper.SeckillProductMapper;
import com.mall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private SeckillProductMapper seckillProductMapper;

    @Override
    public List<SeckillProduct> getCurrentSeckillProducts() {
        // 从数据库查询当前正在进行的秒杀商品
        LocalDateTime now = LocalDateTime.now();
        List<SeckillProduct> allProducts = seckillProductMapper.selectList(null);
        
        return allProducts.stream()
                .filter(p -> p.getStartTime().isBefore(now) && p.getEndTime().isAfter(now) && p.getStatus() == 1)
                .collect(Collectors.toList());
    }

    @Override
    public SeckillProduct getSeckillProduct(Long seckillId) {
        return seckillProductMapper.selectById(seckillId);
    }

    @Override
    public SeckillOrder seckill(Long userId, Long seckillId) {
        // 1. 检查秒杀商品是否存在且有效
        SeckillProduct seckillProduct = getSeckillProduct(seckillId);
        if (seckillProduct == null) {
            throw new BusinessException(400, "秒杀商品不存在");
        }
        
        // 2. 检查是否在秒杀时间内
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(seckillProduct.getStartTime()) || now.isAfter(seckillProduct.getEndTime())) {
            throw new BusinessException(400, "不在秒杀时间内");
        }
        
        // 3. 检查用户是否已秒杀过（Redis分布式限流）
        String userKey = "seckill:user:" + seckillId + ":" + userId;
        Boolean hasSeckilled = redisTemplate.hasKey(userKey);
        if (Boolean.TRUE.equals(hasSeckilled)) {
            throw new BusinessException(400, "您已经参与过秒杀");
        }
        
        // 4. Redis原子操作扣减库存
        String stockKey = "seckill:stock:" + seckillId;
        Long stock = redisTemplate.opsForValue().decrement(stockKey);
        if (stock == null || stock < 0) {
            // 库存不足，回退
            redisTemplate.opsForValue().increment(stockKey);
            throw new BusinessException(400, "库存不足");
        }
        
        // 5. 标记用户已秒杀
        redisTemplate.opsForValue().set(userKey, "1", 24, TimeUnit.HOURS);
        
        // 6. 发送消息到队列异步处理订单
        SeckillMessage message = new SeckillMessage();
        message.setUserId(userId);
        message.setSeckillId(seckillId);
        message.setSeckillProduct(seckillProduct);
        message.setTimestamp(System.currentTimeMillis());
        rabbitTemplate.convertAndSend("seckill.exchange", "seckill.routing", message);
        
        // 7. 返回正在处理状态
        SeckillOrder order = new SeckillOrder();
        order.setSeckillId(seckillId);
        order.setStatus(0); // 0-处理中
        return order;
    }

    @Override
    public String getSeckillResult(Long userId, Long seckillId) {
        String resultKey = "seckill:result:" + seckillId + ":" + userId;
        Object result = redisTemplate.opsForValue().get(resultKey);
        
        if (result == null) {
            return "processing"; // 处理中
        } else if (result instanceof String) {
            return (String) result; // 返回订单号或失败信息
        } else {
            return "failed";
        }
    }

    @Override
    public void updateSeckillResult(Long userId, Long seckillId, String result) {
        String resultKey = "seckill:result:" + seckillId + ":" + userId;
        redisTemplate.opsForValue().set(resultKey, result, 1, TimeUnit.DAYS);
    }

    @Override
    public void rollbackStock(Long seckillId) {
        String stockKey = "seckill:stock:" + seckillId;
        redisTemplate.opsForValue().increment(stockKey);
        log.info("回滚库存: seckillId={}", seckillId);
    }
}