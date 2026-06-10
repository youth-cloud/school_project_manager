package org.example.backend.modules.system.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SysUserManageItemVO {
    private Long id;
    private String username;
    private String realName;
    private String studentNo;
    private Long classId;
    private String className;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<String> roleCodes;
    private List<String> roleNames;
}
