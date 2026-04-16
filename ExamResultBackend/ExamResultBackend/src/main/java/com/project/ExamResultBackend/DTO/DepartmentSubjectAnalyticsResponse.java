package com.project.ExamResultBackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentSubjectAnalyticsResponse {
    private String departmentCode;
    private Integer joiningYear;
    private String subjectCode;
    private Double averageMarks;
    private Integer highestMarks;
    private Integer passCount;
    private Integer failCount;
    private Integer semester;
}