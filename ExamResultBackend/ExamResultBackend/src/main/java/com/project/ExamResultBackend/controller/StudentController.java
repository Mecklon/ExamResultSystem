package com.project.ExamResultBackend.controller;


import com.project.ExamResultBackend.DTO.StudentSaveRequestDTO;
import com.project.ExamResultBackend.DTO.StudentSaveResponse;
import com.project.ExamResultBackend.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;

    @PostMapping("/admin/addStudent")
    public ResponseEntity<List<StudentSaveResponse>> saveStudents(@RequestBody List<StudentSaveRequestDTO> studentSaveRequestDTOs){
        List<StudentSaveResponse> response = null;
            response = studentService.saveStudents(studentSaveRequestDTOs);
            return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
