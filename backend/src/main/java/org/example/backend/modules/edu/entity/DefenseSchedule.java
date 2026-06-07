package org.example.backend.modules.edu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("defense_schedule")
public class DefenseSchedule {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long batchId;

    private Long groupId;

    private LocalDate defenseDate;

    private LocalTime defenseTime;

    private String location;

    private Integer orderNo;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}