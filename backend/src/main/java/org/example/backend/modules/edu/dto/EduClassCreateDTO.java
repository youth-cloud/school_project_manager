package org.example.backend.modules.edu.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EduClassCreateDTO {

    @NotBlank(message = "班级不能为空")
    @Size(max = 100, message = "班级名称长度不能超过100")
    private String className;
    @Size(max = 100, message = "专业名称长度不能超过100")
    private String majorName;
    @Size(max = 20, message = "年级长度不能超过20")
    private String grade;
    @Size(max = 50, message = "辅导员姓名长度不能超过50")
    private String counselorName;
    @NotNull(message = "状态不能为空")
    private Integer status;
}
