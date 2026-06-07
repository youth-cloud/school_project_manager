package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OperationLogPageQueryDTO {
    @Min(value = 1, message = "当前页不能小于1")
    private long current = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private long size = 10;

    @Size(max = 100, message = "模块名称长度不能超过100")
    private String moduleName;

    @Size(max = 50, message = "操作类型长度不能超过50")
    private String operationType;

    private Long operatorId;

    @Size(max = 50, message = "操作结果长度不能超过50")
    private String result;
}