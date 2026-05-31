package com.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.order.dto.CreateOrderDTO;
import com.mall.order.dto.OrderVO;
import com.mall.order.dto.PageDTO;
import com.mall.order.entity.Order;

public interface OrderService extends IService<Order> {
    
    /**
     * 创建订单
     */
    OrderVO createOrder(CreateOrderDTO orderDTO);
    
    /**
     * 获取订单详情
     */
    OrderVO getOrderDetail(Long userId, String orderNo);
    
    /**
     * 获取用户订单列表
     */
    PageDTO<OrderVO> getUserOrders(Long userId, Integer page, Integer size, Integer status);
    
    /**
     * 取消订单
     */
    boolean cancelOrder(Long userId, String orderNo, String cancelReason);
    
    /**
     * 支付订单
     */
    boolean payOrder(Long userId, String orderNo, Integer paymentMethod);
    
    /**
     * 确认收货
     */
    boolean confirmOrder(Long userId, String orderNo);
    
    /**
     * 删除订单
     */
    boolean deleteOrder(Long userId, String orderNo);
    
    /**
     * 根据订单号查询订单
     */
    Order getOrderByNo(String orderNo);
}