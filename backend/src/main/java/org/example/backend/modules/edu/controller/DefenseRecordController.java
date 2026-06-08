package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.DefenseRecordCreateDTO;
import org.example.backend.modules.edu.dto.DefenseRecordPageQueryDTO;
import org.example.backend.modules.edu.dto.DefenseRecordUpdateDTO;
import org.example.backend.modules.edu.entity.DefenseRecord;
import org.example.backend.modules.edu.entity.DefenseSchedule;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.ProjectGroupMember;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.service.DefenseRecordService;
import org.example.backend.modules.edu.service.DefenseScheduleService;
import org.example.backend.modules.edu.service.ProjectGroupMemberService;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/defense-records")
public class DefenseRecordController {

    private final DefenseRecordService defenseRecordService;
    private final DefenseScheduleService defenseScheduleService;
    private final TrainingBatchService trainingBatchService;
    private final ProjectGroupService projectGroupService;
    private final ProjectGroupMemberService projectGroupMemberService;
    private final SysUserService sysUserService;

    public DefenseRecordController(DefenseRecordService defenseRecordService,
                                   DefenseScheduleService defenseScheduleService,
                                   TrainingBatchService trainingBatchService,
                                   ProjectGroupService projectGroupService,
                                   ProjectGroupMemberService projectGroupMemberService,
                                   SysUserService sysUserService) {
        this.defenseRecordService = defenseRecordService;
        this.defenseScheduleService = defenseScheduleService;
        this.trainingBatchService = trainingBatchService;
        this.projectGroupService = projectGroupService;
        this.projectGroupMemberService = projectGroupMemberService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<DefenseRecord>> findAll() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        LambdaQueryWrapper<DefenseRecord> wrapper = Wrappers.lambdaQuery();
        applyDefenseRecordViewScope(wrapper, currentUser);
        wrapper.orderByDesc(DefenseRecord::getCreateTime);
        return Result.success(defenseRecordService.list(wrapper));
    }

    @GetMapping("/page")
    public Result<Page<DefenseRecord>> page(@Valid @ModelAttribute DefenseRecordPageQueryDTO query) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        Page<DefenseRecord> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<DefenseRecord> wrapper = Wrappers.lambdaQuery();

        if (query.getScheduleId() != null) {
            wrapper.eq(DefenseRecord::getScheduleId, query.getScheduleId());
        }
        if (query.getTeacherId() != null) {
            wrapper.eq(DefenseRecord::getTeacherId, query.getTeacherId());
        }

