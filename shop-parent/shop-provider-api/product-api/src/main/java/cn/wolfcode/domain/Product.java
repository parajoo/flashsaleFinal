package cn.wolfcode.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;


@Setter@Getter
public class Product implements Serializable {
    private Long id;//商品iD
    private String productName;//商品名称
    private String productTitle;//商品标题
    private String productImg;//商品图片
    private String productDetail;//商品明细
    private BigDecimal productPrice;//商品价格
}
