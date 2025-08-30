package cn.wolfcode.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by wolfcode
 * 把Seckillproduct和product的信息整合在一块，然后再前台展示.
 */
@Setter
@Getter
public class SeckillProductVo extends SeckillProduct implements Serializable {
    private String productName;
    private String productTitle;
    private String productImg;
    private String productDetail;
    private BigDecimal productPrice;
    private Integer currentCount;
}
