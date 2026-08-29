package com.cruvels.ems.dto;

import com.cruvels.ems.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

// What we send back after a successful login: the JWT token + basic user info
// so the frontend knows what to show (which role, which name).
@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String name;
    private Role role;
}
