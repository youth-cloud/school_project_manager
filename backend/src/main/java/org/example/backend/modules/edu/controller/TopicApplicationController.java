package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.TopicApplicationCreateDTO;
import org.example.backend.modules.edu.dto.TopicApplicationPageQueryDTO;
import org.example.backend.modules.edu.dto.TopicApplicationReviewDTO;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.ProjectGroupMember;
import org.example.backend.modules.edu.entity.ProjectTopic;
import org.example.backend.modules.edu.entity.TopicApplication;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.service.ProjectGroupMemberService;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.edu.service.ProjectTopicService;
import org.example.backend.modules.edu.service.TopicApplicationService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/topic-applications")
public class TopicApplicationController {
    private final TopicApplicationService topicApplicationService;
    private final TrainingBatchService trainingBatchService;
    private final ProjectTopicService projectTopicService;
    private final ProjectGroupService projectGroupService;
    private final ProjectGroupMemberService projectGroupMemberService;
    private final SysUserService sysUserService;

    public TopicApplicationController(TopicApplicationService topicApplicationService,
                                      TrainingBatchService trainingBatchService,
                                      ProjectTopicService projectTopicService,
                                      ProjectGroupService projectGroupService,
                                      ProjectGroupMemberService projectGroupMemberService,
                                      SysUserService sysUserService) {
        this.topicApplicationService = topicApplicationService;
        this.trainingBatchService = trainingBatchService;
        this.projectTopicService = projectTopicService;
        this.projectGroupService = projectGroupService;
        this.projectGroupMemberService = projectGroupMemberService;
        this.sysUserService = sysUserService;
    }


    @GetMapping
    public Result<List<TopicApplication>> findAll(){
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        LambdaQueryWrapper<TopicApplication> wrapper = Wrappers.lambdaQuery();
        applyTopicApplicationViewScope(wrapper, currentUser);
        wrapper.orderByDesc(TopicApplication::getCreateTime);
        return Result.success(topicApplicationService.list(wrapper));
    }

    @GetMapping("/page")
    public Result<Page<TopicApplication>> page(@Valid @ModelAttribute TopicApplicationPageQueryDTO query) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

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

