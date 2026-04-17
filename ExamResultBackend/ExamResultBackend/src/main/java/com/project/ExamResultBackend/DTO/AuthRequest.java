package com.project.ExamResultBackend.DTO;

import lombok.Data;

@Data
public class AuthRequest {
    private Long registrationNumber;
    private String password;
}