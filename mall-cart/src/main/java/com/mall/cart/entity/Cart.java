package com.mall.cart.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class Cart implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 购物车商品列表
     */
    private List<CartItem> items = new ArrayList<>();
    
    /**
     * 商品总数
     */
    private Integer totalQuantity;
    
    /**
     * 总金额
     */
    private BigDecimal totalPrice;
    
    /**
     * 选中的商品数量
     */
    private Integer checkedQuantity;
    
    /**
     * 选中的商品总金额
     */
    private BigDecimal checkedTotalPrice;
    
    /**
     * 计算购物车统计数据
     */
    public void calculate() {
        this.totalQuantity = items.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
            
        this.totalPrice = items.stream()
            .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        this.checkedQuantity = items.stream()
            .filter(CartItem::getChecked)
            .mapToInt(CartItem::getQuantity)
            .sum();
            
        this.checkedTotalPrice = items.stream()
            .filter(CartItem::getChecked)
            .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}