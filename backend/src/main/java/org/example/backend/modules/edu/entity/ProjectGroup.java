package org.example.backend.modules.edu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project_group")
public class ProjectGroup {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long batchId;

    private Long topicId;

    private String groupName;

    private Long leaderId;

    private Long teacherId;

    private String projectName;

    private String projectDescription;

    private String repoUrl;

    private String deployUrl;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
