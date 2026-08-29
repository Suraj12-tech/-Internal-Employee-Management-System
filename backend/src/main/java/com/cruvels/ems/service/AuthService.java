package com.cruvels.ems.service;

import com.cruvels.ems.dto.LoginRequest;
import com.cruvels.ems.dto.LoginResponse;
import com.cruvels.ems.model.User;
import com.cruvels.ems.repository.UserRepository;
import com.cruvels.ems.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        // This line does the actual password check (compares hash internally).
        // If email doesn't exist OR password is wrong, it throws BadCredentialsException,
        // which GlobalExceptionHandler turns into a generic 401 - so we never reveal
        // whether it was the email or the password that was incorrect.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new LoginResponse(token, user.getId(), user.getName(), user.getRole());
    }
}
