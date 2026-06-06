package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.edu.dto.TopicApplicationCreateDTO;
import org.example.backend.modules.edu.dto.TopicApplicationPageQueryDTO;
import org.example.backend.modules.edu.dto.TopicApplicationReviewDTO;
import org.example.backend.modules.edu.entity.ProjectTopic;
import org.example.backend.modules.edu.entity.TopicApplication;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.service.ProjectTopicService;
import org.example.backend.modules.edu.service.TopicApplicationService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/topic-applications")
public class TopicApplicationController {
    private final TopicApplicationService topicApplicationService;
    private final TrainingBatchService trainingBatchService;
    private final ProjectTopicService projectTopicService;
    private final SysUserService sysUserService;

    public TopicApplicationController(TopicApplicationService topicApplicationService,
                                      TrainingBatchService trainingBatchService,
                                      ProjectTopicService projectTopicService,
                                      SysUserService sysUserService) {
        this.topicApplicationService = topicApplicationService;
        this.trainingBatchService = trainingBatchService;
        this.projectTopicService = projectTopicService;
        this.sysUserService = sysUserService;
    }


    @GetMapping
    public Result<List<TopicApplication>> findAll(){
        return Result.success(topicApplicationService.list());
    }

    @GetMapping("/page")
    public Result<Page<TopicApplication>> page(@Valid @ModelAttribute TopicApplicationPageQueryDTO query) {
        Page<TopicApplication> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<TopicApplication> wrapper = Wrappers.lambdaQuery();

        if (query.getBatchId() != null) {
            wrapper.eq(TopicApplication::getBatchId, query.getBatchId());
        }
        if (query.getTopicId() != null) {
            wrapper.eq(TopicApplication::getTopicId, query.getTopicId());
        }
        if (query.getStudentId() != null) {
            wrapper.eq(TopicApplication::getStudentId, query.getStudentId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(TopicApplication::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(TopicApplication::getCreateTime);
        return Result.success(topicApplicationService.page(page, wrapper));
    }

    @PostMapping
    public Result<TopicApplication> create(@Valid @RequestBody TopicApplicationCreateDTO dto) {
        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }

        ProjectTopic projectTopic = projectTopicService.getById(dto.getTopicId());
        if (projectTopic == null) {
            return Result.fail(404, "课题不存在");
        }
        if (!dto.getBatchId().equals(projectTopic.getBatchId())) {
            return Result.fail(400, "课题不属于当前实训批次");
        }
        if (sysUserService.getById(dto.getStudentId()) == null) {
            return Result.fail(404, "学生不存在");
        }

        TopicApplication topicApplication = new TopicApplication();
        topicApplication.setBatchId(dto.getBatchId());
        topicApplication.setTopicId(dto.getTopicId());
        topicApplication.setStudentId(dto.getStudentId());
        topicApplication.setApplyReason(dto.getApplyReason());
        topicApplication.setStatus("PENDING");
        topicApplicationService.save(topicApplication);
        return Result.success(topicApplication);
    }

    @PutMapping("/review")
    public Result<TopicApplication> review(@Valid @RequestBody TopicApplicationReviewDTO dto) {
        TopicApplication existing = topicApplicationService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "选题申请不存在");
        }
        if (!"PENDING".equals(existing.getStatus())) {
            return Result.fail(400, "当前申请状态不允许审核");
        }
        if (sysUserService.getById(dto.getReviewerId()) == null) {
            return Result.fail(404, "审核人不存在");
        }

        ProjectTopic projectTopic = projectTopicService.getById(existing.getTopicId());
        if (projectTopic == null) {
            return Result.fail(404, "关联课题不存在");
        }
        if (!dto.getReviewerId().equals(projectTopic.getTeacherId())) {
            return Result.fail(400, "只有课题发布教师可以审核该申请");
        }

        TopicApplication topicApplication = new TopicApplication();
        topicApplication.setId(dto.getId());
        topicApplication.setStatus(dto.getStatus());
        topicApplication.setReviewerId(dto.getReviewerId());
        topicApplication.setReviewComment(dto.getReviewComment());
        topicApplication.setReviewTime(LocalDateTime.now());
        topicApplicationService.updateById(topicApplication);
        return Result.success(topicApplicationService.getById(dto.getId()));
    }
}
