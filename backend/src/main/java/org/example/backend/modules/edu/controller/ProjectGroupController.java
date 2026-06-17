package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.ProjectGroupCreateDTO;
import org.example.backend.modules.edu.dto.ProjectGroupPageQueryDTO;
import org.example.backend.modules.edu.dto.ProjectGroupUpdateDTO;
import org.example.backend.modules.edu.entity.DefenseSchedule;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.ProjectGroupApplication;
import org.example.backend.modules.edu.entity.ProjectGroupMember;
import org.example.backend.modules.edu.entity.ProjectTopic;
import org.example.backend.modules.edu.entity.ScoreRecord;
import org.example.backend.modules.edu.entity.StageSubmission;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.entity.WeeklyReport;
import org.example.backend.modules.edu.service.DefenseScheduleService;
import org.example.backend.modules.edu.service.ProjectGroupMemberService;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.edu.service.ProjectGroupApplicationService;
import org.example.backend.modules.edu.service.ProjectTopicService;
import org.example.backend.modules.edu.service.ScoreRecordService;
import org.example.backend.modules.edu.service.StageSubmissionService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.example.backend.modules.edu.service.WeeklyReportService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-groups")
public class ProjectGroupController {

    private final ProjectGroupService projectGroupService;
    private final TrainingBatchService trainingBatchService;
    private final ProjectTopicService projectTopicService;
    private final ProjectGroupMemberService projectGroupMemberService;
    private final WeeklyReportService weeklyReportService;
    private final StageSubmissionService stageSubmissionService;
    private final ScoreRecordService scoreRecordService;
    private final DefenseScheduleService defenseScheduleService;
    private final ProjectGroupApplicationService projectGroupApplicationService;
    private final SysUserService sysUserService;

    public ProjectGroupController(ProjectGroupService projectGroupService,
                                  TrainingBatchService trainingBatchService,
                                  ProjectTopicService projectTopicService,
                                  ProjectGroupMemberService projectGroupMemberService,
                                  WeeklyReportService weeklyReportService,
                                  StageSubmissionService stageSubmissionService,
                                  ScoreRecordService scoreRecordService,
                                  DefenseScheduleService defenseScheduleService,
                                  ProjectGroupApplicationService projectGroupApplicationService,
                                  SysUserService sysUserService) {
        this.projectGroupService = projectGroupService;
        this.trainingBatchService = trainingBatchService;
        this.projectTopicService = projectTopicService;
        this.projectGroupMemberService = projectGroupMemberService;
        this.weeklyReportService = weeklyReportService;
        this.stageSubmissionService = stageSubmissionService;
        this.scoreRecordService = scoreRecordService;
        this.defenseScheduleService = defenseScheduleService;
        this.projectGroupApplicationService = projectGroupApplicationService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<ProjectGroup>> findAll() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        LambdaQueryWrapper<ProjectGroup> wrapper = Wrappers.lambdaQuery();
        applyProjectGroupViewScope(wrapper, currentUser);
        wrapper.orderByDesc(ProjectGroup::getCreateTime);
        return Result.success(projectGroupService.list(wrapper));
    }

    @GetMapping("/page")
    public Result<Page<ProjectGroup>> page(@Valid @ModelAttribute ProjectGroupPageQueryDTO query) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        Page<ProjectGroup> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<ProjectGroup> wrapper = Wrappers.lambdaQuery();

        if (StringUtils.hasText(query.getGroupName())) {
            wrapper.like(ProjectGroup::getGroupName, query.getGroupName());
        }
        if (query.getBatchId() != null) {
            wrapper.eq(ProjectGroup::getBatchId, query.getBatchId());
        }
        if (query.getTopicId() != null) {
            wrapper.eq(ProjectGroup::getTopicId, query.getTopicId());
        }
        if (query.getLeaderId() != null) {
            wrapper.eq(ProjectGroup::getLeaderId, query.getLeaderId());
        }
        if (query.getTeacherId() != null) {
            wrapper.eq(ProjectGroup::getTeacherId, query.getTeacherId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(ProjectGroup::getStatus, query.getStatus());
        }

        applyProjectGroupViewScope(wrapper, currentUser);
        wrapper.orderByDesc(ProjectGroup::getCreateTime);
        return Result.success(projectGroupService.page(page, wrapper));
    }

