package com.project.ExamResultBackend.DTO;


import com.project.ExamResultBackend.model.Marks;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResultOutputDTO {
    Integer semester;
    String section;
    Integer joiningYear;
    ArrayList<Marks> marksList;
}
