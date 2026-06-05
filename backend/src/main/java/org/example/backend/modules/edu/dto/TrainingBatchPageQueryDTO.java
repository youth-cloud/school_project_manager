package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TrainingBatchPageQueryDTO {
    @Min(value = 1, message = "当前页不能小于1")
    private long current = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private long size = 10;

    @Size(max = 100, message = "批次名称长度不能超过100")
    private String batchName;

    private Long courseId;

    private Long teacherId;

    private Long classId;

    @Size(max = 50, message = "学期名称长度不能超过50")
    private String termName;

    private Integer status;
}
