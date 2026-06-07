package org.example.backend.modules.edu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.edu.entity.ReviewRecord;
import org.example.backend.modules.edu.mapper.ReviewRecordMapper;
import org.example.backend.modules.edu.service.ReviewRecordService;
import org.springframework.stereotype.Service;

@Service
public class ReviewRecordServiceImpl extends ServiceImpl<ReviewRecordMapper, ReviewRecord> implements ReviewRecordService {
}