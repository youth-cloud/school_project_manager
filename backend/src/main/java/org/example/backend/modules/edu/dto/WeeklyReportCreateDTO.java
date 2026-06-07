package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WeeklyReportCreateDTO {
    @NotNull(message = "实训批次ID不能为空")
    private Long batchId;

    @NotNull(message = "项目组ID不能为空")
    private Long groupId;

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotNull(message = "周次不能为空")
    private Integer weekIndex;

    private String completedWork;

    private String problemDesc;

    private String nextPlan;

    @NotNull(message = "状态不能为空")
    private Integer status;
}