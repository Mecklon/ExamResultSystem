package com.project.ExamResultBackend.controller;


import com.project.ExamResultBackend.DTO.DepartmentDTO;
import com.project.ExamResultBackend.DTO.DepartmentInfo;
import com.project.ExamResultBackend.DTO.DepartmentSaveRequest;
import com.project.ExamResultBackend.service.DepartmentAndSubjectService;
import com.project.ExamResultBackend.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DepartmentAndSubjectController {

    private final DepartmentAndSubjectService departmentAndSubjectService;

    @PostMapping("/admin/addDepartment")
    public ResponseEntity<Void> saveDepartment(@RequestBody DepartmentSaveRequest departmentSaveRequest){
        departmentAndSubjectService.saveDepartment(departmentSaveRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/getDepartmentInfo")
    public ResponseEntity<DepartmentInfo> getDepartmentInfo(){
        return ResponseEntity.status(HttpStatus.OK).body(departmentAndSubjectService.getDepartments());
    }

    @DeleteMapping("/admin/deleteDepartment")
    public ResponseEntity<Void> deleteDepartment(String departmentCode){
        departmentAndSubjectService.deleteDepartment(departmentCode);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
