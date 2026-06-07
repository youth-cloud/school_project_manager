package org.example.backend.modules.edu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("defense_record")
public class DefenseRecord {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long scheduleId;

    private Long teacherId;

    private BigDecimal presentationScore;

    private BigDecimal answerScore;

    private BigDecimal completionScore;

    private BigDecimal totalScore;

    private String comment;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}