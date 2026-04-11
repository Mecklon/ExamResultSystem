package com.project.ExamResultBackend.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "SubjectSnapshot")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class SubjectSnapshot {
    private String name;
    private String code;
    private Integer totalInternalMarks;
    private Integer totalExternalMarks;
    private Integer credits;

}
