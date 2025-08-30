package cn.wolfcode.mq.listener;

import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.mq.OrderMessage;
import cn.wolfcode.service.IOrderInfoService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@RocketMQMessageListener(
        consumerGroup = MQConstant.ORDER_PAY_TIMEOUT_CONSUMER_GROUP,
        topic = MQConstant.ORDER_PAY_TIMEOUT_TOPIC
)

@Component
@Slf4j

public class OrderPayTimeoutMessageListener implements RocketMQListener<OrderMessage> {


    private final IOrderInfoService orderInfoService;

    public OrderPayTimeoutMessageListener(IOrderInfoService orderInfoService) {
        this.orderInfoService = orderInfoService;
    }

    @Override
    public void onMessage(OrderMessage orderMessage) {
        log.info("[CHECK STATUS:PAY TIMEOUT!]received msg,preparing to check order status:{}", JSON.toJSONString(orderMessage));
        orderInfoService.checkPayTimeout(orderMessage);
    }
}
