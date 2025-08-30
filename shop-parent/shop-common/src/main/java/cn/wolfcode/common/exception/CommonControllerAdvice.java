package cn.wolfcode.common.exception;

import cn.wolfcode.common.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by wolfcode
 */
@Slf4j
public class CommonControllerAdvice {

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Result<?> handleBusinessException(BusinessException ex) {
        log.warn("[业务异常] 出现业务异常：", ex);
        return Result.error(ex.getCodeMsg());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<?> handleDefaultException(Exception ex) {
        //在控制台打印错误消息.
        log.error("[通用异常处理] 出现系统异常", ex);
        return Result.defaultError();
    }
}
