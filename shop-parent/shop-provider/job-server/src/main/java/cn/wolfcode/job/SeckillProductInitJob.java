package cn.wolfcode.job;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.feign.SeckillProductFeignApi;
import cn.wolfcode.redis.SeckillRedisKey;
import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 每天凌晨 00:00 执行
 * 将今天的秒杀商品数据, 全部缓存到 Redis 中
 */
@Component
@Setter
@Getter
@RefreshScope
@Slf4j
public class SeckillProductInitJob implements SimpleJob {

    @Value("${job.seckillProduct.cron}")
    private String cron;
    @Value("${job.seckillProduct.shardingCount}")
    private Integer shardingCount;
    @Value("${job.seckillProduct.shardingParameters}")
    private String shardingParameters;
    @Value("${job.seckillProduct.dataFlow}")
    private boolean dataFlow;

    private final SeckillProductFeignApi seckillProductFeignApi;
    private final StringRedisTemplate stringRedisTemplate;

    public SeckillProductInitJob(SeckillProductFeignApi seckillProductFeignApi, StringRedisTemplate stringRedisTemplate) {
        this.seckillProductFeignApi = seckillProductFeignApi;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void execute(ShardingContext shardingContext) {
        // 分片参数利用场次进行分片
        String time = shardingContext.getShardingParameter();//

        // 先清空之前的数据
        String key = SeckillRedisKey.SECKILL_PRODUCT_LIST.join(time);
        stringRedisTemplate.delete(key);//删除前一天的数据，这个key只有天数信息没有场次信息

        // 调用秒杀服务的接口, 查询秒杀商品数据
        Result<List<SeckillProductVo>> result = seckillProductFeignApi.selectTodayListByTime(Integer.valueOf(time));
        if (result.hasError() || result.getData() == null) {
            log.warn("[秒杀商品数据预热] 查询秒杀商品数据失败, 远程服务异常. res={}", JSON.toJSONString(result));
            return;
        }

        List<SeckillProductVo> productVoList = result.getData();
        log.info("[秒杀商品数据预热] 准备开始预热秒杀商品数据, 当前场次:{}, 本次缓存的数据:{}", time, productVoList.size());

        // 将数据存入 Redis : List
        // key=TODAY:{time}:SECKILL:PRODUCTS
        // value=SeckillProductVo => {json}
        for (SeckillProductVo vo : productVoList) {
            String json = JSON.toJSONString(vo);
            stringRedisTemplate.opsForList().rightPush(key, json);
            //库存预热 8.22
            String hashKey = SeckillRedisKey.SECKILL_STOCK_COUNT_HASH.join(time + "");
            stringRedisTemplate.opsForHash().put(hashKey,vo.getId()+"",vo.getStockCount()+"");
        }
        log.info("[秒杀商品数据预热] 数据预热完成...");
    }
}
