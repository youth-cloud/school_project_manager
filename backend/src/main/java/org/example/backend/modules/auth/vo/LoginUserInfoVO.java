package org.example.backend.modules.auth.vo;

import lombok.Data;

import java.util.List;

@Data
public class LoginUserInfoVO {
    private Long id;
    private String username;
    private String realName;
    private List<String> roles;
}
