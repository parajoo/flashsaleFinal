package cn.wolfcode.service.impl;

import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.*;
import cn.wolfcode.feign.PaymentFeignApi;
import cn.wolfcode.mapper.OrderInfoMapper;
import cn.wolfcode.mapper.PayLogMapper;
import cn.wolfcode.mapper.RefundLogMapper;
import cn.wolfcode.mq.DefaultSendCallback;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.mq.OrderMessage;
import cn.wolfcode.redis.SeckillRedisKey;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.util.AssertUtils;
import cn.wolfcode.util.IdGenerateUtil;
import cn.wolfcode.web.controller.OrderInfoController;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by wolfcode
 */
@Service
public class OrderInfoServiceImpl implements IOrderInfoService {
    private final ISeckillProductService seckillProductService;
    private final OrderInfoMapper orderInfoMapper;
    private final StringRedisTemplate redisTemplate;
    private final PayLogMapper payLogMapper;
    private final RefundLogMapper refundLogMapper;
    private final RocketMQTemplate rocketMQTemplate;
    private final PaymentFeignApi paymentFeignApi;

    public OrderInfoServiceImpl(ISeckillProductService seckillProductService, OrderInfoMapper orderInfoMapper, StringRedisTemplate redisTemplate, PayLogMapper payLogMapper, RefundLogMapper refundLogMapper, RocketMQTemplate rocketMQTemplate, PaymentFeignApi paymentFeignApi) {
        this.seckillProductService = seckillProductService;
        this.orderInfoMapper = orderInfoMapper;
        this.redisTemplate = redisTemplate;
        this.payLogMapper = payLogMapper;
        this.refundLogMapper = refundLogMapper;
        this.rocketMQTemplate = rocketMQTemplate;
        this.paymentFeignApi = paymentFeignApi;
    }

