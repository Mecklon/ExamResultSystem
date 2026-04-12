package com.project.ExamResultBackend.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentSaveRequest {
    String code;
    String name;
    Integer duration;
    ArrayList<ArrayList<SubjectDTO>> subjectList = new ArrayList<>();
}
