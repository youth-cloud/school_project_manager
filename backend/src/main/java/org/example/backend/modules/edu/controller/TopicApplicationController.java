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
import java.util.stream.Collectors;

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

        TopicApplication topicApplication = new TopicApplication();
        topicApplication.setId(dto.getId());
        topicApplication.setStatus(dto.getStatus());
        topicApplication.setReviewerId(currentUser.getUserId());
        topicApplication.setReviewComment(dto.getReviewComment());
        topicApplication.setReviewTime(LocalDateTime.now());
        topicApplicationService.updateById(topicApplication);
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
}
