package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectGroupUpdateDTO {
    @NotNull(message = "项目组ID不能为空")
    private Long id;

    @NotNull(message = "实训批次ID不能为空")
    private Long batchId;

    @NotNull(message = "课题ID不能为空")
    private Long topicId;

    @NotBlank(message = "项目组名称不能为空")
    @Size(max = 100, message = "项目组名称长度不能超过100")
    private String groupName;

    @NotNull(message = "组长ID不能为空")
    private Long leaderId;

    @NotNull(message = "指导教师ID不能为空")
    private Long teacherId;

    @Size(max = 200, message = "项目名称长度不能超过200")
    private String projectName;

    @Size(max = 2000, message = "项目简介长度不能超过2000")
    private String projectDescription;

    @Size(max = 255, message = "仓库地址长度不能超过255")
    private String repoUrl;

    @Size(max = 255, message = "部署地址长度不能超过255")
    private String deployUrl;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
