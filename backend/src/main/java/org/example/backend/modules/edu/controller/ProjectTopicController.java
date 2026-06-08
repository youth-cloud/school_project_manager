package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.ProjectTopicCreateDTO;
import org.example.backend.modules.edu.dto.ProjectTopicPageQueryDTO;
import org.example.backend.modules.edu.dto.ProjectTopicUpdateDTO;
import org.example.backend.modules.edu.entity.ProjectTopic;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.service.ProjectTopicService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-topics")
public class ProjectTopicController {

    private final ProjectTopicService projectTopicService;
    private final TrainingBatchService trainingBatchService;
    private final SysUserService sysUserService;

    public ProjectTopicController(ProjectTopicService projectTopicService,
                                  TrainingBatchService trainingBatchService,
                                  SysUserService sysUserService) {
        this.projectTopicService = projectTopicService;
        this.trainingBatchService = trainingBatchService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<ProjectTopic>> findAll() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        LambdaQueryWrapper<ProjectTopic> wrapper = Wrappers.lambdaQuery();
        applyProjectTopicViewScope(wrapper, currentUser);
        wrapper.orderByDesc(ProjectTopic::getCreateTime);
        return Result.success(projectTopicService.list(wrapper));
    }

    @GetMapping("/page")
    public Result<Page<ProjectTopic>> page(@Valid @ModelAttribute ProjectTopicPageQueryDTO query) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        Page<ProjectTopic> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<ProjectTopic> wrapper = Wrappers.lambdaQuery();

        if (StringUtils.hasText(query.getTopicName())) {
            wrapper.like(ProjectTopic::getTopicName, query.getTopicName());
        }
        if (query.getBatchId() != null) {
            wrapper.eq(ProjectTopic::getBatchId, query.getBatchId());
        }
        if (query.getTeacherId() != null) {
            wrapper.eq(ProjectTopic::getTeacherId, query.getTeacherId());
        }
        if (query.getDifficultyLevel() != null) {
            wrapper.eq(ProjectTopic::getDifficultyLevel, query.getDifficultyLevel());
        }
        if (query.getStatus() != null) {
            wrapper.eq(ProjectTopic::getStatus, query.getStatus());
        }

        applyProjectTopicViewScope(wrapper, currentUser);
        wrapper.orderByDesc(ProjectTopic::getCreateTime);
        return Result.success(projectTopicService.page(page, wrapper));
    }

    @PostMapping
    public Result<ProjectTopic> create(@Valid @RequestBody ProjectTopicCreateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return Result.fail(403, "当前用户不是教师角色，不能发布课题");
        }

        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }
        if (!currentUser.getUserId().equals(trainingBatch.getTeacherId())) {
            return Result.fail(403, "当前教师不是该实训批次绑定教师，不能发布课题");
        }

        ProjectTopic projectTopic = new ProjectTopic();
        projectTopic.setBatchId(dto.getBatchId());
        projectTopic.setTeacherId(currentUser.getUserId());
        projectTopic.setTopicName(dto.getTopicName());
        projectTopic.setTopicDescription(dto.getTopicDescription());
        projectTopic.setDifficultyLevel(dto.getDifficultyLevel());
        projectTopic.setTechRequirements(dto.getTechRequirements());
        projectTopic.setMaxMembers(dto.getMaxMembers());
        projectTopic.setSelectedCount(0);
        projectTopic.setStatus(dto.getStatus());
        projectTopicService.save(projectTopic);
        return Result.success(projectTopic);
    }

    @GetMapping("/{id}")
    public Result<ProjectTopic> findById(@PathVariable Long id) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        ProjectTopic projectTopic = projectTopicService.getById(id);
        if (projectTopic == null) {
            return Result.fail(404,"课题不存在");
        }
        if (!canViewProjectTopic(currentUser, projectTopic)) {
            return Result.fail(403, "当前用户无权查看该课题");
        }
        return Result.success(projectTopic);
    }

    @PutMapping
    public Result<ProjectTopic> update(@Valid @RequestBody ProjectTopicUpdateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return Result.fail(403, "当前用户不是教师角色，不能修改课题");
        }

        ProjectTopic existing = projectTopicService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "课题不存在");
        }

        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }
        if (!currentUser.getUserId().equals(trainingBatch.getTeacherId())) {
            return Result.fail(403, "当前教师不是该实训批次绑定教师，不能修改课题");
        }

        ProjectTopic projectTopic = new ProjectTopic();
        projectTopic.setId(dto.getId());
        projectTopic.setBatchId(dto.getBatchId());
        projectTopic.setTeacherId(existing.getTeacherId());
        projectTopic.setTopicName(dto.getTopicName());
        projectTopic.setTopicDescription(dto.getTopicDescription());
        projectTopic.setDifficultyLevel(dto.getDifficultyLevel());
        projectTopic.setTechRequirements(dto.getTechRequirements());
        projectTopic.setMaxMembers(dto.getMaxMembers());
        projectTopic.setStatus(dto.getStatus());
        projectTopicService.updateById(projectTopic);
        return Result.success(projectTopicService.getById(dto.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        ProjectTopic existing = projectTopicService.getById(id);
        if (existing == null) {
            return Result.fail(404,"课题不存在");
        }
        if (sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return Result.success(projectTopicService.removeById(id));
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return Result.fail(403, "当前用户没有删除课题的权限");
        }
        if (!currentUser.getUserId().equals(existing.getTeacherId())) {
            return Result.fail(403, "当前教师不是该课题发布教师，不能删除课题");
        }
        return Result.success(projectTopicService.removeById(id));
    }

    private void applyProjectTopicViewScope(LambdaQueryWrapper<ProjectTopic> wrapper, LoginUserPrincipal currentUser) {
        if (sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return;
        }
        if (sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            wrapper.eq(ProjectTopic::getTeacherId, currentUser.getUserId());
            return;
        }
        wrapper.eq(ProjectTopic::getStatus, 1);
    }

    private boolean canViewProjectTopic(LoginUserPrincipal currentUser, ProjectTopic projectTopic) {
        if (sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return true;
        }
        if (sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return currentUser.getUserId().equals(projectTopic.getTeacherId());
        }
        return Integer.valueOf(1).equals(projectTopic.getStatus());
    }
}