        applyDefenseRecordViewScope(wrapper, currentUser);
        wrapper.orderByDesc(DefenseRecord::getCreateTime);
        return Result.success(defenseRecordService.page(page, wrapper));
    }

    @PostMapping
    public Result<DefenseRecord> create(@Valid @RequestBody DefenseRecordCreateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return Result.fail(403, "当前用户不是教师角色，不能新增答辩记录");
        }

        DefenseSchedule defenseSchedule = defenseScheduleService.getById(dto.getScheduleId());
        if (defenseSchedule == null) {
            return Result.fail(404, "答辩安排不存在");
        }
        Result<Void> defensePermissionResult = validateDefensePermission(defenseSchedule, currentUser.getUserId());
        if (defensePermissionResult != null) {
            return Result.fail(defensePermissionResult.getCode(), defensePermissionResult.getMessage());
        }

        DefenseRecord defenseRecord = new DefenseRecord();
        defenseRecord.setScheduleId(dto.getScheduleId());
        defenseRecord.setTeacherId(currentUser.getUserId());
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
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        DefenseRecord defenseRecord = defenseRecordService.getById(id);
        if (defenseRecord == null) {
            return Result.fail(404, "答辩记录不存在");
        }
        DefenseSchedule defenseSchedule = defenseScheduleService.getById(defenseRecord.getScheduleId());
        if (defenseSchedule == null) {
            return Result.fail(404, "答辩记录关联的答辩安排不存在");
        }
        if (!canViewDefenseSchedule(currentUser, defenseSchedule)) {
            return Result.fail(403, "当前用户无权查看该答辩记录");
        }
        return Result.success(defenseRecord);
    }

    @PutMapping
    public Result<DefenseRecord> update(@Valid @RequestBody DefenseRecordUpdateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return Result.fail(403, "当前用户不是教师角色，不能修改答辩记录");
        }

        DefenseRecord existing = defenseRecordService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "答辩记录不存在");
        }
        if (!currentUser.getUserId().equals(existing.getTeacherId())) {
            return Result.fail(403, "当前用户无权修改该答辩记录");
        }

        DefenseSchedule defenseSchedule = defenseScheduleService.getById(dto.getScheduleId());
        if (defenseSchedule == null) {
            return Result.fail(404, "答辩安排不存在");
        }
        Result<Void> defensePermissionResult = validateDefensePermission(defenseSchedule, currentUser.getUserId());
        if (defensePermissionResult != null) {
            return Result.fail(defensePermissionResult.getCode(), defensePermissionResult.getMessage());
        }

        DefenseRecord defenseRecord = new DefenseRecord();
        defenseRecord.setId(dto.getId());
        defenseRecord.setScheduleId(dto.getScheduleId());
        defenseRecord.setTeacherId(existing.getTeacherId());
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
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return Result.fail(403, "当前用户不是教师角色，不能删除答辩记录");
        }

        DefenseRecord existing = defenseRecordService.getById(id);
        if (existing == null) {
            return Result.fail(404, "答辩记录不存在");
        }
        if (!currentUser.getUserId().equals(existing.getTeacherId())) {
            return Result.fail(403, "当前用户无权删除该答辩记录");
        }
        return Result.success(defenseRecordService.removeById(id));
    }

    private void applyDefenseRecordViewScope(LambdaQueryWrapper<DefenseRecord> wrapper,
                                             LoginUserPrincipal currentUser) {
        if (hasRole(currentUser, "ADMIN")) {
            return;
        }
        List<Long> scheduleIds = getViewableDefenseScheduleIds(currentUser);
        if (scheduleIds.isEmpty()) {
            wrapper.eq(DefenseRecord::getId, -1L);
            return;
        }
        wrapper.in(DefenseRecord::getScheduleId, scheduleIds);
    }

    private List<Long> getViewableDefenseScheduleIds(LoginUserPrincipal currentUser) {
        if (hasRole(currentUser, "TEACHER")) {
            List<Long> batchIds = trainingBatchService.list(Wrappers.<TrainingBatch>lambdaQuery()
                    .eq(TrainingBatch::getTeacherId, currentUser.getUserId()))
                    .stream().map(TrainingBatch::getId).collect(Collectors.toList());
            List<Long> groupIds = projectGroupService.list(Wrappers.<ProjectGroup>lambdaQuery()
                    .eq(ProjectGroup::getTeacherId, currentUser.getUserId()))
                    .stream().map(ProjectGroup::getId).collect(Collectors.toList());
            if (batchIds.isEmpty() && groupIds.isEmpty()) {
                return List.of();
            }
            LambdaQueryWrapper<DefenseSchedule> wrapper = Wrappers.<DefenseSchedule>lambdaQuery();
            wrapper.and(w -> {
                if (!batchIds.isEmpty()) {
                    w.in(DefenseSchedule::getBatchId, batchIds);
                }
                if (!groupIds.isEmpty()) {
                    if (!batchIds.isEmpty()) {
                        w.or();
                    }
                    w.in(DefenseSchedule::getGroupId, groupIds);
                }
            });
            return defenseScheduleService.list(wrapper).stream().map(DefenseSchedule::getId).collect(Collectors.toList());
        }
        List<Long> groupIds = projectGroupMemberService.list(Wrappers.<ProjectGroupMember>lambdaQuery()
                .eq(ProjectGroupMember::getUserId, currentUser.getUserId()))
                .stream().map(ProjectGroupMember::getGroupId).collect(Collectors.toList());
        if (groupIds.isEmpty()) {
            return List.of();
        }
        return defenseScheduleService.list(Wrappers.<DefenseSchedule>lambdaQuery()
                .in(DefenseSchedule::getGroupId, groupIds))
                .stream().map(DefenseSchedule::getId).collect(Collectors.toList());
    }

    private boolean canViewDefenseSchedule(LoginUserPrincipal currentUser, DefenseSchedule defenseSchedule) {
        if (currentUser == null || currentUser.getUserId() == null || defenseSchedule == null) {
            return false;
        }
        if (hasRole(currentUser, "ADMIN")) {
            return true;
        }
        if (hasRole(currentUser, "TEACHER")) {
            TrainingBatch trainingBatch = trainingBatchService.getById(defenseSchedule.getBatchId());
            if (trainingBatch != null && currentUser.getUserId().equals(trainingBatch.getTeacherId())) {
                return true;
            }
            ProjectGroup projectGroup = projectGroupService.getById(defenseSchedule.getGroupId());
            return projectGroup != null && currentUser.getUserId().equals(projectGroup.getTeacherId());
        }
        return isGroupMember(defenseSchedule.getGroupId(), currentUser.getUserId());
    }

    private Result<Void> validateDefensePermission(DefenseSchedule defenseSchedule, Long teacherId) {
        TrainingBatch trainingBatch = trainingBatchService.getById(defenseSchedule.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "答辩安排关联的实训批次不存在");
        }

        ProjectGroup projectGroup = projectGroupService.getById(defenseSchedule.getGroupId());
        if (projectGroup == null) {
            return Result.fail(404, "答辩安排关联的项目组不存在");
        }
        if (!defenseSchedule.getBatchId().equals(projectGroup.getBatchId())) {
            return Result.fail(400, "答辩安排与项目组所属批次不一致");
        }
        if (!teacherId.equals(trainingBatch.getTeacherId())) {
            return Result.fail(403, "当前教师不是该批次的负责教师，不能登记答辩记录");
        }
        if (!teacherId.equals(projectGroup.getTeacherId())) {
            return Result.fail(403, "当前教师不是该项目组的指导教师，不能登记答辩记录");
        }
        return null;
    }

    private boolean isGroupMember(Long groupId, Long userId) {
        return projectGroupMemberService.count(Wrappers.<ProjectGroupMember>lambdaQuery()
                .eq(ProjectGroupMember::getGroupId, groupId)
                .eq(ProjectGroupMember::getUserId, userId)) > 0;
    }

    private boolean hasRole(LoginUserPrincipal currentUser, String roleCode) {
        if (currentUser == null || currentUser.getUserId() == null) {
            return false;
        }
        return sysUserService.hasRole(currentUser.getUserId(), roleCode);
    }
}