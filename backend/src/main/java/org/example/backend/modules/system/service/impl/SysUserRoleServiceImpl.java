package org.example.backend.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.system.entity.SysUserRole;
import org.example.backend.modules.system.mapper.SysUserRoleMapper;
import org.example.backend.modules.system.service.SysUserRoleService;
import org.springframework.stereotype.Service;

@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole>  implements SysUserRoleService {
}
