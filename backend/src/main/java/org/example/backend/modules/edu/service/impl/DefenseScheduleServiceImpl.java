package org.example.backend.modules.edu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.edu.entity.DefenseSchedule;
import org.example.backend.modules.edu.mapper.DefenseScheduleMapper;
import org.example.backend.modules.edu.service.DefenseScheduleService;
import org.springframework.stereotype.Service;

@Service
public class DefenseScheduleServiceImpl extends ServiceImpl<DefenseScheduleMapper, DefenseSchedule> implements DefenseScheduleService {
}