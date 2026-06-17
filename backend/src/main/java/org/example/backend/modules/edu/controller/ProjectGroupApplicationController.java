package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.ProjectGroupApplicationCreateDTO;
import org.example.backend.modules.edu.dto.ProjectGroupApplicationPageQueryDTO;
import org.example.backend.modules.edu.dto.ProjectGroupApplicationReviewDTO;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.ProjectGroupApplication;
import org.example.backend.modules.edu.entity.ProjectGroupApplicationMember;
import org.example.backend.modules.edu.entity.ProjectGroupMember;
import org.example.backend.modules.edu.entity.ProjectTopic;
import org.example.backend.modules.edu.entity.TopicApplication;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.service.ProjectGroupApplicationMemberService;
import org.example.backend.modules.edu.service.ProjectGroupApplicationService;
import org.example.backend.modules.edu.service.ProjectGroupMemberService;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.edu.service.ProjectTopicService;
import org.example.backend.modules.edu.service.TopicApplicationService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/project-group-applications")
public class ProjectGroupApplicationController {

    private final ProjectGroupApplicationService projectGroupApplicationService;
    private final ProjectGroupApplicationMemberService projectGroupApplicationMemberService;
    private final ProjectGroupService projectGroupService;
    private final ProjectGroupMemberService projectGroupMemberService;
    private final TrainingBatchService trainingBatchService;
    private final ProjectTopicService projectTopicService;
    private final TopicApplicationService topicApplicationService;
    private final SysUserService sysUserService;

    public ProjectGroupApplicationController(ProjectGroupApplicationService projectGroupApplicationService,
                                             ProjectGroupApplicationMemberService projectGroupApplicationMemberService,
                                             ProjectGroupService projectGroupService,
                                             ProjectGroupMemberService projectGroupMemberService,
                                             TrainingBatchService trainingBatchService,
                                             ProjectTopicService projectTopicService,
                                             TopicApplicationService topicApplicationService,
                                             SysUserService sysUserService) {
        this.projectGroupApplicationService = projectGroupApplicationService;
        this.projectGroupApplicationMemberService = projectGroupApplicationMemberService;
        this.projectGroupService = projectGroupService;
        this.projectGroupMemberService = projectGroupMemberService;
        this.trainingBatchService = trainingBatchService;
        this.projectTopicService = projectTopicService;
        this.topicApplicationService = topicApplicationService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<ProjectGroupApplication>> findAll() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        LambdaQueryWrapper<ProjectGroupApplication> wrapper = Wrappers.lambdaQuery();
        applyProjectGroupApplicationViewScope(wrapper, currentUser);
        wrapper.orderByDesc(ProjectGroupApplication::getCreateTime);
        return Result.success(projectGroupApplicationService.list(wrapper));
    }

    @GetMapping("/page")
    public Result<Page<ProjectGroupApplication>> page(@Valid @ModelAttribute ProjectGroupApplicationPageQueryDTO query) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        Page<ProjectGroupApplication> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<ProjectGroupApplication> wrapper = Wrappers.lambdaQuery();

        if (StringUtils.hasText(query.getGroupName())) {
            wrapper.like(ProjectGroupApplication::getGroupName, query.getGroupName());
        }
        if (query.getBatchId() != null) {
            wrapper.eq(ProjectGroupApplication::getBatchId, query.getBatchId());
        }
        if (query.getTopicId() != null) {
            wrapper.eq(ProjectGroupApplication::getTopicId, query.getTopicId());
        }
        if (query.getLeaderId() != null) {
            wrapper.eq(ProjectGroupApplication::getLeaderId, query.getLeaderId());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(ProjectGroupApplication::getStatus, query.getStatus());
        }

