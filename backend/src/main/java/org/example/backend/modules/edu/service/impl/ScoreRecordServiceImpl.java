package org.example.backend.modules.edu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.edu.entity.ScoreRecord;
import org.example.backend.modules.edu.mapper.ScoreRecordMapper;
import org.example.backend.modules.edu.service.ScoreRecordService;
import org.springframework.stereotype.Service;

@Service
public class ScoreRecordServiceImpl extends ServiceImpl<ScoreRecordMapper, ScoreRecord> implements ScoreRecordService {
}