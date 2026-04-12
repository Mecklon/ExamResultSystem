package com.project.ExamResultBackend.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "Department")
@CompoundIndex(def = "{'code': 1}", unique = true)

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Department {
    @Id
    private String id;
    private String code;
    private String name;
    private Integer duration;   //years
    private ArrayList<ArrayList<String>> subjectCodes = new ArrayList<>();
}
