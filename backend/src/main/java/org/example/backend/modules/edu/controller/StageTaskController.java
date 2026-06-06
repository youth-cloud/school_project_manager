package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.edu.dto.StageTaskCreateDTO;
import org.example.backend.modules.edu.dto.StageTaskPageQueryDTO;
import org.example.backend.modules.edu.dto.StageTaskUpdateDTO;
import org.example.backend.modules.edu.entity.StageTask;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.service.StageTaskService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stage-tasks")
public class StageTaskController {
    private final StageTaskService stageTaskService;
    private final TrainingBatchService trainingBatchService;
    private final SysUserService sysUserService;

    public StageTaskController(StageTaskService stageTaskService,
                               TrainingBatchService trainingBatchService,
                               SysUserService sysUserService) {
        this.stageTaskService = stageTaskService;
        this.trainingBatchService = trainingBatchService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<StageTask>> findAll() {
        return Result.success(stageTaskService.list());
    }

    @GetMapping("/page")
    public Result<Page<StageTask>> page(@Valid @ModelAttribute StageTaskPageQueryDTO query) {
        Page<StageTask> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<StageTask> wrapper = Wrappers.lambdaQuery();

        if (query.getBatchId() != null) {
            wrapper.eq(StageTask::getBatchId, query.getBatchId());
        }
        if (query.getTeacherId() != null) {
            wrapper.eq(StageTask::getTeacherId, query.getTeacherId());
        }
        if (StringUtils.hasText(query.getTaskTitle())) {
            wrapper.like(StageTask::getTaskTitle, query.getTaskTitle());
        }
        if (query.getStageNo() != null) {
            wrapper.eq(StageTask::getStageNo, query.getStageNo());
        }
        if (query.getStatus() != null) {
            wrapper.eq(StageTask::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(StageTask::getCreateTime);
        return Result.success(stageTaskService.page(page, wrapper));
    }

    @PostMapping
    public Result<StageTask> create(@Valid @RequestBody StageTaskCreateDTO dto) {
        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }
        if (sysUserService.getById(dto.getTeacherId()) == null) {
            return Result.fail(404, "教师不存在");
        }
        if (!dto.getTeacherId().equals(trainingBatch.getTeacherId())) {
            return Result.fail(400, "教师与实训批次绑定教师不一致");
        }

        LambdaQueryWrapper<StageTask> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StageTask::getBatchId, dto.getBatchId())
                .eq(StageTask::getStageNo, dto.getStageNo());
        if (stageTaskService.count(wrapper) > 0) {
            return Result.fail(400, "该实训批次下阶段序号已存在");
        }

        StageTask stageTask = new StageTask();
        stageTask.setBatchId(dto.getBatchId());
        stageTask.setTeacherId(dto.getTeacherId());
        stageTask.setTaskTitle(dto.getTaskTitle());
        stageTask.setTaskDescription(dto.getTaskDescription());
        stageTask.setStageNo(dto.getStageNo());
        stageTask.setDeadline(dto.getDeadline());
        stageTask.setNeedReport(dto.getNeedReport());
        stageTask.setNeedSourceCode(dto.getNeedSourceCode());
        stageTask.setNeedPdf(dto.getNeedPdf());
        stageTask.setNeedScreenshot(dto.getNeedScreenshot());
        stageTask.setNeedDemoUrl(dto.getNeedDemoUrl());
        stageTask.setStatus(dto.getStatus());
        stageTaskService.save(stageTask);
        return Result.success(stageTask);
    }

    @GetMapping("/{id}")
    public Result<StageTask> findById(@PathVariable Long id) {
        StageTask stageTask = stageTaskService.getById(id);
        if (stageTask == null) {
            return Result.fail(404, "阶段任务不存在");
        }
        return Result.success(stageTask);
    }

    @PutMapping
    public Result<StageTask> update(@Valid @RequestBody StageTaskUpdateDTO dto) {
        StageTask existing = stageTaskService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "阶段任务不存在");
        }

        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }
        if (sysUserService.getById(dto.getTeacherId()) == null) {
            return Result.fail(404, "教师不存在");
        }
        if (!dto.getTeacherId().equals(trainingBatch.getTeacherId())) {
            return Result.fail(400, "教师与实训批次绑定教师不一致");
        }

        LambdaQueryWrapper<StageTask> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StageTask::getBatchId, dto.getBatchId())
                .eq(StageTask::getStageNo, dto.getStageNo())
                .ne(StageTask::getId, dto.getId());
        if (stageTaskService.count(wrapper) > 0) {
            return Result.fail(400, "该实训批次下阶段序号已存在");
        }

        StageTask stageTask = new StageTask();
        stageTask.setId(dto.getId());
        stageTask.setBatchId(dto.getBatchId());
        stageTask.setTeacherId(dto.getTeacherId());
        stageTask.setTaskTitle(dto.getTaskTitle());
        stageTask.setTaskDescription(dto.getTaskDescription());
        stageTask.setStageNo(dto.getStageNo());
        stageTask.setDeadline(dto.getDeadline());
        stageTask.setNeedReport(dto.getNeedReport());
        stageTask.setNeedSourceCode(dto.getNeedSourceCode());
        stageTask.setNeedPdf(dto.getNeedPdf());
        stageTask.setNeedScreenshot(dto.getNeedScreenshot());
        stageTask.setNeedDemoUrl(dto.getNeedDemoUrl());
        stageTask.setStatus(dto.getStatus());
        stageTaskService.updateById(stageTask);
        return Result.success(stageTaskService.getById(dto.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        StageTask existing = stageTaskService.getById(id);
        if (existing == null) {
            return Result.fail(404, "阶段任务不存在");
        }
        return Result.success(stageTaskService.removeById(id));
    }
}
