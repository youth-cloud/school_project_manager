package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.ScoreRecordCreateDTO;
import org.example.backend.modules.edu.dto.ScoreRecordPageQueryDTO;
import org.example.backend.modules.edu.dto.ScoreRecordUpdateDTO;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.ProjectGroupMember;
import org.example.backend.modules.edu.entity.ScoreRecord;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.service.ProjectGroupMemberService;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.edu.service.ScoreRecordService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/score-records")
public class ScoreRecordController {

    private final ScoreRecordService scoreRecordService;
    private final TrainingBatchService trainingBatchService;
    private final ProjectGroupService projectGroupService;
    private final ProjectGroupMemberService projectGroupMemberService;
    private final SysUserService sysUserService;

    public ScoreRecordController(ScoreRecordService scoreRecordService,
                                 TrainingBatchService trainingBatchService,
                                 ProjectGroupService projectGroupService,
                                 ProjectGroupMemberService projectGroupMemberService,
                                 SysUserService sysUserService) {
        this.scoreRecordService = scoreRecordService;
        this.trainingBatchService = trainingBatchService;
        this.projectGroupService = projectGroupService;
        this.projectGroupMemberService = projectGroupMemberService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<ScoreRecord>> findAll() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        LambdaQueryWrapper<ScoreRecord> wrapper = Wrappers.lambdaQuery();
        applyScoreRecordViewScope(wrapper, currentUser);
        wrapper.orderByDesc(ScoreRecord::getCreateTime);
        return Result.success(scoreRecordService.list(wrapper));
    }

    @GetMapping("/page")
    public Result<Page<ScoreRecord>> page(@Valid @ModelAttribute ScoreRecordPageQueryDTO query) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

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

