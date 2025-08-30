package cn.wolfcode.service;


import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.domain.PayResult;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.mq.OrderMessage;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

/**
 * Created by wolfcode
 */
public interface IOrderInfoService {

    OrderInfo selectByUserIdAndSeckillId(Long phone, Long seckillId, Integer time);

    String doSeckill(UserInfo userInfo, SeckillProductVo vo);

    String doSeckill(Long phone, SeckillProductVo vo);

    String doSeckill(Long phone, Long seckillId,Integer time);

    OrderInfo selectByOrderNo(String orderNo);

    void failedRollback(OrderMessage message);

    void checkPayTimeout(OrderMessage orderMessage);

    String onlinePay(String orderNo);

    void alipaySuccess(PayResult result);
    //@Transactional(rollbackFor = Exception.class)
    //String doSeckill(Long phone, SeckillProductVo vo);
}
