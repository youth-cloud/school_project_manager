package org.example.backend.modules.edu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.edu.entity.TopicApplication;
import org.example.backend.modules.edu.mapper.TopicApplicationMapper;
import org.example.backend.modules.edu.service.TopicApplicationService;
import org.springframework.stereotype.Service;

@Service
public class TopicApplicationServiceImpl extends ServiceImpl<TopicApplicationMapper, TopicApplication> implements TopicApplicationService {
}
