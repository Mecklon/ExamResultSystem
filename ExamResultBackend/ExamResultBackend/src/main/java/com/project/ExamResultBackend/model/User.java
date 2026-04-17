package com.project.ExamResultBackend.model;
import com.project.ExamResultBackend.model.types.Role;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;
    @Indexed(unique = true)
    private Long registrationNumber;
    private String password;
    private Role role;
    private String studentId;
}