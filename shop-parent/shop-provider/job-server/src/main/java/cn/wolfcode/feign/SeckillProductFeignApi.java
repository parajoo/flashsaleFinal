package cn.wolfcode.feign;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.SeckillProductVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "seckill-service")
public interface SeckillProductFeignApi {

    @RequestMapping("/seckillProduct/selectTodayListByTime")
    Result<List<SeckillProductVo>> selectTodayListByTime(@RequestParam("time") Integer time);
}
