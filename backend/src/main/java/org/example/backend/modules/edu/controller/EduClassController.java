package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.dto.EduClassCreateDTO;
import org.example.backend.modules.edu.dto.EduClassPageQueryDTO;
import org.example.backend.modules.edu.dto.EduClassUpdateDTO;
import org.example.backend.modules.edu.entity.EduClass;
import org.example.backend.modules.edu.service.EduClassService;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class EduClassController {
    private final EduClassService eduClassService;
    private final SysUserService sysUserService;
    public EduClassController(EduClassService eduClassService,
                              SysUserService sysUserService) {
        this.eduClassService = eduClassService;
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<EduClass>> findAll(){
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        LambdaQueryWrapper<EduClass> wrapper = Wrappers.lambdaQuery();
        applyEduClassViewScope(wrapper, currentUser);
        wrapper.orderByDesc(EduClass::getCreateTime);
        return Result.success(eduClassService.list(wrapper));
    }

    @PostMapping
    public Result<EduClass> create(@Valid @RequestBody EduClassCreateDTO dto){
        String adminError = validateAdminOperator();
        if (adminError != null) {
            return Result.fail(403, adminError);
        }
        EduClass eduClass = new EduClass();
        eduClass.setClassName(dto.getClassName());
        eduClass.setMajorName(dto.getMajorName());
        eduClass.setGrade(dto.getGrade());
        eduClass.setCounselorName(dto.getCounselorName());
        eduClass.setStatus(dto.getStatus()==null ? 1 : dto.getStatus());
        eduClassService.save(eduClass);
        return Result.success(eduClass);
    }

    @GetMapping("/{id}")
    public Result<EduClass> findById(@PathVariable Long id){
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        EduClass eduClass = eduClassService.getById(id);
        if (eduClass == null) {
            return Result.fail(404, "班级不存在");
        }
        if (!canViewEduClass(currentUser, eduClass)) {
            return Result.fail(403, "当前用户无权查看该班级");
        }
        return Result.success(eduClass);
    }

    @PutMapping
    public Result<EduClass> update(@Valid @RequestBody EduClassUpdateDTO dto){
        String adminError = validateAdminOperator();
        if (adminError != null) {
            return Result.fail(403, adminError);
        }
        EduClass existing = eduClassService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "班级不存在");
        }

        EduClass eduClass = new EduClass();
        eduClass.setId(dto.getId());
        eduClass.setClassName(dto.getClassName());
        eduClass.setMajorName(dto.getMajorName());
        eduClass.setGrade(dto.getGrade());
        eduClass.setCounselorName(dto.getCounselorName());
        eduClass.setStatus(dto.getStatus());
        eduClassService.updateById(eduClass);
        return Result.success(eduClassService.getById(dto.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id){
        String adminError = validateAdminOperator();
        if (adminError != null) {
            return Result.fail(403, adminError);
        }
        EduClass existing = eduClassService.getById(id);
        if (existing == null) {
            return Result.fail(404, "班级不存在");
        }
        return Result.success(eduClassService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<EduClass>> page(@Valid @ModelAttribute EduClassPageQueryDTO query){
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        Page<EduClass> page=Page.of(query.getCurrent(),query.getSize());
        LambdaQueryWrapper<EduClass> wrapper= Wrappers.lambdaQuery();
        if (StringUtils.hasText(query.getClassName())){
            wrapper.like(EduClass::getClassName,query.getClassName());
        }
        if (StringUtils.hasText(query.getMajorName())) {
            wrapper.like(EduClass::getMajorName, query.getMajorName());
        }

        applyEduClassViewScope(wrapper, currentUser);
        wrapper.orderByDesc(EduClass::getCreateTime);
        return Result.success(eduClassService.page(page, wrapper));
    }

    private void applyEduClassViewScope(LambdaQueryWrapper<EduClass> wrapper, LoginUserPrincipal currentUser) {
        if (sysUserService.hasAnyRole(currentUser.getUserId(), "ADMIN", "TEACHER")) {
            return;
        }
        wrapper.eq(EduClass::getStatus, 1);
    }

    private boolean canViewEduClass(LoginUserPrincipal currentUser, EduClass eduClass) {
        return sysUserService.hasAnyRole(currentUser.getUserId(), "ADMIN", "TEACHER")
                || Integer.valueOf(1).equals(eduClass.getStatus());
    }

    private String validateAdminOperator() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return "当前未登录";
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return "当前用户没有班级维护权限";
        }
        return null;
    }
}
