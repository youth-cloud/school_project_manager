package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ScoreRecordPageQueryDTO {
    @Min(value = 1, message = "当前页不能小于1")
    private long current = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private long size = 10;

    private Long batchId;

    private Long groupId;

    private Long studentId;

    @Size(max = 20, message = "等级长度不能超过20")
    private String gradeLevel;
}