package org.example.backend.modules.edu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("weekly_report")
public class WeeklyReport {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long batchId;

    private Long groupId;

    private Long studentId;

    private Integer weekIndex;

    private String completedWork;

    private String problemDesc;

    private String nextPlan;

    private String teacherComment;

    private BigDecimal score;

    private LocalDateTime submitTime;

    private LocalDateTime reviewTime;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}