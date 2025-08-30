package cn.wolfcode.web.controller;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.Product;
import cn.wolfcode.service.IProductService;
import cn.wolfcode.web.msg.ProductCodeMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/product")
@Slf4j
public class ProductController {
    @Autowired
    private IProductService productService;

    @RequestMapping("/selectByIdList")
    public Result<List<Product>> selectByIdList(@RequestParam("ids") List<Long> idList) {
        return Result.success(productService.selectByIdList(idList));
    }
}
