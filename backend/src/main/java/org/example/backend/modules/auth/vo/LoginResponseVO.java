package org.example.backend.modules.auth.vo;

import lombok.Data;

@Data
public class LoginResponseVO {
    private String token;
    private String tokenType;
    private LoginUserInfoVO userInfo;
}
