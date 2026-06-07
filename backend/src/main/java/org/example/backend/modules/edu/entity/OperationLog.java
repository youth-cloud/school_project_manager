package org.example.backend.modules.edu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String moduleName;

    private String operationType;

    private Long operatorId;

    private String requestMethod;

    private String requestUri;

    private String ip;

    private String operationDesc;

    private String result;

    private LocalDateTime createTime;
}