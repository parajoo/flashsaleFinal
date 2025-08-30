package cn.wolfcode.util;

import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.redis.CommonRedisKey;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.StringRedisTemplate;


public class UserUtil {
    /**
     * 从Redis中根据用户token信息获取用户基本信息
     * @param redisTemplate
     * @param token
     * @return
     */
    public static UserInfo getUser(StringRedisTemplate redisTemplate, String token){
        String strObj = redisTemplate.opsForValue().get(CommonRedisKey.USER_TOKEN.getRealKey(token));
        UserInfo userInfo = JSON.parseObject(strObj, UserInfo.class);
        return userInfo;
    }
}
