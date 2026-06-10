package org.example.backend.modules.system.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class SysUserUpdateDTO {
    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Size(max = 50, message = "用户名长度不能超过50")
    private String username;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50")
    private String realName;

    @Min(value = 0, message = "状态值不合法")
    @Max(value = 1, message = "状态值不合法")
    private Integer status = 1;

    @Size(max = 30, message = "学号长度不能超过30")
    private String studentNo;

    private Long classId;

    @NotEmpty(message = "至少选择一个角色")
    private List<String> roleCodes;
}
