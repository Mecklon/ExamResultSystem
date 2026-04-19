package com.project.ExamResultBackend.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class PredictionDTO {
    Long RegistrationNumber;
    String name;
    String departmentCode;
}
