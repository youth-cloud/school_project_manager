package org.example.backend.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.edu.entity.EduClass;
import org.example.backend.modules.edu.service.EduClassService;
import org.example.backend.modules.system.dto.RoleOptionDTO;
import org.example.backend.modules.system.dto.SysUserCreateDTO;
import org.example.backend.modules.system.dto.SysUserPageQueryDTO;
import org.example.backend.modules.system.dto.SysUserStatusUpdateDTO;
import org.example.backend.modules.system.dto.SysUserUpdateDTO;
import org.example.backend.modules.system.dto.UserOptionDTO;
import org.example.backend.modules.system.entity.SysRole;
import org.example.backend.modules.system.entity.SysUser;
import org.example.backend.modules.system.service.SysUserService;
import org.example.backend.modules.system.vo.SysUserManageItemVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/api/sys-users")
public class SysUserController {

    private final PasswordEncoder passwordEncoder;
    private final SysUserService sysUserService;
    private final EduClassService eduClassService;

    public SysUserController(SysUserService sysUserService,
                             PasswordEncoder passwordEncoder,
                             EduClassService eduClassService) {
        this.passwordEncoder = passwordEncoder;
        this.sysUserService = sysUserService;
        this.eduClassService = eduClassService;
    }

    @GetMapping("/teacher-options")
    public Result<List<UserOptionDTO>> teacherOptions() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        List<UserOptionDTO> data = sysUserService.listEnabledUsersByRole("TEACHER")
                .stream()
                .map(this::toUserOption)
                .toList();

