package cn.wolfcode.web.controller;


import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.domain.PayResult;
import cn.wolfcode.service.IOrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;


@RestController
@RequestMapping("/orderPay")
@RefreshScope
public class OrderPayController {
    private final IOrderInfoService orderInfoService;

    public OrderPayController(IOrderInfoService orderInfoService) {
        this.orderInfoService = orderInfoService;
    }
    @GetMapping("/pay")
    public Result<String> doPay(String orderNo,Integer type) {
        //判断类型 调用不同api
        if(type== OrderInfo.PAY_TYPE_ONLINE){
            return Result.success(orderInfoService.onlinePay(orderNo));
        }
        return null;
    }
    @PostMapping("/success")
    public Result<?> alipaySuccess(@RequestBody PayResult result) {
        orderInfoService.alipaySuccess(result);
        return Result.success();
    }
}
