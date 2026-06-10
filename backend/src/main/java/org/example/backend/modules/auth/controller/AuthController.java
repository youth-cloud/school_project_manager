package org.example.backend.modules.auth.controller;

import jakarta.validation.Valid;
import org.example.backend.common.jwt.JwtTokenUtil;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.dto.AdminResetPasswordDTO;
import org.example.backend.modules.auth.dto.ChangePasswordDTO;
import org.example.backend.modules.auth.dto.LoginRequestDTO;
import org.example.backend.modules.auth.dto.ProfileUpdateDTO;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.auth.vo.LoginResponseVO;
import org.example.backend.modules.auth.vo.ProfileInfoVO;
import org.example.backend.modules.auth.vo.LoginUserInfoVO;
import org.example.backend.modules.edu.entity.EduClass;
import org.example.backend.modules.edu.service.EduClassService;
import org.example.backend.modules.system.entity.SysUser;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String TOKEN_VERSION_KEY_PREFIX = "auth:token:version:";
    private static final String TOKEN_BLACKLIST_KEY_PREFIX = "auth:token:blacklist:";

    private final SysUserService sysUserService;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;
    private final EduClassService eduClassService;

    public AuthController(SysUserService sysUserService,
                          JwtTokenUtil jwtTokenUtil,
                          PasswordEncoder passwordEncoder,
                          StringRedisTemplate stringRedisTemplate,
                          EduClassService eduClassService) {
        this.sysUserService = sysUserService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.passwordEncoder = passwordEncoder;
        this.stringRedisTemplate = stringRedisTemplate;
        this.eduClassService = eduClassService;
    }

    @PostMapping("/login")
    public Result<LoginResponseVO> login(@Valid @RequestBody LoginRequestDTO dto) {
        SysUser sysUser = sysUserService.getByUsername(dto.getUsername());
        if (sysUser == null) {
            return Result.fail(401, "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(sysUser.getStatus())) {
            return Result.fail(403, "账号已被禁用");
        }
        if (!isPasswordMatched(dto.getPassword(), sysUser)) {
            return Result.fail(401, "用户名或密码错误");
        }

        List<String> roleCodes = sysUserService.getRoleCodes(sysUser.getId());
        String token = jwtTokenUtil.generateToken(sysUser.getId(), sysUser.getUsername(), getTokenVersion(sysUser.getId()));

        LoginUserInfoVO userInfo = buildLoginUserInfo(sysUser, roleCodes);

        LoginResponseVO response = new LoginResponseVO();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setUserInfo(userInfo);
        return Result.success("登录成功", response);
    }

    @PostMapping("/refresh")
    public Result<LoginResponseVO> refresh(HttpServletRequest request) {
        LoginUserPrincipal principal = SecurityUtils.getCurrentUser();
        if (principal == null) {
            return Result.fail(401, "当前未登录");
        }
        String oldToken = extractBearerToken(request);
        if (oldToken == null || !jwtTokenUtil.validateToken(oldToken) || isTokenBlacklisted(oldToken)) {
            return Result.fail(401, "token 无效或已失效");
        }

        SysUser sysUser = sysUserService.getById(principal.getUserId());
        if (sysUser == null) {
            return Result.fail(404, "当前用户不存在");
        }
        if (!Integer.valueOf(1).equals(sysUser.getStatus())) {
            return Result.fail(403, "账号已被禁用");
        }

        blacklistToken(oldToken);
        List<String> roleCodes = sysUserService.getRoleCodes(sysUser.getId());
        String newToken = jwtTokenUtil.generateToken(sysUser.getId(), sysUser.getUsername(), getTokenVersion(sysUser.getId()));

        LoginResponseVO response = new LoginResponseVO();
        response.setToken(newToken);
        response.setTokenType("Bearer");
        response.setUserInfo(buildLoginUserInfo(sysUser, roleCodes));
        return Result.success("刷新成功", response);
    }

    @PostMapping("/logout")
    public Result<Boolean> logout(HttpServletRequest request) {
        LoginUserPrincipal principal = SecurityUtils.getCurrentUser();
        if (principal == null) {
            return Result.fail(401, "当前未登录");
        }
        String token = extractBearerToken(request);
        if (token == null || !jwtTokenUtil.validateToken(token) || isTokenBlacklisted(token)) {
            return Result.fail(401, "token 无效或已失效");
        }
        blacklistToken(token);
        return Result.success(true);
    }

    @GetMapping("/me")
    public Result<LoginUserInfoVO> me() {
        LoginUserPrincipal principal = SecurityUtils.getCurrentUser();
        if (principal == null) {
            return Result.fail(401, "当前未登录");
        }

        SysUser sysUser = sysUserService.getById(principal.getUserId());
        if (sysUser == null) {
            return Result.fail(404, "当前用户不存在");
        }
        if (!Integer.valueOf(1).equals(sysUser.getStatus())) {
            return Result.fail(403, "账号已被禁用");
        }
        return Result.success(buildLoginUserInfo(sysUser, principal.getRoles()));
    }

    @GetMapping("/profile")
    public Result<ProfileInfoVO> profile() {
        LoginUserPrincipal principal = SecurityUtils.getCurrentUser();
        if (principal == null) {
            return Result.fail(401, "当前未登录");
        }

        SysUser sysUser = sysUserService.getById(principal.getUserId());
        if (sysUser == null) {
            return Result.fail(404, "当前用户不存在");
        }
        if (!Integer.valueOf(1).equals(sysUser.getStatus())) {
            return Result.fail(403, "账号已被禁用");
        }
        return Result.success(buildProfileInfo(sysUser, principal.getRoles()));
    }

    @PutMapping("/profile")
    public Result<ProfileInfoVO> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto) {
        LoginUserPrincipal principal = SecurityUtils.getCurrentUser();
        if (principal == null) {
            return Result.fail(401, "当前未登录");
        }

        SysUser sysUser = sysUserService.getById(principal.getUserId());
        if (sysUser == null) {
            return Result.fail(404, "当前用户不存在");
        }
        if (!Integer.valueOf(1).equals(sysUser.getStatus())) {
            return Result.fail(403, "账号已被禁用");
        }

        SysUser updateUser = new SysUser();
        updateUser.setId(sysUser.getId());
        updateUser.setPhone(StringUtils.hasText(dto.getPhone()) ? dto.getPhone().trim() : null);
        updateUser.setEmail(StringUtils.hasText(dto.getEmail()) ? dto.getEmail().trim() : null);
        sysUserService.updateById(updateUser);

        SysUser latestUser = sysUserService.getById(sysUser.getId());
        return Result.success(buildProfileInfo(latestUser, principal.getRoles()));
    }

    @PutMapping("/password")
    public Result<Boolean> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        LoginUserPrincipal principal = SecurityUtils.getCurrentUser();
        if (principal == null) {
            return Result.fail(401, "当前未登录");
        }

        SysUser sysUser = sysUserService.getById(principal.getUserId());
        if (sysUser == null) {
            return Result.fail(404, "当前用户不存在");
        }
        if (!Integer.valueOf(1).equals(sysUser.getStatus())) {
            return Result.fail(403, "账号已被禁用");
        }
        if (!isPasswordMatched(dto.getOldPassword(), sysUser)) {
            return Result.fail(400, "原密码错误");
        }
        if (!Objects.equals(dto.getNewPassword(), dto.getConfirmPassword())) {
            return Result.fail(400, "两次输入的新密码不一致");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), sysUser.getPassword())) {
            return Result.fail(400, "新密码不能与原密码相同");
        }

        SysUser updateUser = new SysUser();
        updateUser.setId(sysUser.getId());
        updateUser.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        sysUserService.updateById(updateUser);
        increaseTokenVersion(sysUser.getId());
        return Result.success(true);
    }

    @PutMapping("/admin/reset-password")
    public Result<Boolean> adminResetPassword(@Valid @RequestBody AdminResetPasswordDTO dto) {
        LoginUserPrincipal principal = SecurityUtils.getCurrentUser();
        if (principal == null) {
            return Result.fail(401, "当前未登录");
        }
        if (!sysUserService.hasRole(principal.getUserId(), "ADMIN")) {
            return Result.fail(403, "当前用户没有重置密码权限");
        }
        if (!Objects.equals(dto.getNewPassword(), dto.getConfirmPassword())) {
            return Result.fail(400, "两次输入的新密码不一致");
        }

        SysUser targetUser = sysUserService.getById(dto.getUserId());
        if (targetUser == null) {
            return Result.fail(404, "目标用户不存在");
        }
        if (targetUser.getPassword() != null && passwordEncoder.matches(dto.getNewPassword(), targetUser.getPassword())) {
            return Result.fail(400, "新密码不能与原密码相同");
        }

        SysUser updateUser = new SysUser();
        updateUser.setId(targetUser.getId());
        updateUser.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        sysUserService.updateById(updateUser);
        increaseTokenVersion(targetUser.getId());
        return Result.success(true);
    }

    private LoginUserInfoVO buildLoginUserInfo(SysUser sysUser, List<String> roleCodes) {
        LoginUserInfoVO userInfo = new LoginUserInfoVO();
        userInfo.setId(sysUser.getId());
        userInfo.setUsername(sysUser.getUsername());
        userInfo.setRealName(sysUser.getRealName());
        userInfo.setRoles(roleCodes);
        return userInfo;
    }

    private ProfileInfoVO buildProfileInfo(SysUser sysUser, List<String> roleCodes) {
        ProfileInfoVO profileInfo = new ProfileInfoVO();
        profileInfo.setId(sysUser.getId());
        profileInfo.setUsername(sysUser.getUsername());
        profileInfo.setRealName(sysUser.getRealName());
        profileInfo.setRoles(roleCodes);
        profileInfo.setStudentNo(sysUser.getStudentNo());
        profileInfo.setClassId(sysUser.getClassId());
        profileInfo.setClassName(resolveClassName(sysUser.getClassId()));
        profileInfo.setPhone(sysUser.getPhone());
        profileInfo.setEmail(sysUser.getEmail());
        profileInfo.setStatus(sysUser.getStatus());
        return profileInfo;
    }

    private String resolveClassName(Long classId) {
        if (classId == null) {
            return null;
        }
        EduClass eduClass = eduClassService.getById(classId);
        return eduClass == null ? null : eduClass.getClassName();
    }

    private boolean isPasswordMatched(String rawPassword, SysUser sysUser) {
        String storedPassword = sysUser.getPassword();
        if (storedPassword == null || storedPassword.isBlank()) {
            return false;
        }

        if (isEncodedPassword(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        if (!Objects.equals(storedPassword, rawPassword)) {
            return false;
        }

        upgradePassword(sysUser, rawPassword);
        return true;
    }

    private boolean isEncodedPassword(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }

    private void upgradePassword(SysUser sysUser, String rawPassword) {
        SysUser updateUser = new SysUser();
        updateUser.setId(sysUser.getId());
        updateUser.setPassword(passwordEncoder.encode(rawPassword));
        sysUserService.updateById(updateUser);
        sysUser.setPassword(updateUser.getPassword());
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }

    private boolean isTokenBlacklisted(String token) {
        String tokenId = jwtTokenUtil.getTokenIdFromToken(token);
        return tokenId != null && Boolean.TRUE.equals(stringRedisTemplate.hasKey(TOKEN_BLACKLIST_KEY_PREFIX + tokenId));
    }

    private void blacklistToken(String token) {
        String tokenId = jwtTokenUtil.getTokenIdFromToken(token);
        long remainingMillis = jwtTokenUtil.getRemainingExpiration(token);
        if (tokenId == null || remainingMillis <= 0) {
            return;
        }
        stringRedisTemplate.opsForValue().set(
                TOKEN_BLACKLIST_KEY_PREFIX + tokenId,
                "1",
                Duration.ofMillis(remainingMillis)
        );
    }

    private Long getTokenVersion(Long userId) {
        String value = stringRedisTemplate.opsForValue().get(TOKEN_VERSION_KEY_PREFIX + userId);
        return value == null ? 0L : Long.parseLong(value);
    }

    private void increaseTokenVersion(Long userId) {
        stringRedisTemplate.opsForValue().increment(TOKEN_VERSION_KEY_PREFIX + userId);
    }
}
