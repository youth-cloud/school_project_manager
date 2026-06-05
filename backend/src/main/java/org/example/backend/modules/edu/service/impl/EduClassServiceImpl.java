package org.example.backend.modules.edu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.edu.entity.EduClass;
import org.example.backend.modules.edu.mapper.EduClassMapper;
import org.example.backend.modules.edu.service.EduClassService;
import org.springframework.stereotype.Service;

@Service
public class EduClassServiceImpl extends ServiceImpl<EduClassMapper,EduClass> implements EduClassService {

}
