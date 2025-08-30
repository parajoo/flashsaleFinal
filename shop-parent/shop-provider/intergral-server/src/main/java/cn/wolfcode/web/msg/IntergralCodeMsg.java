package cn.wolfcode.web.msg;
import cn.wolfcode.common.web.CodeMsg;

/**
 * Created by wolfcode
 */
public class IntergralCodeMsg extends CodeMsg {
    private IntergralCodeMsg(Integer code, String msg){
        super(code,msg);
    }
    public static final IntergralCodeMsg OP_INTERGRAL_ERROR = new IntergralCodeMsg(500601,"操作积分失败");
    public static final IntergralCodeMsg INTERGRAL_NOT_ENOUGH = new IntergralCodeMsg(500602,"积分余额不足");
}
