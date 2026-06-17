package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.WeeklyReportCreateDTO;
import org.example.backend.modules.edu.dto.WeeklyReportPageQueryDTO;
import org.example.backend.modules.edu.dto.WeeklyReportUpdateDTO;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.ProjectGroupMember;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.entity.WeeklyReport;
import org.example.backend.modules.edu.service.ProjectGroupMemberService;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.example.backend.modules.edu.service.WeeklyReportService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/weekly-reports")
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;
    private final TrainingBatchService trainingBatchService;
    private final ProjectGroupService projectGroupService;
    private final ProjectGroupMemberService projectGroupMemberService;
    private final SysUserService sysUserService;

    public WeeklyReportController(WeeklyReportService weeklyReportService,
                                  TrainingBatchService trainingBatchService,
                                  ProjectGroupService projectGroupService,
                                  ProjectGroupMemberService projectGroupMemberService,
                                  SysUserService sysUserService) {
        this.weeklyReportService = weeklyReportService;
        this.trainingBatchService = trainingBatchService;
        this.projectGroupService = projectGroupService;
        this.projectGroupMemberService = projectGroupMemberService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<WeeklyReport>> findAll() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        LambdaQueryWrapper<WeeklyReport> wrapper = Wrappers.lambdaQuery();
        applyWeeklyReportViewScope(wrapper, currentUser);
        wrapper.orderByDesc(WeeklyReport::getSubmitTime);
        return Result.success(weeklyReportService.list(wrapper));
    }

    @GetMapping("/page")
    public Result<Page<WeeklyReport>> page(@Valid @ModelAttribute WeeklyReportPageQueryDTO query) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

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

        applyWeeklyReportViewScope(wrapper, currentUser);
        wrapper.orderByDesc(WeeklyReport::getSubmitTime);
        return Result.success(weeklyReportService.page(page, wrapper));
    }

    @PostMapping
    public Result<WeeklyReport> create(@Valid @RequestBody WeeklyReportCreateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "STUDENT")) {
            return Result.fail(403, "当前用户不是学生角色，不能提交周报");
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
        if (!isGroupMember(dto.getGroupId(), currentUser.getUserId())) {
            return Result.fail(403, "当前学生不属于该项目组，不能提交周报");
        }

        WeeklyReport weeklyReport = new WeeklyReport();
        weeklyReport.setBatchId(dto.getBatchId());
        weeklyReport.setGroupId(dto.getGroupId());
        weeklyReport.setStudentId(currentUser.getUserId());
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
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        WeeklyReport weeklyReport = weeklyReportService.getById(id);
        if (weeklyReport == null) {
            return Result.fail(404, "周报不存在");
        }
        if (!canViewWeeklyReport(currentUser, weeklyReport)) {
            return Result.fail(403, "当前用户无权查看该周报");
        }
        return Result.success(weeklyReport);
    }

    @PutMapping
    public Result<WeeklyReport> update(@Valid @RequestBody WeeklyReportUpdateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "STUDENT")) {
            return Result.fail(403, "当前用户不是学生角色，不能修改周报");
        }

        WeeklyReport existing = weeklyReportService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "周报不存在");
        }
        if (!currentUser.getUserId().equals(existing.getStudentId())) {
            return Result.fail(403, "当前用户无权修改该周报");
        }
        if (!existing.getBatchId().equals(dto.getBatchId()) || !existing.getGroupId().equals(dto.getGroupId())) {
            return Result.fail(400, "周报创建后不允许变更所属批次或项目组");
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
        if (!isGroupMember(dto.getGroupId(), currentUser.getUserId())) {
            return Result.fail(403, "当前学生不属于目标项目组，不能修改该周报");
        }

        WeeklyReport weeklyReport = new WeeklyReport();
        weeklyReport.setId(dto.getId());
        weeklyReport.setBatchId(dto.getBatchId());
        weeklyReport.setGroupId(dto.getGroupId());
        weeklyReport.setStudentId(existing.getStudentId());
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
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "STUDENT")) {
            return Result.fail(403, "当前用户不是学生角色，不能删除周报");
        }

        WeeklyReport existing = weeklyReportService.getById(id);
        if (existing == null) {
            return Result.fail(404, "周报不存在");
        }
        if (!currentUser.getUserId().equals(existing.getStudentId())) {
            return Result.fail(403, "当前用户无权删除该周报");
        }
        return Result.success(weeklyReportService.removeById(id));
    }

    private void applyWeeklyReportViewScope(LambdaQueryWrapper<WeeklyReport> wrapper,
                                             LoginUserPrincipal currentUser) {
        if (hasRole(currentUser, "ADMIN")) {
            return;
        }
        if (hasRole(currentUser, "STUDENT")) {
            List<Long> groupIds = listCurrentStudentGroupIds(currentUser.getUserId());
            if (groupIds.isEmpty()) {
                wrapper.eq(WeeklyReport::getId, -1L);
                return;
            }
            wrapper.in(WeeklyReport::getGroupId, groupIds);
            return;
        }
        if (hasRole(currentUser, "TEACHER")) {
            List<Long> groupIds = projectGroupService.list(
                    Wrappers.<ProjectGroup>lambdaQuery()
                            .eq(ProjectGroup::getTeacherId, currentUser.getUserId())
            ).stream().map(ProjectGroup::getId).collect(Collectors.toList());
            if (groupIds.isEmpty()) {
                wrapper.eq(WeeklyReport::getId, -1L);
                return;
            }
            wrapper.in(WeeklyReport::getGroupId, groupIds);
            return;
        }
        wrapper.eq(WeeklyReport::getId, -1L);
    }

    private boolean canViewWeeklyReport(LoginUserPrincipal currentUser, WeeklyReport weeklyReport) {
        if (hasRole(currentUser, "ADMIN")) {
            return true;
        }
        if (hasRole(currentUser, "STUDENT")) {
            return isGroupMember(weeklyReport.getGroupId(), currentUser.getUserId());
        }
        if (hasRole(currentUser, "TEACHER")) {
            ProjectGroup projectGroup = projectGroupService.getById(weeklyReport.getGroupId());
            return projectGroup != null && currentUser.getUserId().equals(projectGroup.getTeacherId());
        }
        return false;
    }

    private boolean hasRole(LoginUserPrincipal currentUser, String roleCode) {
        if (currentUser == null || currentUser.getUserId() == null) {
            return false;
        }
        return sysUserService.hasRole(currentUser.getUserId(), roleCode);
    }

    private boolean isGroupMember(Long groupId, Long userId) {
        return projectGroupMemberService.count(
                Wrappers.<ProjectGroupMember>lambdaQuery()
                        .eq(ProjectGroupMember::getGroupId, groupId)
                        .eq(ProjectGroupMember::getUserId, userId)
        ) > 0;
    }

    private List<Long> listCurrentStudentGroupIds(Long userId) {
        return projectGroupMemberService.list(
                Wrappers.<ProjectGroupMember>lambdaQuery()
                        .eq(ProjectGroupMember::getUserId, userId)
        ).stream().map(ProjectGroupMember::getGroupId).collect(Collectors.toList());
    }
}
