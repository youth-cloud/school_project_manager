package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.TrainingBatchCreateDTO;
import org.example.backend.modules.edu.dto.TrainingBatchPageQueryDTO;
import org.example.backend.modules.edu.dto.TrainingBatchUpdateDTO;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.service.EduClassService;
import org.example.backend.modules.edu.service.EduCourseService;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/training-batches")
public class TrainingBatchController {
    private final TrainingBatchService trainingBatchService;
    private final EduCourseService eduCourseService;
    private final EduClassService eduClassService;
    private final SysUserService sysUserService;

    public TrainingBatchController(TrainingBatchService trainingBatchService,
                                   EduCourseService eduCourseService,
                                   EduClassService eduClassService,
                                   SysUserService sysUserService) {
        this.trainingBatchService = trainingBatchService;
        this.eduCourseService = eduCourseService;
        this.eduClassService = eduClassService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<TrainingBatch>> findAll() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        LambdaQueryWrapper<TrainingBatch> wrapper = Wrappers.lambdaQuery();
        applyTrainingBatchViewScope(wrapper, currentUser);
        wrapper.orderByDesc(TrainingBatch::getCreateTime);
        return Result.success(trainingBatchService.list(wrapper));
    }

    @GetMapping("/page")
    public Result<Page<TrainingBatch>> page(@Valid @ModelAttribute TrainingBatchPageQueryDTO query) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        Page<TrainingBatch> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<TrainingBatch> wrapper = Wrappers.lambdaQuery();

        if (StringUtils.hasText(query.getBatchName())) {
            wrapper.like(TrainingBatch::getBatchName, query.getBatchName());
        }
        if (query.getCourseId() != null) {
            wrapper.eq(TrainingBatch::getCourseId, query.getCourseId());
        }
        if (query.getTeacherId() != null) {
            wrapper.eq(TrainingBatch::getTeacherId, query.getTeacherId());
        }
        if (query.getClassId() != null) {
            wrapper.eq(TrainingBatch::getClassId, query.getClassId());
        }
        if (StringUtils.hasText(query.getTermName())) {
            wrapper.like(TrainingBatch::getTermName, query.getTermName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(TrainingBatch::getStatus, query.getStatus());
        }

        applyTrainingBatchViewScope(wrapper, currentUser);
        wrapper.orderByDesc(TrainingBatch::getCreateTime);
        return Result.success(trainingBatchService.page(page, wrapper));
    }

    @PostMapping
    public Result<TrainingBatch> create(@Valid @RequestBody TrainingBatchCreateDTO dto){
        String operatorError = validateBatchOperator(dto.getTeacherId());
        if (operatorError != null) {
            return Result.fail(403, operatorError);
        }

        String relationError = validateRelatedIds(dto.getCourseId(), dto.getTeacherId(), dto.getClassId());
        if (relationError != null) {
            return Result.fail(404, relationError);
        }
        if (!sysUserService.hasRole(dto.getTeacherId(), "TEACHER")) {
            return Result.fail(400, "目标教师不是教师角色，不能作为实训批次教师");
        }
        String timeError = validateTimeRange(dto.getStartTime(), dto.getEndTime(), dto.getDefenseTime());
        if (timeError != null) {
            return Result.fail(400, timeError);
        }

        TrainingBatch trainingBatch = new TrainingBatch();
        trainingBatch.setBatchName(dto.getBatchName());
        trainingBatch.setCourseId(dto.getCourseId());
        trainingBatch.setTeacherId(dto.getTeacherId());
        trainingBatch.setClassId(dto.getClassId());
        trainingBatch.setTermName(dto.getTermName());
        trainingBatch.setStartTime(dto.getStartTime());
        trainingBatch.setEndTime(dto.getEndTime());
        trainingBatch.setDefenseTime(dto.getDefenseTime());
        trainingBatch.setDescription(dto.getDescription());
        trainingBatch.setStatus(dto.getStatus());
        trainingBatchService.save(trainingBatch);
        return Result.success(trainingBatch);
    }

    @GetMapping("/{id}")
    public Result<TrainingBatch>  findById(@PathVariable Long id){
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        TrainingBatch trainingBatch=trainingBatchService.getById(id);
        if(trainingBatch==null){
            return Result.fail(404,"实训批次不存在");
        }
        if (!canViewTrainingBatch(currentUser, trainingBatch)) {
            return Result.fail(403, "当前用户无权查看该实训批次");
        }
        return Result.success(trainingBatch);
    }

