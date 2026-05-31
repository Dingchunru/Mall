package com.mall.order.mq;

import com.mall.order.entity.Order;
import com.mall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderService orderService;
    
    private static final String DELAY_QUEUE = "order.delay.queue";

    @RabbitListener(queues = DELAY_QUEUE)
    public void handleDelayCancelOrder(String orderNo) {
        log.info("收到延迟取消订单消息: orderNo={}", orderNo);
        
        Order order = orderService.getOrderByNo(orderNo);
        if (order == null) {
            log.error("订单不存在: orderNo={}", orderNo);
            return;
        }
        
        // 如果订单仍未支付，自动取消
        if (order.getStatus() == 0) {
            orderService.cancelOrder(order.getUserId(), orderNo, "超时未支付自动取消");
            log.info("订单已自动取消: orderNo={}", orderNo);
        }
    }
}