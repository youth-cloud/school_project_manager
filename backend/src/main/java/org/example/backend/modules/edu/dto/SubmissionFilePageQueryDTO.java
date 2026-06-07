package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubmissionFilePageQueryDTO {
    @Min(value = 1, message = "当前页不能小于1")
    private long current = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private long size = 10;

    private Long submissionId;

    @Size(max = 50, message = "文件类型长度不能超过50")
    private String fileType;

    @Size(max = 50, message = "业务类型长度不能超过50")
    private String bizType;

    private Long uploadUserId;
}