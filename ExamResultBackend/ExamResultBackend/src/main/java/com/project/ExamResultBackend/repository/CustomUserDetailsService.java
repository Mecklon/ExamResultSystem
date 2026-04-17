package com.project.ExamResultBackend.repository;

import com.project.ExamResultBackend.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String registrationNumber) {
        User user = userRepository.findByRegistrationNumber(Long.parseLong(registrationNumber))
                .orElseThrow(() -> new RuntimeException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(String.valueOf(user.getRegistrationNumber()))
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}