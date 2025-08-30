package cn.wolfcode.service;

import cn.wolfcode.domain.SeckillProductVo;

import java.util.List;


public interface ISeckillProductService {

    List<SeckillProductVo> selectTodayListByTime(Integer time);

    List<SeckillProductVo> selectTodayListByTimeFromRedis(Integer time);

    SeckillProductVo selectByIdAndTime(Long seckillId, Integer time);

    void decrStockCount(Long id, Integer time);

    void decrStockCount(Long id);

    Long selectStockCountId(Long seckillId);

    void incrStockCount(Long seckillId);
}
