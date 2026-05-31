package com.mall.order.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDelayProducer {

    private final RabbitTemplate rabbitTemplate;
    
    private static final String DELAY_EXCHANGE = "order.delay.exchange";
    private static final String DELAY_ROUTING_KEY = "order.delay.routing";

    public void sendDelayCancelOrder(String orderNo, long delayTime) {
        log.info("发送延迟取消订单消息: orderNo={}, delayTime={}", orderNo, delayTime);
        
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("x-delay", delayTime);
        
        byte[] body = orderNo.getBytes();
        Message message = new Message(body, messageProperties);
        
        rabbitTemplate.convertAndSend(DELAY_EXCHANGE, DELAY_ROUTING_KEY, message);
    }
}