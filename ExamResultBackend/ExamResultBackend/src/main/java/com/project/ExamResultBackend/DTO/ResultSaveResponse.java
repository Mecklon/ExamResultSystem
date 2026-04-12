package com.project.ExamResultBackend.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultSaveResponse {
    String studentId;
    String status;
    Long registrationNumber;
    String message;
    String departmentId;
}
