package com.mall.cart.controller;

import com.mall.cart.entity.Cart;
import com.mall.cart.service.CartService;
import com.mall.common.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 获取购物车
     */
    @GetMapping
    public Result<Cart> getCart(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(cartService.getCart(userId));
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public Result<Void> addToCart(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @NotNull(message = "商品ID不能为空") Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        cartService.addToCart(userId, productId, quantity);
        return Result.success();
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/update")
    public Result<Void> updateCart(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @NotNull(message = "商品ID不能为空") Long productId,
            @RequestParam @NotNull(message = "数量不能为空") Integer quantity) {
        cartService.updateCart(userId, productId, quantity);
        return Result.success();
    }

    /**
     * 批量移除商品
     */
    @DeleteMapping("/remove")
    public Result<Void> removeFromCart(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam List<Long> productIds) {
        cartService.removeFromCart(userId, productIds);
        return Result.success();
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public Result<Void> clearCart(@RequestHeader("X-User-Id") Long userId) {
        cartService.clearCart(userId);
        return Result.success();
    }

    /**
     * 选中/取消选中商品
     */
    @PutMapping("/check")
    public Result<Void> checkItem(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @NotNull(message = "商品ID不能为空") Long productId,
            @RequestParam Boolean checked) {
        cartService.checkItem(userId, productId, checked);
        return Result.success();
    }

    /**
     * 全选/全不选
     */
    @PutMapping("/checkAll")
    public Result<Void> checkAll(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Boolean checked) {
        cartService.checkAll(userId, checked);
        return Result.success();
    }

    /**
     * 获取选中的商品（用于下单）
     */
    @GetMapping("/checked")
    public Result<Cart> getCheckedItems(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(cartService.getCheckedItems(userId));
    }

    /**
     * 获取购物车商品数量
     */
    @GetMapping("/count")
    public Result<Integer> getCartCount(@RequestHeader("X-User-Id") Long userId) {
        Cart cart = cartService.getCart(userId);
        return Result.success(cart.getTotalQuantity());
    }
}