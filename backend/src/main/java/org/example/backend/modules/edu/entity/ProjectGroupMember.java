package org.example.backend.modules.edu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project_group_member")
public class ProjectGroupMember {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long groupId;

    private Long userId;

    @TableField("is_leader")
    private Integer isLeader;

    private LocalDateTime joinTime;

    private Integer status;

}
