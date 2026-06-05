package org.example.backend.modules.edu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("training_batch")
public class TrainingBatch {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String batchName;

    private Long courseId;

    private Long teacherId;

    private Long classId;

    private String termName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime defenseTime;

    private String description;

    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
