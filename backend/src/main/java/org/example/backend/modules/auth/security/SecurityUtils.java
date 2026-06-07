package org.example.backend.modules.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUserPrincipal loginUserPrincipal) {
            return loginUserPrincipal;
        }
        return null;
    }

    public static Long getCurrentUserId() {
        LoginUserPrincipal currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getUserId() : null;
    }

    public static String getCurrentUsername() {
        LoginUserPrincipal currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getUsername() : null;
    }
}