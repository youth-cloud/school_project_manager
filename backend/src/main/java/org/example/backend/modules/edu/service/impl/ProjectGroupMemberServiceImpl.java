package org.example.backend.modules.edu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.edu.entity.ProjectGroupMember;
import org.example.backend.modules.edu.mapper.ProjectGroupMemberMapper;
import org.example.backend.modules.edu.service.ProjectGroupMemberService;
import org.springframework.stereotype.Service;

@Service
public class ProjectGroupMemberServiceImpl extends ServiceImpl<ProjectGroupMemberMapper, ProjectGroupMember> implements ProjectGroupMemberService {
}
