package org.example.backend.modules.edu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.edu.entity.TrainingBatch;
import org.example.backend.modules.edu.mapper.TrainingBatchMapper;
import org.example.backend.modules.edu.service.TrainingBatchService;
import org.springframework.stereotype.Service;

@Service
public class TrainingBatchServiceImpl extends ServiceImpl<TrainingBatchMapper, TrainingBatch> implements TrainingBatchService {
}
