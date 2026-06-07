package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.NoticeCreateDTO;
import org.example.backend.modules.edu.dto.NoticePageQueryDTO;
import org.example.backend.modules.edu.dto.NoticeUpdateDTO;
import org.example.backend.modules.edu.entity.Notice;
import org.example.backend.modules.edu.service.NoticeService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;
    private final SysUserService sysUserService;

    public NoticeController(NoticeService noticeService, SysUserService sysUserService) {
        this.noticeService = noticeService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<Notice>> findAll() {
        return Result.success(noticeService.list());
    }

    @GetMapping("/page")
    public Result<Page<Notice>> page(@Valid @ModelAttribute NoticePageQueryDTO query) {
        Page<Notice> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<Notice> wrapper = Wrappers.lambdaQuery();

        if (StringUtils.hasText(query.getTitle())) {
            wrapper.like(Notice::getTitle, query.getTitle());
        }
        if (query.getPublisherId() != null) {
            wrapper.eq(Notice::getPublisherId, query.getPublisherId());
        }
        if (StringUtils.hasText(query.getTargetRole())) {
            wrapper.eq(Notice::getTargetRole, query.getTargetRole());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Notice::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(Notice::getPublishTime);
        return Result.success(noticeService.page(page, wrapper));
    }

    @PostMapping
    public Result<Notice> create(@Valid @RequestBody NoticeCreateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasAnyRole(currentUser.getUserId(), "TEACHER", "ADMIN")) {
            return Result.fail(403, "当前用户没有公告发布权限");
        }

        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setPublisherId(currentUser.getUserId());
        notice.setTargetRole(dto.getTargetRole());
        notice.setStatus(dto.getStatus());
        notice.setPublishTime(dto.getPublishTime() != null ? dto.getPublishTime() : LocalDateTime.now());
        noticeService.save(notice);
        return Result.success(notice);
    }

    @GetMapping("/{id}")
    public Result<Notice> findById(@PathVariable Long id) {
        Notice notice = noticeService.getById(id);
        if (notice == null) {
            return Result.fail(404, "公告不存在");
        }
        return Result.success(notice);
    }

    @PutMapping
    public Result<Notice> update(@Valid @RequestBody NoticeUpdateDTO dto) {
        Notice existing = noticeService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "公告不存在");
        }

        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasAnyRole(currentUser.getUserId(), "TEACHER", "ADMIN")) {
            return Result.fail(403, "当前用户没有公告修改权限");
        }

        Notice notice = new Notice();
        notice.setId(dto.getId());
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setPublisherId(existing.getPublisherId());
        notice.setTargetRole(dto.getTargetRole());
        notice.setStatus(dto.getStatus());
        notice.setPublishTime(dto.getPublishTime() != null ? dto.getPublishTime() : existing.getPublishTime());
        noticeService.updateById(notice);
        return Result.success(noticeService.getById(dto.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        Notice existing = noticeService.getById(id);
        if (existing == null) {
            return Result.fail(404, "公告不存在");
        }
        return Result.success(noticeService.removeById(id));
    }
}