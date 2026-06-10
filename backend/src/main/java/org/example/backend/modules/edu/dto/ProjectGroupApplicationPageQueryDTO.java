package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectGroupApplicationPageQueryDTO {

    @Min(value = 1, message = "当前页不能小于1")
    private long current = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private long size = 10;

    @Size(max = 100, message = "项目组名称长度不能超过100")
    private String groupName;

    private Long batchId;

    private Long topicId;

    private Long leaderId;

    @Pattern(
            regexp = "PENDING|APPROVED|REJECTED|CANCELED",
            message = "状态只能是 PENDING、APPROVED、REJECTED 或 CANCELED"
    )
    private String status;
}