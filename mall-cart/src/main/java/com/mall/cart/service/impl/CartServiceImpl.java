package com.mall.cart.service.impl;

import com.mall.cart.entity.Cart;
import com.mall.cart.entity.CartItem;
import com.mall.cart.service.CartService;
import com.mall.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 购物车缓存key前缀
    private static final String CART_KEY_PREFIX = "cart:user:";
    
    // 购物车过期时间（7天）
    private static final long CART_EXPIRE_TIME = 7;

    @Override
    public Cart getCart(Long userId) {
        String key = CART_KEY_PREFIX + userId;
        Cart cart = (Cart) redisTemplate.opsForValue().get(key);
        
        if (cart == null) {
            cart = createEmptyCart(userId);
            // 保存到Redis
            redisTemplate.opsForValue().set(key, cart, CART_EXPIRE_TIME, TimeUnit.DAYS);
        } else {
            // 重新计算统计数据
            cart.calculate();
            // 更新缓存
            redisTemplate.opsForValue().set(key, cart, CART_EXPIRE_TIME, TimeUnit.DAYS);
        }
        
        return cart;
    }

    @Override
    public void addToCart(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new BusinessException("数量必须大于0");
        }

        String key = CART_KEY_PREFIX + userId;
        Cart cart = getCart(userId);
        
        // 查找是否已存在该商品
        CartItem existingItem = cart.getItems().stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst()
            .orElse(null);

        if (existingItem != null) {
            // 已存在，增加数量
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setUpdateTime(LocalDateTime.now());
        } else {
            // 不存在，新增商品（这里应该调用商品服务获取商品详情）
            CartItem newItem = new CartItem();
            newItem.setProductId(productId);
            newItem.setProductName("商品" + productId); // 实际应该从商品服务获取
            newItem.setProductImage("http://example.com/image.jpg"); // 实际应该从商品服务获取
            newItem.setPrice(new BigDecimal("99.00")); // 实际应该从商品服务获取
            newItem.setQuantity(quantity);
            newItem.setChecked(true);
            newItem.setCreateTime(LocalDateTime.now());
            newItem.setUpdateTime(LocalDateTime.now());
            
            cart.getItems().add(newItem);
        }

        // 重新计算统计数据
        cart.calculate();
        
        // 更新Redis
        redisTemplate.opsForValue().set(key, cart, CART_EXPIRE_TIME, TimeUnit.DAYS);
        
        log.info("用户{}添加商品{}到购物车，数量：{}", userId, productId, quantity);
    }

    @Override
    public void updateCart(Long userId, Long productId, Integer quantity) {
        if (quantity < 0) {
            throw new BusinessException("数量不能小于0");
        }

        String key = CART_KEY_PREFIX + userId;
        Cart cart = getCart(userId);

        // 查找商品
        CartItem item = cart.getItems().stream()
            .filter(i -> i.getProductId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new BusinessException("购物车中不存在该商品"));

        if (quantity == 0) {
            // 数量为0，移除商品
            cart.getItems().remove(item);
        } else {
            // 更新数量
            item.setQuantity(quantity);
            item.setUpdateTime(LocalDateTime.now());
        }

        // 重新计算统计数据
        cart.calculate();
        
        // 更新Redis
        redisTemplate.opsForValue().set(key, cart, CART_EXPIRE_TIME, TimeUnit.DAYS);
        
        log.info("用户{}更新商品{}数量为：{}", userId, productId, quantity);
    }

    @Override
    public void removeFromCart(Long userId, List<Long> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return;
        }

        String key = CART_KEY_PREFIX + userId;
        Cart cart = getCart(userId);

        // 移除指定商品
        cart.getItems().removeIf(item -> productIds.contains(item.getProductId()));

        // 重新计算统计数据
        cart.calculate();
        
        // 更新Redis
        redisTemplate.opsForValue().set(key, cart, CART_EXPIRE_TIME, TimeUnit.DAYS);
        
        log.info("用户{}从购物车移除商品：{}", userId, productIds);
    }

    @Override
    public void clearCart(Long userId) {
        String key = CART_KEY_PREFIX + userId;
        
        // 创建空购物车
        Cart emptyCart = createEmptyCart(userId);
        
        // 更新Redis
        redisTemplate.opsForValue().set(key, emptyCart, CART_EXPIRE_TIME, TimeUnit.DAYS);
        
        log.info("用户{}清空购物车", userId);
    }

    @Override
    public void checkItem(Long userId, Long productId, Boolean checked) {
        String key = CART_KEY_PREFIX + userId;
        Cart cart = getCart(userId);

        // 查找商品并更新选中状态
        cart.getItems().stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst()
            .ifPresent(item -> {
                item.setChecked(checked);
                item.setUpdateTime(LocalDateTime.now());
            });

        // 重新计算统计数据
        cart.calculate();
        
        // 更新Redis
        redisTemplate.opsForValue().set(key, cart, CART_EXPIRE_TIME, TimeUnit.DAYS);
        
        log.info("用户{}设置商品{}选中状态为：{}", userId, productId, checked);
    }

    @Override
    public void checkAll(Long userId, Boolean checked) {
        String key = CART_KEY_PREFIX + userId;
        Cart cart = getCart(userId);

        // 更新所有商品选中状态
        cart.getItems().forEach(item -> {
            item.setChecked(checked);
            item.setUpdateTime(LocalDateTime.now());
        });

        // 重新计算统计数据
        cart.calculate();
        
        // 更新Redis
        redisTemplate.opsForValue().set(key, cart, CART_EXPIRE_TIME, TimeUnit.DAYS);
        
        log.info("用户{}全选/全不选：{}", userId, checked);
    }

    @Override
    public Cart getCheckedItems(Long userId) {
        Cart cart = getCart(userId);
        
        // 创建只包含选中商品的购物车
        Cart checkedCart = new Cart();
        checkedCart.setUserId(userId);
        checkedCart.setItems(cart.getItems().stream()
            .filter(CartItem::getChecked)
            .collect(Collectors.toList()));
        
        // 重新计算统计数据
        checkedCart.calculate();
        
        return checkedCart;
    }

    @Override
    public void removeCheckedItems(Long userId) {
        String key = CART_KEY_PREFIX + userId;
        Cart cart = getCart(userId);

        // 移除选中的商品
        cart.getItems().removeIf(CartItem::getChecked);

        // 重新计算统计数据
        cart.calculate();
        
        // 更新Redis
        redisTemplate.opsForValue().set(key, cart, CART_EXPIRE_TIME, TimeUnit.DAYS);
        
        log.info("用户{}移除选中的商品", userId);
    }

    /**
     * 创建空购物车
     */
    private Cart createEmptyCart(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        cart.setTotalQuantity(0);
        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setCheckedQuantity(0);
        cart.setCheckedTotalPrice(BigDecimal.ZERO);
        return cart;
    }
}