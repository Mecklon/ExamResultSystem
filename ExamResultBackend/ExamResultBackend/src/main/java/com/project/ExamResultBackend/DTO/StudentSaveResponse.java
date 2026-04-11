package com.project.ExamResultBackend.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentSaveResponse {
    String studentId;
    String status;
    long registrationNumber;
    String message;
}
