package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NoticePageQueryDTO {
    @Min(value = 1, message = "当前页不能小于1")
    private long current = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private long size = 10;

    @Size(max = 200, message = "公告标题长度不能超过200")
    private String title;

    private Long publisherId;

    @Size(max = 50, message = "目标角色长度不能超过50")
    private String targetRole;

    private Integer status;
}