package org.example.backend.modules.edu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.edu.entity.SubmissionFile;
import org.example.backend.modules.edu.mapper.SubmissionFileMapper;
import org.example.backend.modules.edu.service.SubmissionFileService;
import org.springframework.stereotype.Service;

@Service
public class SubmissionFileServiceImpl extends ServiceImpl<SubmissionFileMapper, SubmissionFile> implements SubmissionFileService {
}
