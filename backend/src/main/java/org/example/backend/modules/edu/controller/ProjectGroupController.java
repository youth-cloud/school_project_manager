package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.edu.dto.ProjectGroupCreateDTO;
import org.example.backend.modules.edu.dto.ProjectGroupPageQueryDTO;
import org.example.backend.modules.edu.dto.ProjectGroupUpdateDTO;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.ProjectTopic;
import org.example.backend.modules.edu.entity.TrainingBatch;
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
    private final SysUserService sysUserService;

    public ProjectGroupController(ProjectGroupService projectGroupService,
                                  TrainingBatchService trainingBatchService,
                                  ProjectTopicService projectTopicService,
                                  SysUserService sysUserService) {
        this.projectGroupService = projectGroupService;
        this.trainingBatchService = trainingBatchService;
        this.projectTopicService = projectTopicService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<ProjectGroup>> findAll() {
        return Result.success(projectGroupService.list());
    }

    @GetMapping("/page")
    public Result<Page<ProjectGroup>> page(@Valid @ModelAttribute ProjectGroupPageQueryDTO query) {
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

        wrapper.orderByDesc(ProjectGroup::getCreateTime);
        return Result.success(projectGroupService.page(page, wrapper));
    }

    @PostMapping
    public Result<ProjectGroup> create(@Valid @RequestBody ProjectGroupCreateDTO dto) {
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
        if (!dto.getTeacherId().equals(projectTopic.getTeacherId())) {
            return Result.fail(400, "指导教师与课题发布教师不一致");
        }
        if (sysUserService.getById(dto.getLeaderId()) == null) {
            return Result.fail(404, "组长不存在");
        }
        if (sysUserService.getById(dto.getTeacherId()) == null) {
            return Result.fail(404, "指导教师不存在");
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
        projectGroup.setTeacherId(dto.getTeacherId());
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
        ProjectGroup projectGroup = projectGroupService.getById(id);
        if (projectGroup == null) {
            return Result.fail(404,"项目组不存在");
        }
        return Result.success(projectGroup);
    }

    @PutMapping
    public Result<ProjectGroup> update(@Valid @RequestBody ProjectGroupUpdateDTO dto) {
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
        if (!dto.getTeacherId().equals(projectTopic.getTeacherId())) {
            return Result.fail(400, "指导教师与课题发布教师不一致");
        }
        if (sysUserService.getById(dto.getLeaderId()) == null) {
            return Result.fail(404, "组长不存在");
        }
        if (sysUserService.getById(dto.getTeacherId()) == null) {
            return Result.fail(404, "指导教师不存在");
        }

        ProjectGroup projectGroup = new ProjectGroup();
        projectGroup.setId(dto.getId());
        projectGroup.setBatchId(dto.getBatchId());
        projectGroup.setTopicId(dto.getTopicId());
        projectGroup.setGroupName(dto.getGroupName());
        projectGroup.setLeaderId(dto.getLeaderId());
        projectGroup.setTeacherId(dto.getTeacherId());
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
        ProjectGroup existing = projectGroupService.getById(id);
        if (existing == null) {
            return Result.fail(404,"项目组不存在");
        }
        return Result.success(projectGroupService.removeById(id));
    }
}
