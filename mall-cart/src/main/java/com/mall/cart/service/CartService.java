package com.mall.cart.service;

import com.mall.cart.entity.Cart;
import java.util.List;

public interface CartService {
    
    /**
     * 获取购物车
     */
    Cart getCart(Long userId);
    
    /**
     * 添加商品到购物车
     */
    void addToCart(Long userId, Long productId, Integer quantity);
    
    /**
     * 更新购物车商品数量
     */
    void updateCart(Long userId, Long productId, Integer quantity);
    
    /**
     * 从购物车移除商品
     */
    void removeFromCart(Long userId, List<Long> productIds);
    
    /**
     * 清空购物车
     */
    void clearCart(Long userId);
    
    /**
     * 选中/取消选中商品
     */
    void checkItem(Long userId, Long productId, Boolean checked);
    
    /**
     * 全选/全不选
     */
    void checkAll(Long userId, Boolean checked);
    
    /**
     * 获取选中的商品（用于下单）
     */
    Cart getCheckedItems(Long userId);
    
    /**
     * 移除选中的商品（下单后调用）
     */
    void removeCheckedItems(Long userId);
}