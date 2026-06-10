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
import java.util.LinkedHashSet;
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
    public List<SysUser> listEnabledUsers() {
        return list(
                Wrappers.<SysUser>lambdaQuery()
                        .eq(SysUser::getStatus, 1)
                        .orderByAsc(SysUser::getRealName)
                        .orderByAsc(SysUser::getUsername)
        );
    }

    @Override
    public List<SysUser> listEnabledUsersByRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return List.of();
        }

        List<SysRole> roles = sysRoleMapper.selectList(
                Wrappers.<SysRole>lambdaQuery()
                        .eq(SysRole::getRoleCode, roleCode)
                        .eq(SysRole::getStatus, 1)
        );
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        Set<Long> roleIds = roles.stream().map(SysRole::getId).filter(Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        if (roleIds.isEmpty()) {
            return List.of();
        }

        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                Wrappers.<SysUserRole>lambdaQuery().in(SysUserRole::getRoleId, roleIds)
        );
        if (userRoles == null || userRoles.isEmpty()) {
            return List.of();
        }

        Set<Long> userIds = userRoles.stream().map(SysUserRole::getUserId).filter(Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        if (userIds.isEmpty()) {
            return List.of();
        }

        return list(
                Wrappers.<SysUser>lambdaQuery()
                        .in(SysUser::getId, userIds)
                        .eq(SysUser::getStatus, 1)
                        .orderByAsc(SysUser::getRealName)
                        .orderByAsc(SysUser::getUsername)
        );
    }

    @Override
    public List<SysRole> listEnabledRoles() {
        return sysRoleMapper.selectList(
                Wrappers.<SysRole>lambdaQuery()
                        .eq(SysRole::getStatus, 1)
                        .orderByAsc(SysRole::getRoleName)
                        .orderByAsc(SysRole::getRoleCode)
        );
    }

    @Override
    public List<SysRole> listRolesByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }

        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId)
        );
        if (userRoles == null || userRoles.isEmpty()) {
            return List.of();
        }

        Set<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (roleIds.isEmpty()) {
            return List.of();
        }

        return sysRoleMapper.selectList(
                Wrappers.<SysRole>lambdaQuery()
                        .in(SysRole::getId, roleIds)
                        .orderByAsc(SysRole::getRoleName)
                        .orderByAsc(SysRole::getRoleCode)
        );
    }

    @Override
    public Set<Long> listUserIdsByRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return Set.of();
        }

        List<SysRole> roles = sysRoleMapper.selectList(
                Wrappers.<SysRole>lambdaQuery()
                        .eq(SysRole::getRoleCode, roleCode.trim())
                        .eq(SysRole::getStatus, 1)
        );
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }

        Set<Long> roleIds = roles.stream()
                .map(SysRole::getId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (roleIds.isEmpty()) {
            return Set.of();
        }

        return sysUserRoleMapper.selectList(
                Wrappers.<SysUserRole>lambdaQuery().in(SysUserRole::getRoleId, roleIds)
        ).stream()
                .map(SysUserRole::getUserId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public void replaceUserRoles(Long userId, List<String> roleCodes) {
        if (userId == null) {
            return;
        }

        sysUserRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));

        if (roleCodes == null || roleCodes.isEmpty()) {
            return;
        }

        Set<String> distinctRoleCodes = roleCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (distinctRoleCodes.isEmpty()) {
            return;
        }

        List<SysRole> roles = sysRoleMapper.selectList(
                Wrappers.<SysRole>lambdaQuery()
                        .in(SysRole::getRoleCode, distinctRoleCodes)
                        .eq(SysRole::getStatus, 1)
        );
        if (roles == null || roles.isEmpty()) {
            return;
        }

        for (SysRole role : roles) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            sysUserRoleMapper.insert(userRole);
        }
    }

    @Override
    public boolean existsByUsername(String username, Long excludeUserId) {
        if (username == null || username.isBlank()) {
            return false;
        }

        return count(
                Wrappers.<SysUser>lambdaQuery()
                        .eq(SysUser::getUsername, username.trim())
                        .ne(excludeUserId != null, SysUser::getId, excludeUserId)
        ) > 0;
    }

    @Override
    public boolean existsByStudentNo(String studentNo, Long excludeUserId) {
        if (studentNo == null || studentNo.isBlank()) {
            return false;
        }

        return count(
                Wrappers.<SysUser>lambdaQuery()
                        .eq(SysUser::getStudentNo, studentNo.trim())
                        .ne(excludeUserId != null, SysUser::getId, excludeUserId)
        ) > 0;
    }

    @Override
    public long countEnabledUsersByRole(String roleCode) {
        return listEnabledUsersByRole(roleCode).size();
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
