package com.project.ExamResultBackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentAverageResponse {
    private String departmentCode;
    private Integer joiningYear;
    private Double averageCGPA;
}