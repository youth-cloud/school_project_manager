package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.ProjectGroupMemberCreateDTO;
import org.example.backend.modules.edu.entity.ProjectTopic;
import org.example.backend.modules.edu.dto.ProjectGroupMemberPageQueryDTO;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.ProjectGroupMember;
import org.example.backend.modules.edu.entity.TopicApplication;
import org.example.backend.modules.edu.service.ProjectGroupMemberService;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.edu.service.ProjectTopicService;
import org.example.backend.modules.edu.service.TopicApplicationService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/project-group-members")
public class ProjectGroupMemberController {
    private final ProjectGroupMemberService projectGroupMemberService;
    private final ProjectGroupService projectGroupService;
    private final ProjectTopicService projectTopicService;
    private final TopicApplicationService topicApplicationService;
    private final SysUserService sysUserService;

    public ProjectGroupMemberController(ProjectGroupMemberService projectGroupMemberService,
                                        ProjectGroupService projectGroupService,
                                        ProjectTopicService projectTopicService,
                                        TopicApplicationService topicApplicationService,
                                        SysUserService sysUserService) {
        this.projectGroupMemberService = projectGroupMemberService;
        this.projectGroupService = projectGroupService;
        this.projectTopicService = projectTopicService;
        this.topicApplicationService = topicApplicationService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<ProjectGroupMember>> findAll() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        LambdaQueryWrapper<ProjectGroupMember> wrapper = Wrappers.lambdaQuery();
        applyProjectGroupMemberViewScope(wrapper, currentUser);
        wrapper.orderByDesc(ProjectGroupMember::getJoinTime);
        return Result.success(projectGroupMemberService.list(wrapper));
    }

    @GetMapping("/page")
    public Result<Page<ProjectGroupMember>> page(@Valid @ModelAttribute ProjectGroupMemberPageQueryDTO query) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        Page<ProjectGroupMember> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<ProjectGroupMember> wrapper = Wrappers.lambdaQuery();

        if (query.getGroupId() != null) {
            wrapper.eq(ProjectGroupMember::getGroupId, query.getGroupId());
        }
        if (query.getUserId() != null) {
            wrapper.eq(ProjectGroupMember::getUserId, query.getUserId());
        }
        if (query.getIsLeader() != null) {
            wrapper.eq(ProjectGroupMember::getIsLeader, query.getIsLeader());
        }
        if (query.getStatus() != null) {
            wrapper.eq(ProjectGroupMember::getStatus, query.getStatus());
        }

