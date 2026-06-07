package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.edu.dto.ProjectGroupMemberCreateDTO;
import org.example.backend.modules.edu.dto.ProjectGroupMemberPageQueryDTO;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.ProjectGroupMember;
import org.example.backend.modules.edu.service.ProjectGroupMemberService;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return Result.success(projectGroupMemberService.list());
    }

    @GetMapping("/page")
    public Result<Page<ProjectGroupMember>> page(@Valid @ModelAttribute ProjectGroupMemberPageQueryDTO query) {
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

        wrapper.orderByDesc(ProjectGroupMember::getJoinTime);
        return Result.success(projectGroupMemberService.page(page, wrapper));
    }

    @PostMapping
    public Result<ProjectGroupMember> create(@Valid @RequestBody ProjectGroupMemberCreateDTO dto) {
        ProjectGroup projectGroup = projectGroupService.getById(dto.getGroupId());
        if (projectGroup == null) {
            return Result.fail(404, "项目组不存在");
        }
        if (sysUserService.getById(dto.getUserId()) == null) {
            return Result.fail(404, "用户不存在");
        }
        if (!sysUserService.hasRole(dto.getUserId(), "STUDENT")) {
            return Result.fail(400, "当前用户不是学生角色，不能加入项目组");
        }

        LambdaQueryWrapper<ProjectGroupMember> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ProjectGroupMember::getGroupId, dto.getGroupId())
                .eq(ProjectGroupMember::getUserId, dto.getUserId());
        if (projectGroupMemberService.count(wrapper) > 0) {
            return Result.fail(400, "该用户已在当前项目组中");
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
        projectGroupMember.setUserId(dto.getUserId());
        projectGroupMember.setIsLeader(dto.getIsLeader());
        projectGroupMember.setStatus(dto.getStatus());
        projectGroupMemberService.save(projectGroupMember);
        return Result.success(projectGroupMember);
    }

    @GetMapping("/{id}")
    public Result<ProjectGroupMember> findById(@PathVariable Long id){
        ProjectGroupMember projectGroupMember = projectGroupMemberService.getById(id);
        if (projectGroupMember == null) {
            return Result.fail(404,"项目组成员不存在");
        }
        return Result.success(projectGroupMember);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id){
        ProjectGroupMember existing=projectGroupMemberService.getById(id);
        if (existing == null) {
            return Result.fail(404,"项目组成员不存在");
        }
        return Result.success(projectGroupMemberService.removeById(id));
    }
}
