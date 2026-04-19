package com.project.ExamResultBackend.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DepartmentInfo {
    List<DepartmentDTO> departments;
    List<SubjectInfoDTO> subjects;
}
