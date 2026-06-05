package org.example.backend.modules.edu.dto;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Data
public class ProjectTopicPageQueryDTO {
    @Min(value = 1, message = "当前页不能小于1")
    private long current = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private long size = 10;

    @Size(max = 200, message = "课题名称长度不能超过200")
    private String topicName;

    private Long batchId;

    private Long teacherId;

    private Integer difficultyLevel;

    private Integer status;
}
