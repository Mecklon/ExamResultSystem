package com.project.ExamResultBackend.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResultDTO {
    String studentId;
    Long registrationNumber;
    Integer semester;
    ArrayList<MarksDTO> marksList;
}
