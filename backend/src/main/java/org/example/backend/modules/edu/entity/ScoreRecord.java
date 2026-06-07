package org.example.backend.modules.edu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("score_record")
public class ScoreRecord {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long batchId;

    private Long groupId;

    private Long studentId;

    private BigDecimal processScore;

    private BigDecimal reportScore;

    private BigDecimal submissionScore;

    private BigDecimal defenseScore;

    private BigDecimal finalScore;

    private String gradeLevel;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}