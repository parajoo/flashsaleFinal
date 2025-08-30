package cn.wolfcode.web.controller;

import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.common.web.anno.RequireLogin;
import cn.wolfcode.common.web.resolver.RequestUser;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.mq.DefaultSendCallback;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.mq.OrderMessage;
import cn.wolfcode.redis.CommonRedisKey;
import cn.wolfcode.redis.SeckillRedisKey;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.util.AssertUtils;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


@RestController //Rest API controller
@RequestMapping("/order")
@Slf4j
public class OrderInfoController {
    /*
    标记已经sold out的商品
     */
    private static final Map<Long,Boolean> STOCK_OVER_FLOW_MAP = new ConcurrentHashMap<>();//线程安全性

    private final ISeckillProductService seckillProductService;
    private final StringRedisTemplate redisTemplate;
    private final IOrderInfoService orderInfoService;
    private final RocketMQTemplate rocketMQTemplate;

    public OrderInfoController(ISeckillProductService seckillProductService, StringRedisTemplate redisTemplate, IOrderInfoService orderInfoService,RocketMQTemplate rocketMQTemplate){
        this.seckillProductService = seckillProductService;
        this.redisTemplate = redisTemplate;
        this.orderInfoService = orderInfoService;
        this.rocketMQTemplate = rocketMQTemplate;
    }
    public static void deleteKey(Long key){
        STOCK_OVER_FLOW_MAP.remove(key);
    }

    @RequireLogin
    @GetMapping("/find")
    public Result<OrderInfo> findById(String orderNo,@RequestUser UserInfo userInfo){
        OrderInfo orderInfo = orderInfoService.selectByOrderNo(orderNo);
        Long userId = orderInfo.getUserId();
        //查询是否是当前用户的订单 如果不是则不允许查询
        if(!userInfo.getPhone().equals(userId)){
            return Result.error(SeckillCodeMsg.REMOTE_DATA_ERROR);
        }
        return Result.success(orderInfo);
    }

    /**
     * 优化前:
     * QPS:
     * 存在超卖问题
     *
     * 优化后
     * QPS:
     */

    @RequireLogin
    @PostMapping("/doSeckill")
    public Result<?> doSeckill(Long seckillId, Integer time, @RequestUser UserInfo userInfo, @RequestHeader("token") String token){
           //判断库存是否已经卖完了 如果已经卖完 直接返回异常
           Boolean flag = STOCK_OVER_FLOW_MAP.get(seckillId);
           if(flag!=null && flag){
               return Result.error(SeckillCodeMsg.SECKILL_STOCK_OVER);
           }
           //synchronized (this){加这里 锁力度太大
           //@RequestHeader("token") String token
           //1.基于token查询当前用户信息
           //2.基于秒杀id+场次查询秒杀商品对象
           SeckillProductVo sp =seckillProductService.selectByIdAndTime(seckillId,time);
           if(sp == null){
               return Result.error(SeckillCodeMsg.REMOTE_DATA_ERROR);
           }
           //3.判断当前时间是否在秒杀时间范围内
           boolean range = this.betweenSecKillTime(sp);
           if(!range){
               return Result.error(SeckillCodeMsg.OUT_OF_SECKILL_TIME_ERROR);
           }

           //5.判断用户是否已经下过订单--
           String userOrderFlag = SeckillRedisKey.SECKILL_ORDER_HASH.join(seckillId + "");
           Long orderCount = redisTemplate.opsForHash().increment(userOrderFlag, userInfo.getPhone() + "", -1);
           //OrderInfo orderInfo = orderInfoService.selectByUserIdAndSeckillId(userInfo.getPhone(),seckillId,time);
           if(orderCount>1){
               return Result.error(SeckillCodeMsg.REPEAT_SECKILL);
           }
           String orderNum = null;

           try {
               //4.判断库存是否充足
               String hashKey = SeckillRedisKey.SECKILL_STOCK_COUNT_HASH.join(time + "");
               Long remain = redisTemplate.opsForHash().increment(hashKey, seckillId + "", -1);
               AssertUtils.isTrue(remain>=0,"sold out!");
               //6.发送mq异步消息，创建
               rocketMQTemplate.asyncSend(MQConstant.ORDER_PENDING_TOPIC,
                       new OrderMessage(time,seckillId,token,userInfo.getPhone()),
                       new DefaultSendCallback("create order"));
               return Result.success("creating....");
           } catch (BusinessException e) {
               //库存不够时 直接标记当前商品
               STOCK_OVER_FLOW_MAP.put(seckillId,true);
               //将用户重复下单标识删除
               redisTemplate.opsForHash().delete(userOrderFlag,userInfo.getPhone()+"");
               return Result.error(e.getCodeMsg());
           }catch (Exception e){
               e.printStackTrace();
           }
           return Result.defaultError();
    }

    private boolean betweenSecKillTime(SeckillProductVo sp){
        Calendar instance = Calendar.getInstance();
        instance.setTime(sp.getStartDate());
        //get hour
        instance.set(Calendar.HOUR_OF_DAY,sp.getTime());
        //start time
        Date startTime = instance.getTime();
        //end time
        instance.add(Calendar.HOUR_OF_DAY,2);
        Date endTime = instance.getTime();

        //now
        long now = System.currentTimeMillis();
        //start < now < end

        return startTime.getTime() <=now  &&  now<endTime.getTime();
    }
    private UserInfo getUserByToken(String token){
        return JSON.parseObject(redisTemplate.opsForValue().get(CommonRedisKey.USER_TOKEN.getRealKey(token)), UserInfo.class);
    }
}