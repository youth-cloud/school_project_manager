package org.example.backend.modules.edu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.edu.entity.WeeklyReport;
import org.example.backend.modules.edu.mapper.WeeklyReportMapper;
import org.example.backend.modules.edu.service.WeeklyReportService;
import org.springframework.stereotype.Service;

@Service
public class WeeklyReportServiceImpl extends ServiceImpl<WeeklyReportMapper, WeeklyReport> implements WeeklyReportService {
}
