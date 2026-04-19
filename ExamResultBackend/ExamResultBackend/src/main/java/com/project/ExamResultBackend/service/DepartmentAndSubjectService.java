package com.project.ExamResultBackend.service;


import com.project.ExamResultBackend.DTO.*;
import com.project.ExamResultBackend.model.Department;
import com.project.ExamResultBackend.model.Subject;
import com.project.ExamResultBackend.repository.DepartmentRepository;
import com.project.ExamResultBackend.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DepartmentAndSubjectService {

    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;


    @Transactional
    public void saveDepartment(DepartmentSaveRequest departmentSaveRequest) {
        if(departmentSaveRequest.getCode() == null || departmentSaveRequest.getName()==null || departmentSaveRequest.getDuration()==null){
            throw new RuntimeException("Incomplete request");
        }
        if(departmentSaveRequest.getDuration()*2!=departmentSaveRequest.getSubjectList().size()){
            throw new RuntimeException("Invalid subject list according to duration");
        }

        ArrayList<Subject> subjects = new ArrayList<>(subjectRepository.findAll());
        HashMap<String, Subject> subjectHashMap = new HashMap<>();
        for(Subject subject: subjects){
            subjectHashMap.put(subject.getCode(), subject);
        }

        Optional<Department> optionalDepartment = departmentRepository.findByCode(departmentSaveRequest.getCode());
        Department newDepartment = null;
        if(optionalDepartment.isPresent()){
            newDepartment = optionalDepartment.get();
            newDepartment.setCode(departmentSaveRequest.getCode());
            newDepartment.setName(departmentSaveRequest.getName());
            newDepartment.setDuration(departmentSaveRequest.getDuration());
        }else{
            newDepartment = Department.builder()
                    .code(departmentSaveRequest.getCode())
                    .name(departmentSaveRequest.getName())
                    .duration(departmentSaveRequest.getDuration())
                    .build();
        }

        Set<String> seenInDept = new HashSet<>();

        for (List<SubjectDTO> sem : departmentSaveRequest.getSubjectList()) {
            if(sem.isEmpty()) throw new RuntimeException("Empty semester subject list");
            for (SubjectDTO dto : sem) {
                if (!seenInDept.add(dto.getCode())) {
                    throw new RuntimeException("Duplicate subject in department");
                }
            }
        }

        ArrayList<ArrayList<String>> newSubjectsReference = new ArrayList<>();
        Map<String, Subject> subjectToSave = new HashMap<>();
        newDepartment.setSubjectCodes(newSubjectsReference);

        for(ArrayList<SubjectDTO> semesterSubjects: departmentSaveRequest.getSubjectList()){
            newSubjectsReference.add(new ArrayList<>());
            for(SubjectDTO subject : semesterSubjects){
                Subject newSubject;
                if(subject.isNew()){
                    if(subjectHashMap.containsKey(subject.getCode())){
                        throw new RuntimeException("Subject already exists");
                    }
                    newSubject = Subject.builder()
                            .name(subject.getName())
                            .code(subject.getCode())
                            .totalExternalMarks(subject.getTotalExternalMarks())
                            .totalInternalMarks(subject.getTotalInternalMarks())
                            .credits(subject.getCredits())
                            .build();

                }else{
                    if(!subjectHashMap.containsKey(subject.getCode())){
                        throw new RuntimeException(("Subject does not exist, update not possible"));
                    }
                    newSubject = subjectHashMap.get(subject.getCode());
                    newSubject.setTotalInternalMarks(subject.getTotalInternalMarks());
                    newSubject.setTotalExternalMarks(subject.getTotalExternalMarks());
                    newSubject.setCredits(subject.getCredits());
                    newSubject.setName(subject.getName());
                }

                subjectToSave.put(newSubject.getCode(), newSubject);
                newSubjectsReference.getLast().add(newSubject.getCode());
            }
        }

        subjectRepository.saveAll(subjectToSave.values());
        departmentRepository.save(newDepartment);
        redisService.delete("AllSubjects");
        redisService.delete("AllDepartments");

    }

    public DepartmentInfo getDepartments() {
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

        List<DepartmentDTO> departmentDTOS = allDepartments.stream().map(department -> {
            return new DepartmentDTO(
                    department.getCode(),
                    department.getName(),
                    department.getDuration(),
                    department.getSubjectCodes()
            );
        }).toList();

        List<SubjectInfoDTO> subjectDTOS = allSubject.stream().map(subject ->{
            return new SubjectInfoDTO(
                    subject.getName(),
                    subject.getCode(),
                    subject.getTotalInternalMarks(),
                    subject.getTotalExternalMarks(),
                    subject.getCredits()
            );
        }).toList();
        redisService.delete("AllSubjects");
        redisService.delete("AllDepartments");
        return new DepartmentInfo(departmentDTOS, subjectDTOS);


    }

    @Transactional
    public void deleteDepartment(String departmentCode) {
        if(!departmentRepository.existsByCode(departmentCode)){
            throw new RuntimeException("This department does not exist");
        }

        departmentRepository.deleteByCode(departmentCode);
    }
}
