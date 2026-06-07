package org.example.backend.modules.edu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("review_record")
public class ReviewRecord {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long submissionId;

    private Long reviewerId;

    private String reviewResult;

    private BigDecimal score;

    private String comment;

    private LocalDateTime reviewTime;

    private LocalDateTime createTime;
}