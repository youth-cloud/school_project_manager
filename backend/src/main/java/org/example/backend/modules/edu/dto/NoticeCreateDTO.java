package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeCreateDTO {
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 200, message = "公告标题长度不能超过200")
    private String title;

    private String content;

    @Size(max = 50, message = "目标角色长度不能超过50")
    private String targetRole;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private LocalDateTime publishTime;
}