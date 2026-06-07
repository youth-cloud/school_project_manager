package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.edu.dto.StageSubmissionCreateDTO;
import org.example.backend.modules.edu.dto.StageSubmissionPageQueryDTO;
import org.example.backend.modules.edu.dto.StageSubmissionUpdateDTO;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.StageSubmission;
import org.example.backend.modules.edu.entity.StageTask;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.edu.service.StageSubmissionService;
import org.example.backend.modules.edu.service.StageTaskService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stage-submissions")
public class StageSubmissionController {
    private final StageSubmissionService stageSubmissionService;
    private final StageTaskService stageTaskService;
    private final TrainingBatchService trainingBatchService;
    private final ProjectGroupService projectGroupService;
    private final SysUserService sysUserService;

    public StageSubmissionController(StageSubmissionService stageSubmissionService,
                                     StageTaskService stageTaskService,
                                     TrainingBatchService trainingBatchService,
                                     ProjectGroupService projectGroupService,
                                     SysUserService sysUserService) {
        this.stageSubmissionService = stageSubmissionService;
        this.stageTaskService = stageTaskService;
        this.trainingBatchService = trainingBatchService;
        this.projectGroupService = projectGroupService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<StageSubmission>> findAll() {
        return Result.success(stageSubmissionService.list());
    }

    @GetMapping("/page")
    public Result<Page<StageSubmission>> page(@Valid @ModelAttribute StageSubmissionPageQueryDTO query) {
        Page<StageSubmission> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<StageSubmission> wrapper = Wrappers.lambdaQuery();

        if (query.getTaskId() != null) {
            wrapper.eq(StageSubmission::getTaskId, query.getTaskId());
        }
        if (query.getBatchId() != null) {
            wrapper.eq(StageSubmission::getBatchId, query.getBatchId());
        }
        if (query.getGroupId() != null) {
            wrapper.eq(StageSubmission::getGroupId, query.getGroupId());
        }
        if (query.getSubmitterId() != null) {
            wrapper.eq(StageSubmission::getSubmitterId, query.getSubmitterId());
        }
        if (query.getVersionNo() != null) {
            wrapper.eq(StageSubmission::getVersionNo, query.getVersionNo());
        }
        if (StringUtils.hasText(query.getSummary())) {
            wrapper.like(StageSubmission::getSummary, query.getSummary());
        }
        if (query.getStatus() != null) {
            wrapper.eq(StageSubmission::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(StageSubmission::getSubmitTime);
        return Result.success(stageSubmissionService.page(page, wrapper));
    }

    @PostMapping
    public Result<StageSubmission> create(@Valid @RequestBody StageSubmissionCreateDTO dto) {
        StageTask stageTask = stageTaskService.getById(dto.getTaskId());
        if (stageTask == null) {
            return Result.fail(404, "阶段任务不存在");
        }

        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }

        ProjectGroup projectGroup = projectGroupService.getById(dto.getGroupId());
        if (projectGroup == null) {
            return Result.fail(404, "项目组不存在");
        }

        if (sysUserService.getById(dto.getSubmitterId()) == null) {
            return Result.fail(404, "提交人不存在");
        }
        if (!sysUserService.hasRole(dto.getSubmitterId(), "STUDENT")) {
            return Result.fail(400, "当前用户不是学生角色，不能提交阶段成果");
        }

        if (!dto.getBatchId().equals(stageTask.getBatchId())) {
            return Result.fail(400, "阶段任务不属于当前实训批次");
        }

        if (!dto.getBatchId().equals(projectGroup.getBatchId())) {
            return Result.fail(400, "项目组不属于当前实训批次");
        }

        StageSubmission stageSubmission = new StageSubmission();
        stageSubmission.setTaskId(dto.getTaskId());
        stageSubmission.setBatchId(dto.getBatchId());
        stageSubmission.setGroupId(dto.getGroupId());
        stageSubmission.setSubmitterId(dto.getSubmitterId());
        stageSubmission.setVersionNo(dto.getVersionNo());
        stageSubmission.setSummary(dto.getSummary());
        stageSubmission.setReportText(dto.getReportText());
        stageSubmission.setRepoUrl(dto.getRepoUrl());
        stageSubmission.setDeployUrl(dto.getDeployUrl());
        stageSubmission.setStatus(dto.getStatus());
        stageSubmission.setSubmitTime(LocalDateTime.now());
        stageSubmissionService.save(stageSubmission);
        return Result.success(stageSubmission);
    }

    @GetMapping("/{id}")
    public Result<StageSubmission> findById(@PathVariable Long id) {
        StageSubmission stageSubmission = stageSubmissionService.getById(id);
        if (stageSubmission == null) {
            return Result.fail(404, "阶段提交不存在");
        }
        return Result.success(stageSubmission);
    }

    @PutMapping
    public Result<StageSubmission> update(@Valid @RequestBody StageSubmissionUpdateDTO dto) {
        StageSubmission existing = stageSubmissionService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "阶段提交不存在");
        }

        StageTask stageTask = stageTaskService.getById(dto.getTaskId());
        if (stageTask == null) {
            return Result.fail(404, "阶段任务不存在");
        }

        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }

        ProjectGroup projectGroup = projectGroupService.getById(dto.getGroupId());
        if (projectGroup == null) {
            return Result.fail(404, "项目组不存在");
        }

        if (sysUserService.getById(dto.getSubmitterId()) == null) {
            return Result.fail(404, "提交人不存在");
        }
        if (!sysUserService.hasRole(dto.getSubmitterId(), "STUDENT")) {
            return Result.fail(400, "当前用户不是学生角色，不能修改阶段提交");
        }

        if (!dto.getBatchId().equals(stageTask.getBatchId())) {
            return Result.fail(400, "阶段任务不属于当前实训批次");
        }

        if (!dto.getBatchId().equals(projectGroup.getBatchId())) {
            return Result.fail(400, "项目组不属于当前实训批次");
        }

        StageSubmission stageSubmission = new StageSubmission();
        stageSubmission.setId(dto.getId());
        stageSubmission.setTaskId(dto.getTaskId());
        stageSubmission.setBatchId(dto.getBatchId());
        stageSubmission.setGroupId(dto.getGroupId());
        stageSubmission.setSubmitterId(dto.getSubmitterId());
        stageSubmission.setVersionNo(dto.getVersionNo());
        stageSubmission.setSummary(dto.getSummary());
        stageSubmission.setReportText(dto.getReportText());
        stageSubmission.setRepoUrl(dto.getRepoUrl());
        stageSubmission.setDeployUrl(dto.getDeployUrl());
        stageSubmission.setStatus(dto.getStatus());
        stageSubmissionService.updateById(stageSubmission);
        return Result.success(stageSubmissionService.getById(dto.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        StageSubmission existing = stageSubmissionService.getById(id);
        if (existing == null) {
            return Result.fail(404, "阶段提交不存在");
        }
        return Result.success(stageSubmissionService.removeById(id));
    }
}
