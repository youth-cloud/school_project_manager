package org.example.backend.modules.edu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.edu.entity.ProjectGroupApplication;
import org.example.backend.modules.edu.mapper.ProjectGroupApplicationMapper;
import org.example.backend.modules.edu.service.ProjectGroupApplicationService;
import org.springframework.stereotype.Service;

@Service
public class ProjectGroupApplicationServiceImpl extends ServiceImpl<ProjectGroupApplicationMapper, ProjectGroupApplication>
        implements ProjectGroupApplicationService {
}