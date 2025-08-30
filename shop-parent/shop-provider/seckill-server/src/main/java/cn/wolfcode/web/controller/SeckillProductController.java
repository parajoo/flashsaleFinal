package cn.wolfcode.web.controller;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.service.ISeckillProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/seckillProduct")
@Slf4j
public class SeckillProductController {
    @Autowired
    private ISeckillProductService seckillProductService;

    @RequestMapping("/queryByTime")
    public Result<List<SeckillProductVo>> queryByTime(Integer time) {
        return Result.success(seckillProductService.selectTodayListByTimeFromRedis(time));
    }

    @RequestMapping("/selectTodayListByTime")
    public Result<List<SeckillProductVo>> selectTodayListByTime(@RequestParam("time") Integer time) {
        return Result.success(seckillProductService.selectTodayListByTime(time));
    }

    @RequestMapping("/find")
    public Result<SeckillProductVo> findById(Integer time, Long seckillId) {
        return Result.success(seckillProductService.selectByIdAndTime(seckillId, time));
    }

}
