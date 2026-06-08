package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.ProjectGroupMemberCreateDTO;
import org.example.backend.modules.edu.dto.ProjectGroupMemberPageQueryDTO;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.ProjectGroupMember;
import org.example.backend.modules.edu.service.ProjectGroupMemberService;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/project-group-members")
public class ProjectGroupMemberController {
    private final ProjectGroupMemberService projectGroupMemberService;
    private final ProjectGroupService projectGroupService;
    private final SysUserService sysUserService;

    public ProjectGroupMemberController(ProjectGroupMemberService projectGroupMemberService,
                                        ProjectGroupService projectGroupService,
                                        SysUserService sysUserService) {
        this.projectGroupMemberService = projectGroupMemberService;
        this.projectGroupService = projectGroupService;
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
        if (sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return Result.success(projectGroupMemberService.removeById(id));
        }
        if (sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            ProjectGroup projectGroup = projectGroupService.getById(existing.getGroupId());
            if (projectGroup == null) {
                return Result.fail(404, "关联项目组不存在");
            }
            if (!currentUser.getUserId().equals(projectGroup.getTeacherId())) {
                return Result.fail(403, "当前教师不是该项目组指导教师，不能删除成员记录");
            }
            return Result.success(projectGroupMemberService.removeById(id));
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "STUDENT")) {
            return Result.fail(403, "当前用户没有删除项目组成员的权限");
        }
        if (!currentUser.getUserId().equals(existing.getUserId())) {
            return Result.fail(403, "学生只能删除自己的成员记录");
        }
        return Result.success(projectGroupMemberService.removeById(id));
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
}
