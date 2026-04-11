package com.project.ExamResultBackend.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Department")
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
}
