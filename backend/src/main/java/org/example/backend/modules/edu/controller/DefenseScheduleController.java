package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.DefenseScheduleCreateDTO;
import org.example.backend.modules.edu.dto.DefenseSchedulePageQueryDTO;
import org.example.backend.modules.edu.dto.DefenseScheduleUpdateDTO;
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
@RequestMapping("/api/defense-schedules")
public class DefenseScheduleController {

    private final DefenseScheduleService defenseScheduleService;
    private final DefenseRecordService defenseRecordService;
    private final TrainingBatchService trainingBatchService;
    private final ProjectGroupService projectGroupService;
    private final ProjectGroupMemberService projectGroupMemberService;
    private final SysUserService sysUserService;

    public DefenseScheduleController(DefenseScheduleService defenseScheduleService,
                                     DefenseRecordService defenseRecordService,
                                     TrainingBatchService trainingBatchService,
                                     ProjectGroupService projectGroupService,
                                     ProjectGroupMemberService projectGroupMemberService,
                                     SysUserService sysUserService) {
        this.defenseScheduleService = defenseScheduleService;
        this.defenseRecordService = defenseRecordService;
        this.trainingBatchService = trainingBatchService;
        this.projectGroupService = projectGroupService;
        this.projectGroupMemberService = projectGroupMemberService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<DefenseSchedule>> findAll() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        LambdaQueryWrapper<DefenseSchedule> wrapper = Wrappers.lambdaQuery();
        applyDefenseScheduleViewScope(wrapper, currentUser);
        wrapper.orderByDesc(DefenseSchedule::getDefenseDate)
                .orderByAsc(DefenseSchedule::getDefenseTime);
        return Result.success(defenseScheduleService.list(wrapper));
    }

    @GetMapping("/page")
    public Result<Page<DefenseSchedule>> page(@Valid @ModelAttribute DefenseSchedulePageQueryDTO query) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
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

        applyDefenseScheduleViewScope(wrapper, currentUser);
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
        String operatorError = validateScheduleOperator(trainingBatch.getTeacherId());
        if (operatorError != null) {
            return Result.fail(403, operatorError);
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
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        DefenseSchedule defenseSchedule = defenseScheduleService.getById(id);
        if (defenseSchedule == null) {
            return Result.fail(404, "答辩安排不存在");
        }
        if (!canViewDefenseSchedule(currentUser, defenseSchedule)) {
            return Result.fail(403, "当前用户无权查看该答辩安排");
        }
        return Result.success(defenseSchedule);
    }

    @PutMapping
    public Result<DefenseSchedule> update(@Valid @RequestBody DefenseScheduleUpdateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        DefenseSchedule existing = defenseScheduleService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "答辩安排不存在");
        }
        if (!canViewDefenseSchedule(currentUser, existing)) {
            return Result.fail(403, "当前用户无权修改该答辩安排");
        }
        if (!existing.getBatchId().equals(dto.getBatchId()) || !existing.getGroupId().equals(dto.getGroupId())) {
            return Result.fail(400, "答辩安排创建后不允许变更所属批次或项目组");
        }

        TrainingBatch trainingBatch = trainingBatchService.getById(dto.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }
        String operatorError = validateScheduleOperator(trainingBatch.getTeacherId());
        if (operatorError != null) {
            return Result.fail(403, operatorError);
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
        if (defenseRecordService.count(
                Wrappers.<DefenseRecord>lambdaQuery()
                        .eq(DefenseRecord::getScheduleId, id)
        ) > 0) {
            return Result.fail(400, "该答辩安排已有关联答辩记录，不能删除");
        }

        TrainingBatch trainingBatch = trainingBatchService.getById(existing.getBatchId());
        if (trainingBatch == null) {
            return Result.fail(404, "关联实训批次不存在");
        }
        String operatorError = validateScheduleOperator(trainingBatch.getTeacherId());
        if (operatorError != null) {
            return Result.fail(403, operatorError);
        }

        return Result.success(defenseScheduleService.removeById(id));
    }


    private void applyDefenseScheduleViewScope(LambdaQueryWrapper<DefenseSchedule> wrapper,
                                               LoginUserPrincipal currentUser) {
        if (hasRole(currentUser, "ADMIN")) {
            return;
        }
        if (hasRole(currentUser, "TEACHER")) {
            List<Long> batchIds = getTeacherBatchIds(currentUser);
            List<Long> groupIds = getTeacherGroupIds(currentUser);
            if (batchIds.isEmpty() && groupIds.isEmpty()) {
                wrapper.eq(DefenseSchedule::getId, -1L);
                return;
            }
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
            return;
        }
        List<Long> groupIds = projectGroupMemberService.list(Wrappers.<ProjectGroupMember>lambdaQuery()
                        .eq(ProjectGroupMember::getUserId, currentUser.getUserId()))
                .stream().map(ProjectGroupMember::getGroupId).collect(Collectors.toList());
        if (groupIds.isEmpty()) {
            wrapper.eq(DefenseSchedule::getId, -1L);
            return;
        }
        wrapper.in(DefenseSchedule::getGroupId, groupIds);
    }

    private boolean canViewDefenseSchedule(LoginUserPrincipal currentUser, DefenseSchedule defenseSchedule) {
        if (hasRole(currentUser, "ADMIN")) {
            return true;
        }
        if (hasRole(currentUser, "TEACHER")) {
            TrainingBatch batch = trainingBatchService.getById(defenseSchedule.getBatchId());
            if (batch != null && currentUser.getUserId().equals(batch.getTeacherId())) {
                return true;
            }
            ProjectGroup group = projectGroupService.getById(defenseSchedule.getGroupId());
            return group != null && currentUser.getUserId().equals(group.getTeacherId());
        }
        return projectGroupMemberService.count(Wrappers.<ProjectGroupMember>lambdaQuery()
                .eq(ProjectGroupMember::getGroupId, defenseSchedule.getGroupId())
                .eq(ProjectGroupMember::getUserId, currentUser.getUserId())) > 0;
    }

    private List<Long> getTeacherBatchIds(LoginUserPrincipal currentUser) {
        return trainingBatchService.list(Wrappers.<TrainingBatch>lambdaQuery()
                        .eq(TrainingBatch::getTeacherId, currentUser.getUserId()))
                .stream().map(TrainingBatch::getId).collect(Collectors.toList());
    }

    private List<Long> getTeacherGroupIds(LoginUserPrincipal currentUser) {
        return projectGroupService.list(Wrappers.<ProjectGroup>lambdaQuery()
                        .eq(ProjectGroup::getTeacherId, currentUser.getUserId()))
                .stream().map(ProjectGroup::getId).collect(Collectors.toList());
    }

    private boolean hasRole(LoginUserPrincipal currentUser, String roleCode) {
        return currentUser != null && currentUser.getUserId() != null
                && sysUserService.hasRole(currentUser.getUserId(), roleCode);
    }


    private String validateScheduleOperator(Long batchTeacherId) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return "当前未登录";
        }

        boolean isAdmin = sysUserService.hasRole(currentUser.getUserId(), "ADMIN");
        boolean isTeacher = sysUserService.hasRole(currentUser.getUserId(), "TEACHER");
        if (!isAdmin && !isTeacher) {
            return "当前用户没有答辩安排维护权限";
        }
        if (isTeacher && !isAdmin && !currentUser.getUserId().equals(batchTeacherId)) {
            return "教师只能维护自己负责批次下的答辩安排";
        }
        return null;
    }
}
