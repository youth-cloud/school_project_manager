package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainingBatchCreateDTO {

    @NotBlank(message = "批次名称不能为空")
    @Size(max = 100, message = "批次名称长度不能超过100")
    private String batchName;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    @NotNull(message = "班级ID不能为空")
    private Long classId;

    @Size(max = 50, message = "学期名称长度不能超过50")
    private String termName;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    private LocalDateTime defenseTime;

    @Size(max = 500, message = "批次说明长度不能超过500")
    private String description;

    @NotNull(message = "状态不能为空")
    private Integer status;


}
