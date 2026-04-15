package com.project.ExamResultBackend.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentSaveRequestDTO {
    String name;
    Long registrationNumber;
    String section;
    String departmentId;
    Integer joiningYear;
}
