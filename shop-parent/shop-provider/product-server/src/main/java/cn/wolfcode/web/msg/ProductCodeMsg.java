package cn.wolfcode.web.msg;

import cn.wolfcode.common.web.CodeMsg;

/**
 * Created by wolfcode
 */
public class ProductCodeMsg extends CodeMsg {
    public static final CodeMsg TEST = new ProductCodeMsg(600101, "测试 Feign 接口异常!");

    private ProductCodeMsg(Integer code, String msg) {
        super(code, msg);
    }
}
