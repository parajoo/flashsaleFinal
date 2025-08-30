package cn.wolfcode.util;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.CodeMsg;

public class AssertUtils {
    public static final int ASSERT_UTIL_EXCEPTION_CODE = 501;

    public static void notNull(Object obj, String msg) {
        if (obj == null) {
            throw new BusinessException(new CodeMsg(ASSERT_UTIL_EXCEPTION_CODE, msg));
        }
    }

    public static void isTrue(boolean ret, String msg) {
        if (!ret) {
            throw new BusinessException(new CodeMsg(ASSERT_UTIL_EXCEPTION_CODE, msg));
        }
    }

    public static void isFalse(boolean ret, String msg) {
        if (ret) {
            throw new BusinessException(new CodeMsg(ASSERT_UTIL_EXCEPTION_CODE, msg));
        }
    }
}
