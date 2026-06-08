package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectGroupMemberCreateDTO {
    @NotNull(message = "项目组ID不能为空")
    private Long groupId;

    @NotNull(message = "是否组长不能为空")
    private Integer isLeader;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
