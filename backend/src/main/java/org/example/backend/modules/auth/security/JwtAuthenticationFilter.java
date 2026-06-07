package org.example.backend.modules.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.common.jwt.JwtTokenUtil;
import org.example.backend.modules.system.entity.SysUser;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final SysUserService sysUserService;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, SysUserService sysUserService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.sysUserService = sysUserService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);
        if (!jwtTokenUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = jwtTokenUtil.getUserIdFromToken(token);
        String username = jwtTokenUtil.getUsernameFromToken(token);
        SysUser sysUser = userId != null ? sysUserService.getById(userId) : null;
        if (sysUser == null || !Integer.valueOf(1).equals(sysUser.getStatus())) {
            filterChain.doFilter(request, response);
            return;
        }

        List<String> roleCodes = sysUserService.getRoleCodes(userId);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            LoginUserPrincipal principal = new LoginUserPrincipal(userId, username, roleCodes);

            List<SimpleGrantedAuthority> authorities = roleCodes.stream()
                    .map(roleCode -> new SimpleGrantedAuthority("ROLE_" + roleCode))
                    .toList();

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }
}