package com.project.ExamResultBackend.service;


import com.project.ExamResultBackend.DTO.MarksDTO;
import com.project.ExamResultBackend.DTO.ResultOutputDTO;
import com.project.ExamResultBackend.DTO.ResultSaveResponse;
import com.project.ExamResultBackend.DTO.ResultDTO;
import com.project.ExamResultBackend.model.Department;
import com.project.ExamResultBackend.model.Result;
import com.project.ExamResultBackend.model.Student;
import com.project.ExamResultBackend.model.Subject;
import com.project.ExamResultBackend.repository.DepartmentRepository;
import com.project.ExamResultBackend.repository.ResultRepository;
import com.project.ExamResultBackend.repository.StudentRepository;
import com.project.ExamResultBackend.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ResultService {
    private final UtilityService utilityService;
    private final RedisService redisService;
    private final SubjectRepository subjectRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final ResultRepository resultRepository;
    private final ObjectMapper objectMapper;
    public ArrayList<ResultSaveResponse> bulkSaveResults(List<ResultDTO> ResultDTOS) {


        Object cachedSubjects = redisService.get("AllSubjects", Object.class);

        ArrayList<Subject> allSubject;

        if (cachedSubjects == null) {
            allSubject = new ArrayList<>(subjectRepository.findAll());
            redisService.set("AllSubjects", allSubject, 60 * 60L);
        } else {
            allSubject = objectMapper.convertValue(
                    cachedSubjects,
                    new TypeReference<ArrayList<Subject>>() {}
            );
        }
        HashMap<String, Subject> subjectMap = new HashMap<>();
        for(Subject subject: allSubject){
            subjectMap.put(subject.getCode(), subject);
        }
        Object cachedDepartments = redisService.get("AllDepartments", Object.class);

        ArrayList<Department> allDepartments;

        if (cachedDepartments == null) {
            allDepartments = new ArrayList<>(departmentRepository.findAll());
            redisService.set("AllDepartments", allDepartments, 60 * 60L);
        } else {
            allDepartments = objectMapper.convertValue(
                    cachedDepartments,
                    new TypeReference<ArrayList<Department>>() {}
            );
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
            if(currResponse.getStatus().equals("SUCCESS") && !recomputedRankSet.contains(currResponse.getDepartmentId()+":"+currResult.getSemester()+":"+currResponse.getJoiningYear())){
                recomputedRankSet.add(currResponse.getDepartmentId()+":"+currResult.getSemester()+":"+currResponse.getJoiningYear());
                utilityService.recomputeSemesterWiseRank(currResponse.getDepartmentId(), currResult.getSemester(), currResponse.getJoiningYear());
            }
            if(currResponse.getStatus().equals("SUCCESS") && !recomputedRankSet.contains(currResponse.getDepartmentId()+":"+currResponse.getJoiningYear())){
                recomputedRankSet.add(currResponse.getDepartmentId()+":"+currResponse.getJoiningYear());
                utilityService.recomputeOverallRank(currResponse.getDepartmentId(), currResponse.getJoiningYear());
            }
        }
        return resultSaveResponse;
    }


    public List<ResultOutputDTO> getStudentResult(Long registrationNumber) {
        Object cachedResultObject = redisService.get("result:"+registrationNumber, Object.class);
        if(cachedResultObject!=null){
            List<ResultOutputDTO> cachedResult = objectMapper.convertValue(
                    cachedResultObject,
                    new TypeReference<ArrayList<ResultOutputDTO>>() {}
            );
            return cachedResult;
        }
        Optional<Student> studentFetch = studentRepository.findByRegistrationNumber(registrationNumber);
        if(studentFetch.isEmpty()){
            throw new RuntimeException("Invalid registration Number student not found");
        }
        List<Result> results = resultRepository.findByStudentId(studentFetch.get().getId());
        List<ResultOutputDTO> resultsDtos = results.stream().map(result -> {
            return new ResultOutputDTO(
                    result.getSemester(),
                    result.getMarksList()
            );
        }).toList();

        redisService.set("result:"+registrationNumber, resultsDtos, 60*10L);
        return resultsDtos;
    }
}
