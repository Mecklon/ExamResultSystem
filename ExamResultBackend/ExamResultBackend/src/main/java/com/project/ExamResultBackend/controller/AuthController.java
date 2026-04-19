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
        System.out.println(request.getPassword());
        System.out.println(request.getRegistrationNumber());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        String.valueOf(request.getRegistrationNumber()),
                        request.getPassword()
                )
        );

        UserDetails userDetails = userDetailsService
                .loadUserByUsername(String.valueOf(request.getRegistrationNumber()));

        String token = jwtUtil.generateToken(userDetails);

        String role = userDetails.getAuthorities()
                .iterator()
                .next()
                .getAuthority(); // e.g. ROLE_ADMIN

        return new AuthResponse(token, userDetails.getUsername(),role,request.getRegistrationNumber() );
    }
}
