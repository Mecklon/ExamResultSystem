package com.project.ExamResultBackend.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "Result")
@CompoundIndex(def = "{'studentId': 1, 'semester': 1}", unique = true)
@CompoundIndex(def = "{'departmentId': 1, 'semester': 1, 'sgpa': -1}")
@CompoundIndex(def = "{'departmentId': 1, 'semester': 1, 'section': 1, 'sgpa': -1}")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {
    @Id
    private String id;
    private ArrayList<Marks> marksList;
    private Integer semester;
    private Double sgpa;
    private String studentId;
    private String departmentId;
    private String section;
    private Integer totalCredits;
    private Integer classRank;
    private Integer departmentRank;
    private Boolean departmentTopper;
    private Integer joiningYear;
}
