package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TopicApplicationCreateDTO {
    @NotNull(message = "实训批次ID不能为空")
    private Long batchId;

    @NotNull(message = "课题ID不能为空")
    private Long topicId;

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @Size(max = 500, message = "申请理由长度不能超过500")
    private String applyReason;
}
