package org.example.backend.modules.edu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("edu_course")
public class EduCourse {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;


    private String courseName;

    private String courseCode;

    private BigDecimal credit;

    private String remark;

    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
