package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ProjectGroupApplicationCreateDTO {

    @NotNull(message = "实训批次ID不能为空")
    private Long batchId;

    @NotNull(message = "课题ID不能为空")
    private Long topicId;

    @NotBlank(message = "项目组名称不能为空")
    @Size(max = 100, message = "项目组名称长度不能超过100")
    private String groupName;

    @Size(max = 200, message = "项目名称长度不能超过200")
    private String projectName;

    @Size(max = 2000, message = "项目简介长度不能超过2000")
    private String projectDescription;

    @Size(max = 255, message = "仓库地址长度不能超过255")
    private String repoUrl;

    @Size(max = 255, message = "部署地址长度不能超过255")
    private String deployUrl;

    @Size(max = 500, message = "建组申请理由长度不能超过500")
    private String applyReason;

    private List<Long> memberIds;
}
