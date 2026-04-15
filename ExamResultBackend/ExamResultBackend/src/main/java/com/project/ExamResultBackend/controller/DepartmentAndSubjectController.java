package com.project.ExamResultBackend.controller;


import com.project.ExamResultBackend.DTO.DepartmentSaveRequest;
import com.project.ExamResultBackend.service.DepartmentAndSubjectService;
import com.project.ExamResultBackend.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DepartmentAndSubjectController {

    private final DepartmentAndSubjectService departmentAndSubjectService;

    @PostMapping("/admin/addDepartment")
    public ResponseEntity<Void> saveDepartment(@RequestBody DepartmentSaveRequest departmentSaveRequest){
        departmentAndSubjectService.saveDepartment(departmentSaveRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


}
