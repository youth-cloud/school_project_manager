package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
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
        return Result.success(projectTopicService.list());
    }

    @GetMapping("/page")
    public Result<Page<ProjectTopic>> page(@Valid @ModelAttribute ProjectTopicPageQueryDTO query) {
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

        wrapper.orderByDesc(ProjectTopic::getCreateTime);
        return Result.success(projectTopicService.page(page, wrapper));
    }

    @PostMapping
    public Result<ProjectTopic> create(@Valid @RequestBody ProjectTopicCreateDTO dto) {
        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }
        if (sysUserService.getById(dto.getTeacherId()) == null) {
            return Result.fail(404, "教师不存在");
        }
        if (!sysUserService.hasRole(dto.getTeacherId(), "TEACHER")) {
            return Result.fail(400, "当前用户不是教师角色，不能发布课题");
        }
        if (!dto.getTeacherId().equals(trainingBatch.getTeacherId())) {
            return Result.fail(400, "课题发布教师与实训批次教师不一致");
        }

        ProjectTopic projectTopic = new ProjectTopic();
        projectTopic.setBatchId(dto.getBatchId());
        projectTopic.setTeacherId(dto.getTeacherId());
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
        ProjectTopic projectTopic = projectTopicService.getById(id);
        if (projectTopic == null) {
            return Result.fail(404,"课题不存在");
        }
        return Result.success(projectTopic);
    }

    @PutMapping
    public Result<ProjectTopic> update(@Valid @RequestBody ProjectTopicUpdateDTO dto) {
        ProjectTopic existing = projectTopicService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "课题不存在");
        }

        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }
        if (sysUserService.getById(dto.getTeacherId()) == null) {
            return Result.fail(404, "教师不存在");
        }
        if (!sysUserService.hasRole(dto.getTeacherId(), "TEACHER")) {
            return Result.fail(400, "当前用户不是教师角色，不能修改课题");
        }
        if (!dto.getTeacherId().equals(trainingBatch.getTeacherId())) {
            return Result.fail(400, "课题发布教师与实训批次教师不一致");
        }

        ProjectTopic projectTopic = new ProjectTopic();
        projectTopic.setId(dto.getId());
        projectTopic.setBatchId(dto.getBatchId());
        projectTopic.setTeacherId(dto.getTeacherId());
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
        ProjectTopic existing = projectTopicService.getById(id);
        if (existing == null) {
            return Result.fail(404,"课题不存在");
        }
        return Result.success(projectTopicService.removeById(id));
    }
}
