package com.project.ExamResultBackend.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "Student")
@CompoundIndex(def = "{'registrationNumber': 1}", unique = true)
@CompoundIndex(def = "{'departmentId': 1, 'cgpa': -1}")
@CompoundIndex(def = "{'departmentId': 1, 'section': 1, 'cgpa': -1}")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Student {
    @Id
    private String id;
    private String name;
    private Long registrationNumber;
    private Character section;
    private Double cgpa;
    private Boolean isTopper;
    private String departmentId;
    private Integer totalCredits;
    private Double totalWeightGradeSum;

    private Integer overAllClassRank;
    private Integer departmentRank;
    private Boolean departmentTopper;
}

