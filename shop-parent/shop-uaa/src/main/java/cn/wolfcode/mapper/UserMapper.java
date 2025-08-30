package cn.wolfcode.mapper;

import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.domain.LoginLog;
import cn.wolfcode.domain.UserLogin;

/**
 * Created by wolfcode
 */
public interface UserMapper {
    /**
     * 根据用户手机号码查询用户登录信息对象
     * @param phone
     * @return
     */
    UserLogin selectUserLoginByPhone(Long phone);

    /**
     * 根据用户手机号码查询用户的基础信息
     * @param phone
     * @return
     */
    UserInfo selectUserInfoByPhone(Long phone);

    /**
     * 插入登录日志
     * @param loginLog
     * @return
     */
    int insertLoginLong(LoginLog loginLog);
}
