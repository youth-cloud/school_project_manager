package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TopicApplicationPageQueryDTO {
    @Min(value = 1, message = "当前页不能小于1")
    private long current = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private long size = 10;

    private Long batchId;

    private Long topicId;

    private Long studentId;

    @Pattern(
            regexp = "PENDING|APPROVED|REJECTED|CANCELED",
            message = "状态只能是 PENDING、APPROVED、REJECTED 或 CANCELED"
    )
    private String status;
}
