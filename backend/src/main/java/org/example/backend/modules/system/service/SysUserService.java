package org.example.backend.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.modules.system.entity.SysUser;

import java.util.List;

public interface SysUserService extends IService<SysUser> {
    SysUser getByUsername(String username);

    List<String> getRoleCodes(Long userId);

    boolean hasRole(Long userId, String roleCode);

    boolean hasAnyRole(Long userId, String... roleCodes);
}
