package cn.wolfcode.service.impl;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.CodeMsg;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.Product;
import cn.wolfcode.domain.SeckillProduct;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.feign.ProductFeignApi;
import cn.wolfcode.mapper.SeckillProductMapper;
import cn.wolfcode.redis.SeckillRedisKey;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.util.AssertUtils;
import cn.wolfcode.util.IdGenerateUtil;
import com.alibaba.fastjson.JSON;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@CacheConfig(cacheNames = "SeckillProduct")
public class SeckillProductServiceImpl implements ISeckillProductService {
    private final SeckillProductMapper seckillProductMapper;
    private final StringRedisTemplate redisTemplate;
    private final ProductFeignApi productFeignApi;
//    @Autowired
//    private RocketMQTemplate rocketMQTemplate;
    private final RedisScript<Boolean> redisScript;
    private final ScheduledExecutorService scheduledExecutorService;

    public SeckillProductServiceImpl(SeckillProductMapper seckillProductMapper, StringRedisTemplate redisTemplate, ProductFeignApi productFeignApi, RedisScript<Boolean> redisScript, ScheduledExecutorService scheduledExecutorService) {
        this.seckillProductMapper = seckillProductMapper;
        this.redisTemplate = redisTemplate;
        this.productFeignApi = productFeignApi;
        this.redisScript = redisScript;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public List<SeckillProductVo> selectTodayListByTime(Integer time) {
        // 1. 调用秒杀服务接口, 基于今天的时间, 查询今天的所有秒杀商品数据
        List<SeckillProduct> todayList = seckillProductMapper.queryCurrentlySeckillProduct(time);
        // 2. 遍历秒杀商品列表, 得到商品 id 列表
        List<Long> productIdList = todayList.stream() // Stream<SeckillProduct>
                .map(SeckillProduct::getProductId) // SeckillProduct => Long
                .distinct()
                .collect(Collectors.toList());
        // 3. 根据商品 id 列表, 调用商品服务查询接口, 得到商品列表
        Result<List<Product>> result = productFeignApi.selectByIdList(productIdList);
        /**
         * result 可能存在的几种情况:
         *  1. 远程接口正常返回, code == 200, data == 想要的数据
         *  2. 远程接口出现异常, code != 200
         *  3. 接口被熔断降级, data == null
         */
        if (result.hasError() || result.getData() == null) {
            throw new BusinessException(new CodeMsg(result.getCode(), result.getMsg()));
        }

        List<Product> products = result.getData();

        // 4. 遍历秒杀商品列表, 将商品对象与秒杀商品对象聚合到一起
        // List<SeckillProduct> => List<SeckillProductVo>
        List<SeckillProductVo> productVoList = todayList.stream()
                .map(sp -> {
                    SeckillProductVo vo = new SeckillProductVo();
                    BeanUtils.copyProperties(sp, vo);

                    List<Product> list = products.stream().filter(p -> sp.getProductId().equals(p.getId())).collect(Collectors.toList());
                    if (list.size() > 0) {
                        Product product = list.get(0);
                        BeanUtils.copyProperties(product, vo);
                    }
                    vo.setId(sp.getId());//单个商品有可能参与多次秒杀 为保证唯一性 得是秒杀商品id

                    return vo;
                }) // Stream<SeckillProductVo>
                .collect(Collectors.toList());

        return productVoList;
    }

    @Override
    public List<SeckillProductVo> selectTodayListByTimeFromRedis(Integer time) {
        String key = SeckillRedisKey.SECKILL_PRODUCT_LIST.join(time + "");
        List<String> stringList = redisTemplate.opsForList().range(key, 0, -1);

        if (stringList == null || stringList.size() == 0) {
            log.warn("[秒杀商品] 查询秒杀商品列表异常, Redis 中没有数据, 从 DB 中查询...");
            return this.selectTodayListByTime(time);
        }

        return stringList.stream().map(json -> JSON.parseObject(json, SeckillProductVo.class)).collect(Collectors.toList());
    }

    @Override
    @Cacheable(key = "'selectByIdAndTime:' + #seckillId")//2025/8/17 --origin:key = "'selectByIdAndTime:' +#time+':' + #seckillId"
    public SeckillProductVo selectByIdAndTime(Long seckillId, Integer time) {
        SeckillProduct seckillProduct = seckillProductMapper.selectByIdAndTime(seckillId, time);

        Result<List<Product>> result = productFeignApi.selectByIdList(Collections.singletonList(seckillProduct.getProductId()));
        if (result.hasError() || result.getData() == null || result.getData().size() == 0) {
            throw new BusinessException(new CodeMsg(result.getCode(), result.getMsg()));
        }

        Product product = result.getData().get(0);

        SeckillProductVo vo = new SeckillProductVo();
        // 先将商品的属性 copy 到 vo 对象中
        BeanUtils.copyProperties(product, vo);

        // 再将秒杀商品的属性 copy 到 vo 对象中, 并覆盖 id 属性
        BeanUtils.copyProperties(seckillProduct, vo);
        return vo;
    }
    //optimistic lock
    @CacheEvict(key = "'selectByIdAndTime:'  + #Id")
    @Override
    public void decrStockCount(Long id){
        int row = seckillProductMapper.decrStock(id);
        AssertUtils.isTrue(row>0,"understock!!!");
    }

    @Override
    public Long selectStockCountId(Long seckillId) {
        return seckillProductMapper.selectStockCountById(seckillId);
    }

    @Override
    public void incrStockCount(Long seckillId) {
        seckillProductMapper.incrStock(seckillId);
    }
    // pessimistic lock
    /*
    1.利用redis setnx的原子性命令实现分布式锁
    2.为避免死锁，加锁时引入超时机制，如果意外宕机，需要自动释放锁
    3.为避免加锁操作与设置超时时间操作原子性问题，使用lua脚本保证多个命令执行的原子性
    4.锁只能由加锁的线程释放，不能被其它线程释放，利用唯一线程Id作为value，删除时是否相同
    5.为避免业务没执行完，锁就过期了，引入watchdog实现锁自动续期
     */

    @CacheEvict(key = "'selectByIdAndTime:'  + #Id")//2025/8/17加
    //@CacheEvict(key = "'selectByIdAndTime:' + #time + ':' + #id")
    @Override
    public void decrStockCount(Long id, Integer time) {
        //1.锁对象，锁记录存在哪里=>指定场地的制定商品
        //2.当多线程同时加锁是  只能由一个线程锁成功=>Redis的setnx命令
        //3.实现线程阻塞/线程等待=>Redis的setnx命令存储在Redis的String数据结构中
        String key = "seckill:product:stockcount:" + time + ":" + id;
        String threadId = "";
        ScheduledFuture<?> future = null;
        try {
            //自旋超过5抛出异常
            int count = 0;
            Boolean ret = false;
            int timeout = 5;
            do {
                //生成分布式唯一线程id
                threadId = IdGenerateUtil.get().nextId() + "";
                //Lua脚本
                //ret = redisTemplate.opsForValue().setIfAbsent(key, "1");
                ret = redisTemplate.execute(redisScript, Collections.singletonList(key), threadId, timeout + "");//expire time:10s
                if (ret!=null && ret) {
                    break;
                }
                AssertUtils.isTrue((count++) < 5,"bust now!");
                //
                Thread.sleep(20);
            } while (true);
            //4.线程获取不到锁的时候=>阻塞/自旋等待
            //加锁成功后 创建WatchDog监听业务是否执行完成 实现续期
            long delayTime = (long) (timeout * 0.8);
            String finalThreadId = threadId;
            System.out.println("watchdog delaytime = "+ delayTime);
            future = scheduledExecutorService.scheduleAtFixedRate(
                    () -> {
                        //1.query if key in redis existed?if exist=>renew
                        String value = redisTemplate.opsForValue().get(key);//only key in redis?
                        System.out.println("wacthDog---redis renew" +"ThreadId="+finalThreadId+",key="+key+",value="+value);
                        if (finalThreadId.equals(value)) {
                            //1.renew the current  key
                            redisTemplate.expire(key, delayTime + 2, TimeUnit.SECONDS);
                            System.out.println("wacthDog---redis renew"+key);
                            return;
                        }
                        //if not exist,stop the current task
                        //Thread.currentThread().interrupt();
                    },
                    delayTime,
                    delayTime,
                    TimeUnit.SECONDS
            );
            //TimeUnit.SECONDS.sleep(11);

            //先查库存 再扣库存
            Long stockCount = seckillProductMapper.selectStockCountById(id);
            AssertUtils.isTrue(stockCount > 0, "no inventory");
            seckillProductMapper.decrStock(id);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(future != null){
                future.cancel(true);
            }
            //先获取value 判断当前value是否与线程id相同 只有id相同才需要释放锁
            String value  = redisTemplate.opsForValue().get(key);
            if(threadId.equals(value)) {//value有可能null,但是threadId不会
                redisTemplate.delete(key);//释放锁
            }
        }
    }
    }

