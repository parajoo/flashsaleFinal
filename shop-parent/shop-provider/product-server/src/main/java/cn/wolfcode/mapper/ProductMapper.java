package cn.wolfcode.mapper;

import cn.wolfcode.domain.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ProductMapper {
    /**
     * 根据用户传入的id集合查询商品对象信息
     * @param ids
     * @return
     */
    List<Product> queryProductByIds(@Param("ids") List<Long> ids);
}
