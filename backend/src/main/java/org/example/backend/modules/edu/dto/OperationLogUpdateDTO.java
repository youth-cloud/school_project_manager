package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OperationLogUpdateDTO {
    @NotNull(message = "操作日志ID不能为空")
    private Long id;

    @NotBlank(message = "模块名称不能为空")
    @Size(max = 100, message = "模块名称长度不能超过100")
    private String moduleName;

    @NotBlank(message = "操作类型不能为空")
    @Size(max = 50, message = "操作类型长度不能超过50")
    private String operationType;

    private Long operatorId;

    @Size(max = 20, message = "请求方式长度不能超过20")
    private String requestMethod;

    @Size(max = 255, message = "请求地址长度不能超过255")
    private String requestUri;

    @Size(max = 50, message = "IP地址长度不能超过50")
    private String ip;

    @Size(max = 1000, message = "操作描述长度不能超过1000")
    private String operationDesc;

    @Size(max = 50, message = "操作结果长度不能超过50")
    private String result;
}