package org.example.backend.modules.system.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SysUserPageQueryDTO {
    @Min(value = 1, message = "当前页不能小于1")
    private long current = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private long size = 10;

    @Size(max = 50, message = "用户名长度不能超过50")
    private String username;

    @Size(max = 50, message = "姓名长度不能超过50")
    private String realName;

    @Size(max = 50, message = "角色编码长度不能超过50")
    private String roleCode;

    private Integer status;
}
