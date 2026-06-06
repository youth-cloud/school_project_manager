package org.example.backend.modules.edu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.mapper.ProjectGroupMapper;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.springframework.stereotype.Service;

@Service
public class ProjectGroupServiceImpl extends ServiceImpl<ProjectGroupMapper, ProjectGroup>  implements ProjectGroupService {
}
