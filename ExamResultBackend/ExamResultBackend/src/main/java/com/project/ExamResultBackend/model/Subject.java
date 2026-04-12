package com.project.ExamResultBackend.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Subject")
@CompoundIndex(def = "{'code': 1}", unique = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Subject {
    @Id
    private String id;
    private String name;
    private String code;
    private Integer totalInternalMarks;
    private Integer totalExternalMarks;
    private Integer credits;

}
