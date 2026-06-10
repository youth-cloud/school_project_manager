package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReviewRecordUpdateDTO {
    @NotNull(message = "审核记录ID不能为空")
    private Long id;

    @NotNull(message = "阶段提交ID不能为空")
    private Long submissionId;

    @NotBlank(message = "审核结果不能为空")
    @Pattern(regexp = "APPROVED|REJECTED|NEED_FIX", message = "审核结果只能是 APPROVED、REJECTED 或 NEED_FIX")
    @Size(max = 30, message = "审核结果长度不能超过30")
    private String reviewResult;

    @DecimalMin(value = "0.0", message = "分数不能小于0")
    private BigDecimal score;

    @Size(max = 1000, message = "审核评语长度不能超过1000")
    private String comment;
}