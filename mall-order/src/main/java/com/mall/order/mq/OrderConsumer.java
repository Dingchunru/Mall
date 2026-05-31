package com.mall.order.mq;

import com.mall.order.entity.Order;
import com.mall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "order-delay-topic",
        consumerGroup = "mall-order-consumer"
)
public class OrderConsumer implements RocketMQListener<String> {

    private final OrderService orderService;

    @Override
    public void onMessage(String orderNo) {
        log.info("收到延迟取消订单消息: orderNo={}", orderNo);

        Order order = orderService.getOrderByNo(orderNo);
        if (order == null) {
            log.error("订单不存在: orderNo={}", orderNo);
            return;
        }

        if (order.getStatus() == 0) {
            orderService.cancelOrder(order.getUserId(), orderNo, "超时未支付自动取消");
            log.info("订单已自动取消: orderNo={}", orderNo);
        }
    }
}