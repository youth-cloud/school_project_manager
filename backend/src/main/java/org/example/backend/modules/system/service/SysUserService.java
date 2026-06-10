package org.example.backend.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.modules.system.entity.SysRole;
import org.example.backend.modules.system.entity.SysUser;

import java.util.List;
import java.util.Set;

public interface SysUserService extends IService<SysUser> {
    SysUser getByUsername(String username);

    List<String> getRoleCodes(Long userId);

    List<SysUser> listEnabledUsers();

    List<SysUser> listEnabledUsersByRole(String roleCode);

    List<SysRole> listEnabledRoles();

    List<SysRole> listRolesByUserId(Long userId);

    Set<Long> listUserIdsByRole(String roleCode);

    void replaceUserRoles(Long userId, List<String> roleCodes);

    boolean existsByUsername(String username, Long excludeUserId);

    boolean existsByStudentNo(String studentNo, Long excludeUserId);

    long countEnabledUsersByRole(String roleCode);

    boolean hasRole(Long userId, String roleCode);

    boolean hasAnyRole(Long userId, String... roleCodes);
}
