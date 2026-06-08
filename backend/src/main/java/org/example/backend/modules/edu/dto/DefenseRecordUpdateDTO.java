package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DefenseRecordUpdateDTO {
    @NotNull(message = "答辩记录ID不能为空")
    private Long id;

    @NotNull(message = "答辩安排ID不能为空")
    private Long scheduleId;

    @DecimalMin(value = "0.0", message = "展示分不能小于0")
    private BigDecimal presentationScore;

    @DecimalMin(value = "0.0", message = "问答分不能小于0")
    private BigDecimal answerScore;

    @DecimalMin(value = "0.0", message = "完成度分不能小于0")
    private BigDecimal completionScore;

    @DecimalMin(value = "0.0", message = "总分不能小于0")
    private BigDecimal totalScore;

    @Size(max = 1000, message = "评语长度不能超过1000")
    private String comment;
}