        applyProjectGroupMemberViewScope(wrapper, currentUser);
        wrapper.orderByDesc(ProjectGroupMember::getJoinTime);
        return Result.success(projectGroupMemberService.page(page, wrapper));
    }

    @Transactional
    @PostMapping
    public Result<ProjectGroupMember> create(@Valid @RequestBody ProjectGroupMemberCreateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "STUDENT")) {
            return Result.fail(403, "当前用户不是学生角色，不能加入项目组");
        }

        ProjectGroup projectGroup = projectGroupService.getById(dto.getGroupId());
        if (projectGroup == null) {
            return Result.fail(404, "项目组不存在");
        }

        LambdaQueryWrapper<ProjectGroupMember> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ProjectGroupMember::getGroupId, dto.getGroupId())
                .eq(ProjectGroupMember::getUserId, currentUser.getUserId());
        if (projectGroupMemberService.count(wrapper) > 0) {
            return Result.fail(400, "当前用户已在该项目组中");
        }

        Result<Void> leaderValidationResult = validateLeaderRelation(dto, projectGroup, currentUser);
        if (leaderValidationResult != null) {
            return Result.fail(leaderValidationResult.getCode(), leaderValidationResult.getMessage());
        }

        Result<Void> batchValidationResult = validateBatchGroupUniqueness(projectGroup, currentUser);
        if (batchValidationResult != null) {
            return Result.fail(batchValidationResult.getCode(), batchValidationResult.getMessage());
        }
        Result<Void> topicLockValidationResult = validateTopicLockRelation(projectGroup, currentUser.getUserId());
        if (topicLockValidationResult != null) {
            return Result.fail(topicLockValidationResult.getCode(), topicLockValidationResult.getMessage());
        }

        if (Integer.valueOf(1).equals(dto.getIsLeader())) {
            LambdaQueryWrapper<ProjectGroupMember> leaderWrapper = Wrappers.lambdaQuery();
            leaderWrapper.eq(ProjectGroupMember::getGroupId, dto.getGroupId())
                    .eq(ProjectGroupMember::getIsLeader, 1);
            if (projectGroupMemberService.count(leaderWrapper) > 0) {
                return Result.fail(400, "当前项目组已存在组长");
            }
        }

        ProjectGroupMember projectGroupMember = new ProjectGroupMember();
        projectGroupMember.setGroupId(dto.getGroupId());
        projectGroupMember.setUserId(currentUser.getUserId());
        projectGroupMember.setIsLeader(dto.getIsLeader());
        projectGroupMember.setStatus(dto.getStatus());
        projectGroupMemberService.save(projectGroupMember);
        ensureApprovedTopicApplication(projectGroup, currentUser.getUserId());
        return Result.success(projectGroupMember);
    }

    @GetMapping("/{id}")
    public Result<ProjectGroupMember> findById(@PathVariable Long id){
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        ProjectGroupMember projectGroupMember = projectGroupMemberService.getById(id);
        if (projectGroupMember == null) {
            return Result.fail(404,"项目组成员不存在");
        }
        if (!canViewProjectGroupMember(currentUser, projectGroupMember)) {
            return Result.fail(403, "当前用户无权查看该项目组成员记录");
        }
        return Result.success(projectGroupMember);
    }

    @Transactional
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id){
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        ProjectGroupMember existing=projectGroupMemberService.getById(id);
        if (existing == null) {
            return Result.fail(404,"项目组成员不存在");
        }
        if (Integer.valueOf(1).equals(existing.getIsLeader())) {
            return Result.fail(400, "项目组组长不能直接删除成员记录，请先删除项目组或调整组长后再操作");
        }
        ProjectGroup projectGroup = projectGroupService.getById(existing.getGroupId());
        if (sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return Result.success(removeMemberAndSync(existing, projectGroup));
        }
        if (sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            if (projectGroup == null) {
                return Result.fail(404, "关联项目组不存在");
            }
            if (!currentUser.getUserId().equals(projectGroup.getTeacherId())) {
                return Result.fail(403, "当前教师不是该项目组指导教师，不能删除成员记录");
            }
            return Result.success(removeMemberAndSync(existing, projectGroup));
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "STUDENT")) {
            return Result.fail(403, "当前用户没有删除项目组成员的权限");
        }
        if (!currentUser.getUserId().equals(existing.getUserId())) {
            return Result.fail(403, "学生只能删除自己的成员记录");
        }
        return Result.success(removeMemberAndSync(existing, projectGroup));
    }

    private void applyProjectGroupMemberViewScope(LambdaQueryWrapper<ProjectGroupMember> wrapper,
                                                  LoginUserPrincipal currentUser) {
        if (sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return;
        }
        if (sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            List<Long> groupIds = projectGroupService.list(Wrappers.<ProjectGroup>lambdaQuery()
                            .eq(ProjectGroup::getTeacherId, currentUser.getUserId()))
                    .stream().map(ProjectGroup::getId).toList();
            if (groupIds.isEmpty()) {
                wrapper.eq(ProjectGroupMember::getId, -1L);
                return;
            }
            wrapper.in(ProjectGroupMember::getGroupId, groupIds);
            return;
        }
        List<Long> groupIds = projectGroupMemberService.list(Wrappers.<ProjectGroupMember>lambdaQuery()
                        .eq(ProjectGroupMember::getUserId, currentUser.getUserId()))
                .stream().map(ProjectGroupMember::getGroupId).collect(Collectors.toList());
        if (groupIds.isEmpty()) {
            wrapper.eq(ProjectGroupMember::getId, -1L);
            return;
        }
        wrapper.in(ProjectGroupMember::getGroupId, groupIds);
    }

    private boolean canViewProjectGroupMember(LoginUserPrincipal currentUser, ProjectGroupMember member) {
        if (sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return true;
        }
        if (sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            ProjectGroup projectGroup = projectGroupService.getById(member.getGroupId());
            return projectGroup != null && currentUser.getUserId().equals(projectGroup.getTeacherId());
        }
        return projectGroupMemberService.count(Wrappers.<ProjectGroupMember>lambdaQuery()
                .eq(ProjectGroupMember::getGroupId, member.getGroupId())
                .eq(ProjectGroupMember::getUserId, currentUser.getUserId())) > 0;
    }

    private Result<Void> validateLeaderRelation(ProjectGroupMemberCreateDTO dto,
                                                ProjectGroup projectGroup,
                                                LoginUserPrincipal currentUser) {
        boolean isCurrentUserLeader = currentUser.getUserId().equals(projectGroup.getLeaderId());
        if (Integer.valueOf(1).equals(dto.getIsLeader()) && !isCurrentUserLeader) {
            return Result.fail(400, "只有项目组主表中配置的组长本人才能以组长身份加入");
        }
        if (!Integer.valueOf(1).equals(dto.getIsLeader()) && isCurrentUserLeader) {
            return Result.fail(400, "项目组主表中配置的组长加入成员表时必须标记为组长");
        }
        return null;
    }

    private Result<Void> validateBatchGroupUniqueness(ProjectGroup targetGroup, LoginUserPrincipal currentUser) {
        List<ProjectGroupMember> existingMembers = projectGroupMemberService.list(
                Wrappers.<ProjectGroupMember>lambdaQuery().eq(ProjectGroupMember::getUserId, currentUser.getUserId())
        );
        if (existingMembers == null || existingMembers.isEmpty()) {
            return null;
        }

        Set<Long> joinedGroupIds = existingMembers.stream()
                .map(ProjectGroupMember::getGroupId)
                .collect(Collectors.toSet());
        List<ProjectGroup> joinedGroups = projectGroupService.listByIds(joinedGroupIds);
        if (joinedGroups == null || joinedGroups.isEmpty()) {
            return null;
        }

        boolean alreadyJoinedSameBatch = joinedGroups.stream()
                .anyMatch(group -> !group.getId().equals(targetGroup.getId())
                        && group.getBatchId() != null
                        && group.getBatchId().equals(targetGroup.getBatchId()));
        if (alreadyJoinedSameBatch) {
            return Result.fail(400, "当前学生已加入该批次下的其他项目组，不能重复加入");
        }
        return null;
    }

    private Result<Void> validateTopicLockRelation(ProjectGroup targetGroup, Long userId) {
        if (targetGroup == null || targetGroup.getBatchId() == null || userId == null) {
            return null;
        }

        List<TopicApplication> lockedApplications = topicApplicationService.list(
                Wrappers.<TopicApplication>lambdaQuery()
                        .eq(TopicApplication::getStudentId, userId)
                        .eq(TopicApplication::getBatchId, targetGroup.getBatchId())
                        .in(TopicApplication::getStatus, "PENDING", "APPROVED")
        );
        boolean hasOtherTopicLock = lockedApplications.stream()
                .anyMatch(item -> item.getTopicId() != null && !item.getTopicId().equals(targetGroup.getTopicId()));
        if (hasOtherTopicLock) {
            return Result.fail(400, "当前学生在该批次已锁定其他课题，不能直接加入当前项目组");
        }
        return null;
    }

    private void ensureApprovedTopicApplication(ProjectGroup projectGroup, Long studentId) {
        if (projectGroup == null || projectGroup.getTopicId() == null || studentId == null) {
            return;
        }

        TopicApplication existingApplication = topicApplicationService.getOne(
                Wrappers.<TopicApplication>lambdaQuery()
                        .eq(TopicApplication::getBatchId, projectGroup.getBatchId())
                        .eq(TopicApplication::getTopicId, projectGroup.getTopicId())
                        .eq(TopicApplication::getStudentId, studentId)
                        .orderByDesc(TopicApplication::getCreateTime)
                        .last("limit 1")
        );

        if (existingApplication == null) {
            TopicApplication topicApplication = buildApprovedTopicApplication(projectGroup, studentId);
            topicApplicationService.save(topicApplication);
            syncTopicSelectedCount(projectGroup.getTopicId());
            return;
        }

        if (!"APPROVED".equals(existingApplication.getStatus())) {
            TopicApplication updateApplication = new TopicApplication();
            updateApplication.setId(existingApplication.getId());
            updateApplication.setStatus("APPROVED");
            updateApplication.setReviewerId(projectGroup.getTeacherId());
            updateApplication.setReviewTime(LocalDateTime.now());
            updateApplication.setReviewComment("学生加入项目组后系统自动通过对应课题选题申请");
            if (existingApplication.getApplyReason() == null || existingApplication.getApplyReason().isBlank()) {
                updateApplication.setApplyReason("学生加入项目组后系统自动补建选题申请");
            }
            topicApplicationService.updateById(updateApplication);
        }

        syncTopicSelectedCount(projectGroup.getTopicId());
    }

    private TopicApplication buildApprovedTopicApplication(ProjectGroup projectGroup, Long studentId) {
        TopicApplication topicApplication = new TopicApplication();
        topicApplication.setBatchId(projectGroup.getBatchId());
        topicApplication.setTopicId(projectGroup.getTopicId());
        topicApplication.setStudentId(studentId);
        topicApplication.setApplyReason("学生加入项目组后系统自动补建选题申请");
        topicApplication.setStatus("APPROVED");
        topicApplication.setReviewerId(projectGroup.getTeacherId());
        topicApplication.setReviewTime(LocalDateTime.now());
        topicApplication.setReviewComment("学生加入项目组后系统自动通过对应课题选题申请");
        return topicApplication;
    }

    private void syncTopicSelectedCount(Long topicId) {
        if (topicId == null) {
            return;
        }

        ProjectTopic projectTopic = projectTopicService.getById(topicId);
        if (projectTopic == null) {
            return;
        }

        int approvedStudentCount = countApprovedStudents(topicId);
        Integer currentSelectedCount = projectTopic.getSelectedCount();
        if (currentSelectedCount != null && currentSelectedCount == approvedStudentCount) {
            return;
        }

        ProjectTopic updateTopic = new ProjectTopic();
        updateTopic.setId(topicId);
        updateTopic.setSelectedCount(approvedStudentCount);
        projectTopicService.updateById(updateTopic);
    }

    private int countApprovedStudents(Long topicId) {
        Set<Long> approvedStudentIds = new LinkedHashSet<>();
        topicApplicationService.list(
                Wrappers.<TopicApplication>lambdaQuery()
                        .eq(TopicApplication::getTopicId, topicId)
                        .eq(TopicApplication::getStatus, "APPROVED")
        ).forEach(item -> {
            if (item.getStudentId() != null) {
                approvedStudentIds.add(item.getStudentId());
            }
        });
        return approvedStudentIds.size();
    }

    private boolean removeMemberAndSync(ProjectGroupMember member, ProjectGroup projectGroup) {
        boolean removed = projectGroupMemberService.removeById(member.getId());
        if (!removed) {
            return false;
        }
        revokeAutoApprovedTopicApplicationIfNeeded(projectGroup, member.getUserId());
        if (projectGroup != null) {
            syncTopicSelectedCount(projectGroup.getTopicId());
        }
        return true;
    }

    private void revokeAutoApprovedTopicApplicationIfNeeded(ProjectGroup projectGroup, Long studentId) {
        if (projectGroup == null || projectGroup.getBatchId() == null || projectGroup.getTopicId() == null || studentId == null) {
            return;
        }

        boolean stillJoinedSameBatch = projectGroupMemberService.list(
                Wrappers.<ProjectGroupMember>lambdaQuery()
                        .eq(ProjectGroupMember::getUserId, studentId)
        ).stream()
                .map(ProjectGroupMember::getGroupId)
                .filter(id -> id != null)
                .map(projectGroupService::getById)
                .filter(group -> group != null)
                .anyMatch(group -> projectGroup.getBatchId().equals(group.getBatchId()));
        if (stillJoinedSameBatch) {
            return;
        }

        topicApplicationService.update(
                Wrappers.<TopicApplication>lambdaUpdate()
                        .eq(TopicApplication::getBatchId, projectGroup.getBatchId())
                        .eq(TopicApplication::getTopicId, projectGroup.getTopicId())
                        .eq(TopicApplication::getStudentId, studentId)
                        .eq(TopicApplication::getStatus, "APPROVED")
                        .and(wrapper -> wrapper
                                .eq(TopicApplication::getApplyReason, "学生加入项目组后系统自动补建选题申请")
                                .or()
                                .eq(TopicApplication::getReviewComment, "学生加入项目组后系统自动通过对应课题选题申请")
                        )
                        .set(TopicApplication::getStatus, "CANCELED")
                        .set(TopicApplication::getReviewTime, LocalDateTime.now())
                        .set(TopicApplication::getReviewComment, "学生退出项目组后系统自动取消对应课题锁定")
        );
    }
}
