package com.project.ExamResultBackend.service;


import com.project.ExamResultBackend.DTO.ResultSaveResponse;
import com.project.ExamResultBackend.DTO.ResultDTO;
import com.project.ExamResultBackend.model.Department;
import com.project.ExamResultBackend.model.Subject;
import com.project.ExamResultBackend.repository.DepartmentRepository;
import com.project.ExamResultBackend.repository.StudentRepository;
import com.project.ExamResultBackend.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultService {
    private final UtilityService utilityService;
    private final RedisService redisService;
    private final SubjectRepository subjectRepository;
    private final DepartmentRepository departmentRepository;

    public ArrayList<ResultSaveResponse> bulkSaveResults(List<ResultDTO> ResultDTOS) {


        ArrayList<Subject> allSubject = redisService.get("AllSubjects", ArrayList.class);
        if(allSubject==null){
            allSubject = new ArrayList<>(subjectRepository.findAll());
            redisService.set("AllSubjects",allSubject, (long)60*60);
        }
        HashMap<String, Subject> subjectMap = new HashMap<>();
        for(Subject subject: allSubject){
            subjectMap.put(subject.getCode(), subject);
        }
        ArrayList<Department> allDepartments = redisService.get("AllDepartments", ArrayList.class);
        if(allDepartments==null){
            allDepartments = new ArrayList<>(departmentRepository.findAll());
            redisService.set("AllDepartments", allDepartments, (long)60*60);
        }
        HashMap<String, Department> departmentMap = new HashMap<>();
        for(Department department: allDepartments){
            departmentMap.put(department.getCode(),department);
        }

        ArrayList<ResultSaveResponse> resultSaveResponse = new ArrayList<>();
        for(ResultDTO resultDTO: ResultDTOS){
            utilityService.saveSingleResult(resultDTO, resultSaveResponse, subjectMap, departmentMap);
        }
        HashSet<String> recomputedRankSet = new HashSet<>();
        for(int i =0;i< resultSaveResponse.size();i++){
            ResultSaveResponse currResponse = resultSaveResponse.get(i);
            ResultDTO currResult = ResultDTOS.get(i);
            if(currResponse.getStatus().equals("SUCCESS") && !recomputedRankSet.contains(currResponse.getDepartmentId()+":"+currResult.getSemester())){
                recomputedRankSet.add(currResponse.getDepartmentId()+":"+currResult.getSemester());
                utilityService.recomputeSemesterWiseRank(currResponse.getDepartmentId(), currResult.getSemester());
            }
            if(currResponse.getStatus().equals("SUCCESS") && !recomputedRankSet.contains(currResponse.getDepartmentId())){
                recomputedRankSet.add(currResponse.getDepartmentId());
                utilityService.recomputeOverallRank(currResponse.getDepartmentId());
            }
        }
        return resultSaveResponse;
    }


}
