package com.project.ExamResultBackend.service;

import com.project.ExamResultBackend.DTO.StudentSaveRequestDTO;
import com.project.ExamResultBackend.DTO.StudentSaveResponse;
import com.project.ExamResultBackend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class StudentService {
    private final UtilityService utilityService;
    public List<StudentSaveResponse> saveStudents(List<StudentSaveRequestDTO> studentSaveRequestDTOs) {
        List<StudentSaveResponse> studentSaveResponses = new ArrayList<>();
        for(StudentSaveRequestDTO studentSaveRequestDTO: studentSaveRequestDTOs){
            utilityService.saveSingleStudent(studentSaveRequestDTO, studentSaveResponses);
        }
        return studentSaveResponses;
    }
}
