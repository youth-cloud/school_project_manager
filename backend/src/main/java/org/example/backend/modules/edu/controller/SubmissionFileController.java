package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.edu.dto.SubmissionFileCreateDTO;
import org.example.backend.modules.edu.dto.SubmissionFilePageQueryDTO;
import org.example.backend.modules.edu.dto.SubmissionFileUpdateDTO;
import org.example.backend.modules.edu.entity.SubmissionFile;
import org.example.backend.modules.edu.entity.StageSubmission;
import org.example.backend.modules.edu.service.SubmissionFileService;
import org.example.backend.modules.edu.service.StageSubmissionService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submission-files")
public class SubmissionFileController {

    private final SubmissionFileService submissionFileService;
    private final StageSubmissionService stageSubmissionService;
    private final SysUserService sysUserService;

    public SubmissionFileController(SubmissionFileService submissionFileService,
                                    StageSubmissionService stageSubmissionService,
                                    SysUserService sysUserService) {
        this.submissionFileService = submissionFileService;
        this.stageSubmissionService = stageSubmissionService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<SubmissionFile>> findAll() {
        return Result.success(submissionFileService.list());
    }

    @GetMapping("/page")
    public Result<Page<SubmissionFile>> page(@Valid @ModelAttribute SubmissionFilePageQueryDTO query) {
        Page<SubmissionFile> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<SubmissionFile> wrapper = Wrappers.lambdaQuery();

        if (query.getSubmissionId() != null) {
            wrapper.eq(SubmissionFile::getSubmissionId, query.getSubmissionId());
        }
        if (StringUtils.hasText(query.getFileType())) {
            wrapper.eq(SubmissionFile::getFileType, query.getFileType());
        }
        if (StringUtils.hasText(query.getBizType())) {
            wrapper.eq(SubmissionFile::getBizType, query.getBizType());
        }
        if (query.getUploadUserId() != null) {
            wrapper.eq(SubmissionFile::getUploadUserId, query.getUploadUserId());
        }

        wrapper.orderByDesc(SubmissionFile::getCreateTime);
        return Result.success(submissionFileService.page(page, wrapper));
    }

    @PostMapping
    public Result<SubmissionFile> create(@Valid @RequestBody SubmissionFileCreateDTO dto) {
        StageSubmission stageSubmission = stageSubmissionService.getById(dto.getSubmissionId());
        if (stageSubmission == null) {
            return Result.fail(404, "阶段提交不存在");
        }
        if (sysUserService.getById(dto.getUploadUserId()) == null) {
            return Result.fail(404, "上传人不存在");
        }

        SubmissionFile submissionFile = new SubmissionFile();
        submissionFile.setSubmissionId(dto.getSubmissionId());
        submissionFile.setFileName(dto.getFileName());
        submissionFile.setOriginalName(dto.getOriginalName());
        submissionFile.setFileType(dto.getFileType());
        submissionFile.setFileSize(dto.getFileSize());
        submissionFile.setFilePath(dto.getFilePath());
        submissionFile.setFileUrl(dto.getFileUrl());
        submissionFile.setBizType(dto.getBizType());
        submissionFile.setUploadUserId(dto.getUploadUserId());
        submissionFileService.save(submissionFile);
        return Result.success(submissionFile);
    }

    @GetMapping("/{id}")
    public Result<SubmissionFile> findById(@PathVariable Long id) {
        SubmissionFile submissionFile = submissionFileService.getById(id);
        if (submissionFile == null) {
            return Result.fail(404, "提交文件不存在");
        }
        return Result.success(submissionFile);
    }

    @PutMapping
    public Result<SubmissionFile> update(@Valid @RequestBody SubmissionFileUpdateDTO dto) {
        SubmissionFile existing = submissionFileService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "提交文件不存在");
        }

        StageSubmission stageSubmission = stageSubmissionService.getById(dto.getSubmissionId());
        if (stageSubmission == null) {
            return Result.fail(404, "阶段提交不存在");
        }
        if (sysUserService.getById(dto.getUploadUserId()) == null) {
            return Result.fail(404, "上传人不存在");
        }

        SubmissionFile submissionFile = new SubmissionFile();
        submissionFile.setId(dto.getId());
        submissionFile.setSubmissionId(dto.getSubmissionId());
        submissionFile.setFileName(dto.getFileName());
        submissionFile.setOriginalName(dto.getOriginalName());
        submissionFile.setFileType(dto.getFileType());
        submissionFile.setFileSize(dto.getFileSize());
        submissionFile.setFilePath(dto.getFilePath());
        submissionFile.setFileUrl(dto.getFileUrl());
        submissionFile.setBizType(dto.getBizType());
        submissionFile.setUploadUserId(dto.getUploadUserId());
        submissionFileService.updateById(submissionFile);
        return Result.success(submissionFileService.getById(dto.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        SubmissionFile existing = submissionFileService.getById(id);
        if (existing == null) {
            return Result.fail(404, "提交文件不存在");
        }
        return Result.success(submissionFileService.removeById(id));
    }
}