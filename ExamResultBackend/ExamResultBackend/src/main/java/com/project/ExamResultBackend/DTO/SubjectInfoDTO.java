package com.project.ExamResultBackend.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubjectInfoDTO {
    private String name;
    private String code;
    private Integer totalInternalMarks;
    private Integer totalExternalMarks;
    private Integer credits;
}
