package org.example.backend.modules.edu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.edu.dto.EduClassCreateDTO;
import org.example.backend.modules.edu.dto.EduClassPageQueryDTO;
import org.example.backend.modules.edu.dto.EduClassUpdateDTO;
import org.example.backend.modules.edu.entity.EduClass;
import org.example.backend.modules.edu.service.EduClassService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class EduClassController {
    private final EduClassService eduClassService;
    public EduClassController(EduClassService eduClassService) {
        this.eduClassService = eduClassService;
    }

    @GetMapping
    public Result<List<EduClass>> findAll(){
        return Result.success(eduClassService.list());
    }

    @PostMapping
    public Result<EduClass> create(@Valid @RequestBody EduClassCreateDTO dto){
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
        EduClass eduClass = eduClassService.getById(id);
        if (eduClass == null) {
            return Result.fail(404, "班级不存在");
        }
        return Result.success(eduClass);
    }

    @PutMapping
    public Result<EduClass> update(@Valid @RequestBody EduClassUpdateDTO dto){
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
        EduClass existing = eduClassService.getById(id);
        if (existing == null) {
            return Result.fail(404, "班级不存在");
        }
        return Result.success(eduClassService.removeById(id));
    }

    @GetMapping("/page")
    public Result<Page<EduClass>> page(@Valid @ModelAttribute EduClassPageQueryDTO query){
        Page<EduClass> page=Page.of(query.getCurrent(),query.getSize());
        LambdaQueryWrapper<EduClass> wrapper= Wrappers.lambdaQuery();
        if (StringUtils.hasText(query.getClassName())){
            wrapper.like(EduClass::getClassName,query.getClassName());
        }
        if (StringUtils.hasText(query.getMajorName())) {
            wrapper.like(EduClass::getMajorName, query.getMajorName());
        }

        wrapper.orderByDesc(EduClass::getCreateTime);
        return Result.success(eduClassService.page(page, wrapper));
    }
}
