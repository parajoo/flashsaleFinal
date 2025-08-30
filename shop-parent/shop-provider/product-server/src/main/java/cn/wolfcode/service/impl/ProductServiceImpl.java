package cn.wolfcode.service.impl;

import cn.wolfcode.domain.Product;
import cn.wolfcode.mapper.ProductMapper;
import cn.wolfcode.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProductServiceImpl implements IProductService {
    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Product> selectByIdList(List<Long> idList) {
        return productMapper.queryProductByIds(idList);
    }
}
