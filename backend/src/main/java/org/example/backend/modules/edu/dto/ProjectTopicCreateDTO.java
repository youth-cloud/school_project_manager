package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProjectTopicCreateDTO {
    @NotNull(message = "实训批次ID不能为空")
    private Long batchId;

    @NotBlank(message = "课题名称不能为空")
    @Size(max = 200, message = "课题名称长度不能超过200")
    private String topicName;

    @Size(max = 2000, message = "课题描述长度不能超过2000")
    private String topicDescription;

    @NotNull(message = "难度等级不能为空")
    @Min(value = 1, message = "难度等级不能小于1")
    @Max(value = 4, message = "难度等级不能大于4")
    private Integer difficultyLevel;

    @Size(max = 500, message = "技术要求长度不能超过500")
    private String techRequirements;

    @NotNull(message = "最大人数不能为空")
    @Min(value = 1, message = "最大人数不能小于1")
    private Integer maxMembers;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
