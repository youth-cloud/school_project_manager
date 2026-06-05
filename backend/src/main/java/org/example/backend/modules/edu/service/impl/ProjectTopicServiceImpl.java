package org.example.backend.modules.edu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.edu.entity.ProjectTopic;
import org.example.backend.modules.edu.mapper.ProjectTopicMapper;
import org.example.backend.modules.edu.service.ProjectTopicService;
import org.springframework.stereotype.Service;

@Service
public class ProjectTopicServiceImpl extends ServiceImpl<ProjectTopicMapper, ProjectTopic> implements ProjectTopicService {
}
