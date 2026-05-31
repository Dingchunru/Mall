package com.mall.order.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDelayProducer {

    private final RocketMQTemplate rocketMQTemplate;

    private static final String DELAY_TOPIC = "order-delay-topic";

    /**
     * 发送延迟取消订单消息
     * @param orderNo 订单号
     * @param delayLevel RocketMQ 延迟级别：
     *   1=1s, 2=5s, 3=10s, 4=30s, 5=1m, 6=2m, 7=3m, 8=4m,
     *   9=5m, 10=6m, 11=7m, 12=8m, 13=9m, 14=10m, 15=20m, 16=30m, 17=1h, 18=2h
     */
    public void sendDelayCancelOrder(String orderNo, int delayLevel) {
        log.info("发送延迟取消订单消息: orderNo={}, delayLevel={}", orderNo, delayLevel);

        Message<String> message = MessageBuilder
                .withPayload(orderNo)
                .setHeader(MessageConst.PROPERTY_DELAY_TIME_LEVEL, delayLevel)
                .build();

        rocketMQTemplate.syncSend(DELAY_TOPIC, message);
        log.info("延迟消息发送成功: orderNo={}", orderNo);
    }
}