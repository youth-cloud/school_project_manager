package org.example.backend.modules.edu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.edu.entity.DefenseRecord;
import org.example.backend.modules.edu.mapper.DefenseRecordMapper;
import org.example.backend.modules.edu.service.DefenseRecordService;
import org.springframework.stereotype.Service;

@Service
public class DefenseRecordServiceImpl extends ServiceImpl<DefenseRecordMapper, DefenseRecord> implements DefenseRecordService {
}