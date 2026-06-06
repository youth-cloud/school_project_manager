package org.example.backend.modules.edu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.edu.entity.StageTask;
import org.example.backend.modules.edu.mapper.StageTaskMapper;
import org.example.backend.modules.edu.service.StageTaskService;
import org.springframework.stereotype.Service;

@Service
public class StageTaskServiceImpl extends ServiceImpl<StageTaskMapper, StageTask> implements StageTaskService {
}
