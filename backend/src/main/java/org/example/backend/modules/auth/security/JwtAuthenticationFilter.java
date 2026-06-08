package org.example.backend.modules.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.common.jwt.JwtTokenUtil;
import org.example.backend.modules.system.entity.SysUser;
import org.example.backend.modules.system.service.SysUserService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String TOKEN_VERSION_KEY_PREFIX = "auth:token:version:";
    private static final String TOKEN_BLACKLIST_KEY_PREFIX = "auth:token:blacklist:";

    private final JwtTokenUtil jwtTokenUtil;
    private final SysUserService sysUserService;
    private final StringRedisTemplate stringRedisTemplate;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil,
                                   SysUserService sysUserService,
                                   StringRedisTemplate stringRedisTemplate) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.sysUserService = sysUserService;
        this.stringRedisTemplate = stringRedisTemplate;
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
        String tokenId = jwtTokenUtil.getTokenIdFromToken(token);
        Long tokenVersion = jwtTokenUtil.getTokenVersionFromToken(token);
        if (tokenId == null || Boolean.TRUE.equals(stringRedisTemplate.hasKey(TOKEN_BLACKLIST_KEY_PREFIX + tokenId))) {
            filterChain.doFilter(request, response);
            return;
        }

        SysUser sysUser = userId != null ? sysUserService.getById(userId) : null;
        if (sysUser == null || !Integer.valueOf(1).equals(sysUser.getStatus())) {
            filterChain.doFilter(request, response);
            return;
        }

        String currentVersionValue = stringRedisTemplate.opsForValue().get(TOKEN_VERSION_KEY_PREFIX + userId);
        Long currentTokenVersion = currentVersionValue == null ? 0L : Long.parseLong(currentVersionValue);
        if (!Objects.equals(tokenVersion, currentTokenVersion)) {
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