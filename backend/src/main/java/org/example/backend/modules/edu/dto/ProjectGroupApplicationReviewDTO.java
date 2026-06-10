package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectGroupApplicationReviewDTO {

    @NotNull(message = "申请ID不能为空")
    private Long id;

    @NotBlank(message = "审核结果不能为空")
    @Pattern(regexp = "APPROVED|REJECTED", message = "审核结果只能是 APPROVED 或 REJECTED")
    private String status;

    @Size(max = 500, message = "审核意见长度不能超过500")
    private String reviewComment;
}