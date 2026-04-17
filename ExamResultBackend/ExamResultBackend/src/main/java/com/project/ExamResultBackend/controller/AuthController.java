package com.project.ExamResultBackend.controller;

import com.project.ExamResultBackend.DTO.AuthRequest;
import com.project.ExamResultBackend.DTO.AuthResponse;
import com.project.ExamResultBackend.configuration.JwtUtil;
import com.project.ExamResultBackend.repository.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        String.valueOf(request.getRegistrationNumber()),
                        request.getPassword()
                )
        );

        UserDetails userDetails = userDetailsService
                .loadUserByUsername(String.valueOf(request.getRegistrationNumber()));

        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token);
    }
}
