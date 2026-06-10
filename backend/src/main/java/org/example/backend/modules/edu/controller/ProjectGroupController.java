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
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.ProjectGroupMember;
import org.example.backend.modules.edu.entity.ProjectTopic;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.service.ProjectGroupMemberService;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.edu.service.ProjectTopicService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.example.backend.modules.system.service.SysUserService;
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
    private final SysUserService sysUserService;

    public ProjectGroupController(ProjectGroupService projectGroupService,
                                  TrainingBatchService trainingBatchService,
                                  ProjectTopicService projectTopicService,
                                  ProjectGroupMemberService projectGroupMemberService,
                                  SysUserService sysUserService) {
        this.projectGroupService = projectGroupService;
        this.trainingBatchService = trainingBatchService;
        this.projectTopicService = projectTopicService;
        this.projectGroupMemberService = projectGroupMemberService;
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

        LambdaQueryWrapper<ProjectGroup> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ProjectGroup::getTopicId, dto.getTopicId());
        if (projectGroupService.count(wrapper) > 0) {
            return Result.fail(400, "该课题已创建项目组");
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
        if (sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return Result.success(projectGroupService.removeById(id));
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return Result.fail(403, "当前用户没有删除项目组的权限");
        }
        if (!currentUser.getUserId().equals(existing.getTeacherId())) {
            return Result.fail(403, "当前教师不是该项目组指导教师，不能删除项目组");
        }
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
}
