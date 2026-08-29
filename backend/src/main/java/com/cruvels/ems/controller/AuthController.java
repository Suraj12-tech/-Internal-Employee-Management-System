package com.cruvels.ems.controller;

import com.cruvels.ems.dto.LoginRequest;
import com.cruvels.ems.dto.LoginResponse;
import com.cruvels.ems.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Logout is stateless with JWT - the frontend simply deletes the token.
    // This endpoint exists mainly for a clean API contract / future token-blacklisting.
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }
}
