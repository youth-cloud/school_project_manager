package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EduCourseCreateDTO {

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称长度不能超过100")
    private String courseName;
    @Size(max = 100,message = "课程编码长度不能超过100")
    private String courseCode;

    @NotNull(message = "学分不能为空")
    private BigDecimal credit;
    @Size(max = 200,message = "备注长度不能超过200")
    private String remark;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
