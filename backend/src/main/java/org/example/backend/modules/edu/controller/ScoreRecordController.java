package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.edu.dto.ScoreRecordCreateDTO;
import org.example.backend.modules.edu.dto.ScoreRecordPageQueryDTO;
import org.example.backend.modules.edu.dto.ScoreRecordUpdateDTO;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.ScoreRecord;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.edu.service.ScoreRecordService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/score-records")
public class ScoreRecordController {

    private final ScoreRecordService scoreRecordService;
    private final TrainingBatchService trainingBatchService;
    private final ProjectGroupService projectGroupService;
    private final SysUserService sysUserService;

    public ScoreRecordController(ScoreRecordService scoreRecordService,
                                 TrainingBatchService trainingBatchService,
                                 ProjectGroupService projectGroupService,
                                 SysUserService sysUserService) {
        this.scoreRecordService = scoreRecordService;
        this.trainingBatchService = trainingBatchService;
        this.projectGroupService = projectGroupService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<ScoreRecord>> findAll() {
        return Result.success(scoreRecordService.list());
    }

    @GetMapping("/page")
    public Result<Page<ScoreRecord>> page(@Valid @ModelAttribute ScoreRecordPageQueryDTO query) {
        Page<ScoreRecord> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<ScoreRecord> wrapper = Wrappers.lambdaQuery();

        if (query.getBatchId() != null) {
            wrapper.eq(ScoreRecord::getBatchId, query.getBatchId());
        }
        if (query.getGroupId() != null) {
            wrapper.eq(ScoreRecord::getGroupId, query.getGroupId());
        }
        if (query.getStudentId() != null) {
            wrapper.eq(ScoreRecord::getStudentId, query.getStudentId());
        }
        if (StringUtils.hasText(query.getGradeLevel())) {
            wrapper.eq(ScoreRecord::getGradeLevel, query.getGradeLevel());
        }

        wrapper.orderByDesc(ScoreRecord::getCreateTime);
        return Result.success(scoreRecordService.page(page, wrapper));
    }

    @PostMapping
    public Result<ScoreRecord> create(@Valid @RequestBody ScoreRecordCreateDTO dto) {
        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }

        if (sysUserService.getById(dto.getStudentId()) == null) {
            return Result.fail(404, "学生不存在");
        }
        if (!sysUserService.hasRole(dto.getStudentId(), "STUDENT")) {
            return Result.fail(400, "当前用户不是学生角色，不能生成该成绩记录");
        }

        if (dto.getGroupId() != null) {
            ProjectGroup projectGroup = projectGroupService.getById(dto.getGroupId());
            if (projectGroup == null) {
                return Result.fail(404, "项目组不存在");
            }
            if (!dto.getBatchId().equals(projectGroup.getBatchId())) {
                return Result.fail(400, "项目组不属于当前实训批次");
            }
        }

        ScoreRecord scoreRecord = new ScoreRecord();
        scoreRecord.setBatchId(dto.getBatchId());
        scoreRecord.setGroupId(dto.getGroupId());
        scoreRecord.setStudentId(dto.getStudentId());
        scoreRecord.setProcessScore(dto.getProcessScore());
        scoreRecord.setReportScore(dto.getReportScore());
        scoreRecord.setSubmissionScore(dto.getSubmissionScore());
        scoreRecord.setDefenseScore(dto.getDefenseScore());
        scoreRecord.setFinalScore(dto.getFinalScore());
        scoreRecord.setGradeLevel(dto.getGradeLevel());
        scoreRecord.setRemark(dto.getRemark());
        scoreRecordService.save(scoreRecord);
        return Result.success(scoreRecord);
    }

    @GetMapping("/{id}")
    public Result<ScoreRecord> findById(@PathVariable Long id) {
        ScoreRecord scoreRecord = scoreRecordService.getById(id);
        if (scoreRecord == null) {
            return Result.fail(404, "成绩记录不存在");
        }
        return Result.success(scoreRecord);
    }

    @PutMapping
    public Result<ScoreRecord> update(@Valid @RequestBody ScoreRecordUpdateDTO dto) {
        ScoreRecord existing = scoreRecordService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "成绩记录不存在");
        }

        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }

        if (sysUserService.getById(dto.getStudentId()) == null) {
            return Result.fail(404, "学生不存在");
        }
        if (!sysUserService.hasRole(dto.getStudentId(), "STUDENT")) {
            return Result.fail(400, "当前用户不是学生角色，不能修改该成绩记录");
        }

        if (dto.getGroupId() != null) {
            ProjectGroup projectGroup = projectGroupService.getById(dto.getGroupId());
            if (projectGroup == null) {
                return Result.fail(404, "项目组不存在");
            }
            if (!dto.getBatchId().equals(projectGroup.getBatchId())) {
                return Result.fail(400, "项目组不属于当前实训批次");
            }
        }

        ScoreRecord scoreRecord = new ScoreRecord();
        scoreRecord.setId(dto.getId());
        scoreRecord.setBatchId(dto.getBatchId());
        scoreRecord.setGroupId(dto.getGroupId());
        scoreRecord.setStudentId(dto.getStudentId());
        scoreRecord.setProcessScore(dto.getProcessScore());
        scoreRecord.setReportScore(dto.getReportScore());
        scoreRecord.setSubmissionScore(dto.getSubmissionScore());
        scoreRecord.setDefenseScore(dto.getDefenseScore());
        scoreRecord.setFinalScore(dto.getFinalScore());
        scoreRecord.setGradeLevel(dto.getGradeLevel());
        scoreRecord.setRemark(dto.getRemark());
        scoreRecordService.updateById(scoreRecord);
        return Result.success(scoreRecordService.getById(dto.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        ScoreRecord existing = scoreRecordService.getById(id);
        if (existing == null) {
            return Result.fail(404, "成绩记录不存在");
        }
        return Result.success(scoreRecordService.removeById(id));
    }
}