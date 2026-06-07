package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreRecordUpdateDTO {
    @NotNull(message = "成绩记录ID不能为空")
    private Long id;

    @NotNull(message = "实训批次ID不能为空")
    private Long batchId;

    private Long groupId;

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @DecimalMin(value = "0.0", message = "过程分不能小于0")
    private BigDecimal processScore;

    @DecimalMin(value = "0.0", message = "报告分不能小于0")
    private BigDecimal reportScore;

    @DecimalMin(value = "0.0", message = "材料分不能小于0")
    private BigDecimal submissionScore;

    @DecimalMin(value = "0.0", message = "答辩分不能小于0")
    private BigDecimal defenseScore;

    @DecimalMin(value = "0.0", message = "最终总分不能小于0")
    private BigDecimal finalScore;

    @Size(max = 20, message = "等级长度不能超过20")
    private String gradeLevel;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}