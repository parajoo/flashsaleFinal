package cn.wolfcode.common.web.interceptor;

import cn.wolfcode.common.constants.CommonConstants;
import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.common.web.CommonCodeMsg;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.common.web.anno.RequireLogin;
import cn.wolfcode.redis.CommonRedisKey;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RequireLoginInterceptor implements HandlerInterceptor {

    private StringRedisTemplate redisTemplate;

    public RequireLoginInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            String feignRequest = request.getHeader(CommonConstants.FEIGN_REQUEST_KEY);
            if (!StringUtils.isEmpty(feignRequest) && CommonConstants.FEIGN_REQUEST_FALSE.equals(feignRequest) && handlerMethod.getMethodAnnotation(RequireLogin.class) != null) {
                response.setContentType("application/json;charset=utf-8");//feign内部请求 不需要拦截
                String token = request.getHeader(CommonConstants.TOKEN_NAME);//请求头里拿token
                if (StringUtils.isEmpty(token)) {
                    response.getWriter().write(JSON.toJSONString(Result.error(CommonCodeMsg.TOKEN_INVALID)));
                    return false;//拦截请求
                }
                UserInfo userInfo = JSON.parseObject(redisTemplate.opsForValue().get(CommonRedisKey.USER_TOKEN.getRealKey(token)), UserInfo.class);
                //从redis根据token获取用户信息
                if (userInfo == null) {
                    response.getWriter().write(JSON.toJSONString(Result.error(CommonCodeMsg.TOKEN_INVALID)));
                    return false;
                }
                /*String ip = request.getHeader(CommonConstants.REAL_IP);//获取真实ip
                if (!userInfo.getLoginIp().equals(ip)) {
                    response.getWriter().write(JSON.toJSONString(Result.error(CommonCodeMsg.LOGIN_IP_CHANGE)));
                    return false;
                }
                 */
            }
        }
        return true;//放行请求
    }
}

