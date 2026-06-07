package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.edu.dto.DefenseScheduleCreateDTO;
import org.example.backend.modules.edu.dto.DefenseSchedulePageQueryDTO;
import org.example.backend.modules.edu.dto.DefenseScheduleUpdateDTO;
import org.example.backend.modules.edu.entity.DefenseSchedule;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.service.DefenseScheduleService;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/defense-schedules")
public class DefenseScheduleController {

    private final DefenseScheduleService defenseScheduleService;
    private final TrainingBatchService trainingBatchService;
    private final ProjectGroupService projectGroupService;

    public DefenseScheduleController(DefenseScheduleService defenseScheduleService,
                                     TrainingBatchService trainingBatchService,
                                     ProjectGroupService projectGroupService) {
        this.defenseScheduleService = defenseScheduleService;
        this.trainingBatchService = trainingBatchService;
        this.projectGroupService = projectGroupService;
    }

    @GetMapping
    public Result<List<DefenseSchedule>> findAll() {
        return Result.success(defenseScheduleService.list());
    }

    @GetMapping("/page")
    public Result<Page<DefenseSchedule>> page(@Valid @ModelAttribute DefenseSchedulePageQueryDTO query) {
        Page<DefenseSchedule> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<DefenseSchedule> wrapper = Wrappers.lambdaQuery();

        if (query.getBatchId() != null) {
            wrapper.eq(DefenseSchedule::getBatchId, query.getBatchId());
        }
        if (query.getGroupId() != null) {
            wrapper.eq(DefenseSchedule::getGroupId, query.getGroupId());
        }
        if (query.getOrderNo() != null) {
            wrapper.eq(DefenseSchedule::getOrderNo, query.getOrderNo());
        }
        if (query.getStatus() != null) {
            wrapper.eq(DefenseSchedule::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(DefenseSchedule::getDefenseDate)
                .orderByAsc(DefenseSchedule::getDefenseTime);
        return Result.success(defenseScheduleService.page(page, wrapper));
    }

    @PostMapping
    public Result<DefenseSchedule> create(@Valid @RequestBody DefenseScheduleCreateDTO dto) {
        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }

        ProjectGroup projectGroup = projectGroupService.getById(dto.getGroupId());
        if (projectGroup == null) {
            return Result.fail(404, "项目组不存在");
        }

        if (!dto.getBatchId().equals(projectGroup.getBatchId())) {
            return Result.fail(400, "项目组不属于当前实训批次");
        }

        DefenseSchedule defenseSchedule = new DefenseSchedule();
        defenseSchedule.setBatchId(dto.getBatchId());
        defenseSchedule.setGroupId(dto.getGroupId());
        defenseSchedule.setDefenseDate(dto.getDefenseDate());
        defenseSchedule.setDefenseTime(dto.getDefenseTime());
        defenseSchedule.setLocation(dto.getLocation());
        defenseSchedule.setOrderNo(dto.getOrderNo());
        defenseSchedule.setStatus(dto.getStatus());
        defenseScheduleService.save(defenseSchedule);
        return Result.success(defenseSchedule);
    }

    @GetMapping("/{id}")
    public Result<DefenseSchedule> findById(@PathVariable Long id) {
        DefenseSchedule defenseSchedule = defenseScheduleService.getById(id);
        if (defenseSchedule == null) {
            return Result.fail(404, "答辩安排不存在");
        }
        return Result.success(defenseSchedule);
    }

    @PutMapping
    public Result<DefenseSchedule> update(@Valid @RequestBody DefenseScheduleUpdateDTO dto) {
        DefenseSchedule existing = defenseScheduleService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "答辩安排不存在");
        }

        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }

        ProjectGroup projectGroup = projectGroupService.getById(dto.getGroupId());
        if (projectGroup == null) {
            return Result.fail(404, "项目组不存在");
        }

        if (!dto.getBatchId().equals(projectGroup.getBatchId())) {
            return Result.fail(400, "项目组不属于当前实训批次");
        }

        DefenseSchedule defenseSchedule = new DefenseSchedule();
        defenseSchedule.setId(dto.getId());
        defenseSchedule.setBatchId(dto.getBatchId());
        defenseSchedule.setGroupId(dto.getGroupId());
        defenseSchedule.setDefenseDate(dto.getDefenseDate());
        defenseSchedule.setDefenseTime(dto.getDefenseTime());
        defenseSchedule.setLocation(dto.getLocation());
        defenseSchedule.setOrderNo(dto.getOrderNo());
        defenseSchedule.setStatus(dto.getStatus());
        defenseScheduleService.updateById(defenseSchedule);
        return Result.success(defenseScheduleService.getById(dto.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        DefenseSchedule existing = defenseScheduleService.getById(id);
        if (existing == null) {
            return Result.fail(404, "答辩安排不存在");
        }
        return Result.success(defenseScheduleService.removeById(id));
    }
}