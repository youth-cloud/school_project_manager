package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.StageTaskCreateDTO;
import org.example.backend.modules.edu.dto.StageTaskPageQueryDTO;
import org.example.backend.modules.edu.dto.StageTaskUpdateDTO;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.ProjectGroupMember;
import org.example.backend.modules.edu.entity.StageTask;
import org.example.backend.modules.edu.entity.TopicApplication;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.service.ProjectGroupMemberService;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.edu.service.StageTaskService;
import org.example.backend.modules.edu.service.TopicApplicationService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stage-tasks")
public class StageTaskController {
    private final StageTaskService stageTaskService;
    private final TrainingBatchService trainingBatchService;
    private final ProjectGroupService projectGroupService;
    private final ProjectGroupMemberService projectGroupMemberService;
    private final TopicApplicationService topicApplicationService;
    private final SysUserService sysUserService;

    public StageTaskController(StageTaskService stageTaskService,
                               TrainingBatchService trainingBatchService,
                               ProjectGroupService projectGroupService,
                               ProjectGroupMemberService projectGroupMemberService,
                               TopicApplicationService topicApplicationService,
                               SysUserService sysUserService) {
        this.stageTaskService = stageTaskService;
        this.trainingBatchService = trainingBatchService;
        this.projectGroupService = projectGroupService;
        this.projectGroupMemberService = projectGroupMemberService;
        this.topicApplicationService = topicApplicationService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<StageTask>> findAll() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        LambdaQueryWrapper<StageTask> wrapper = Wrappers.lambdaQuery();
        applyStageTaskViewScope(wrapper, currentUser);
        wrapper.orderByDesc(StageTask::getCreateTime);
        return Result.success(stageTaskService.list(wrapper));
    }

    @GetMapping("/page")
    public Result<Page<StageTask>> page(@Valid @ModelAttribute StageTaskPageQueryDTO query) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
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

        applyStageTaskViewScope(wrapper, currentUser);
        wrapper.orderByDesc(StageTask::getCreateTime);
        return Result.success(stageTaskService.page(page, wrapper));
    }

    @PostMapping
    public Result<StageTask> create(@Valid @RequestBody StageTaskCreateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return Result.fail(403, "当前用户不是教师角色，不能发布阶段任务");
        }

        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }
        if (!currentUser.getUserId().equals(trainingBatch.getTeacherId())) {
            return Result.fail(403, "当前教师不是该实训批次绑定教师，不能发布阶段任务");
        }

        LambdaQueryWrapper<StageTask> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StageTask::getBatchId, dto.getBatchId())
                .eq(StageTask::getStageNo, dto.getStageNo());
        if (stageTaskService.count(wrapper) > 0) {
            return Result.fail(400, "该实训批次下阶段序号已存在");
        }

        StageTask stageTask = new StageTask();
        stageTask.setBatchId(dto.getBatchId());
        stageTask.setTeacherId(currentUser.getUserId());
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
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        StageTask stageTask = stageTaskService.getById(id);
        if (stageTask == null) {
            return Result.fail(404, "阶段任务不存在");
        }
        if (!canViewStageTask(currentUser, stageTask)) {
            return Result.fail(403, "当前用户无权查看该阶段任务");
        }
        return Result.success(stageTask);
    }

    @PutMapping
    public Result<StageTask> update(@Valid @RequestBody StageTaskUpdateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return Result.fail(403, "当前用户不是教师角色，不能修改阶段任务");
        }

        StageTask existing = stageTaskService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "阶段任务不存在");
        }

        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }
        if (!currentUser.getUserId().equals(trainingBatch.getTeacherId())) {
            return Result.fail(403, "当前教师不是该实训批次绑定教师，不能修改阶段任务");
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
        stageTask.setTeacherId(existing.getTeacherId());
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
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        StageTask existing = stageTaskService.getById(id);
        if (existing == null) {
            return Result.fail(404, "阶段任务不存在");
        }
        if (hasRole(currentUser, "ADMIN")) {
            return Result.success(stageTaskService.removeById(id));
        }
        if (!hasRole(currentUser, "TEACHER")) {
            return Result.fail(403, "当前用户没有删除阶段任务的权限");
        }
        TrainingBatch trainingBatch = trainingBatchService.getById(existing.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "关联实训批次不存在");
        }
        if (!currentUser.getUserId().equals(trainingBatch.getTeacherId())) {
            return Result.fail(403, "当前教师不是该实训批次绑定教师，不能删除阶段任务");
        }
        return Result.success(stageTaskService.removeById(id));
    }

    private void applyStageTaskViewScope(LambdaQueryWrapper<StageTask> wrapper, LoginUserPrincipal currentUser) {
        if (hasRole(currentUser, "ADMIN")) {
            return;
        }
        if (hasRole(currentUser, "TEACHER")) {
            wrapper.eq(StageTask::getTeacherId, currentUser.getUserId());
            return;
        }
        Set<Long> batchIds = getStudentVisibleBatchIds(currentUser.getUserId());
        if (batchIds.isEmpty()) {
            wrapper.eq(StageTask::getId, -1L);
            return;
        }
        wrapper.in(StageTask::getBatchId, batchIds);
    }

    private boolean canViewStageTask(LoginUserPrincipal currentUser, StageTask stageTask) {
        if (currentUser == null || currentUser.getUserId() == null || stageTask == null) {
            return false;
        }
        if (hasRole(currentUser, "ADMIN")) {
            return true;
        }
        if (hasRole(currentUser, "TEACHER")) {
            return currentUser.getUserId().equals(stageTask.getTeacherId());
        }
        return getStudentVisibleBatchIds(currentUser.getUserId()).contains(stageTask.getBatchId());
    }

    private Set<Long> getStudentVisibleBatchIds(Long userId) {
        Set<Long> batchIds = new LinkedHashSet<>();

        List<Long> groupIds = projectGroupMemberService.list(Wrappers.<ProjectGroupMember>lambdaQuery()
                        .eq(ProjectGroupMember::getUserId, userId))
                .stream()
                .map(ProjectGroupMember::getGroupId)
                .collect(Collectors.toList());
        if (!groupIds.isEmpty()) {
            batchIds.addAll(projectGroupService.list(Wrappers.<ProjectGroup>lambdaQuery()
                            .in(ProjectGroup::getId, groupIds))
                    .stream()
                    .map(ProjectGroup::getBatchId)
                    .filter(batchId -> batchId != null)
                    .collect(Collectors.toSet()));
        }

        batchIds.addAll(topicApplicationService.list(Wrappers.<TopicApplication>lambdaQuery()
                        .eq(TopicApplication::getStudentId, userId)
                        .eq(TopicApplication::getStatus, "APPROVED"))
                .stream()
                .map(TopicApplication::getBatchId)
                .filter(batchId -> batchId != null)
                .collect(Collectors.toSet()));

        return batchIds;
    }

    private boolean hasRole(LoginUserPrincipal currentUser, String roleCode) {
        if (currentUser == null || currentUser.getUserId() == null) {
            return false;
        }
        return sysUserService.hasRole(currentUser.getUserId(), roleCode);
    }
}
