package com.project.ExamResultBackend.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Marks")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Marks {
    private Integer internalMarks;
    private Integer externalMarks;
    private Character grade;
    private Subject subject;
}
