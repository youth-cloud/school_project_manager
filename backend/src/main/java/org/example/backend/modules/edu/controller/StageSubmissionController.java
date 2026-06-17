package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.StageSubmissionCreateDTO;
import org.example.backend.modules.edu.dto.StageSubmissionPageQueryDTO;
import org.example.backend.modules.edu.dto.StageSubmissionUpdateDTO;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.ProjectGroupMember;
import org.example.backend.modules.edu.entity.ReviewRecord;
import org.example.backend.modules.edu.entity.StageSubmission;
import org.example.backend.modules.edu.entity.StageTask;
import org.example.backend.modules.edu.entity.SubmissionFile;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.service.ProjectGroupMemberService;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.edu.service.ReviewRecordService;
import org.example.backend.modules.edu.service.StageSubmissionService;
import org.example.backend.modules.edu.service.StageTaskService;
import org.example.backend.modules.edu.service.SubmissionFileService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stage-submissions")
public class StageSubmissionController {
    private final StageSubmissionService stageSubmissionService;
    private final StageTaskService stageTaskService;
    private final TrainingBatchService trainingBatchService;
    private final ProjectGroupService projectGroupService;
    private final ProjectGroupMemberService projectGroupMemberService;
    private final SubmissionFileService submissionFileService;
    private final ReviewRecordService reviewRecordService;
    private final SysUserService sysUserService;

    public StageSubmissionController(StageSubmissionService stageSubmissionService,
                                     StageTaskService stageTaskService,
                                     TrainingBatchService trainingBatchService,
                                     ProjectGroupService projectGroupService,
                                     ProjectGroupMemberService projectGroupMemberService,
                                     SubmissionFileService submissionFileService,
                                     ReviewRecordService reviewRecordService,
                                     SysUserService sysUserService) {
        this.stageSubmissionService = stageSubmissionService;
        this.stageTaskService = stageTaskService;
        this.trainingBatchService = trainingBatchService;
        this.projectGroupService = projectGroupService;
        this.projectGroupMemberService = projectGroupMemberService;
        this.submissionFileService = submissionFileService;
        this.reviewRecordService = reviewRecordService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<StageSubmission>> findAll() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        LambdaQueryWrapper<StageSubmission> wrapper = Wrappers.lambdaQuery();
        applyStageSubmissionViewScope(wrapper, currentUser);
        wrapper.orderByDesc(StageSubmission::getSubmitTime);
        return Result.success(stageSubmissionService.list(wrapper));
    }

    @GetMapping("/page")
    public Result<Page<StageSubmission>> page(@Valid @ModelAttribute StageSubmissionPageQueryDTO query) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

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

