package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.edu.dto.DefenseRecordCreateDTO;
import org.example.backend.modules.edu.dto.DefenseRecordPageQueryDTO;
import org.example.backend.modules.edu.dto.DefenseRecordUpdateDTO;
import org.example.backend.modules.edu.entity.DefenseRecord;
import org.example.backend.modules.edu.entity.DefenseSchedule;
import org.example.backend.modules.edu.service.DefenseRecordService;
import org.example.backend.modules.edu.service.DefenseScheduleService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/defense-records")
public class DefenseRecordController {

    private final DefenseRecordService defenseRecordService;
    private final DefenseScheduleService defenseScheduleService;
    private final SysUserService sysUserService;

    public DefenseRecordController(DefenseRecordService defenseRecordService,
                                   DefenseScheduleService defenseScheduleService,
                                   SysUserService sysUserService) {
        this.defenseRecordService = defenseRecordService;
        this.defenseScheduleService = defenseScheduleService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<DefenseRecord>> findAll() {
        return Result.success(defenseRecordService.list());
    }

    @GetMapping("/page")
    public Result<Page<DefenseRecord>> page(@Valid @ModelAttribute DefenseRecordPageQueryDTO query) {
        Page<DefenseRecord> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<DefenseRecord> wrapper = Wrappers.lambdaQuery();

        if (query.getScheduleId() != null) {
            wrapper.eq(DefenseRecord::getScheduleId, query.getScheduleId());
        }
        if (query.getTeacherId() != null) {
            wrapper.eq(DefenseRecord::getTeacherId, query.getTeacherId());
        }

        wrapper.orderByDesc(DefenseRecord::getCreateTime);
        return Result.success(defenseRecordService.page(page, wrapper));
    }

    @PostMapping
    public Result<DefenseRecord> create(@Valid @RequestBody DefenseRecordCreateDTO dto) {
        DefenseSchedule defenseSchedule = defenseScheduleService.getById(dto.getScheduleId());
        if (defenseSchedule == null) {
            return Result.fail(404, "答辩安排不存在");
        }

        if (sysUserService.getById(dto.getTeacherId()) == null) {
            return Result.fail(404, "教师不存在");
        }
        if (!sysUserService.hasRole(dto.getTeacherId(), "TEACHER")) {
            return Result.fail(400, "当前用户不是教师角色，不能新增答辩记录");
        }

        DefenseRecord defenseRecord = new DefenseRecord();
        defenseRecord.setScheduleId(dto.getScheduleId());
        defenseRecord.setTeacherId(dto.getTeacherId());
        defenseRecord.setPresentationScore(dto.getPresentationScore());
        defenseRecord.setAnswerScore(dto.getAnswerScore());
        defenseRecord.setCompletionScore(dto.getCompletionScore());
        defenseRecord.setTotalScore(dto.getTotalScore());
        defenseRecord.setComment(dto.getComment());
        defenseRecordService.save(defenseRecord);
        return Result.success(defenseRecord);
    }

    @GetMapping("/{id}")
    public Result<DefenseRecord> findById(@PathVariable Long id) {
        DefenseRecord defenseRecord = defenseRecordService.getById(id);
        if (defenseRecord == null) {
            return Result.fail(404, "答辩记录不存在");
        }
        return Result.success(defenseRecord);
    }

    @PutMapping
    public Result<DefenseRecord> update(@Valid @RequestBody DefenseRecordUpdateDTO dto) {
        DefenseRecord existing = defenseRecordService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "答辩记录不存在");
        }

        DefenseSchedule defenseSchedule = defenseScheduleService.getById(dto.getScheduleId());
        if (defenseSchedule == null) {
            return Result.fail(404, "答辩安排不存在");
        }

        if (sysUserService.getById(dto.getTeacherId()) == null) {
            return Result.fail(404, "教师不存在");
        }
        if (!sysUserService.hasRole(dto.getTeacherId(), "TEACHER")) {
            return Result.fail(400, "当前用户不是教师角色，不能修改答辩记录");
        }

        DefenseRecord defenseRecord = new DefenseRecord();
        defenseRecord.setId(dto.getId());
        defenseRecord.setScheduleId(dto.getScheduleId());
        defenseRecord.setTeacherId(dto.getTeacherId());
        defenseRecord.setPresentationScore(dto.getPresentationScore());
        defenseRecord.setAnswerScore(dto.getAnswerScore());
        defenseRecord.setCompletionScore(dto.getCompletionScore());
        defenseRecord.setTotalScore(dto.getTotalScore());
        defenseRecord.setComment(dto.getComment());
        defenseRecordService.updateById(defenseRecord);
        return Result.success(defenseRecordService.getById(dto.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        DefenseRecord existing = defenseRecordService.getById(id);
        if (existing == null) {
            return Result.fail(404, "答辩记录不存在");
        }
        return Result.success(defenseRecordService.removeById(id));
    }
}