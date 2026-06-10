package org.example.backend.modules.edu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.edu.entity.ProjectGroupApplicationMember;
import org.example.backend.modules.edu.mapper.ProjectGroupApplicationMemberMapper;
import org.example.backend.modules.edu.service.ProjectGroupApplicationMemberService;
import org.springframework.stereotype.Service;

@Service
public class ProjectGroupApplicationMemberServiceImpl
        extends ServiceImpl<ProjectGroupApplicationMemberMapper, ProjectGroupApplicationMember>
        implements ProjectGroupApplicationMemberService {
}