    @PostMapping
    public Result<ProjectGroup> create(@Valid @RequestBody ProjectGroupCreateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return Result.fail(403, "当前版本正式项目组默认由建组申请审核通过后自动生成，仅管理员可手工创建");
        }

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
        if (sysUserService.getById(dto.getLeaderId()) == null) {
            return Result.fail(404, "组长不存在");
        }
        if (!sysUserService.hasRole(dto.getLeaderId(), "STUDENT")) {
            return Result.fail(400, "当前组长不是学生角色");
        }

        ProjectGroup projectGroup = new ProjectGroup();
        projectGroup.setBatchId(dto.getBatchId());
        projectGroup.setTopicId(dto.getTopicId());
        projectGroup.setGroupName(dto.getGroupName());
        projectGroup.setLeaderId(dto.getLeaderId());
        projectGroup.setTeacherId(projectTopic.getTeacherId());
        projectGroup.setProjectName(dto.getProjectName());
        projectGroup.setProjectDescription(dto.getProjectDescription());
        projectGroup.setRepoUrl(dto.getRepoUrl());
        projectGroup.setDeployUrl(dto.getDeployUrl());
        projectGroup.setStatus(dto.getStatus());
        projectGroupService.save(projectGroup);
        return Result.success(projectGroup);
    }

    @GetMapping("/{id}")
    public Result<ProjectGroup> findById(@PathVariable Long id) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        ProjectGroup projectGroup = projectGroupService.getById(id);
        if (projectGroup == null) {
            return Result.fail(404,"项目组不存在");
        }
        if (!canViewProjectGroup(currentUser, projectGroup)) {
            return Result.fail(403, "当前用户无权查看该项目组");
        }
        return Result.success(projectGroup);
    }

    @PutMapping
    public Result<ProjectGroup> update(@Valid @RequestBody ProjectGroupUpdateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return Result.fail(403, "当前用户不是教师角色，不能修改项目组");
        }

        ProjectGroup existing = projectGroupService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "项目组不存在");
        }
        if (!currentUser.getUserId().equals(existing.getTeacherId())) {
            return Result.fail(403, "当前教师不是该项目组指导教师，不能修改项目组");
        }
        if (!existing.getBatchId().equals(dto.getBatchId()) || !existing.getTopicId().equals(dto.getTopicId())) {
            return Result.fail(400, "正式项目组创建后不允许变更所属批次或课题");
        }

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
        if (!currentUser.getUserId().equals(projectTopic.getTeacherId())) {
            return Result.fail(403, "当前教师不是该课题的发布教师，不能修改项目组");
        }
        if (!existing.getLeaderId().equals(dto.getLeaderId())) {
            return Result.fail(400, "正式项目组组长默认来自已通过的建组申请，当前版本不支持在结果层直接变更组长");
        }
        if (sysUserService.getById(dto.getLeaderId()) == null) {
            return Result.fail(404, "组长不存在");
        }
        if (!sysUserService.hasRole(dto.getLeaderId(), "STUDENT")) {
            return Result.fail(400, "当前组长不是学生角色");
        }

        ProjectGroup projectGroup = new ProjectGroup();
        projectGroup.setId(dto.getId());
        projectGroup.setBatchId(dto.getBatchId());
        projectGroup.setTopicId(dto.getTopicId());
        projectGroup.setGroupName(dto.getGroupName());
        projectGroup.setLeaderId(dto.getLeaderId());
        projectGroup.setTeacherId(currentUser.getUserId());
        projectGroup.setProjectName(dto.getProjectName());
        projectGroup.setProjectDescription(dto.getProjectDescription());
        projectGroup.setRepoUrl(dto.getRepoUrl());
        projectGroup.setDeployUrl(dto.getDeployUrl());
        projectGroup.setStatus(dto.getStatus());
        projectGroupService.updateById(projectGroup);
        return Result.success(projectGroupService.getById(dto.getId()));
    }

    @Transactional
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        ProjectGroup existing = projectGroupService.getById(id);
        if (existing == null) {
            return Result.fail(404,"项目组不存在");
        }
        Result<Void> dependencyValidationResult = validateGroupDeleteDependencies(id);
        if (dependencyValidationResult != null) {
            return Result.fail(dependencyValidationResult.getCode(), dependencyValidationResult.getMessage());
        }
        if (sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            clearGeneratedGroupReference(id);
            removeGroupMembers(id);
            return Result.success(projectGroupService.removeById(id));
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return Result.fail(403, "当前用户没有删除项目组的权限");
        }
        if (!currentUser.getUserId().equals(existing.getTeacherId())) {
            return Result.fail(403, "当前教师不是该项目组指导教师，不能删除项目组");
        }
        clearGeneratedGroupReference(id);
        removeGroupMembers(id);
        return Result.success(projectGroupService.removeById(id));
    }

    private void applyProjectGroupViewScope(LambdaQueryWrapper<ProjectGroup> wrapper, LoginUserPrincipal currentUser) {
        if (sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return;
        }
        if (sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            wrapper.eq(ProjectGroup::getTeacherId, currentUser.getUserId());
            return;
        }
        List<Long> groupIds = projectGroupMemberService.list(
                Wrappers.<ProjectGroupMember>lambdaQuery().eq(ProjectGroupMember::getUserId, currentUser.getUserId())
        ).stream().map(ProjectGroupMember::getGroupId).toList();
        if (groupIds.isEmpty()) {
            wrapper.eq(ProjectGroup::getId, -1L);
            return;
        }
        wrapper.in(ProjectGroup::getId, groupIds);
    }

    private boolean canViewProjectGroup(LoginUserPrincipal currentUser, ProjectGroup projectGroup) {
        if (sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return true;
        }
        if (sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return currentUser.getUserId().equals(projectGroup.getTeacherId());
        }
        return projectGroupMemberService.count(Wrappers.<ProjectGroupMember>lambdaQuery()
                .eq(ProjectGroupMember::getGroupId, projectGroup.getId())
                .eq(ProjectGroupMember::getUserId, currentUser.getUserId())) > 0;
    }

    private void removeGroupMembers(Long groupId) {
        projectGroupMemberService.remove(
                Wrappers.<ProjectGroupMember>lambdaQuery()
                        .eq(ProjectGroupMember::getGroupId, groupId)
        );
    }

    private Result<Void> validateGroupDeleteDependencies(Long groupId) {
        if (weeklyReportService.count(
                Wrappers.<WeeklyReport>lambdaQuery()
                        .eq(WeeklyReport::getGroupId, groupId)
        ) > 0) {
            return Result.fail(400, "该项目组已有关联周报，不能删除");
        }

        if (stageSubmissionService.count(
                Wrappers.<StageSubmission>lambdaQuery()
                        .eq(StageSubmission::getGroupId, groupId)
        ) > 0) {
            return Result.fail(400, "该项目组已有关联阶段提交，不能删除");
        }

        if (scoreRecordService.count(
                Wrappers.<ScoreRecord>lambdaQuery()
                        .eq(ScoreRecord::getGroupId, groupId)
        ) > 0) {
            return Result.fail(400, "该项目组已有关联成绩记录，不能删除");
        }

        if (defenseScheduleService.count(
                Wrappers.<DefenseSchedule>lambdaQuery()
                        .eq(DefenseSchedule::getGroupId, groupId)
        ) > 0) {
            return Result.fail(400, "该项目组已有关联答辩安排，不能删除");
        }

        return null;
    }

    private void clearGeneratedGroupReference(Long groupId) {
        projectGroupApplicationService.update(
                Wrappers.<ProjectGroupApplication>lambdaUpdate()
                        .eq(ProjectGroupApplication::getGeneratedGroupId, groupId)
                        .set(ProjectGroupApplication::getGeneratedGroupId, null)
        );
    }
}