        return Result.success(data);
    }

    @GetMapping("/student-options")
    public Result<List<UserOptionDTO>> studentOptions() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (sysUserService.hasAnyRole(currentUser.getUserId(), "ADMIN", "TEACHER")) {
            List<UserOptionDTO> data = sysUserService.listEnabledUsersByRole("STUDENT")
                    .stream()
                    .map(this::toUserOption)
                    .toList();
            return Result.success(data);
        }
        if (sysUserService.hasRole(currentUser.getUserId(), "STUDENT")) {
            SysUser current = sysUserService.getById(currentUser.getUserId());
            if (current == null || !Integer.valueOf(1).equals(current.getStatus())) {
                return Result.success(List.of());
            }
            return Result.success(List.of(toUserOption(current)));
        }
        return Result.success(List.of());
    }

    @GetMapping("/user-options")
    public Result<List<UserOptionDTO>> userOptions() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }

        List<UserOptionDTO> data = sysUserService.listEnabledUsers()
                .stream()
                .map(this::toUserOption)
                .toList();
        return Result.success(data);
    }

    @GetMapping("/role-options")
    public Result<List<RoleOptionDTO>> roleOptions() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return Result.fail(403, "当前用户没有用户管理权限");
        }

        List<RoleOptionDTO> data = sysUserService.listEnabledRoles().stream()
                .map(this::toRoleOption)
                .toList();
        return Result.success(data);
    }

    @GetMapping("/page")
    public Result<Page<SysUserManageItemVO>> page(@Valid @ModelAttribute SysUserPageQueryDTO query) {
        String adminError = validateAdminOperator();
        if (adminError != null) {
            return Result.fail(403, adminError);
        }

        Page<SysUser> page = Page.of(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.lambdaQuery();

        if (StringUtils.hasText(query.getUsername())) {
            wrapper.like(SysUser::getUsername, query.getUsername().trim());
        }
        if (StringUtils.hasText(query.getRealName())) {
            wrapper.like(SysUser::getRealName, query.getRealName().trim());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }

        if (StringUtils.hasText(query.getRoleCode())) {
            Set<Long> userIds = sysUserService.listUserIdsByRole(query.getRoleCode().trim());
            if (userIds.isEmpty()) {
                wrapper.eq(SysUser::getId, -1L);
            } else {
                wrapper.in(SysUser::getId, userIds);
            }
        }

        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> userPage = sysUserService.page(page, wrapper);
        Page<SysUserManageItemVO> resultPage = Page.of(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        resultPage.setRecords(userPage.getRecords().stream().map(this::toManageItem).toList());
        return Result.success(resultPage);
    }

    @GetMapping("/{id}")
    public Result<SysUserManageItemVO> findById(@PathVariable Long id) {
        String adminError = validateAdminOperator();
        if (adminError != null) {
            return Result.fail(403, adminError);
        }

        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        return Result.success(toManageItem(user));
    }

    @Transactional
    @PostMapping
    public Result<SysUserManageItemVO> create(@Valid @RequestBody SysUserCreateDTO dto) {
        String adminError = validateAdminOperator();
        if (adminError != null) {
            return Result.fail(403, adminError);
        }
        Set<String> roleCodes = sanitizeRoleCodes(dto.getRoleCodes());
        boolean isStudentUser = roleCodes.contains("STUDENT");
        String normalizedUsername = normalizeUsername(dto.getUsername(), dto.getStudentNo(), isStudentUser);
        if (normalizedUsername == null) {
            return Result.fail(400, isStudentUser ? "学生账号必须填写学号" : "用户名不能为空");
        }
        if (sysUserService.existsByUsername(normalizedUsername, null)) {
            return Result.fail(400, "用户名已存在");
        }
        String normalizedStudentNo = normalizeStudentNo(dto.getStudentNo(), isStudentUser);
        if (normalizedStudentNo == null && isStudentUser) {
            return Result.fail(400, "学生账号必须填写学号");
        }
        if (normalizedStudentNo != null && sysUserService.existsByStudentNo(normalizedStudentNo, null)) {
            return Result.fail(400, "学号已存在");
        }
        EduClass selectedClass = resolveStudentClass(dto.getClassId(), isStudentUser);
        if (isStudentUser && selectedClass == null) {
            return Result.fail(400, "学生账号必须绑定有效班级");
        }

        List<SysRole> selectedRoles = resolveActiveRoles(dto.getRoleCodes());
        if (selectedRoles.size() != roleCodes.size()) {
            return Result.fail(400, "存在无效或已停用的角色");
        }

        SysUser user = new SysUser();
        user.setUsername(normalizedUsername);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName().trim());
        user.setStatus(dto.getStatus());
        user.setStudentNo(normalizedStudentNo);
        user.setClassId(selectedClass == null ? null : selectedClass.getId());
        sysUserService.save(user);
        sysUserService.replaceUserRoles(user.getId(), dto.getRoleCodes());
        return Result.success(toManageItem(sysUserService.getById(user.getId())));
    }

    @Transactional
    @PutMapping
    public Result<SysUserManageItemVO> update(@Valid @RequestBody SysUserUpdateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return Result.fail(403, "当前用户没有用户管理权限");
        }

        SysUser existing = sysUserService.getById(dto.getId());
        if (existing == null) {
            return Result.fail(404, "用户不存在");
        }
        List<SysRole> currentRoles = sysUserService.listRolesByUserId(existing.getId());
        Set<String> currentRoleCodes = currentRoles.stream()
                .map(SysRole::getRoleCode)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        Set<String> nextRoleCodes = sanitizeRoleCodes(dto.getRoleCodes());
        boolean isStudentUser = nextRoleCodes.contains("STUDENT");
        String normalizedUsername = normalizeUsername(dto.getUsername(), dto.getStudentNo(), isStudentUser);
        if (normalizedUsername == null) {
            return Result.fail(400, isStudentUser ? "学生账号必须填写学号" : "用户名不能为空");
        }
        if (sysUserService.existsByUsername(normalizedUsername, dto.getId())) {
            return Result.fail(400, "用户名已存在");
        }
        String normalizedStudentNo = normalizeStudentNo(dto.getStudentNo(), isStudentUser);
        if (normalizedStudentNo == null && isStudentUser) {
            return Result.fail(400, "学生账号必须填写学号");
        }
        if (normalizedStudentNo != null && sysUserService.existsByStudentNo(normalizedStudentNo, dto.getId())) {
            return Result.fail(400, "学号已存在");
        }
        EduClass selectedClass = resolveStudentClass(dto.getClassId(), isStudentUser);
        if (isStudentUser && selectedClass == null) {
            return Result.fail(400, "学生账号必须绑定有效班级");
        }
        List<SysRole> selectedRoles = resolveActiveRoles(dto.getRoleCodes());
        if (selectedRoles.size() != nextRoleCodes.size()) {
            return Result.fail(400, "存在无效或已停用的角色");
        }

        String adminGuardError = validateAdminGuard(currentUser.getUserId(), existing, currentRoleCodes, nextRoleCodes, dto.getStatus());
        if (adminGuardError != null) {
            return Result.fail(400, adminGuardError);
        }

        SysUser user = new SysUser();
        user.setId(existing.getId());
        user.setUsername(normalizedUsername);
        user.setRealName(dto.getRealName().trim());
        user.setStudentNo(normalizedStudentNo);
        user.setClassId(selectedClass == null ? null : selectedClass.getId());
        user.setStatus(dto.getStatus());
        sysUserService.updateById(user);
        sysUserService.replaceUserRoles(user.getId(), dto.getRoleCodes());
        return Result.success(toManageItem(sysUserService.getById(dto.getId())));
    }

    @Transactional
    @PutMapping("/{id}/status")
    public Result<SysUserManageItemVO> updateStatus(@PathVariable Long id, @Valid @RequestBody SysUserStatusUpdateDTO dto) {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return Result.fail(403, "当前用户没有用户管理权限");
        }

        SysUser existing = sysUserService.getById(id);
        if (existing == null) {
            return Result.fail(404, "用户不存在");
        }

        Set<String> currentRoleCodes = sysUserService.listRolesByUserId(existing.getId()).stream()
                .map(SysRole::getRoleCode)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        String adminGuardError = validateAdminGuard(currentUser.getUserId(), existing, currentRoleCodes, currentRoleCodes, dto.getStatus());
        if (adminGuardError != null) {
            return Result.fail(400, adminGuardError);
        }

        SysUser user = new SysUser();
        user.setId(existing.getId());
        user.setStatus(dto.getStatus());
        sysUserService.updateById(user);
        return Result.success(toManageItem(sysUserService.getById(id)));
    }

    private UserOptionDTO toUserOption(SysUser user) {
        UserOptionDTO dto = new UserOptionDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        return dto;
    }

    private RoleOptionDTO toRoleOption(SysRole role) {
        RoleOptionDTO dto = new RoleOptionDTO();
        dto.setId(role.getId());
        dto.setRoleCode(role.getRoleCode());
        dto.setRoleName(role.getRoleName());
        return dto;
    }

    private SysUserManageItemVO toManageItem(SysUser user) {
        List<SysRole> roles = sysUserService.listRolesByUserId(user.getId());
        SysUserManageItemVO item = new SysUserManageItemVO();
        item.setId(user.getId());
        item.setUsername(user.getUsername());
        item.setRealName(user.getRealName());
        item.setStudentNo(user.getStudentNo());
        item.setClassId(user.getClassId());
        item.setClassName(resolveClassName(user.getClassId()));
        item.setStatus(user.getStatus());
        item.setCreateTime(user.getCreateTime());
        item.setUpdateTime(user.getUpdateTime());
        item.setRoleCodes(roles.stream().map(SysRole::getRoleCode).filter(Objects::nonNull).toList());
        item.setRoleNames(roles.stream().map(SysRole::getRoleName).filter(Objects::nonNull).toList());
        return item;
    }

    private String validateAdminOperator() {
        LoginUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return "当前未登录";
        }
        if (!sysUserService.hasRole(currentUser.getUserId(), "ADMIN")) {
            return "当前用户没有用户管理权限";
        }
        return null;
    }

    private Set<String> sanitizeRoleCodes(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return Set.of();
        }
        return roleCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private List<SysRole> resolveActiveRoles(List<String> roleCodes) {
        Set<String> roleCodeSet = sanitizeRoleCodes(roleCodes);
        if (roleCodeSet.isEmpty()) {
            return List.of();
        }
        return sysUserService.listEnabledRoles().stream()
                .filter(role -> roleCodeSet.contains(role.getRoleCode()))
                .toList();
    }

    private String normalizeUsername(String username, String studentNo, boolean isStudentUser) {
        if (isStudentUser) {
            if (!StringUtils.hasText(studentNo)) {
                return null;
            }
            return studentNo.trim();
        }
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return username.trim();
    }

    private String normalizeStudentNo(String studentNo, boolean isStudentUser) {
        if (!isStudentUser) {
            return null;
        }
        if (!StringUtils.hasText(studentNo)) {
            return null;
        }
        return studentNo.trim();
    }

    private EduClass resolveStudentClass(Long classId, boolean isStudentUser) {
        if (!isStudentUser || classId == null) {
            return null;
        }
        EduClass eduClass = eduClassService.getById(classId);
        if (eduClass == null || !Integer.valueOf(1).equals(eduClass.getStatus())) {
            return null;
        }
        return eduClass;
    }

    private String resolveClassName(Long classId) {
        if (classId == null) {
            return null;
        }
        EduClass eduClass = eduClassService.getById(classId);
        return eduClass == null ? null : eduClass.getClassName();
    }

    private String validateAdminGuard(Long operatorId,
                                      SysUser targetUser,
                                      Set<String> currentRoleCodes,
                                      Set<String> nextRoleCodes,
                                      Integer nextStatus) {
        boolean targetIsAdmin = currentRoleCodes.contains("ADMIN");
        if (!targetIsAdmin) {
            return null;
        }

        boolean keepsAdminRole = nextRoleCodes.contains("ADMIN");
        boolean keepsEnabledStatus = Integer.valueOf(1).equals(nextStatus);
        boolean remainsEnabledAdmin = keepsAdminRole && keepsEnabledStatus;

        if (Objects.equals(operatorId, targetUser.getId()) && !remainsEnabledAdmin) {
            return "不能禁用当前登录管理员或移除自己的管理员角色";
        }

        if (remainsEnabledAdmin) {
            return null;
        }

        if (sysUserService.countEnabledUsersByRole("ADMIN") <= 1) {
            return "系统至少需要保留一个启用中的管理员账号";
        }
        return null;
    }
}
