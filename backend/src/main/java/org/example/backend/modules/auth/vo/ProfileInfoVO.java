package org.example.backend.modules.auth.vo;

import lombok.Data;

import java.util.List;

@Data
public class ProfileInfoVO {
    private Long id;
    private String username;
    private String realName;
    private List<String> roles;
    private String studentNo;
    private Long classId;
    private String className;
    private String phone;
    private String email;
    private Integer status;
}
