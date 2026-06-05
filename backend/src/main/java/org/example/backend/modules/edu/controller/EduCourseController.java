package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.edu.dto.EduCourseCreateDTO;
import org.example.backend.modules.edu.dto.EduCoursePageQueryDTO;
import org.example.backend.modules.edu.dto.EduCourseUpdateDTO;
import org.example.backend.modules.edu.entity.EduCourse;
import org.example.backend.modules.edu.service.EduCourseService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class EduCourseController {
    private final EduCourseService eduCourseService;
    public EduCourseController(EduCourseService eduCourseService) {
        this.eduCourseService = eduCourseService;
    }

    @GetMapping
    public Result<List<EduCourse>> findAll() {
        return Result.success(eduCourseService.list());
    }

    @GetMapping("/page")
    public Result<Page<EduCourse>> page(@Valid @ModelAttribute EduCoursePageQueryDTO query) {
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

        wrapper.orderByDesc(EduCourse::getCreateTime);
        return Result.success(eduCourseService.page(page, wrapper));
    }

    @PostMapping
    public Result<EduCourse> create(@Valid @RequestBody EduCourseCreateDTO dto){
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
        EduCourse eduCourse=eduCourseService.getById(id);
        if(eduCourse==null){
            return Result.fail(404,"课程不存在");
        }
        return Result.success(eduCourse);
    }

    @PutMapping
    public Result<EduCourse> update(@Valid @RequestBody EduCourseUpdateDTO dto){
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
        EduCourse existing =eduCourseService.getById(id);
        if(existing==null){
            return Result.fail(404,"课程不存在");
        }
        return Result.success(eduCourseService.removeById(id));
    }
}
