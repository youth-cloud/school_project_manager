package org.example.backend.modules.edu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project_group_application_member")
public class ProjectGroupApplicationMember {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long applicationId;

    private Long userId;

    private Integer isLeader;

    private LocalDateTime createTime;
}