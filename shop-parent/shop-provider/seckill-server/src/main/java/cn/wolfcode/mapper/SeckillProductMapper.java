package cn.wolfcode.mapper;

import cn.wolfcode.domain.SeckillProduct;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface SeckillProductMapper {
    /**
     * 根据time时间场次查询对应的秒杀商品集合
     * @param time
     * @return
     */
    List<SeckillProduct> queryCurrentlySeckillProduct(Integer time);

    /**
     * 对秒杀商品库存进行递减操作
     * @param seckillId
     * @return
     */
    int decrStock(Long seckillId);

    /**
     * 对秒杀商品库存进行增加操作
     * @param seckillId
     * @return
     */
    int incrStock(Long seckillId);

    /**
     * 获取数据库中商品库存的数量
     * @param seckillId
     * @return
     */
    int getStockCount(Long seckillId);

    SeckillProduct selectByIdAndTime(@Param("seckillId") Long seckillId, @Param("time") Integer time);

    Long selectStockCountById(Long id);

}
