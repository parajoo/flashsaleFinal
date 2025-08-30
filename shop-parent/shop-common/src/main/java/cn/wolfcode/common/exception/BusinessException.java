package cn.wolfcode.common.exception;

import cn.wolfcode.common.web.CodeMsg;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by wolfcode
 */
@Setter
@Getter
public class BusinessException extends RuntimeException {

    private CodeMsg codeMsg;

    public BusinessException(CodeMsg codeMsg){
        //super(codeMsg.getMsg());
        this.codeMsg = codeMsg;
    }
}
