package com.project.ExamResultBackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassFailResponse {
    private String departmentCode;
    private Integer joiningYear;
    private Integer passCount;
    private Integer failCount;
}