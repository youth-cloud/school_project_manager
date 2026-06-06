package org.example.backend.modules.edu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("topic_application")
public class TopicApplication {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long batchId;

    private Long topicId;

    private Long studentId;

    private String applyReason;

    private String status;

    private LocalDateTime reviewTime;

    private Long reviewerId;

    private String reviewComment;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
