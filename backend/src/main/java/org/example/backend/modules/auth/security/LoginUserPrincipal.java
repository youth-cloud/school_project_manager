package org.example.backend.modules.auth.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginUserPrincipal {
    private Long userId;
    private String username;
    private List<String> roles;
}
