package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.EduCourseCreateDTO;
import org.example.backend.modules.edu.dto.EduCoursePageQueryDTO;
import org.example.backend.modules.edu.dto.EduCourseUpdateDTO;
import org.example.backend.modules.edu.entity.EduCourse;
import org.example.backend.modules.edu.service.EduCourseService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class EduCourseController {
    private final EduCourseService eduCourseService;
    private final SysUserService sysUserService;
    public EduCourseController(EduCourseService eduCourseService,
                               SysUserService sysUserService) {
        this.eduCourseService = eduCourseService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<EduCourse>> findAll() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        LambdaQueryWrapper<EduCourse> wrapper = Wrappers.lambdaQuery();
        applyEduCourseViewScope(wrapper, currentUser);
        wrapper.orderByDesc(EduCourse::getCreateTime);
        return Result.success(eduCourseService.list(wrapper));
    }

    @GetMapping("/page")
    public Result<Page<EduCourse>> page(@Valid @ModelAttribute EduCoursePageQueryDTO query) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        Page<EduCourse> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<EduCourse> wrapper = Wrappers.lambdaQuery();

        if (StringUtils.hasText(query.getCourseName())) {
            wrapper.like(EduCourse::getCourseName, query.getCourseName());
        }
        if (StringUtils.hasText(query.getCourseCode())) {
            wrapper.like(EduCourse::getCourseCode, query.getCourseCode());
        }
        if (query.getStatus() != null) {
            wrapper.eq(EduCourse::getStatus, query.getStatus());
        }

        applyEduCourseViewScope(wrapper, currentUser);
        wrapper.orderByDesc(EduCourse::getCreateTime);
        return Result.success(eduCourseService.page(page, wrapper));
    }

    @PostMapping
    public Result<EduCourse> create(@Valid @RequestBody EduCourseCreateDTO dto){
        String adminError = validateAdminOperator();
        if (adminError != null) {
            return Result.fail(403, adminError);
        }
        EduCourse eduCourse = new EduCourse();
        eduCourse.setCourseName(dto.getCourseName());
        eduCourse.setCourseCode(dto.getCourseCode());
        eduCourse.setCredit(dto.getCredit());
        eduCourse.setRemark(dto.getRemark());
        eduCourse.setStatus(dto.getStatus());
        eduCourseService.save(eduCourse);
        return Result.success(eduCourse);
    }

    @GetMapping("/{id}")
    public Result<EduCourse> findById(@PathVariable Long id){
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        EduCourse eduCourse=eduCourseService.getById(id);
        if(eduCourse==null){
            return Result.fail(404,"课程不存在");
        }
        if (!canViewEduCourse(currentUser, eduCourse)) {
            return Result.fail(403, "当前用户无权查看该课程");
        }
        return Result.success(eduCourse);
    }

    @PutMapping
    public Result<EduCourse> update(@Valid @RequestBody EduCourseUpdateDTO dto){
        String adminError = validateAdminOperator();
        if (adminError != null) {
            return Result.fail(403, adminError);
        }
        EduCourse existing =eduCourseService.getById(dto.getId());
        if(existing==null){
            return Result.fail(404,"课程不存在");
        }
        EduCourse eduCourse = new EduCourse();
        eduCourse.setId(existing.getId());
        eduCourse.setCourseName(dto.getCourseName());
        eduCourse.setCourseCode(dto.getCourseCode());
        eduCourse.setCredit(dto.getCredit());
        eduCourse.setRemark(dto.getRemark());
        eduCourse.setStatus(dto.getStatus());
        eduCourseService.updateById(eduCourse);
        return Result.success(eduCourseService.getById(dto.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id){
        String adminError = validateAdminOperator();
        if (adminError != null) {
            return Result.fail(403, adminError);
        }
        EduCourse existing =eduCourseService.getById(id);
        if(existing==null){
            return Result.fail(404,"课程不存在");
        }
        return Result.success(eduCourseService.removeById(id));
    }

    private void applyEduCourseViewScope(LambdaQueryWrapper<EduCourse> wrapper, LoginUserPrincipal currentUser) {
        if (sysUserService.hasAnyRole(currentUser.getUserId(), "ADMIN", "TEACHER")) {
            return;
        }
        wrapper.eq(EduCourse::getStatus, 1);
    }

    private boolean canViewEduCourse(LoginUserPrincipal currentUser, EduCourse eduCourse) {
        return sysUserService.hasAnyRole(currentUser.getUserId(), "ADMIN", "TEACHER")
                || Integer.valueOf(1).equals(eduCourse.getStatus());
    }

    private String validateAdminOperator() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return "当前未登录";
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return "当前用户没有课程维护权限";
        }
        return null;
    }
}
