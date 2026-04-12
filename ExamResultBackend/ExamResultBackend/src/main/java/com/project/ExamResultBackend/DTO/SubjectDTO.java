package com.project.ExamResultBackend.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubjectDTO {
    boolean isNew;
    String name;
    String code;
    Integer totalInternalMarks;
    Integer totalExternalMarks;
    Integer credits;
}
