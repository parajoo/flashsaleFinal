package cn.wolfcode.web.controller;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.CodeMsg;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.config.AlipayProperties;
import cn.wolfcode.domain.PayResult;
import cn.wolfcode.domain.PayVo;
import cn.wolfcode.domain.RefundVo;
import cn.wolfcode.feign.SeckillFeignService;
import cn.wolfcode.web.msg.PayCodeMsg;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/alipay")
public class AlipayController {
    private final AlipayClient alipayClient;
    private final AlipayProperties alipayProperties;
    private final SeckillFeignService  seckillFeignService;

    public AlipayController(AlipayClient alipayClient, AlipayProperties alipayProperties, SeckillFeignService seckillFeignService) {
        this.alipayClient = alipayClient;
        this.alipayProperties = alipayProperties;
        this.seckillFeignService = seckillFeignService;
    }
    @GetMapping("/return_url")
    public String returnUrl(HttpServletRequest request) {
        Map<String,String> params = new HashMap<String,String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        try {
            log.info("[alipay同步回调]收到交易消息：{}",params);

            boolean verify_result = AlipaySignature.rsaCheckV1(params, alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(), "RSA2");
            if (!verify_result) {
                throw new BusinessException(new CodeMsg(501, "支付宝签名验证失败"));
            }
            String outTradeNo = request.getParameter("out_trade_no");
            return "redirect:http://localhost/order_detail.html?orderNo="+outTradeNo;
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return "签名验证失败";
    }

    @PostMapping("/notify_url")
    public String notifyUrl(HttpServletRequest request) {
        //接收支付宝参数
        Map<String,String> params = new HashMap<String,String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }
        try {
            log.info("[alipay异步回调]收到交易消息：{}",params);

            boolean verify_result = AlipaySignature.rsaCheckV1(params, alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(), "RSA2");
            if (!verify_result) {
                throw new BusinessException(new CodeMsg(501, "支付宝签名验证失败"));
            }
            String outTradeNo = request.getParameter("out_trade_no");
            //支付宝交易号
            String tradeNo = request.getParameter("trade_no");
            //交易状态
            String tradeStatus = request.getParameter("trade_status");
            String totalAmount = request.getParameter("total_amount");

            if(tradeStatus.equals("TRADE_FINISHED")){
                log.info("[alipay异步回调]{}订单已完成",outTradeNo);
            } else if (tradeStatus.equals("TRADE_SUCCESS")){
                log.info("[alipay异步回调]{}订单已支付成功",outTradeNo);
                //远程调用秒杀服务,更新订单状态
                PayResult result = new PayResult(outTradeNo,tradeNo,totalAmount);
                Result<?> ret = seckillFeignService.updateOrderPaySuccess(result);
                if (ret.hasError()) {
                    throw new BusinessException(new CodeMsg(501, "更新订单支付成功失败"));
                }
            }
            return "success";
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return "fail";
    }

    //用户发起支付的接口
    @PostMapping("/prepay")
    public Result<String> prepay(@RequestBody PayVo payVo) {
        // 构造请求参数以调用接口
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        /**** required parameters ****/
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payVo.getOutTradeNo());
        bizContent.put("total_amount", payVo.getTotalAmount());
        bizContent.put("subject", payVo.getSubject());
        bizContent.put("body", payVo.getBody());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        //receive address asynchronously,support http/https only,access by public net
        request.setNotifyUrl("");
        //skip address synchronously,support http/https only
        request.setReturnUrl("");
        request.setBizContent(bizContent.toString());
        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if(response.isSuccess()){
                String body = response.getBody();
                return Result.success(body);
            }
            return Result.error(new CodeMsg(506001,response.getMsg()));
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return Result.error(PayCodeMsg.PAY_FAILD);
    }
}
