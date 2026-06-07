package org.example.backend.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class SysRole {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String roleCode;

    private String roleName;

    private String remark;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}