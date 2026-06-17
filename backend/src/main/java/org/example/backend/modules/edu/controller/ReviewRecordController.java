package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.ReviewRecordCreateDTO;
import org.example.backend.modules.edu.dto.ReviewRecordPageQueryDTO;
import org.example.backend.modules.edu.dto.ReviewRecordUpdateDTO;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.ProjectGroupMember;
import org.example.backend.modules.edu.entity.ReviewRecord;
import org.example.backend.modules.edu.entity.StageSubmission;
import org.example.backend.modules.edu.entity.StageTask;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.service.ProjectGroupMemberService;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.edu.service.ReviewRecordService;
import org.example.backend.modules.edu.service.StageSubmissionService;
import org.example.backend.modules.edu.service.StageTaskService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/review-records")
public class ReviewRecordController {

    private final ReviewRecordService reviewRecordService;
    private final StageSubmissionService stageSubmissionService;
    private final StageTaskService stageTaskService;
    private final TrainingBatchService trainingBatchService;
    private final ProjectGroupService projectGroupService;
    private final ProjectGroupMemberService projectGroupMemberService;
    private final SysUserService sysUserService;

    public ReviewRecordController(ReviewRecordService reviewRecordService,
                                  StageSubmissionService stageSubmissionService,
                                  StageTaskService stageTaskService,
                                  TrainingBatchService trainingBatchService,
                                  ProjectGroupService projectGroupService,
                                  ProjectGroupMemberService projectGroupMemberService,
                                  SysUserService sysUserService) {
        this.reviewRecordService = reviewRecordService;
        this.stageSubmissionService = stageSubmissionService;
        this.stageTaskService = stageTaskService;
        this.trainingBatchService = trainingBatchService;
        this.projectGroupService = projectGroupService;
        this.projectGroupMemberService = projectGroupMemberService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<ReviewRecord>> findAll() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        LambdaQueryWrapper<ReviewRecord> wrapper = Wrappers.lambdaQuery();
        applyReviewRecordViewScope(wrapper, currentUser);
        wrapper.orderByDesc(ReviewRecord::getReviewTime);
        return Result.success(reviewRecordService.list(wrapper));
    }

    @GetMapping("/page")
    public Result<Page<ReviewRecord>> page(@Valid @ModelAttribute ReviewRecordPageQueryDTO query) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        Page<ReviewRecord> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<ReviewRecord> wrapper = Wrappers.lambdaQuery();

        if (query.getSubmissionId() != null) {
            wrapper.eq(ReviewRecord::getSubmissionId, query.getSubmissionId());
        }
        if (query.getReviewerId() != null) {
            wrapper.eq(ReviewRecord::getReviewerId, query.getReviewerId());
        }
        if (StringUtils.hasText(query.getReviewResult())) {
            wrapper.eq(ReviewRecord::getReviewResult, query.getReviewResult());
        }

