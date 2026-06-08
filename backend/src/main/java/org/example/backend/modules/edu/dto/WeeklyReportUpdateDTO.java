package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WeeklyReportUpdateDTO {
    @NotNull(message = "周报ID不能为空")
    private Long id;

    @NotNull(message = "实训批次ID不能为空")
    private Long batchId;

    @NotNull(message = "项目组ID不能为空")
    private Long groupId;

    @NotNull(message = "周次不能为空")
    private Integer weekIndex;

    private String completedWork;

    private String problemDesc;

    private String nextPlan;

    @NotNull(message = "状态不能为空")
    private Integer status;
}