        applyTopicApplicationViewScope(wrapper, currentUser);
        wrapper.orderByDesc(TopicApplication::getCreateTime);
        return Result.success(topicApplicationService.page(page, wrapper));
    }

    @PostMapping
    public Result<TopicApplication> create(@Valid @RequestBody TopicApplicationCreateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "STUDENT")) {
            return Result.fail(403, "当前用户不是学生角色，不能申请选题");
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
        Result<Void> studentTopicValidationResult = validateStudentTopicApplication(
                currentUser.getUserId(),
                dto.getBatchId(),
                null,
                false
        );
        if (studentTopicValidationResult != null) {
            return Result.fail(studentTopicValidationResult.getCode(), studentTopicValidationResult.getMessage());
        }

        TopicApplication topicApplication = new TopicApplication();
        topicApplication.setBatchId(dto.getBatchId());
        topicApplication.setTopicId(dto.getTopicId());
        topicApplication.setStudentId(currentUser.getUserId());
        topicApplication.setApplyReason(dto.getApplyReason());
        topicApplication.setStatus("PENDING");
        topicApplicationService.save(topicApplication);
        return Result.success(topicApplication);
    }

    @GetMapping("/{id}")
    public Result<TopicApplication> findById(@PathVariable Long id) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        TopicApplication topicApplication = topicApplicationService.getById(id);
        if (topicApplication == null) {
            return Result.fail(404, "选题申请不存在");
        }
        if (!canViewTopicApplication(currentUser, topicApplication)) {
            return Result.fail(403, "当前用户无权查看该选题申请");
        }
        return Result.success(topicApplication);
    }

    @PutMapping("/review")
    public Result<TopicApplication> review(@Valid @RequestBody TopicApplicationReviewDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return Result.fail(403, "当前用户不是教师角色，不能审核选题申请");
        }

        TopicApplication existing = topicApplicationService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "选题申请不存在");
        }
        if (!"PENDING".equals(existing.getStatus())) {
            return Result.fail(400, "当前申请状态不允许审核");
        }

        ProjectTopic projectTopic = projectTopicService.getById(existing.getTopicId());
        if (projectTopic == null) {
            return Result.fail(404, "关联课题不存在");
        }
        if (!currentUser.getUserId().equals(projectTopic.getTeacherId())) {
            return Result.fail(403, "只有课题发布教师可以审核该申请");
        }
        if ("APPROVED".equals(dto.getStatus())) {
            Result<Void> studentTopicValidationResult = validateStudentTopicApplication(
                    existing.getStudentId(),
                    existing.getBatchId(),
                    existing.getId(),
                    true
            );
            if (studentTopicValidationResult != null) {
                return Result.fail(studentTopicValidationResult.getCode(), studentTopicValidationResult.getMessage());
            }
        }

        TopicApplication topicApplication = new TopicApplication();
        topicApplication.setId(dto.getId());
        topicApplication.setStatus(dto.getStatus());
        topicApplication.setReviewerId(currentUser.getUserId());
        topicApplication.setReviewComment(dto.getReviewComment());
        topicApplication.setReviewTime(LocalDateTime.now());
        topicApplicationService.updateById(topicApplication);
        syncTopicSelectedCount(existing.getTopicId());
        return Result.success(topicApplicationService.getById(dto.getId()));
    }

    private void applyTopicApplicationViewScope(LambdaQueryWrapper<TopicApplication> wrapper,
                                                LoginUserPrincipal currentUser) {
        if (hasRole(currentUser, "ADMIN")) {
            return;
        }
        if (hasRole(currentUser, "STUDENT")) {
            wrapper.eq(TopicApplication::getStudentId, currentUser.getUserId());
            return;
        }
        if (hasRole(currentUser, "TEACHER")) {
            List<Long> topicIds = projectTopicService.list(
                    Wrappers.<ProjectTopic>lambdaQuery()
                            .eq(ProjectTopic::getTeacherId, currentUser.getUserId())
            ).stream().map(ProjectTopic::getId).collect(Collectors.toList());
            if (topicIds.isEmpty()) {
                wrapper.eq(TopicApplication::getId, -1L);
                return;
            }
            wrapper.in(TopicApplication::getTopicId, topicIds);
            return;
        }
        wrapper.eq(TopicApplication::getId, -1L);
    }

    private boolean canViewTopicApplication(LoginUserPrincipal currentUser, TopicApplication topicApplication) {
        if (hasRole(currentUser, "ADMIN")) {
            return true;
        }
        if (hasRole(currentUser, "STUDENT")) {
            return currentUser.getUserId().equals(topicApplication.getStudentId());
        }
        if (hasRole(currentUser, "TEACHER")) {
            ProjectTopic projectTopic = projectTopicService.getById(topicApplication.getTopicId());
            return projectTopic != null && currentUser.getUserId().equals(projectTopic.getTeacherId());
        }
        return false;
    }

    private boolean hasRole(LoginUserPrincipal currentUser, String roleCode) {
        if (currentUser == null || currentUser.getUserId() == null) {
            return false;
        }
        return sysUserService.hasRole(currentUser.getUserId(), roleCode);
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

    private Result<Void> validateStudentTopicApplication(Long studentId,
                                                         Long batchId,
                                                         Long excludeApplicationId,
                                                         boolean approvalOnly) {
        if (studentId == null || batchId == null) {
            return null;
        }

        List<TopicApplication> existingApplications = topicApplicationService.list(
                Wrappers.<TopicApplication>lambdaQuery()
                        .eq(TopicApplication::getStudentId, studentId)
                        .eq(TopicApplication::getBatchId, batchId)
                        .in(TopicApplication::getStatus, "PENDING", "APPROVED")
                        .ne(excludeApplicationId != null, TopicApplication::getId, excludeApplicationId)
        );
        if (!existingApplications.isEmpty()) {
            boolean hasApproved = existingApplications.stream().anyMatch(item -> "APPROVED".equals(item.getStatus()));
            if (hasApproved) {
                return Result.fail(400, "该学生在当前批次已有已通过的选题申请，不能重复锁定其他课题");
            }
            if (!approvalOnly) {
                return Result.fail(400, "该学生在当前批次已有待审核选题申请，不能重复提交");
            }
        }

        List<ProjectGroupMember> joinedMembers = projectGroupMemberService.list(
                Wrappers.<ProjectGroupMember>lambdaQuery()
                        .eq(ProjectGroupMember::getUserId, studentId)
        );
        if (joinedMembers.isEmpty()) {
            return null;
        }

        Set<Long> joinedGroupIds = joinedMembers.stream()
                .map(ProjectGroupMember::getGroupId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (joinedGroupIds.isEmpty()) {
            return null;
        }

        boolean alreadyJoinedBatchGroup = projectGroupService.listByIds(joinedGroupIds).stream()
                .anyMatch(group -> batchId.equals(group.getBatchId()));
        if (alreadyJoinedBatchGroup) {
            return Result.fail(400, "该学生已加入当前批次下的正式项目组，不能重复申请或审批其他课题");
        }

        return null;
    }
}
