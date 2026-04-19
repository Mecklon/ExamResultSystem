package com.project.ExamResultBackend.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;

@Data
@AllArgsConstructor
public class DepartmentDTO {
    String code;
    String name;
    Integer duration;
    ArrayList<ArrayList<String>> subjectCodes = new ArrayList<>();
}
