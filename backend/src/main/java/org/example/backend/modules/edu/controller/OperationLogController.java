package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.edu.dto.OperationLogCreateDTO;
import org.example.backend.modules.edu.dto.OperationLogPageQueryDTO;
import org.example.backend.modules.edu.dto.OperationLogUpdateDTO;
import org.example.backend.modules.edu.entity.OperationLog;
import org.example.backend.modules.edu.service.OperationLogService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operation-logs")
public class OperationLogController {

    private final OperationLogService operationLogService;
    private final SysUserService sysUserService;

    public OperationLogController(OperationLogService operationLogService, SysUserService sysUserService) {
        this.operationLogService = operationLogService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<OperationLog>> findAll() {
        return Result.success(operationLogService.list());
    }

    @GetMapping("/page")
    public Result<Page<OperationLog>> page(@Valid @ModelAttribute OperationLogPageQueryDTO query) {
        Page<OperationLog> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<OperationLog> wrapper = Wrappers.lambdaQuery();

        if (StringUtils.hasText(query.getModuleName())) {
            wrapper.like(OperationLog::getModuleName, query.getModuleName());
        }
        if (StringUtils.hasText(query.getOperationType())) {
            wrapper.eq(OperationLog::getOperationType, query.getOperationType());
        }
        if (query.getOperatorId() != null) {
            wrapper.eq(OperationLog::getOperatorId, query.getOperatorId());
        }
        if (StringUtils.hasText(query.getResult())) {
            wrapper.eq(OperationLog::getResult, query.getResult());
        }

        wrapper.orderByDesc(OperationLog::getCreateTime);
        return Result.success(operationLogService.page(page, wrapper));
    }

    @PostMapping
    public Result<OperationLog> create(@Valid @RequestBody OperationLogCreateDTO dto) {
        if (dto.getOperatorId() != null && sysUserService.getById(dto.getOperatorId()) == null) {
            return Result.fail(404, "操作人不存在");
        }

        OperationLog operationLog = new OperationLog();
        operationLog.setModuleName(dto.getModuleName());
        operationLog.setOperationType(dto.getOperationType());
        operationLog.setOperatorId(dto.getOperatorId());
        operationLog.setRequestMethod(dto.getRequestMethod());
        operationLog.setRequestUri(dto.getRequestUri());
        operationLog.setIp(dto.getIp());
        operationLog.setOperationDesc(dto.getOperationDesc());
        operationLog.setResult(dto.getResult());
        operationLogService.save(operationLog);
        return Result.success(operationLog);
    }

    @GetMapping("/{id}")
    public Result<OperationLog> findById(@PathVariable Long id) {
        OperationLog operationLog = operationLogService.getById(id);
        if (operationLog == null) {
            return Result.fail(404, "操作日志不存在");
        }
        return Result.success(operationLog);
    }

    @PutMapping
    public Result<OperationLog> update(@Valid @RequestBody OperationLogUpdateDTO dto) {
        OperationLog existing = operationLogService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "操作日志不存在");
        }

        if (dto.getOperatorId() != null && sysUserService.getById(dto.getOperatorId()) == null) {
            return Result.fail(404, "操作人不存在");
        }

        OperationLog operationLog = new OperationLog();
        operationLog.setId(dto.getId());
        operationLog.setModuleName(dto.getModuleName());
        operationLog.setOperationType(dto.getOperationType());
        operationLog.setOperatorId(dto.getOperatorId());
        operationLog.setRequestMethod(dto.getRequestMethod());
        operationLog.setRequestUri(dto.getRequestUri());
        operationLog.setIp(dto.getIp());
        operationLog.setOperationDesc(dto.getOperationDesc());
        operationLog.setResult(dto.getResult());
        operationLogService.updateById(operationLog);
        return Result.success(operationLogService.getById(dto.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        OperationLog existing = operationLogService.getById(id);
        if (existing == null) {
            return Result.fail(404, "操作日志不存在");
        }
        return Result.success(operationLogService.removeById(id));
    }
}