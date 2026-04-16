package com.project.ExamResultBackend.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "SubjectSnapshot")
public class SubjectSnapshot {
    private String name;
    private String code;
    private Integer totalInternalMarks;
    private Integer totalExternalMarks;
    private Integer credits;
}
