package cn.wolfcode.domain;

import cn.wolfcode.common.domain.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter@Getter@AllArgsConstructor@NoArgsConstructor
public class UserResponse {
    private String token;
    private UserInfo userInfo;
}
