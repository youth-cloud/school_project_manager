package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StageTaskCreateDTO {
    @NotNull(message = "实训批次ID不能为空")
    private Long batchId;

    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "任务标题长度不能超过200")
    private String taskTitle;

    private String taskDescription;

    @NotNull(message = "阶段序号不能为空")
    private Integer stageNo;

    private LocalDateTime deadline;

    @NotNull(message = "是否需要报告不能为空")
    private Integer needReport;

    @NotNull(message = "是否需要源代码不能为空")
    private Integer needSourceCode;

    @NotNull(message = "是否需要PDF不能为空")
    private Integer needPdf;

    @NotNull(message = "是否需要截图不能为空")
    private Integer needScreenshot;

    @NotNull(message = "是否需要演示地址不能为空")
    private Integer needDemoUrl;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
