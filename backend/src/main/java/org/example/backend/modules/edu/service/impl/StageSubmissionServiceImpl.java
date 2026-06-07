package org.example.backend.modules.edu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.edu.entity.StageSubmission;
import org.example.backend.modules.edu.mapper.StageSubmissionMapper;
import org.example.backend.modules.edu.service.StageSubmissionService;
import org.springframework.stereotype.Service;

@Service
public class StageSubmissionServiceImpl extends ServiceImpl<StageSubmissionMapper, StageSubmission>  implements StageSubmissionService {
}
