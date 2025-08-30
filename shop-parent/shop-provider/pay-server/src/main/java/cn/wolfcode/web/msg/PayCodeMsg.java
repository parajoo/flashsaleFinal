package cn.wolfcode.web.msg;
import cn.wolfcode.common.web.CodeMsg;

/**
 * Created by wolfcode
 */
public class PayCodeMsg extends CodeMsg {
    public static final PayCodeMsg PAY_FAILD = new PayCodeMsg(506000,"支付失败");

    private PayCodeMsg(Integer code, String msg){
        super(code,msg);
    }
}
