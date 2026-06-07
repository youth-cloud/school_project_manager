package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.edu.dto.WeeklyReportCreateDTO;
import org.example.backend.modules.edu.dto.WeeklyReportPageQueryDTO;
import org.example.backend.modules.edu.dto.WeeklyReportUpdateDTO;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.entity.WeeklyReport;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.example.backend.modules.edu.service.WeeklyReportService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/weekly-reports")
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;
    private final TrainingBatchService trainingBatchService;
    private final ProjectGroupService projectGroupService;
    private final SysUserService sysUserService;

    public WeeklyReportController(WeeklyReportService weeklyReportService,
                                  TrainingBatchService trainingBatchService,
                                  ProjectGroupService projectGroupService,
                                  SysUserService sysUserService) {
        this.weeklyReportService = weeklyReportService;
        this.trainingBatchService = trainingBatchService;
        this.projectGroupService = projectGroupService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<WeeklyReport>> findAll() {
        return Result.success(weeklyReportService.list());
    }

    @GetMapping("/page")
    public Result<Page<WeeklyReport>> page(@Valid @ModelAttribute WeeklyReportPageQueryDTO query) {
        Page<WeeklyReport> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<WeeklyReport> wrapper = Wrappers.lambdaQuery();

        if (query.getBatchId() != null) {
            wrapper.eq(WeeklyReport::getBatchId, query.getBatchId());
        }
        if (query.getGroupId() != null) {
            wrapper.eq(WeeklyReport::getGroupId, query.getGroupId());
        }
        if (query.getStudentId() != null) {
            wrapper.eq(WeeklyReport::getStudentId, query.getStudentId());
        }
        if (query.getWeekIndex() != null) {
            wrapper.eq(WeeklyReport::getWeekIndex, query.getWeekIndex());
        }
        if (query.getStatus() != null) {
            wrapper.eq(WeeklyReport::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(WeeklyReport::getSubmitTime);
        return Result.success(weeklyReportService.page(page, wrapper));
    }

    @PostMapping
    public Result<WeeklyReport> create(@Valid @RequestBody WeeklyReportCreateDTO dto) {
        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }

        ProjectGroup projectGroup = projectGroupService.getById(dto.getGroupId());
        if (projectGroup == null) {
            return Result.fail(404, "项目组不存在");
        }

        if (sysUserService.getById(dto.getStudentId()) == null) {
            return Result.fail(404, "学生不存在");
        }
        if (!sysUserService.hasRole(dto.getStudentId(), "STUDENT")) {
            return Result.fail(400, "当前用户不是学生角色，不能提交周报");
        }

        if (!dto.getBatchId().equals(projectGroup.getBatchId())) {
            return Result.fail(400, "项目组不属于当前实训批次");
        }

        WeeklyReport weeklyReport = new WeeklyReport();
        weeklyReport.setBatchId(dto.getBatchId());
        weeklyReport.setGroupId(dto.getGroupId());
        weeklyReport.setStudentId(dto.getStudentId());
        weeklyReport.setWeekIndex(dto.getWeekIndex());
        weeklyReport.setCompletedWork(dto.getCompletedWork());
        weeklyReport.setProblemDesc(dto.getProblemDesc());
        weeklyReport.setNextPlan(dto.getNextPlan());
        weeklyReport.setStatus(dto.getStatus());
        weeklyReport.setSubmitTime(LocalDateTime.now());
        weeklyReportService.save(weeklyReport);
        return Result.success(weeklyReport);
    }

    @GetMapping("/{id}")
    public Result<WeeklyReport> findById(@PathVariable Long id) {
        WeeklyReport weeklyReport = weeklyReportService.getById(id);
        if (weeklyReport == null) {
            return Result.fail(404, "周报不存在");
        }
        return Result.success(weeklyReport);
    }

    @PutMapping
    public Result<WeeklyReport> update(@Valid @RequestBody WeeklyReportUpdateDTO dto) {
        WeeklyReport existing = weeklyReportService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "周报不存在");
        }

        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }

        ProjectGroup projectGroup = projectGroupService.getById(dto.getGroupId());
        if (projectGroup == null) {
            return Result.fail(404, "项目组不存在");
        }

        if (sysUserService.getById(dto.getStudentId()) == null) {
            return Result.fail(404, "学生不存在");
        }
        if (!sysUserService.hasRole(dto.getStudentId(), "STUDENT")) {
            return Result.fail(400, "当前用户不是学生角色，不能修改周报");
        }

        if (!dto.getBatchId().equals(projectGroup.getBatchId())) {
            return Result.fail(400, "项目组不属于当前实训批次");
        }

        WeeklyReport weeklyReport = new WeeklyReport();
        weeklyReport.setId(dto.getId());
        weeklyReport.setBatchId(dto.getBatchId());
        weeklyReport.setGroupId(dto.getGroupId());
        weeklyReport.setStudentId(dto.getStudentId());
        weeklyReport.setWeekIndex(dto.getWeekIndex());
        weeklyReport.setCompletedWork(dto.getCompletedWork());
        weeklyReport.setProblemDesc(dto.getProblemDesc());
        weeklyReport.setNextPlan(dto.getNextPlan());
        weeklyReport.setStatus(dto.getStatus());
        weeklyReportService.updateById(weeklyReport);
        return Result.success(weeklyReportService.getById(dto.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        WeeklyReport existing = weeklyReportService.getById(id);
        if (existing == null) {
            return Result.fail(404, "周报不存在");
        }
        return Result.success(weeklyReportService.removeById(id));
    }
}