        applyReviewRecordViewScope(wrapper, currentUser);
        wrapper.orderByDesc(ReviewRecord::getReviewTime);
        return Result.success(reviewRecordService.page(page, wrapper));
    }

    @PostMapping
    public Result<ReviewRecord> create(@Valid @RequestBody ReviewRecordCreateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return Result.fail(403, "当前用户不是教师角色，不能新增审核记录");
        }

        StageSubmission stageSubmission = stageSubmissionService.getById(dto.getSubmissionId());
        if (stageSubmission == null) {
            return Result.fail(404, "阶段提交不存在");
        }
        Result<Void> reviewPermissionResult = validateReviewPermission(stageSubmission, currentUser.getUserId());
        if (reviewPermissionResult != null) {
            return Result.fail(reviewPermissionResult.getCode(), reviewPermissionResult.getMessage());
        }

        ReviewRecord reviewRecord = new ReviewRecord();
        reviewRecord.setSubmissionId(dto.getSubmissionId());
        reviewRecord.setReviewerId(currentUser.getUserId());
        reviewRecord.setReviewResult(dto.getReviewResult());
        reviewRecord.setScore(dto.getScore());
        reviewRecord.setComment(dto.getComment());
        reviewRecord.setReviewTime(LocalDateTime.now());
        reviewRecordService.save(reviewRecord);
        return Result.success(reviewRecord);
    }

    @GetMapping("/{id}")
    public Result<ReviewRecord> findById(@PathVariable Long id) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        ReviewRecord reviewRecord = reviewRecordService.getById(id);
        if (reviewRecord == null) {
            return Result.fail(404, "审核记录不存在");
        }
        StageSubmission stageSubmission = stageSubmissionService.getById(reviewRecord.getSubmissionId());
        if (stageSubmission == null) {
            return Result.fail(404, "审核记录关联的阶段提交不存在");
        }
        if (!canViewStageSubmission(currentUser, stageSubmission)) {
            return Result.fail(403, "当前用户无权查看该审核记录");
        }
        return Result.success(reviewRecord);
    }

    @PutMapping
    public Result<ReviewRecord> update(@Valid @RequestBody ReviewRecordUpdateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return Result.fail(403, "当前用户不是教师角色，不能修改审核记录");
        }

        ReviewRecord existing = reviewRecordService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "审核记录不存在");
        }
        if (!currentUser.getUserId().equals(existing.getReviewerId())) {
            return Result.fail(403, "当前用户无权修改该审核记录");
        }
        if (!existing.getSubmissionId().equals(dto.getSubmissionId())) {
            return Result.fail(400, "审核记录创建后不允许变更所属阶段提交");
        }

        StageSubmission stageSubmission = stageSubmissionService.getById(dto.getSubmissionId());
        if (stageSubmission == null) {
            return Result.fail(404, "阶段提交不存在");
        }
        Result<Void> reviewPermissionResult = validateReviewPermission(stageSubmission, currentUser.getUserId());
        if (reviewPermissionResult != null) {
            return Result.fail(reviewPermissionResult.getCode(), reviewPermissionResult.getMessage());
        }

        ReviewRecord reviewRecord = new ReviewRecord();
        reviewRecord.setId(dto.getId());
        reviewRecord.setSubmissionId(dto.getSubmissionId());
        reviewRecord.setReviewerId(existing.getReviewerId());
        reviewRecord.setReviewResult(dto.getReviewResult());
        reviewRecord.setScore(dto.getScore());
        reviewRecord.setComment(dto.getComment());
        reviewRecordService.updateById(reviewRecord);
        return Result.success(reviewRecordService.getById(dto.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return Result.fail(403, "当前用户不是教师角色，不能删除审核记录");
        }

        ReviewRecord existing = reviewRecordService.getById(id);
        if (existing == null) {
            return Result.fail(404, "审核记录不存在");
        }
        if (!currentUser.getUserId().equals(existing.getReviewerId())) {
            return Result.fail(403, "当前用户无权删除该审核记录");
        }
        return Result.success(reviewRecordService.removeById(id));
    }

    private void applyReviewRecordViewScope(LambdaQueryWrapper<ReviewRecord> wrapper,
                                            LoginUserPrincipal currentUser) {
        if (hasRole(currentUser, "ADMIN")) {
            return;
        }
        List<Long> submissionIds = getViewableSubmissionIds(currentUser);
        if (submissionIds.isEmpty()) {
            wrapper.eq(ReviewRecord::getId, -1L);
            return;
        }
        wrapper.in(ReviewRecord::getSubmissionId, submissionIds);
    }

    private List<Long> getViewableSubmissionIds(LoginUserPrincipal currentUser) {
        if (hasRole(currentUser, "TEACHER")) {
            List<Long> groupIds = projectGroupService.list(Wrappers.<ProjectGroup>lambdaQuery()
                    .eq(ProjectGroup::getTeacherId, currentUser.getUserId()))
                    .stream().map(ProjectGroup::getId).collect(Collectors.toList());
            if (groupIds.isEmpty()) {
                return List.of();
            }
            return stageSubmissionService.list(Wrappers.<StageSubmission>lambdaQuery()
                    .in(StageSubmission::getGroupId, groupIds))
                    .stream().map(StageSubmission::getId).collect(Collectors.toList());
        }
        List<Long> groupIds = projectGroupMemberService.list(Wrappers.<ProjectGroupMember>lambdaQuery()
                .eq(ProjectGroupMember::getUserId, currentUser.getUserId()))
                .stream().map(ProjectGroupMember::getGroupId).collect(Collectors.toList());
        if (groupIds.isEmpty()) {
            return List.of();
        }
        return stageSubmissionService.list(Wrappers.<StageSubmission>lambdaQuery()
                .in(StageSubmission::getGroupId, groupIds))
                .stream().map(StageSubmission::getId).collect(Collectors.toList());
    }

    private boolean canViewStageSubmission(LoginUserPrincipal currentUser, StageSubmission stageSubmission) {
        if (currentUser == null || currentUser.getUserId() == null || stageSubmission == null) {
            return false;
        }
        if (hasRole(currentUser, "ADMIN")) {
            return true;
        }
        if (currentUser.getUserId().equals(stageSubmission.getSubmitterId())) {
            return true;
        }
        if (hasRole(currentUser, "TEACHER")) {
            ProjectGroup projectGroup = projectGroupService.getById(stageSubmission.getGroupId());
            return projectGroup != null && currentUser.getUserId().equals(projectGroup.getTeacherId());
        }
        return isGroupMember(stageSubmission.getGroupId(), currentUser.getUserId());
    }

    private Result<Void> validateReviewPermission(StageSubmission stageSubmission, Long teacherId) {
        StageTask stageTask = stageTaskService.getById(stageSubmission.getTaskId());
        if (stageTask == null) {
            return Result.fail(404, "阶段提交关联的阶段任务不存在");
        }
        if (!stageSubmission.getBatchId().equals(stageTask.getBatchId())) {
            return Result.fail(400, "阶段提交与阶段任务所属批次不一致");
        }

        TrainingBatch trainingBatch = trainingBatchService.getById(stageSubmission.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "阶段提交关联的实训批次不存在");
        }
        ProjectGroup projectGroup = projectGroupService.getById(stageSubmission.getGroupId());
        if (projectGroup == null) {
            return Result.fail(404, "阶段提交关联的项目组不存在");
        }
        if (!stageSubmission.getBatchId().equals(projectGroup.getBatchId())) {
            return Result.fail(400, "阶段提交与项目组所属批次不一致");
        }
        if (!teacherId.equals(trainingBatch.getTeacherId())) {
            return Result.fail(403, "当前教师不是该批次的负责教师，不能审核该阶段提交");
        }
        if (!teacherId.equals(stageTask.getTeacherId())) {
            return Result.fail(403, "当前教师不是该阶段任务的发布教师，不能审核该阶段提交");
        }
        if (!teacherId.equals(projectGroup.getTeacherId())) {
            return Result.fail(403, "当前教师不是该项目组的指导教师，不能审核该阶段提交");
        }
        return null;
    }

    private boolean isGroupMember(Long groupId, Long userId) {
        return projectGroupMemberService.count(Wrappers.<ProjectGroupMember>lambdaQuery()
                .eq(ProjectGroupMember::getGroupId, groupId)
                .eq(ProjectGroupMember::getUserId, userId)) > 0;
    }

    private boolean hasRole(LoginUserPrincipal currentUser, String roleCode) {
        if (currentUser == null || currentUser.getUserId() == null) {
            return false;
        }
        return sysUserService.hasRole(currentUser.getUserId(), roleCode);
    }
}
