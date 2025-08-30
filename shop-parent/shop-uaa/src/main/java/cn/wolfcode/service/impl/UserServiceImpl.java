package cn.wolfcode.service.impl;

import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.domain.LoginLog;
import cn.wolfcode.domain.UserLogin;
import cn.wolfcode.domain.UserResponse;
import cn.wolfcode.mapper.UserMapper;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.redis.CommonRedisKey;
import cn.wolfcode.redis.UaaRedisKey;
import cn.wolfcode.service.IUserService;
import cn.wolfcode.util.MD5Util;
import cn.wolfcode.web.msg.UAACodeMsg;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * Created by wolfcode
 */
@Service
public class UserServiceImpl implements IUserService {
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final RocketMQTemplate rocketMQTemplate;

    public UserServiceImpl(UserMapper userMapper, StringRedisTemplate redisTemplate, RocketMQTemplate rocketMQTemplate) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    private UserLogin getUser(Long phone) {
        UserLogin userLogin;
        String hashKey = UaaRedisKey.USER_HASH.getRealKey("");
        String zSetKey = UaaRedisKey.USER_ZSET.getRealKey("");
        String userKey = String.valueOf(phone);

        String objStr = (String) redisTemplate.opsForHash().get(hashKey, String.valueOf(phone));
        if (StringUtils.isEmpty(objStr)) {
            //缓存中并没有，从数据库中查询
            userLogin = userMapper.selectUserLoginByPhone(phone);
            //把用户的登录信息存储到Hash结构中.
            redisTemplate.opsForHash().put(hashKey, userKey, JSON.toJSONString(userLogin));
            //使用zSet结构,value存用户手机号码，分数为登录时间，在定时器中找出7天前登录的用户，然后再缓存中删除.
            //我们缓存中的只存储7天的用户登录信息(热点用户)
        } else {
            //缓存中有这个key
            userLogin = JSON.parseObject(objStr, UserLogin.class);
        }
        redisTemplate.opsForZSet().add(zSetKey, userKey, new Date().getTime());
        return userLogin;
    }

    @Override
    public UserResponse login(Long phone, String password, String ip, String token) {
        //无论登录成功还是登录失败,都需要进行日志记录
        LoginLog loginLog = new LoginLog(phone, ip, new Date());
        rocketMQTemplate.sendOneWay(MQConstant.LOGIN_TOPIC, loginLog);

        //如果token还在有效期之内就不在进行登录操作了.
        UserInfo userInfo = getByToken(token);
        if (userInfo == null) {
            // 根据用户手机号码查询用户对象
            UserLogin userLogin = this.getUser(phone);
            //进行密码加盐比对
            if (userLogin == null || !userLogin.getPassword().equals(MD5Util.encode(password, userLogin.getSalt()))) {
                //进入这里说明登录失败
                loginLog.setState(LoginLog.LOGIN_FAIL);
                //往MQ中发送消息,登录失败
                rocketMQTemplate.sendOneWay(MQConstant.LOGIN_TOPIC + ":" + LoginLog.LOGIN_FAIL, loginLog);
                //同时抛出异常，提示前台账号密码有误
                throw new BusinessException(UAACodeMsg.LOGIN_ERROR);
            }

            //查询
            userInfo = userMapper.selectUserInfoByPhone(phone);
            userInfo.setLoginIp(ip);
            token = createToken(userInfo);
            rocketMQTemplate.sendOneWay(MQConstant.LOGIN_TOPIC, loginLog);
        }

        return new UserResponse(token, userInfo);
    }

    private String createToken(UserInfo userInfo) {
        //token创建
        String token = UUID.randomUUID().toString().replace("-", "");
        //把user对象存储到redis中
        CommonRedisKey redisKey = CommonRedisKey.USER_TOKEN;
        redisTemplate.opsForValue().set(redisKey.getRealKey(token), JSON.toJSONString(userInfo), redisKey.getExpireTime(), redisKey.getUnit());
        return token;
    }

    /**
     * 根据传入的token获取UserInfo对象
     *
     * @param token
     * @return
     */
    private UserInfo getByToken(String token) {
        String strObj = redisTemplate.opsForValue().get(CommonRedisKey.USER_TOKEN.getRealKey(token));
        if (StringUtils.isEmpty(strObj)) {
            return null;
        }
        return JSON.parseObject(strObj, UserInfo.class);
    }
}
