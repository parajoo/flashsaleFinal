package cn.wolfcode.mq.listener;

import cn.wolfcode.core.WebsocketServer;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.mq.OrderMQResult;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.sql.Time;

@RocketMQMessageListener(
        consumerGroup = MQConstant.ORDER_RESULT_CONSUMER_GROUP,
        topic = MQConstant.ORDER_RESULT_TOPIC
)
@Component
@Slf4j
public class OrderResultMessageListener implements RocketMQListener <OrderMQResult>{

    @Override
    public void onMessage(OrderMQResult orderMQResult) {
        String json = JSON.toJSONString(orderMQResult);
        log.info("[order results]receive msg of creating order:{}",json);
        //根据token获取到session对象
        try {
            int count = 0;
            do {
                Session session = WebsocketServer.SESSION_MAP.get(orderMQResult.getToken());
                if (session != null) {
                    //通过session对象 将数据写到前端
                    session.getBasicRemote().sendText(json);
                    return;
                }
                count++;
                log.info("[order result] nums:{}get session object failed,try again",count);
                //
                Thread.sleep(1000);
            }while (count<=5);//5s拿不到就通知
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
