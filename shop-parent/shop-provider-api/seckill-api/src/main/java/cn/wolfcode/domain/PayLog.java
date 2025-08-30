package cn.wolfcode.domain;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class PayLog {
    public static final int PAY_TYPE_ONLINE = 0;//在线支付
    public static final int PAY_TYPE_INTERGRAL = 1;//积分支付
    private String tradeNo;//订单编号
    private String outTradeNo;//支付流水号
    private String notifyTime;//订单修改时间
    private String totalAmount;//交易数值
    private int payType;//支付类型
}
