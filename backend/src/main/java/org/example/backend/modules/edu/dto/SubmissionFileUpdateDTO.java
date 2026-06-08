package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubmissionFileUpdateDTO {
    @NotNull(message = "提交文件ID不能为空")
    private Long id;

    @NotNull(message = "阶段提交ID不能为空")
    private Long submissionId;

    @NotBlank(message = "存储文件名不能为空")
    @Size(max = 255, message = "存储文件名长度不能超过255")
    private String fileName;

    @NotBlank(message = "原始文件名不能为空")
    @Size(max = 255, message = "原始文件名长度不能超过255")
    private String originalName;

    @NotBlank(message = "文件类型不能为空")
    @Size(max = 50, message = "文件类型长度不能超过50")
    private String fileType;

    @NotNull(message = "文件大小不能为空")
    private Long fileSize;

    @NotBlank(message = "文件路径不能为空")
    @Size(max = 500, message = "文件路径长度不能超过500")
    private String filePath;

    @Size(max = 500, message = "文件访问地址长度不能超过500")
    private String fileUrl;

    @Size(max = 50, message = "业务类型长度不能超过50")
    private String bizType;
}