        applyProjectGroupApplicationViewScope(wrapper, currentUser);
        wrapper.orderByDesc(ProjectGroupApplication::getCreateTime);
        return Result.success(projectGroupApplicationService.page(page, wrapper));
    }

    @PostMapping
    public Result<ProjectGroupApplication> create(@Valid @RequestBody ProjectGroupApplicationCreateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "STUDENT")) {
            return Result.fail(403, "当前用户不是学生角色，不能发起建组申请");
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

        Set<Long> memberIds = normalizeMemberIds(currentUser.getUserId(), dto.getMemberIds());
        if (projectTopic.getMaxMembers() != null && memberIds.size() > projectTopic.getMaxMembers()) {
            return Result.fail(400, "申请成员数超过课题最大人数限制");
        }

        Result<Void> validationResult = validateMembersForApplication(
                dto.getBatchId(),
                dto.getTopicId(),
                memberIds,
                null
        );
        if (validationResult != null) {
            return Result.fail(validationResult.getCode(), validationResult.getMessage());
        }

        ProjectGroupApplication application = new ProjectGroupApplication();
        application.setBatchId(dto.getBatchId());
        application.setTopicId(dto.getTopicId());
        application.setLeaderId(currentUser.getUserId());
        application.setGroupName(dto.getGroupName());
        application.setProjectName(dto.getProjectName());
        application.setProjectDescription(dto.getProjectDescription());
        application.setRepoUrl(dto.getRepoUrl());
        application.setDeployUrl(dto.getDeployUrl());
        application.setApplyReason(dto.getApplyReason());
        application.setStatus("PENDING");
        projectGroupApplicationService.save(application);

        List<ProjectGroupApplicationMember> members = memberIds.stream().map(userId -> {
            ProjectGroupApplicationMember member = new ProjectGroupApplicationMember();
            member.setApplicationId(application.getId());
            member.setUserId(userId);
            member.setIsLeader(userId.equals(currentUser.getUserId()) ? 1 : 0);
            return member;
        }).toList();
        projectGroupApplicationMemberService.saveBatch(members);

        return Result.success(application);
    }

    @GetMapping("/{id}")
    public Result<ProjectGroupApplication> findById(@PathVariable Long id) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        ProjectGroupApplication application = projectGroupApplicationService.getById(id);
        if (application == null) {
            return Result.fail(404, "建组申请不存在");
        }
        if (!canViewProjectGroupApplication(currentUser, application)) {
            return Result.fail(403, "当前用户无权查看该建组申请");
        }
        return Result.success(application);
    }

    @Transactional
    @PutMapping("/review")
    public Result<ProjectGroupApplication> review(@Valid @RequestBody ProjectGroupApplicationReviewDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return Result.fail(403, "当前用户不是教师角色，不能审核建组申请");
        }

        ProjectGroupApplication existing = projectGroupApplicationService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "建组申请不存在");
        }
        if (!"PENDING".equals(existing.getStatus())) {
            return Result.fail(400, "当前申请状态不允许审核");
        }

        ProjectTopic projectTopic = projectTopicService.getById(existing.getTopicId());
        if (projectTopic == null) {
            return Result.fail(404, "关联课题不存在");
        }
        if (!currentUser.getUserId().equals(projectTopic.getTeacherId())) {
            return Result.fail(403, "只有课题发布教师可以审核该建组申请");
        }

        if ("APPROVED".equals(dto.getStatus())) {
            List<ProjectGroupApplicationMember> applicationMembers = projectGroupApplicationMemberService.list(
                    Wrappers.<ProjectGroupApplicationMember>lambdaQuery()
                            .eq(ProjectGroupApplicationMember::getApplicationId, existing.getId())
            );
            if (applicationMembers == null || applicationMembers.isEmpty()) {
                return Result.fail(400, "建组申请成员为空，不能审批通过");
            }

            Set<Long> memberIds = applicationMembers.stream()
                    .map(ProjectGroupApplicationMember::getUserId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            Result<Void> validationResult = validateMembersForApplication(
                    existing.getBatchId(),
                    existing.getTopicId(),
                    memberIds,
                    existing.getId()
            );
            if (validationResult != null) {
                return Result.fail(validationResult.getCode(), validationResult.getMessage());
            }

            ProjectGroup projectGroup = new ProjectGroup();
            projectGroup.setBatchId(existing.getBatchId());
            projectGroup.setTopicId(existing.getTopicId());
            projectGroup.setGroupName(existing.getGroupName());
            projectGroup.setLeaderId(existing.getLeaderId());
            projectGroup.setTeacherId(projectTopic.getTeacherId());
            projectGroup.setProjectName(existing.getProjectName());
            projectGroup.setProjectDescription(existing.getProjectDescription());
            projectGroup.setRepoUrl(existing.getRepoUrl());
            projectGroup.setDeployUrl(existing.getDeployUrl());
            projectGroup.setStatus(1);
            projectGroupService.save(projectGroup);

            List<ProjectGroupMember> groupMembers = applicationMembers.stream().map(item -> {
                ProjectGroupMember member = new ProjectGroupMember();
                member.setGroupId(projectGroup.getId());
                member.setUserId(item.getUserId());
                member.setIsLeader(item.getIsLeader());
                member.setStatus(1);
                member.setJoinTime(LocalDateTime.now());
                return member;
            }).toList();
            projectGroupMemberService.saveBatch(groupMembers);

            existing.setGeneratedGroupId(projectGroup.getId());
        }

        existing.setStatus(dto.getStatus());
        existing.setReviewerId(currentUser.getUserId());
        existing.setReviewComment(dto.getReviewComment());
        existing.setReviewTime(LocalDateTime.now());
        projectGroupApplicationService.updateById(existing);

        return Result.success(projectGroupApplicationService.getById(dto.getId()));
    }

    @PutMapping("/{id}/cancel")
    public Result<ProjectGroupApplication> cancel(@PathVariable Long id) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "STUDENT")) {
            return Result.fail(403, "当前用户不是学生角色，不能撤回建组申请");
        }

        ProjectGroupApplication existing = projectGroupApplicationService.getById(id);
        if (existing == null) {
            return Result.fail(404, "建组申请不存在");
        }
        if (!currentUser.getUserId().equals(existing.getLeaderId())) {
            return Result.fail(403, "只有申请组长本人可以撤回建组申请");
        }
        if (!"PENDING".equals(existing.getStatus())) {
            return Result.fail(400, "只有待审核状态的建组申请才允许撤回");
        }

        ProjectGroupApplication application = new ProjectGroupApplication();
        application.setId(id);
        application.setStatus("CANCELED");
        projectGroupApplicationService.updateById(application);
        return Result.success(projectGroupApplicationService.getById(id));
    }

    private void applyProjectGroupApplicationViewScope(LambdaQueryWrapper<ProjectGroupApplication> wrapper,
                                                       LoginUserPrincipal currentUser) {
        if (hasRole(currentUser, "ADMIN")) {
            return;
        }
        if (hasRole(currentUser, "STUDENT")) {
            wrapper.eq(ProjectGroupApplication::getLeaderId, currentUser.getUserId());
            return;
        }
        if (hasRole(currentUser, "TEACHER")) {
            List<Long> topicIds = projectTopicService.list(
                    Wrappers.<ProjectTopic>lambdaQuery()
                            .eq(ProjectTopic::getTeacherId, currentUser.getUserId())
            ).stream().map(ProjectTopic::getId).toList();
            if (topicIds.isEmpty()) {
                wrapper.eq(ProjectGroupApplication::getId, -1L);
                return;
            }
            wrapper.in(ProjectGroupApplication::getTopicId, topicIds);
            return;
        }
        wrapper.eq(ProjectGroupApplication::getId, -1L);
    }

    private boolean canViewProjectGroupApplication(LoginUserPrincipal currentUser, ProjectGroupApplication application) {
        if (hasRole(currentUser, "ADMIN")) {
            return true;
        }
        if (hasRole(currentUser, "STUDENT")) {
            return currentUser.getUserId().equals(application.getLeaderId());
        }
        if (hasRole(currentUser, "TEACHER")) {
            ProjectTopic projectTopic = projectTopicService.getById(application.getTopicId());
            return projectTopic != null && currentUser.getUserId().equals(projectTopic.getTeacherId());
        }
        return false;
    }

    private Result<Void> validateMembersForApplication(Long batchId,
                                                       Long topicId,
                                                       Set<Long> memberIds,
                                                       Long excludeApplicationId) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Result.fail(400, "建组成员不能为空");
        }

        ProjectTopic projectTopic = projectTopicService.getById(topicId);
        if (projectTopic == null) {
            return Result.fail(404, "课题不存在");
        }

        for (Long memberId : memberIds) {
            if (memberId == null) {
                return Result.fail(400, "成员ID不能为空");
            }
            if (sysUserService.getById(memberId) == null) {
                return Result.fail(404, "存在不存在的成员用户");
            }
            if (!sysUserService.hasRole(memberId, "STUDENT")) {
                return Result.fail(400, "建组成员必须全部是学生");
            }

            boolean approvedTopicApplication = topicApplicationService.count(
                    Wrappers.<TopicApplication>lambdaQuery()
                            .eq(TopicApplication::getBatchId, batchId)
                            .eq(TopicApplication::getTopicId, topicId)
                            .eq(TopicApplication::getStudentId, memberId)
                            .eq(TopicApplication::getStatus, "APPROVED")
            ) > 0;
            if (!approvedTopicApplication) {
                return Result.fail(400, "存在未通过该课题选题申请的成员，不能发起建组申请");
            }

            List<ProjectGroupMember> joinedMembers = projectGroupMemberService.list(
                    Wrappers.<ProjectGroupMember>lambdaQuery().eq(ProjectGroupMember::getUserId, memberId)
            );
            if (joinedMembers != null && !joinedMembers.isEmpty()) {
                Set<Long> joinedGroupIds = joinedMembers.stream().map(ProjectGroupMember::getGroupId).collect(Collectors.toSet());
                List<ProjectGroup> joinedGroups = projectGroupService.listByIds(joinedGroupIds);
                boolean alreadyInSameBatchGroup = joinedGroups.stream()
                        .anyMatch(group -> batchId.equals(group.getBatchId()));
                if (alreadyInSameBatchGroup) {
                    return Result.fail(400, "存在成员已加入该批次下的正式项目组，不能重复申请");
                }
            }

            List<ProjectGroupApplicationMember> pendingApplicationMembers = projectGroupApplicationMemberService.list(
                    Wrappers.<ProjectGroupApplicationMember>lambdaQuery().eq(ProjectGroupApplicationMember::getUserId, memberId)
            );
            if (pendingApplicationMembers != null && !pendingApplicationMembers.isEmpty()) {
                Set<Long> applicationIds = pendingApplicationMembers.stream()
                        .map(ProjectGroupApplicationMember::getApplicationId)
                        .collect(Collectors.toSet());
                List<ProjectGroupApplication> pendingApplications = projectGroupApplicationService.listByIds(applicationIds);
                boolean alreadyInPendingApplication = pendingApplications.stream()
                        .anyMatch(application -> "PENDING".equals(application.getStatus())
                                && batchId.equals(application.getBatchId())
                                && (excludeApplicationId == null
                                || !excludeApplicationId.equals(application.getId())));
                if (alreadyInPendingApplication) {
                    return Result.fail(400, "存在成员已参与该批次下的其他待审核建组申请，不能重复申请");
                }
            }
        }

        if (projectTopic.getMaxMembers() != null && memberIds.size() > projectTopic.getMaxMembers()) {
            return Result.fail(400, "建组成员数超过课题最大人数限制");
        }

        return null;
    }

    private Set<Long> normalizeMemberIds(Long leaderId, List<Long> memberIds) {
        Set<Long> normalized = new LinkedHashSet<>();
        normalized.add(leaderId);
        if (memberIds != null) {
            normalized.addAll(memberIds);
        }
        normalized.remove(null);
        return normalized;
    }

    private boolean hasRole(LoginUserPrincipal currentUser, String roleCode) {
        if (currentUser == null || currentUser.getUserId() == null) {
            return false;
        }
        return sysUserService.hasRole(currentUser.getUserId(), roleCode);
    }
}