    @PutMapping
    public Result<TrainingBatch>  update(@Valid @RequestBody TrainingBatchUpdateDTO dto){
        TrainingBatch existing=trainingBatchService.getById(dto.getId());
        if(existing==null){
            return Result.fail(404,"实训批次不存在");
        }

        String operatorError = validateBatchOperator(dto.getTeacherId());
        if (operatorError != null) {
            return Result.fail(403, operatorError);
        }

        String relationError = validateRelatedIds(dto.getCourseId(), dto.getTeacherId(), dto.getClassId());
        if (relationError != null) {
            return Result.fail(404, relationError);
        }
        if (!sysUserService.hasRole(dto.getTeacherId(), "TEACHER")) {
            return Result.fail(400, "目标教师不是教师角色，不能作为实训批次教师");
        }
        String timeError = validateTimeRange(dto.getStartTime(), dto.getEndTime(), dto.getDefenseTime());
        if (timeError != null) {
            return Result.fail(400, timeError);
        }

        TrainingBatch trainingBatch=new TrainingBatch();
        trainingBatch.setId(existing.getId());
        trainingBatch.setBatchName(dto.getBatchName());
        trainingBatch.setCourseId(dto.getCourseId());
        trainingBatch.setTeacherId(dto.getTeacherId());
        trainingBatch.setClassId(dto.getClassId());
        trainingBatch.setTermName(dto.getTermName());
        trainingBatch.setStartTime(dto.getStartTime());
        trainingBatch.setEndTime(dto.getEndTime());
        trainingBatch.setDefenseTime(dto.getDefenseTime());
        trainingBatch.setDescription(dto.getDescription());
        trainingBatch.setStatus(dto.getStatus());
        trainingBatchService.updateById(trainingBatch);
        return Result.success(trainingBatchService.getById(dto.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id){
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        TrainingBatch existing=trainingBatchService.getById(id);
        if(existing==null){
            return Result.fail(404,"实训批次不存在");
        }
        if (sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return Result.success(trainingBatchService.removeById(id));
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return Result.fail(403, "当前用户没有删除实训批次的权限");
        }
        if (!currentUser.getUserId().equals(existing.getTeacherId())) {
            return Result.fail(403, "教师只能删除绑定给自己的实训批次");
        }
        return Result.success(trainingBatchService.removeById(id));
    }

    private void applyTrainingBatchViewScope(LambdaQueryWrapper<TrainingBatch> wrapper,
                                             LoginUserPrincipal currentUser) {
        if (sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return;
        }
        if (sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            wrapper.eq(TrainingBatch::getTeacherId, currentUser.getUserId());
            return;
        }
        wrapper.eq(TrainingBatch::getStatus, 1);
    }

    private boolean canViewTrainingBatch(LoginUserPrincipal currentUser, TrainingBatch trainingBatch) {
        if (sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return true;
        }
        if (sysUserService.hasRole(currentUser.getUserId(), "TEACHER")) {
            return currentUser.getUserId().equals(trainingBatch.getTeacherId());
        }
        return Integer.valueOf(1).equals(trainingBatch.getStatus());
    }

    private String validateRelatedIds(Long courseId, Long teacherId, Long classId) {
        if (eduCourseService.getById(courseId) == null) {
            return "课程不存在";
        }
        if (sysUserService.getById(teacherId) == null) {
            return "教师不存在";
        }
        if (eduClassService.getById(classId) == null) {
            return "班级不存在";
        }
        return null;
    }

    private String validateBatchOperator(Long teacherId) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return "当前未登录";
        }

        boolean isAdmin = sysUserService.hasRole(currentUser.getUserId(), "ADMIN");
        boolean isTeacher = sysUserService.hasRole(currentUser.getUserId(), "TEACHER");
        if (!isAdmin && !isTeacher) {
            return "当前用户没有批次维护权限";
        }
        if (isTeacher && !isAdmin && !currentUser.getUserId().equals(teacherId)) {
            return "教师只能创建或修改绑定给自己的实训批次";
        }
        return null;
    }

    private String validateTimeRange(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime defenseTime) {
        if (startTime.isAfter(endTime)) {
            return "开始时间不能晚于结束时间";
        }
        if (defenseTime != null && defenseTime.isBefore(startTime)) {
            return "答辩时间不能早于开始时间";
        }
        return null;
    }
}
