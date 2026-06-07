package org.example.backend.modules.edu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("submission_file")
public class SubmissionFile {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long submissionId;

    private String fileName;

    private String originalName;

    private String fileType;

    private Long fileSize;

    private String filePath;

    private String fileUrl;

    private String bizType;

    private Long uploadUserId;

    private LocalDateTime createTime;
}
