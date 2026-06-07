package org.example.backend.modules.edu.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DefenseScheduleUpdateDTO {
    @NotNull(message = "答辩安排ID不能为空")
    private Long id;

    @NotNull(message = "实训批次ID不能为空")
    private Long batchId;

    @NotNull(message = "项目组ID不能为空")
    private Long groupId;

    @NotNull(message = "答辩日期不能为空")
    private LocalDate defenseDate;

    @NotNull(message = "答辩时间不能为空")
    private LocalTime defenseTime;

    @Size(max = 100, message = "答辩地点长度不能超过100")
    private String location;

    private Integer orderNo;

    @NotNull(message = "状态不能为空")
    private Integer status;
}