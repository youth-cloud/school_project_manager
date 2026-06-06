package org.example.backend.modules.edu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stage_task")
public class StageTask {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long batchId;

    private Long teacherId;

    private String taskTitle;

    private String taskDescription;

    private Integer stageNo;

    private LocalDateTime deadline;

    private Integer needReport;

    private Integer needSourceCode;

    private Integer needPdf;

    private Integer needScreenshot;

    private Integer needDemoUrl;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
