package cn.wolfcode.feign.fallback;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.Product;
import cn.wolfcode.feign.ProductFeignApi;

import java.util.List;

public class ProductFeignFallback implements ProductFeignApi {

    @Override
    public Result<List<Product>> selectByIdList(List<Long> idList) {
        return null;
    }
}