        applyScoreRecordViewScope(wrapper, currentUser);
        wrapper.orderByDesc(ScoreRecord::getCreateTime);
        return Result.success(scoreRecordService.page(page, wrapper));
    }

    @PostMapping
    public Result<ScoreRecord> create(@Valid @RequestBody ScoreRecordCreateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasAnyRole(currentUser.getUserId(), "TEACHER", "ADMIN")) {
            return Result.fail(403, "当前用户没有成绩管理权限");
        }

        boolean isAdmin = sysUserService.hasRole(currentUser.getUserId(), "ADMIN");
        Result<Void> relationResult = validateScoreRecordRelation(
                dto.getBatchId(), dto.getGroupId(), dto.getStudentId(), currentUser.getUserId(), isAdmin);
        if (relationResult != null) {
            return Result.fail(relationResult.getCode(), relationResult.getMessage());
        }
        Result<Void> uniqueResult = validateUniqueScoreRecord(null, dto.getBatchId(), dto.getStudentId());
        if (uniqueResult != null) {
            return Result.fail(uniqueResult.getCode(), uniqueResult.getMessage());
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
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        ScoreRecord scoreRecord = scoreRecordService.getById(id);
        if (scoreRecord == null) {
            return Result.fail(404, "成绩记录不存在");
        }
        if (!canViewScoreRecord(currentUser, scoreRecord)) {
            return Result.fail(403, "当前用户无权查看该成绩记录");
        }
        return Result.success(scoreRecord);
    }

    @PutMapping
    public Result<ScoreRecord> update(@Valid @RequestBody ScoreRecordUpdateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasAnyRole(currentUser.getUserId(), "TEACHER", "ADMIN")) {
            return Result.fail(403, "当前用户没有成绩管理权限");
        }

        ScoreRecord existing = scoreRecordService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "成绩记录不存在");
        }

        boolean isAdmin = sysUserService.hasRole(currentUser.getUserId(), "ADMIN");
        Result<Void> relationResult = validateScoreRecordRelation(
                dto.getBatchId(), dto.getGroupId(), dto.getStudentId(), currentUser.getUserId(), isAdmin);
        if (relationResult != null) {
            return Result.fail(relationResult.getCode(), relationResult.getMessage());
        }
        Result<Void> uniqueResult = validateUniqueScoreRecord(dto.getId(), dto.getBatchId(), dto.getStudentId());
        if (uniqueResult != null) {
            return Result.fail(uniqueResult.getCode(), uniqueResult.getMessage());
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
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasAnyRole(currentUser.getUserId(), "TEACHER", "ADMIN")) {
            return Result.fail(403, "当前用户没有成绩管理权限");
        }

        ScoreRecord existing = scoreRecordService.getById(id);
        if (existing == null) {
            return Result.fail(404, "成绩记录不存在");
        }

        boolean isAdmin = sysUserService.hasRole(currentUser.getUserId(), "ADMIN");
        Result<Void> relationResult = validateScoreRecordRelation(
                existing.getBatchId(), existing.getGroupId(), existing.getStudentId(), currentUser.getUserId(), isAdmin);
        if (relationResult != null) {
            return Result.fail(relationResult.getCode(), relationResult.getMessage());
        }
        return Result.success(scoreRecordService.removeById(id));
    }

    private void applyScoreRecordViewScope(LambdaQueryWrapper<ScoreRecord> wrapper,
                                           LoginUserPrincipal currentUser) {
        if (hasRole(currentUser, "ADMIN")) {
            return;
        }
        if (hasRole(currentUser, "STUDENT")) {
            wrapper.eq(ScoreRecord::getStudentId, currentUser.getUserId());
            return;
        }
        if (hasRole(currentUser, "TEACHER")) {
            List<Long> batchIds = trainingBatchService.list(Wrappers.<TrainingBatch>lambdaQuery()
                    .eq(TrainingBatch::getTeacherId, currentUser.getUserId()))
                    .stream().map(TrainingBatch::getId).collect(Collectors.toList());
            List<Long> groupIds = projectGroupService.list(Wrappers.<ProjectGroup>lambdaQuery()
                    .eq(ProjectGroup::getTeacherId, currentUser.getUserId()))
                    .stream().map(ProjectGroup::getId).collect(Collectors.toList());
            if (batchIds.isEmpty() && groupIds.isEmpty()) {
                wrapper.eq(ScoreRecord::getId, -1L);
                return;
            }
            wrapper.and(w -> {
                if (!batchIds.isEmpty()) {
                    w.in(ScoreRecord::getBatchId, batchIds);
                }
                if (!groupIds.isEmpty()) {
                    if (!batchIds.isEmpty()) {
                        w.or();
                    }
                    w.in(ScoreRecord::getGroupId, groupIds);
                }
            });
            return;
        }
        wrapper.eq(ScoreRecord::getId, -1L);
    }

    private boolean canViewScoreRecord(LoginUserPrincipal currentUser, ScoreRecord scoreRecord) {
        if (currentUser == null || currentUser.getUserId() == null || scoreRecord == null) {
            return false;
        }
        if (hasRole(currentUser, "ADMIN")) {
            return true;
        }
        if (hasRole(currentUser, "STUDENT")) {
            return currentUser.getUserId().equals(scoreRecord.getStudentId());
        }
        if (!hasRole(currentUser, "TEACHER")) {
            return false;
        }
        TrainingBatch trainingBatch = trainingBatchService.getById(scoreRecord.getBatchId());
        if (trainingBatch != null && currentUser.getUserId().equals(trainingBatch.getTeacherId())) {
            return true;
        }
        if (scoreRecord.getGroupId() == null) {
            return false;
        }
        ProjectGroup projectGroup = projectGroupService.getById(scoreRecord.getGroupId());
        return projectGroup != null && currentUser.getUserId().equals(projectGroup.getTeacherId());
    }

    private Result<Void> validateUniqueScoreRecord(Long currentId, Long batchId, Long studentId) {
        LambdaQueryWrapper<ScoreRecord> wrapper = Wrappers.<ScoreRecord>lambdaQuery()
                .eq(ScoreRecord::getBatchId, batchId)
                .eq(ScoreRecord::getStudentId, studentId);
        if (currentId != null) {
            wrapper.ne(ScoreRecord::getId, currentId);
        }
        if (scoreRecordService.count(wrapper) > 0) {
            return Result.fail(400, "该学生在当前实训批次下已存在成绩记录，不能重复新增或修改为重复记录");
        }
        return null;
    }

    private Result<Void> validateScoreRecordRelation(Long batchId, Long groupId, Long studentId,
                                                     Long operatorId, boolean isAdmin) {
        TrainingBatch trainingBatch = trainingBatchService.getById(batchId);
        if (trainingBatch == null) {
            return Result.fail(404, "实训批次不存在");
        }

        if (sysUserService.getById(studentId) == null) {
            return Result.fail(404, "学生不存在");
        }
        if (!sysUserService.hasRole(studentId, "STUDENT")) {
            return Result.fail(400, "目标用户不是学生角色，不能维护该成绩记录");
        }
        if (!isAdmin && !operatorId.equals(trainingBatch.getTeacherId())) {
            return Result.fail(403, "当前教师不是该批次的负责教师，不能维护该成绩记录");
        }

        if (groupId == null) {
            return Result.fail(400, "成绩记录必须绑定项目组");
        }

        ProjectGroup projectGroup = projectGroupService.getById(groupId);
        if (projectGroup == null) {
            return Result.fail(404, "项目组不存在");
        }
        if (!batchId.equals(projectGroup.getBatchId())) {
            return Result.fail(400, "项目组不属于当前实训批次");
        }
        if (!isGroupMember(groupId, studentId)) {
            return Result.fail(400, "目标学生不属于该项目组，不能维护该成绩记录");
        }
        if (!isAdmin && !operatorId.equals(projectGroup.getTeacherId())) {
            return Result.fail(403, "当前教师不是该项目组的指导教师，不能维护该成绩记录");
        }
        return null;
    }

    private boolean isGroupMember(Long groupId, Long userId) {
        return projectGroupMemberService.count(
                Wrappers.<ProjectGroupMember>lambdaQuery()
                        .eq(ProjectGroupMember::getGroupId, groupId)
                        .eq(ProjectGroupMember::getUserId, userId)
        ) > 0;
    }

    private boolean hasRole(LoginUserPrincipal currentUser, String roleCode) {
        if (currentUser == null || currentUser.getUserId() == null) {
            return false;
        }
        return sysUserService.hasRole(currentUser.getUserId(), roleCode);
    }
}
