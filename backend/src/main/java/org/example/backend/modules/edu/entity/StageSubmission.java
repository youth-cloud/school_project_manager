package org.example.backend.modules.edu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stage_submission")
public class StageSubmission {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long taskId;

    private Long batchId;

    private Long groupId;

    private Long submitterId;

    private Integer versionNo;

    private String summary;

    private String reportText;

    private String repoUrl;

    private String deployUrl;

    private Integer status;

    private LocalDateTime submitTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
