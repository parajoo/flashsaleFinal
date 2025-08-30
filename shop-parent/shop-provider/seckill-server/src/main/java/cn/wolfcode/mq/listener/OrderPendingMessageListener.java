package cn.wolfcode.mq.listener;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.mq.DefaultSendCallback;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.mq.OrderMQResult;
import cn.wolfcode.mq.OrderMessage;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@RocketMQMessageListener(
        consumerGroup = MQConstant.ORDER_PENDING_CONSUMER_GROUP,
        topic = MQConstant.ORDER_PENDING_TOPIC
)
@Component
@Slf4j
public class OrderPendingMessageListener implements RocketMQListener<OrderMessage> {

    private final IOrderInfoService orderInfoService;
    private final RocketMQTemplate rocketMQTemplate;

    public OrderPendingMessageListener(IOrderInfoService orderInfoService, RocketMQTemplate rocketMQTemplate) {
        this.orderInfoService = orderInfoService;
        this.rocketMQTemplate = rocketMQTemplate;
    }


    @Override
    public void onMessage(OrderMessage message) {
        log.info("[create order] received the msg,preparing to creating order: {}", JSON.toJSONString(message));
        OrderMQResult orderMQResult = new OrderMQResult();
        orderMQResult.setToken(message.getToken());
        try {
            String orderNo = orderInfoService.doSeckill(message.getUserPhone(), message.getSeckillId(), message.getTime());
            orderMQResult.setOrderNo(orderNo);
            //订单创建成功
            orderMQResult.setCode(Result.SUCCESS_CODE);
            orderMQResult.setMsg("order creating successfully");
            //下单成功后  发送delay消息 检查订单支付状态 若超时未支付 直接取消订单 -- 回滚redis和MySQL的库存
            message.setOrderNo(orderNo);
            Message<OrderMessage> orderMessage = MessageBuilder.withPayload(message).build();
            rocketMQTemplate.asyncSend(MQConstant.ORDER_PAY_TIMEOUT_TOPIC,orderMessage,
                    new DefaultSendCallback("CHECK STATUS:PAY TIMEOUT!"),5000,3);

        } catch (Exception e) {
            orderMQResult.setCode(SeckillCodeMsg.SECKILL_ERROR.getCode());
            orderMQResult.setMsg(SeckillCodeMsg.SECKILL_ERROR.getMsg());
            //创建失败,回补redis数量控制，删除用户下单标识（在哪），本地下单标识
            orderInfoService.failedRollback(message);
        }
        //无论成功还是失败  都要返回订单创建结果消息
        rocketMQTemplate.asyncSend(MQConstant.ORDER_RESULT_TOPIC,orderMQResult,new DefaultSendCallback("order result"));
    }
}
