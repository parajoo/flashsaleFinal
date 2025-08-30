package cn.wolfcode.common.web.resolver;

import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.redis.CommonRedisKey;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class UserInfoMethodArgumentResolver implements HandlerMethodArgumentResolver {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        //判断参数类型是否是要处理的类型
        return parameter.hasParameterAnnotation(RequestUser.class) &&
                parameter.getParameterType() == UserInfo.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        //从请求头中获取token
        String token = nativeWebRequest.getHeader("token");
        return JSON.parseObject(redisTemplate.opsForValue().get(CommonRedisKey.USER_TOKEN.getRealKey(token)), UserInfo.class);
    }
}
