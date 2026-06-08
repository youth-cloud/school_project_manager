package org.example.backend.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, 0L);
    }

    public String generateToken(Long userId, String username, Long tokenVersion) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("userId", userId)
                .claim("tokenVersion", tokenVersion == null ? 0L : tokenVersion)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(getSignKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        Object userId = claims.get("userId");
        if (userId instanceof Integer integerUserId) {
            return integerUserId.longValue();
        }
        if (userId instanceof Long longUserId) {
            return longUserId;
        }
        if (userId instanceof String stringUserId) {
            return Long.parseLong(stringUserId);
        }
        return null;
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getTokenIdFromToken(String token) {
        return getClaims(token).getId();
    }

    public Long getTokenVersionFromToken(String token) {
        Object tokenVersion = getClaims(token).get("tokenVersion");
        if (tokenVersion instanceof Integer integerTokenVersion) {
            return integerTokenVersion.longValue();
        }
        if (tokenVersion instanceof Long longTokenVersion) {
            return longTokenVersion;
        }
        if (tokenVersion instanceof String stringTokenVersion) {
            return Long.parseLong(stringTokenVersion);
        }
        return 0L;
    }

    public long getRemainingExpiration(String token) {
        return getClaims(token).getExpiration().getTime() - System.currentTimeMillis();
    }

    public boolean isTokenExpired(String token) {
        Date expirationDate = getClaims(token).getExpiration();
        return expirationDate.before(new Date());
    }

    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}