    @Override
    public OrderInfo selectByUserIdAndSeckillId(Long userId, Long seckillId, Integer time) {
        return orderInfoMapper.selectByUserIdAndSeckillId(userId, seckillId, time);
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String doSeckill(UserInfo userInfo, SeckillProductVo vo) {
        // 从 UserInfo 里拿到手机号，调用另一个方法
        return doSeckill(userInfo.getPhone(), vo);
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String doSeckill(Long phone, SeckillProductVo vo) {
        // 1. 扣除秒杀商品库存
        seckillProductService.decrStockCount(vo.getId());//,vo.getTime()
        // 2. 创建秒杀订单并保存
        OrderInfo orderInfo = this.buildOrderInfo(phone, vo);

        // 3. 返回订单编号
        orderInfoMapper.insert(orderInfo);
        return orderInfo.getOrderNo();
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String doSeckill(Long phone, Long seckillId,Integer time) {
        SeckillProductVo sp = seckillProductService.selectByIdAndTime(seckillId, time);
        return this.doSeckill(phone,sp);
    }

    @Override
    public OrderInfo selectByOrderNo(String orderNo){
        return orderInfoMapper.selectById(orderNo);
    }

    @Override
    public void failedRollback(OrderMessage message) {
        //1.rollback:数据库不需要回补，只有redis需要
        Long stockCount = seckillProductService.selectStockCountId(message.getSeckillId());
        String hashKey = SeckillRedisKey.SECKILL_STOCK_COUNT_HASH.join(message.getToken() + "");
        redisTemplate.opsForHash().put(hashKey,message.getSeckillId()+"",stockCount+"");
        //2.delete user sign
        String userOrderFlag = SeckillRedisKey.SECKILL_ORDER_HASH.join(message.getSeckillId() + "");
        redisTemplate.opsForHash().delete(hashKey,userOrderFlag,message.getUserPhone()+"");
        //3.delete local sign ==> 通过mq发送广播消息 让每一个服务
        rocketMQTemplate.asyncSend(MQConstant.CANCEL_SECKILL_OVER_SIGE_TOPIC,message.getSeckillId(),new DefaultSendCallback("取消本地标识"));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void checkPayTimeout(OrderMessage orderMessage) {
        //1.基于订单编号 查询订单对象
        //2.未支付，则取消订单
        int row = orderInfoMapper.changePayStatus(orderMessage.getOrderNo(), OrderInfo.STATUS_CANCEL, OrderInfo.PAY_TYPE_ONLINE);
        if (row>0){
            //3.mysql该秒杀商品库存加1
            seckillProductService.incrStockCount(orderMessage.getSeckillId());
            //4.失败订单信息回滚：redis库存删除用户下单标识 删除本地标识
            this.failedRollback(orderMessage);
        }
    }

    @Override
    public String onlinePay(String orderNo) {
        //1.基于订单号 查询订单对象
        OrderInfo orderInfo = this.selectByOrderNo(orderNo);
        //2.判断订单状态是否为未支付状态，未支付才能发起支付请求
        AssertUtils.isTrue(OrderInfo.STATUS_ARREARAGE.equals(orderInfo.getStatus()),"订单状态异常，无法发起支付");
        //封装支付参数
        PayVo vo = new PayVo();
        vo.setBody("秒杀："+orderInfo.getProductName());
        vo.setSubject(orderInfo.getProductName());
        vo.setOutTradeNo(orderNo);
        vo.setTotalAmount(orderInfo.getSeckillPrice().toString());//big decimal->string
        //远程调用支付服务支付
        Result<String> result = paymentFeignApi.prepay(vo);
        return result.checkAndGet();
    }

    @Override
    public void alipaySuccess(PayResult result) {
        //1.获取订单信息对象
        OrderInfo orderInfo = this.selectByOrderNo(result.getOutTradeNo());
        AssertUtils.notNull(orderInfo,"订单信息有误");
        //2.判断订单信息是否正确
        AssertUtils.isTrue(orderInfo.getSeckillPrice().toString().equals(result.getTotalAmount()),"支付金额有误");
        //3.更新订单状态  保证幂等性
        int row = orderInfoMapper.changePayStatus(result.getTradeNo(), OrderInfo.STATUS_ACCOUNT_PAID, OrderInfo.PAY_TYPE_ONLINE);
        AssertUtils.isTrue(row>0,"订单状态修改失败");
        //4.记录支付日志
        PayLog payLog = new PayLog();
        payLog.setPayType(OrderInfo.PAY_TYPE_ONLINE);
        payLog.setTotalAmount(result.getTotalAmount());
        payLog.setOutTradeNo(result.getOutTradeNo());
        payLog.setTradeNo(result.getTradeNo());
        payLog.setNotifyTime(System.currentTimeMillis()+"");
        payLogMapper.insert(payLog);
    }

    //private OrderInfo buildOrderInfo(UserInfo userInfo, SeckillProductVo vo) {
    private OrderInfo buildOrderInfo(Long phone, SeckillProductVo vo) {
        Date now = new Date();
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(now);
        orderInfo.setDeliveryAddrId(1L);
        orderInfo.setIntergral(vo.getIntergral());
        orderInfo.setOrderNo(IdGenerateUtil.get().nextId() + "");//id generator=>雪花算法==>保证唯一性（如果按1，2，3排序 分表之后数据库自增 id就不唯一）
        orderInfo.setPayType(OrderInfo.PAY_TYPE_ONLINE);
        orderInfo.setProductCount(1);
        orderInfo.setProductId(vo.getProductId());
        orderInfo.setProductImg(vo.getProductImg());
        orderInfo.setProductName(vo.getProductName());
        orderInfo.setProductPrice(vo.getProductPrice());
        orderInfo.setSeckillDate(now);
        orderInfo.setSeckillId(vo.getId());
        orderInfo.setSeckillPrice(vo.getSeckillPrice());
        orderInfo.setSeckillTime(vo.getTime());
        orderInfo.setStatus(OrderInfo.STATUS_ARREARAGE);
        orderInfo.setUserId(phone);
        return orderInfo;
    }
}
