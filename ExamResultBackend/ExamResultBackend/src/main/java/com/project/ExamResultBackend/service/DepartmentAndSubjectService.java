package com.project.ExamResultBackend.service;


import com.project.ExamResultBackend.DTO.DepartmentSaveRequest;
import com.project.ExamResultBackend.DTO.SubjectDTO;
import com.project.ExamResultBackend.model.Department;
import com.project.ExamResultBackend.model.Subject;
import com.project.ExamResultBackend.repository.DepartmentRepository;
import com.project.ExamResultBackend.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DepartmentAndSubjectService {

    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final RedisService redisService;


    @Transactional
    public void saveDepartment(DepartmentSaveRequest departmentSaveRequest) {
        if(departmentSaveRequest.getCode() == null || departmentSaveRequest.getName()==null || departmentSaveRequest.getDuration()==null){
            throw new RuntimeException("Incomplete request");
        }
//        ArrayList<Subject> subjects = redisService.get("AllSubjects", ArrayList.class);
//        if(subjects == null){
//            subjects = new ArrayList<>(subjectRepository.findAll());
//            redisService.set("AllSubjects", subjects, (long)60*60);
//        }
//        ArrayList<Department> departments = redisService.get("AllDepartments", ArrayList.class);
//        if(departments==null){
//            departments = new ArrayList<>(departmentRepository.findAll());
//            redisService.set("AllDepartments",departments, (long)60*60);
//        }
//
//        HashMap<String, Department> departmentHashMap = new HashMap<>();
        ArrayList<Subject> subjects = new ArrayList<>(subjectRepository.findAll());
        HashMap<String, Subject> subjectHashMap = new HashMap<>();
//        HashMap<String, ArrayList<ArrayList<Subject>>> departmentSubjectMap = new HashMap<>();
        for(Subject subject: subjects){
            subjectHashMap.put(subject.getCode(), subject);
        }
//        for(Department department: departments){
//            departmentHashMap.put(department.getCode(), department);
//            ArrayList<ArrayList<Subject>> semesterList = new ArrayList<>();
//            departmentSubjectMap.put(department.getCode(),semesterList);
//            for(ArrayList<String> semesterSubjectList: department.getSubjectList()){
//                semesterList.add(new ArrayList<>());
//                for(String subjectCode: semesterSubjectList){
//                    semesterList.get(semesterList.size()-1).add(subjectHashMap.get(subjectCode));
//                }
//            }
//        }

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
        newDepartment.setSubjectList(newSubjectsReference);

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
}
