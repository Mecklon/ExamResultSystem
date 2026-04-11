package com.project.ExamResultBackend.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarksDTO {
    String subjectId;
    Integer internalMarks;
    Integer externalMarks;
}
