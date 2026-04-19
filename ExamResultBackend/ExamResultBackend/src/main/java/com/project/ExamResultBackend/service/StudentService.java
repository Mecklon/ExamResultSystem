package com.project.ExamResultBackend.service;

import com.project.ExamResultBackend.DTO.PredictionDTO;
import com.project.ExamResultBackend.DTO.StudentSaveRequestDTO;
import com.project.ExamResultBackend.DTO.StudentSaveResponse;
import com.project.ExamResultBackend.model.Student;
import com.project.ExamResultBackend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class StudentService {
    private final UtilityService utilityService;
    private final StudentRepository studentRepository;

    public List<StudentSaveResponse> saveStudents(List<StudentSaveRequestDTO> studentSaveRequestDTOs) {
        List<StudentSaveResponse> studentSaveResponses = new ArrayList<>();
        for(StudentSaveRequestDTO studentSaveRequestDTO: studentSaveRequestDTOs){
            utilityService.saveSingleStudent(studentSaveRequestDTO, studentSaveResponses);
        }
        return studentSaveResponses;
    }



    public  List<PredictionDTO> getPrediction(String prefix) {
        long prefixNum = Long.parseLong(prefix);

        long multiplier = (long) Math.pow(10, 12 - prefix.length());

        long start = prefixNum * multiplier;
        long end = start + multiplier;

        List<Student> students =
                studentRepository.findByRegistrationNumberBetween(start, end, PageRequest.of(0, 10));
        return students.stream().map(student -> {
            return new PredictionDTO(student.getRegistrationNumber(),
                    student.getName(),
                    student.getDepartmentId());
        }).toList();
    }
}
