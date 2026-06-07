package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewRecordPageQueryDTO {
    @Min(value = 1, message = "当前页不能小于1")
    private long current = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private long size = 10;

    private Long submissionId;

    private Long reviewerId;

    @Size(max = 30, message = "审核结果长度不能超过30")
    private String reviewResult;
}