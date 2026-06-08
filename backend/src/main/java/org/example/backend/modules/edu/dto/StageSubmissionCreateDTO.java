package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StageSubmissionCreateDTO {
    @NotNull(message = "阶段任务ID不能为空")
    private Long taskId;

    @NotNull(message = "实训批次ID不能为空")
    private Long batchId;

    @NotNull(message = "项目组ID不能为空")
    private Long groupId;

    @NotNull(message = "版本号不能为空")
    private Integer versionNo;

    @Size(max = 500, message = "提交摘要长度不能超过500")
    private String summary;

    private String reportText;

    @Size(max = 255, message = "仓库地址长度不能超过255")
    private String repoUrl;

    @Size(max = 255, message = "部署地址长度不能超过255")
    private String deployUrl;

    @NotNull(message = "状态不能为空")
    private Integer status;
}