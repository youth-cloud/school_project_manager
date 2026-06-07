package org.example.backend.modules.auth.controller;

import jakarta.validation.Valid;
import org.example.backend.common.jwt.JwtTokenUtil;
import org.example.backend.common.result.Result;
import org.example.backend.modules.auth.dto.LoginRequestDTO;
import org.example.backend.modules.auth.security.LoginUserPrincipal;
import org.example.backend.modules.auth.security.SecurityUtils;
import org.example.backend.modules.auth.vo.LoginResponseVO;
import org.example.backend.modules.auth.vo.LoginUserInfoVO;
import org.example.backend.modules.system.entity.SysUser;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final SysUserService sysUserService;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthController(SysUserService sysUserService, JwtTokenUtil jwtTokenUtil) {
        this.sysUserService = sysUserService;
        this.jwtTokenUtil = jwtTokenUtil;
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
        if (!Objects.equals(sysUser.getPassword(), dto.getPassword())) {
            return Result.fail(401, "用户名或密码错误");
        }

        List<String> roleCodes = sysUserService.getRoleCodes(sysUser.getId());
        String token = jwtTokenUtil.generateToken(sysUser.getId(), sysUser.getUsername());

        LoginUserInfoVO userInfo = buildLoginUserInfo(sysUser, roleCodes);

        LoginResponseVO response = new LoginResponseVO();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setUserInfo(userInfo);
        return Result.success("登录成功", response);
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

    private LoginUserInfoVO buildLoginUserInfo(SysUser sysUser, List<String> roleCodes) {
        LoginUserInfoVO userInfo = new LoginUserInfoVO();
        userInfo.setId(sysUser.getId());
        userInfo.setUsername(sysUser.getUsername());
        userInfo.setRealName(sysUser.getRealName());
        userInfo.setRoles(roleCodes);
        return userInfo;
    }
}
