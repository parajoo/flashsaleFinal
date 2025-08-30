package cn.wolfcode.common.web;

import cn.wolfcode.common.exception.BusinessException;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by wolfcode
 */
@Setter
@Getter
public class Result<T> implements Serializable {

    public static final int SUCCESS_CODE = 200;//成功码.
    public static final String SUCCESS_MESSAGE = "操作成功";//成功信息.

    public static final int ERROR_CODE = 500000;//错误码.
    public static final String ERROR_MESSAGE = "系统异常";//错误信息.
    public static final int ASSERT_UTIL_EXCEPTION_CODE = 501;


    private int code;
    private String msg;
    private T data;

    public Result(){}

    private Result(int code, String msg, T data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    public static <T> Result<T> success(){return success(null);}
    public static <T> Result<T> success(T data){
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE,data);
    }

    public static <T> Result<T> success(String msg,T data){
        return new Result<>(SUCCESS_CODE, msg, data);

    }

    public static <T> Result<T> error(CodeMsg codeMsg){
        return new Result<>(codeMsg.getCode(), codeMsg.getMsg(),null);
    }

    public static <T> Result<T> defaultError(){
        return new Result<>(ERROR_CODE, ERROR_MESSAGE,null);
    }

    public boolean hasError(){
        // 状态码 !=200 说明有错误.
        return this.code != SUCCESS_CODE;
    }

    public T checkAndGet() {
        if(hasError()){
            throw new BusinessException(new CodeMsg(501,msg));
        }
        return this.data;
    }
}
