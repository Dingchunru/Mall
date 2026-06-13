package com.mall.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BusinessException;
import com.mall.seckill.dto.SeckillMessage;
import com.mall.seckill.entity.SeckillOrder;
import com.mall.seckill.entity.SeckillProduct;
import com.mall.seckill.mapper.SeckillProductMapper;
import com.mall.seckill.service.SeckillService;
import com.mall.seckill.utils.SeckillCacheKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RocketMQTemplate rocketMQTemplate;
    private final SeckillProductMapper seckillProductMapper;

    private static final String SECKILL_TOPIC = "seckill-topic";

    /**
     * Lua 脚本：原子扣减库存
     * 返回 -1=库存未初始化  0=库存不足  1=扣减成功
     */
    private static final String DEDUCT_STOCK_LUA =
            "local stock = redis.call('get', KEYS[1]) " +
                    "if not stock then return -1 end " +
                    "if tonumber(stock) <= 0 then return 0 end " +
                    "redis.call('decr', KEYS[1]) " +
                    "return 1";

    private final DefaultRedisScript<Long> deductStockScript = new DefaultRedisScript<>(DEDUCT_STOCK_LUA, Long.class);

    @Override
    public List<SeckillProduct> getCurrentSeckillProducts() {
        LocalDateTime now = LocalDateTime.now();
        List<SeckillProduct> allProducts = seckillProductMapper.selectList(
                new LambdaQueryWrapper<SeckillProduct>().eq(SeckillProduct::getStatus, 1));

        return allProducts.stream()
                .filter(p -> p.getStartTime().isBefore(now) && p.getEndTime().isAfter(now))
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

        // 3. 检查用户是否已秒杀过
        String userKey = SeckillCacheKey.getUserSeckillKey(seckillId, userId);
        Boolean hasSeckilled = redisTemplate.hasKey(userKey);
        if (Boolean.TRUE.equals(hasSeckilled)) {
            throw new BusinessException(400, "您已经参与过秒杀");
        }

        // 4. 库存预热（如果 Redis 里没有，从 DB 加载）
        String stockKey = SeckillCacheKey.getStockKey(seckillId);
        if (Boolean.FALSE.equals(redisTemplate.hasKey(stockKey))) {
            redisTemplate.opsForValue().set(stockKey, seckillProduct.getStock().toString(), 2, TimeUnit.HOURS);
        }

        // 5. Lua 脚本原子扣减库存
        Long result = redisTemplate.execute(deductStockScript, Collections.singletonList(stockKey));
        if (result == null || result == -1) {
            throw new BusinessException(400, "秒杀未开始");
        }
        if (result == 0) {
            throw new BusinessException(400, "库存不足");
        }

        // 6. 标记用户已秒杀
        redisTemplate.opsForValue().set(userKey, "1", 24, TimeUnit.HOURS);

        // 7. 发送消息到 RocketMQ 异步处理订单
        SeckillMessage message = new SeckillMessage();
        message.setUserId(userId);
        message.setSeckillId(seckillId);
        message.setSeckillProduct(seckillProduct);

        try {
            rocketMQTemplate.syncSend(SECKILL_TOPIC, message);
        } catch (Exception e) {
            log.error("MQ发送失败，回滚: seckillId={}, userId={}", seckillId, userId);
            redisTemplate.opsForValue().increment(stockKey);
            redisTemplate.delete(userKey);
            throw new BusinessException(500, "系统繁忙，请重试");
        }

        log.info("秒杀消息已发送: userId={}, seckillId={}", userId, seckillId);

        // 8. 返回正在处理状态
        SeckillOrder order = new SeckillOrder();
        order.setSeckillId(seckillId);
        order.setStatus(0);
        return order;
    }

    @Override
    public String getSeckillResult(Long userId, Long seckillId) {
        String resultKey = SeckillCacheKey.getResultKey(seckillId, userId);
        Object result = redisTemplate.opsForValue().get(resultKey);

        if (result == null) {
            return "processing";
        } else if (result instanceof String) {
            return (String) result;
        } else {
            return "failed";
        }
    }

    @Override
    public void updateSeckillResult(Long userId, Long seckillId, String result) {
        String resultKey = SeckillCacheKey.getResultKey(seckillId, userId);
        redisTemplate.opsForValue().set(resultKey, result, 1, TimeUnit.DAYS);
    }

    @Override
    public void rollbackStock(Long seckillId) {
        String stockKey = SeckillCacheKey.getStockKey(seckillId);
        redisTemplate.opsForValue().increment(stockKey);
        log.info("回滚库存: seckillId={}", seckillId);
    }
}