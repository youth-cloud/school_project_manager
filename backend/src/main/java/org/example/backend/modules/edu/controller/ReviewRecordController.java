package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.edu.dto.ReviewRecordCreateDTO;
import org.example.backend.modules.edu.dto.ReviewRecordPageQueryDTO;
import org.example.backend.modules.edu.dto.ReviewRecordUpdateDTO;
import org.example.backend.modules.edu.entity.ReviewRecord;
import org.example.backend.modules.edu.entity.StageSubmission;
import org.example.backend.modules.edu.service.ReviewRecordService;
import org.example.backend.modules.edu.service.StageSubmissionService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/review-records")
public class ReviewRecordController {

    private final ReviewRecordService reviewRecordService;
    private final StageSubmissionService stageSubmissionService;
    private final SysUserService sysUserService;

    public ReviewRecordController(ReviewRecordService reviewRecordService,
                                  StageSubmissionService stageSubmissionService,
                                  SysUserService sysUserService) {
        this.reviewRecordService = reviewRecordService;
        this.stageSubmissionService = stageSubmissionService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<ReviewRecord>> findAll() {
        return Result.success(reviewRecordService.list());
    }

    @GetMapping("/page")
    public Result<Page<ReviewRecord>> page(@Valid @ModelAttribute ReviewRecordPageQueryDTO query) {
        Page<ReviewRecord> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<ReviewRecord> wrapper = Wrappers.lambdaQuery();

        if (query.getSubmissionId() != null) {
            wrapper.eq(ReviewRecord::getSubmissionId, query.getSubmissionId());
        }
        if (query.getReviewerId() != null) {
            wrapper.eq(ReviewRecord::getReviewerId, query.getReviewerId());
        }
        if (StringUtils.hasText(query.getReviewResult())) {
            wrapper.eq(ReviewRecord::getReviewResult, query.getReviewResult());
        }

        wrapper.orderByDesc(ReviewRecord::getReviewTime);
        return Result.success(reviewRecordService.page(page, wrapper));
    }

    @PostMapping
    public Result<ReviewRecord> create(@Valid @RequestBody ReviewRecordCreateDTO dto) {
        StageSubmission stageSubmission = stageSubmissionService.getById(dto.getSubmissionId());
        if (stageSubmission == null) {
            return Result.fail(404, "阶段提交不存在");
        }
        if (sysUserService.getById(dto.getReviewerId()) == null) {
            return Result.fail(404, "审核人不存在");
        }
        if (!sysUserService.hasRole(dto.getReviewerId(), "TEACHER")) {
            return Result.fail(400, "当前用户不是教师角色，不能新增审核记录");
        }

        ReviewRecord reviewRecord = new ReviewRecord();
        reviewRecord.setSubmissionId(dto.getSubmissionId());
        reviewRecord.setReviewerId(dto.getReviewerId());
        reviewRecord.setReviewResult(dto.getReviewResult());
        reviewRecord.setScore(dto.getScore());
        reviewRecord.setComment(dto.getComment());
        reviewRecord.setReviewTime(LocalDateTime.now());
        reviewRecordService.save(reviewRecord);
        return Result.success(reviewRecord);
    }

    @GetMapping("/{id}")
    public Result<ReviewRecord> findById(@PathVariable Long id) {
        ReviewRecord reviewRecord = reviewRecordService.getById(id);
        if (reviewRecord == null) {
            return Result.fail(404, "审核记录不存在");
        }
        return Result.success(reviewRecord);
    }

    @PutMapping
    public Result<ReviewRecord> update(@Valid @RequestBody ReviewRecordUpdateDTO dto) {
        ReviewRecord existing = reviewRecordService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "审核记录不存在");
        }

        StageSubmission stageSubmission = stageSubmissionService.getById(dto.getSubmissionId());
        if (stageSubmission == null) {
            return Result.fail(404, "阶段提交不存在");
        }
        if (sysUserService.getById(dto.getReviewerId()) == null) {
            return Result.fail(404, "审核人不存在");
        }
        if (!sysUserService.hasRole(dto.getReviewerId(), "TEACHER")) {
            return Result.fail(400, "当前用户不是教师角色，不能修改审核记录");
        }

        ReviewRecord reviewRecord = new ReviewRecord();
        reviewRecord.setId(dto.getId());
        reviewRecord.setSubmissionId(dto.getSubmissionId());
        reviewRecord.setReviewerId(dto.getReviewerId());
        reviewRecord.setReviewResult(dto.getReviewResult());
        reviewRecord.setScore(dto.getScore());
        reviewRecord.setComment(dto.getComment());
        reviewRecordService.updateById(reviewRecord);
        return Result.success(reviewRecordService.getById(dto.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        ReviewRecord existing = reviewRecordService.getById(id);
        if (existing == null) {
            return Result.fail(404, "审核记录不存在");
        }
        return Result.success(reviewRecordService.removeById(id));
    }
}