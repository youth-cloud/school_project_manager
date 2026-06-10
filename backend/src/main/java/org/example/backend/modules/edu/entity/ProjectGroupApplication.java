package org.example.backend.modules.edu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project_group_application")
public class ProjectGroupApplication {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long batchId;

    private Long topicId;

    private Long leaderId;

    private String groupName;

    private String projectName;

    private String projectDescription;

    private String repoUrl;

    private String deployUrl;

    private String applyReason;

    private String status;

    private Long reviewerId;

    private String reviewComment;

    private LocalDateTime reviewTime;

    private Long generatedGroupId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}