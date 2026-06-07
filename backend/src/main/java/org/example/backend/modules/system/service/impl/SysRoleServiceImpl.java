package org.example.backend.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.system.entity.SysRole;
import org.example.backend.modules.system.mapper.SysRoleMapper;
import org.example.backend.modules.system.service.SysRoleService;
import org.springframework.stereotype.Service;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
}