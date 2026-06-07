package org.example.backend.modules.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.modules.system.entity.SysRole;
import org.example.backend.modules.system.entity.SysUser;
import org.example.backend.modules.system.entity.SysUserRole;
import org.example.backend.modules.system.mapper.SysRoleMapper;
import org.example.backend.modules.system.mapper.SysUserMapper;
import org.example.backend.modules.system.mapper.SysUserRoleMapper;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;

    public SysUserServiceImpl(SysUserRoleMapper sysUserRoleMapper, SysRoleMapper sysRoleMapper) {
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
    }

    @Override
    public SysUser getByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return getOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username).last("limit 1"));
    }

    @Override
    public List<String> getRoleCodes(Long userId) {
        if (userId == null) {
            return List.of();
        }

        SysUser sysUser = getById(userId);
        if (sysUser == null || !Integer.valueOf(1).equals(sysUser.getStatus())) {
            return List.of();
        }

        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId)
        );
        if (userRoles == null || userRoles.isEmpty()) {
            return List.of();
        }

        Set<Long> roleIds = new HashSet<>();
        for (SysUserRole userRole : userRoles) {
            roleIds.add(userRole.getRoleId());
        }

        List<SysRole> roles = sysRoleMapper.selectList(
                Wrappers.<SysRole>lambdaQuery()
                        .in(SysRole::getId, roleIds)
                        .eq(SysRole::getStatus, 1)
        );
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        return roles.stream()
                .map(SysRole::getRoleCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        return hasAnyRole(userId, roleCode);
    }

    @Override
    public boolean hasAnyRole(Long userId, String... roleCodes) {
        if (roleCodes == null || roleCodes.length == 0) {
            return false;
        }

        List<String> userRoleCodes = getRoleCodes(userId);
        if (userRoleCodes.isEmpty()) {
            return false;
        }

        Set<String> roleCodeSet = new HashSet<>(userRoleCodes);
        for (String roleCode : roleCodes) {
            if (roleCodeSet.contains(roleCode)) {
                return true;
            }
        }
        return false;
    }
}
