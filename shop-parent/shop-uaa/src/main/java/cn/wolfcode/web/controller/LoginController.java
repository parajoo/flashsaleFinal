package cn.wolfcode.web.controller;

import cn.wolfcode.common.constants.CommonConstants;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.UserLogin;
import cn.wolfcode.domain.UserResponse;
import cn.wolfcode.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/token")
public class LoginController {
    @Autowired
    private IUserService userService;

    @PostMapping
    public Result<UserResponse> login(@RequestBody UserLogin userLogin,
                                      @RequestHeader(value = CommonConstants.REAL_IP, required = false) String ip,
                                      @RequestHeader(value = CommonConstants.TOKEN_NAME, required = false) String token) {
        /**
         * 获取用户IP,因为微服务的请求是网关转发过来的.
         * 所以request.getRemoteAddr()获取到的是网关的IP
         * 我们需要在网关中获取到真实IP,然后放入到请求头中。
         * 在微服务中通过获取请求头从而获取到真实的客户端IP
         */
        //进行登录，并将这个token返回给前台
        UserResponse userResponse = userService.login(userLogin.getPhone(), userLogin.getPassword(), ip, token);
        return Result.success(userResponse);
    }
}


