package cn.wolfcode.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Setter@Getter
public class RefundVo implements Serializable {
    private String outTradeNo;//交易订单号
    private String refundAmount;//退款金额
    private String refundReason;//退款原因
}
