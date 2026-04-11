package com.project.ExamResultBackend.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "Subject")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Subject {
    @Id
    private String id;                // courseCode (CS101)
    private String name;
    private ArrayList<String> departmentIds = new ArrayList<>();
    private int credits;
    private int totalInternalMarks;
    private int totalExternalMarks;
}
