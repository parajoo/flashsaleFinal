package cn.wolfcode.mq;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by wolfcode
 * 封装异步下单的参数
 */
@Setter
@Getter
@NoArgsConstructor
public class OrderMessage implements Serializable {
    public OrderMessage(Integer time, Long seckillId, String token, Long userPhone) {
        this.time = time;
        this.seckillId = seckillId;
        this.token = token;
        this.userPhone = userPhone;
    }

    private Integer time;//秒杀场次
    private Long seckillId;//秒杀商品ID
    private String token;//用户的token信息
    private Long userPhone;//用户手机号码
    private String orderNo;
}