        applyStageSubmissionViewScope(wrapper, currentUser);
        wrapper.orderByDesc(StageSubmission::getSubmitTime);
        return Result.success(stageSubmissionService.page(page, wrapper));
    }

    @PostMapping
    public Result<StageSubmission> create(@Valid @RequestBody StageSubmissionCreateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "STUDENT")) {
            return Result.fail(403, "当前用户不是学生角色，不能提交阶段成果");
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

        if (!dto.getBatchId().equals(stageTask.getBatchId())) {
            return Result.fail(400, "阶段任务不属于当前实训批次");
        }

        if (!dto.getBatchId().equals(projectGroup.getBatchId())) {
            return Result.fail(400, "项目组不属于当前实训批次");
        }
        if (!isGroupMember(dto.getGroupId(), currentUser.getUserId())) {
            return Result.fail(403, "当前学生不属于该项目组，不能提交阶段成果");
        }

        StageSubmission stageSubmission = new StageSubmission();
        stageSubmission.setTaskId(dto.getTaskId());
        stageSubmission.setBatchId(dto.getBatchId());
        stageSubmission.setGroupId(dto.getGroupId());
        stageSubmission.setSubmitterId(currentUser.getUserId());
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
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        StageSubmission stageSubmission = stageSubmissionService.getById(id);
        if (stageSubmission == null) {
            return Result.fail(404, "阶段提交不存在");
        }
        if (!canViewStageSubmission(currentUser, stageSubmission)) {
            return Result.fail(403, "当前用户无权查看该阶段提交");
        }
        return Result.success(stageSubmission);
    }

    @PutMapping
    public Result<StageSubmission> update(@Valid @RequestBody StageSubmissionUpdateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasAnyRole(currentUser.getUserId(), "STUDENT", "ADMIN")) {
            return Result.fail(403, "当前用户无权修改阶段提交");
        }

        StageSubmission existing = stageSubmissionService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "阶段提交不存在");
        }
        if (!canManageStageSubmission(currentUser, existing)) {
            return Result.fail(403, "当前用户无权修改该阶段提交");
        }
        if (hasSubmissionDependencies(existing.getId())
                && (!equalsValue(existing.getTaskId(), dto.getTaskId())
                || !equalsValue(existing.getBatchId(), dto.getBatchId())
                || !equalsValue(existing.getGroupId(), dto.getGroupId()))) {
            return Result.fail(400, "该阶段提交已有关联附件或审核记录，不能再修改所属任务、批次或项目组");
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

        if (!dto.getBatchId().equals(stageTask.getBatchId())) {
            return Result.fail(400, "阶段任务不属于当前实训批次");
        }

        if (!dto.getBatchId().equals(projectGroup.getBatchId())) {
            return Result.fail(400, "项目组不属于当前实训批次");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "ADMIN") && !isGroupMember(dto.getGroupId(), currentUser.getUserId())) {
            return Result.fail(403, "当前用户不属于目标项目组，不能修改该阶段提交");
        }

        StageSubmission stageSubmission = new StageSubmission();
        stageSubmission.setId(dto.getId());
        stageSubmission.setTaskId(dto.getTaskId());
        stageSubmission.setBatchId(dto.getBatchId());
        stageSubmission.setGroupId(dto.getGroupId());
        stageSubmission.setSubmitterId(existing.getSubmitterId());
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
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        StageSubmission existing = stageSubmissionService.getById(id);
        if (existing == null) {
            return Result.fail(404, "阶段提交不存在");
        }
        if (!canManageStageSubmission(currentUser, existing)) {
            return Result.fail(403, "当前用户无权删除该阶段提交");
        }
        if (submissionFileService.count(
                Wrappers.<SubmissionFile>lambdaQuery()
                        .eq(SubmissionFile::getSubmissionId, id)
        ) > 0) {
            return Result.fail(400, "该阶段提交已有关联提交附件，不能删除");
        }
        if (reviewRecordService.count(
                Wrappers.<ReviewRecord>lambdaQuery()
                        .eq(ReviewRecord::getSubmissionId, id)
        ) > 0) {
            return Result.fail(400, "该阶段提交已有关联审核记录，不能删除");
        }
        return Result.success(stageSubmissionService.removeById(id));
    }

    private void applyStageSubmissionViewScope(LambdaQueryWrapper<StageSubmission> wrapper,
                                               LoginUserPrincipal currentUser) {
        if (hasRole(currentUser, "ADMIN")) {
            return;
        }
        if (hasRole(currentUser, "STUDENT")) {
            List<Long> groupIds = projectGroupMemberService.list(
                    Wrappers.<ProjectGroupMember>lambdaQuery()
                            .eq(ProjectGroupMember::getUserId, currentUser.getUserId())
            ).stream().map(ProjectGroupMember::getGroupId).collect(Collectors.toList());
            if (groupIds.isEmpty()) {
                wrapper.eq(StageSubmission::getId, -1L);
                return;
            }
            wrapper.in(StageSubmission::getGroupId, groupIds);
            return;
        }
        if (hasRole(currentUser, "TEACHER")) {
            List<Long> groupIds = projectGroupService.list(
                    Wrappers.<ProjectGroup>lambdaQuery()
                            .eq(ProjectGroup::getTeacherId, currentUser.getUserId())
            ).stream().map(ProjectGroup::getId).collect(Collectors.toList());
            if (groupIds.isEmpty()) {
                wrapper.eq(StageSubmission::getId, -1L);
                return;
            }
            wrapper.in(StageSubmission::getGroupId, groupIds);
            return;
        }
        wrapper.eq(StageSubmission::getId, -1L);
    }

    private boolean canViewStageSubmission(LoginUserPrincipal currentUser, StageSubmission submission) {
        if (currentUser == null || currentUser.getUserId() == null || submission == null) {
            return false;
        }
        if (hasRole(currentUser, "ADMIN")) {
            return true;
        }
        if (currentUser.getUserId().equals(submission.getSubmitterId())) {
            return true;
        }
        if (hasRole(currentUser, "TEACHER")) {
            ProjectGroup projectGroup = projectGroupService.getById(submission.getGroupId());
            return projectGroup != null && currentUser.getUserId().equals(projectGroup.getTeacherId());
        }
        return isGroupMember(submission.getGroupId(), currentUser.getUserId());
    }

    private boolean isGroupMember(Long groupId, Long userId) {
        return projectGroupMemberService.count(
                Wrappers.<ProjectGroupMember>lambdaQuery()
                        .eq(ProjectGroupMember::getGroupId, groupId)
                        .eq(ProjectGroupMember::getUserId, userId)
        ) > 0;
    }

    private boolean canManageStageSubmission(LoginUserPrincipal currentUser, StageSubmission submission) {
        if (currentUser == null || currentUser.getUserId() == null || submission == null) {
            return false;
        }
        return hasRole(currentUser, "ADMIN")
                || currentUser.getUserId().equals(submission.getSubmitterId());
    }

    private boolean hasSubmissionDependencies(Long submissionId) {
        if (submissionId == null) {
            return false;
        }
        return submissionFileService.count(
                Wrappers.<SubmissionFile>lambdaQuery()
                        .eq(SubmissionFile::getSubmissionId, submissionId)
        ) > 0 || reviewRecordService.count(
                Wrappers.<ReviewRecord>lambdaQuery()
                        .eq(ReviewRecord::getSubmissionId, submissionId)
        ) > 0;
    }

    private boolean equalsValue(Long left, Long right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }

    private boolean hasRole(LoginUserPrincipal currentUser, String roleCode) {
        if (currentUser == null || currentUser.getUserId() == null) {
            return false;
        }
        return sysUserService.hasRole(currentUser.getUserId(), roleCode);
    }
}
