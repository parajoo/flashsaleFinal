package cn.wolfcode.mapper;

import cn.wolfcode.domain.OrderInfo;
import org.apache.ibatis.annotations.Param;

/**
 * Created by wolfcode
 */
public interface OrderInfoMapper {
    /**
     * 插入订单信息
     *
     * @param orderInfo
     * @return
     */
    int insert(OrderInfo orderInfo);

    /**
     * 根据订单编号查找订单
     *
     * @param orderNo
     * @return
     */
    OrderInfo find(String orderNo);

    /**
     * 将订单状态修改成取消状态
     *
     * @param orderNo
     * @param status
     * @return
     */
    int updateCancelStatus(@Param("orderNo") String orderNo, @Param("status") Integer status);

    /**
     * 将订单状态修改成支付状态
     *
     * @param orderNo
     * @param status
     * @param payType
     * @return
     */
    int changePayStatus(@Param("orderNo") String orderNo, @Param("status") Integer status, @Param("payType") int payType);

    /**
     * 将订单状态修改成已退款状态
     *
     * @param outTradeNo
     * @param statusRefund
     * @return
     */
    int changeRefundStatus(@Param("orderNo") String outTradeNo, @Param("status") Integer statusRefund);

    OrderInfo selectByUserIdAndSeckillId(@Param("userId") Long userId, @Param("seckillId") Long seckillId, @Param("time") Integer time);

    OrderInfo selectById(String orderNo);
}
