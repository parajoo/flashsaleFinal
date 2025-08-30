package cn.wolfcode.mq.listener;

import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.web.controller.OrderInfoController;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;


@RocketMQMessageListener(
        consumerGroup = MQConstant.CANCEL_SECKILL_OVER_SIGE_CONSUMER_GROUP,
        topic = MQConstant.CANCEL_SECKILL_OVER_SIGE_TOPIC,
        messageModel = MessageModel.BROADCASTING //将消息模式修改为广播模式
)
@Component
@Slf4j
public class CancelStockOverFlagMessageListener implements RocketMQListener<String> {

    @Override
    public void onMessage(String msg) {
        log.info("[取消本地标识]received msg for cancelling local sign,prepare to delete it:{}",msg);
        Long seckillId = Long.parseLong(msg);
        OrderInfoController.deleteKey(seckillId);
    }
}
