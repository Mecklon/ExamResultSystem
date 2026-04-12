package com.project.ExamResultBackend.service;


import com.project.ExamResultBackend.DTO.*;
import com.project.ExamResultBackend.model.*;
import com.project.ExamResultBackend.repository.ResultRepository;
import com.project.ExamResultBackend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UtilityService {
    private final StudentRepository studentRepository;
    private final ResultRepository resultRepository;
    private final MongoTemplate mongoTemplate;
    private final RedisService redisService;


    public char calculateGrade(int marks, int totalMarks) {
        double percentage = (marks * 100.0) / totalMarks;

        if (percentage >= 90) return 'A';
        else if (percentage >= 80) return 'B';
        else if (percentage >= 70) return 'C';
        else if (percentage >= 60) return 'D';
        else return 'F';
    }

    public int getGradePoint(char grade) {
        switch (grade) {
            case 'A':
                return 10;
            case 'B':
                return 8;
            case 'C':
                return 6;
            case 'D':
                return 4;
            default:
                System.out.println("Returning 0");
                return 0;
        }
    }


    @Transactional
    public void saveSingleResult(ResultDTO resultDTO, List<ResultSaveResponse> resultSaveResponse, HashMap<String, Subject> subjectMap, HashMap<String, Department> departmentMap){

        if (resultDTO.getMarksList() == null || resultDTO.getMarksList().isEmpty()) {
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Empty result list",""));return;
        }
        if (resultDTO.getRegistrationNumber()==null) {
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",null,"null registration number",""));return;
        }
        Optional<Student> studentRequest = studentRepository.findByRegistrationNumber(resultDTO.getRegistrationNumber());
        if(studentRequest.isEmpty()){
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Student does not exist",""));return;
        }
        Student savedStudent = studentRequest.get();
        if(!departmentMap.containsKey(savedStudent.getDepartmentId())){
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Department does not exist",""));return;
        }
        ArrayList<ArrayList<String>> allSemesterSubjectList = departmentMap.get(savedStudent.getDepartmentId()).getSubjectCodes();
        if (resultDTO.getSemester() <= 0 || resultDTO.getSemester() > allSemesterSubjectList.size()) {
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Invalid semester index",""));return;
        }
        ArrayList<String> currentSemesterSubjectList = allSemesterSubjectList.get(resultDTO.getSemester()-1);
        HashSet<String> currSemesterSubjetHashMap = new HashSet<>(currentSemesterSubjectList);
        if(currentSemesterSubjectList.size()!=resultDTO.getMarksList().size()){
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Missing subject details",""));return;
        }
        ArrayList<Marks> marksList = new ArrayList<>();
        Set<String> seenSubjects = new HashSet<>();
        for(MarksDTO marks: resultDTO.getMarksList()){
            if(marks.getCode()==null  || marks.getInternalMarks()== null || marks.getExternalMarks() == null){
                resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Incomplete Marks detail",""));return;
            }
            if (!seenSubjects.add(marks.getCode())) {
                resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Duplicate subject details",""));return;
            }
            if(!subjectMap.containsKey(marks.getCode())){
                resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Subject does not exist",""));return;
            }
            if(!currSemesterSubjetHashMap.contains(marks.getCode())){
                resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Department subject miss match",""));return;
            }
            Subject currentSubject = subjectMap.get(marks.getCode());
            if (currentSubject.getTotalExternalMarks() < marks.getExternalMarks() ||
                    currentSubject.getTotalInternalMarks() < marks.getInternalMarks() ||
                    marks.getInternalMarks() < 0 || marks.getExternalMarks() < 0) {
                resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Invalid marks range",""));
                return;
            }
            marksList.add(Marks.builder()
                    .internalMarks(marks.getInternalMarks())
                    .externalMarks(marks.getExternalMarks())
                    .grade(calculateGrade(marks.getInternalMarks() + marks.getExternalMarks(), currentSubject.getTotalExternalMarks() + currentSubject.getTotalInternalMarks()))
                    .subject(currentSubject)
                    .build());
        }

        double weightedSum = 0;
        int totalCredits = 0;

        for (Marks marks : marksList) {
            int gradePoint = getGradePoint(marks.getGrade());
            int credits = marks.getSubject().getCredits();
            if (credits == 0) continue;
            weightedSum += gradePoint * credits;
            totalCredits += credits;
        }
        if (totalCredits == 0) {
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Total credits cannot be zero",""));
            return;
        }
        double sgpa = weightedSum / totalCredits;

        Result newResult = Result.builder()
                .marksList(marksList)
                .semester(resultDTO.getSemester())
                .sgpa(sgpa)
                .studentId(savedStudent.getId())
                .departmentId(savedStudent.getDepartmentId())
                .totalCredits(totalCredits)
                .classRank(0)
                .departmentRank(0)
                .departmentTopper(false)
                .section(savedStudent.getSection())
                .build();

        Result oldResult = resultRepository.findByStudentIdAndSemester(savedStudent.getId(), resultDTO.getSemester());
        if(oldResult!=null){
            savedStudent.setTotalCredits(savedStudent.getTotalCredits()- oldResult.getTotalCredits());
            savedStudent.setTotalWeightGradeSum(savedStudent.getTotalWeightGradeSum()- oldResult.getSgpa()* oldResult.getTotalCredits());
            newResult.setId(oldResult.getId());
        }

        double newTotalWeightedSum = savedStudent.getTotalWeightGradeSum() + weightedSum ;
        int newTotalCredits = savedStudent.getTotalCredits() + totalCredits;
        double newCGPA = newTotalWeightedSum / newTotalCredits;
        savedStudent.setCgpa(newCGPA);
        savedStudent.setTotalCredits(newTotalCredits);
        savedStudent.setTotalWeightGradeSum(newTotalWeightedSum);

        try {
            resultRepository.save(newResult);
            studentRepository.save(savedStudent);
        } catch (DataIntegrityViolationException e) {
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Duplicate entry of semester",""));
            return;
        }
        resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "SUCCESS",resultDTO.getRegistrationNumber(),"",savedStudent.getDepartmentId()));
    }

    @Transactional
    public void saveSingleStudent(StudentSaveRequestDTO studentSaveRequestDTO, List<StudentSaveResponse> studentSaveResponses){
        if(studentSaveRequestDTO.getName()==null|| studentSaveRequestDTO.getRegistrationNumber()==null ||studentSaveRequestDTO.getSection()==null || studentSaveRequestDTO.getDepartmentId()==null){
            studentSaveResponses.add(new StudentSaveResponse(null, "FAIlED", studentSaveRequestDTO.getRegistrationNumber(), "Incomplete student details"));
            return;
        }
        Student newStudent = Student.builder()
                .name(studentSaveRequestDTO.getName())
                .registrationNumber(studentSaveRequestDTO.getRegistrationNumber())
                .section(studentSaveRequestDTO.getSection())
                .departmentId(studentSaveRequestDTO.getDepartmentId())
                .cgpa(0.0)
                .isTopper(false)
                .totalCredits(0)
                .totalWeightGradeSum(0.0)
                .overAllClassRank(0)
                .departmentRank(0)
                .departmentTopper(false)
                .build();
        Student savedStudent=null;
        try{
            savedStudent = studentRepository.save(newStudent);
        }catch (DataIntegrityViolationException e){
            studentSaveResponses.add(new StudentSaveResponse(null, "FAILED", studentSaveRequestDTO.getRegistrationNumber(), "DUPLICATE ENTRY"));
            return;
        }

        studentSaveResponses.add(new StudentSaveResponse(savedStudent.getId(), "SUCCESS", savedStudent.getRegistrationNumber(), ""));
    }

    @Transactional
    public void recomputeOverallRank(String department) {
        List<Document> pipeline = List.of(
                new Document("$match",
                        new Document("departmentId", department)
                ),
                new Document("$setWindowFields",
                        new Document("sortBy", new Document("cgpa", -1))
                                .append("output",
                                        new Document("departmentRank",
                                                new Document("$rank", new Document())
                                        )
                                )
                ),
                new Document("$merge",
                        new Document("into", "Student")
                                .append("whenMatched", "merge")
                                .append("whenNotMatched", "discard")
                )
        );

        mongoTemplate.getCollection("Student")
                .aggregate(pipeline)
                .toCollection();

        pipeline = List.of(
                new Document("$match",
                        new Document("departmentId", department)
                ),
                new Document("$setWindowFields",
                        new Document("partitionBy", "$section")
                                .append("sortBy", new Document("cgpa", -1))
                                .append("output",
                                        new Document("overAllClassRank",
                                                new Document("$rank", new Document())
                                        )
                                )
                ),
                new Document("$merge",
                        new Document("into", "Student")
                                .append("whenMatched", "merge")
                                .append("whenNotMatched", "discard")
                )
        );

        mongoTemplate.getCollection("Student")
                .aggregate(pipeline)
                .toCollection();


        mongoTemplate.getCollection("Student").updateMany(
                new Document("departmentId", department),
                new Document("$set", new Document("departmentTopper", false))
        );

        mongoTemplate.getCollection("Student").updateMany(
                new Document("departmentId", department)
                        .append("departmentRank", 1),
                new Document("$set", new Document("departmentTopper", true))
        );
    }

    @Transactional
    public void recomputeSemesterWiseRank(String department, Integer semester) {
        List<Document> pipeline = List.of(
                new Document("$match",
                        new Document("departmentId", department)
                                .append("semester", semester)
                ),
                new Document("$setWindowFields",
                        new Document("sortBy", new Document("sgpa", -1))
                                .append("output",
                                        new Document("departmentRank",
                                                new Document("$rank", new Document())
                                        )
                                )
                ),
                new Document("$merge",
                        new Document("into", "Result")
                                .append("whenMatched", "merge")
                                .append("whenNotMatched", "discard")
                )
        );

        mongoTemplate.getCollection("Result")
                .aggregate(pipeline)
                .toCollection();


        pipeline = List.of(
                new Document("$match",
                        new Document("departmentId", department)
                                .append("semester", semester)
                ),
                new Document("$setWindowFields",
                        new Document("partitionBy", "$section")
                                .append("sortBy", new Document("sgpa", -1))
                                .append("output",
                                        new Document("classRank",
                                                new Document("$rank", new Document())
                                        )
                                )
                ),
                new Document("$merge",
                        new Document("into", "Result")
                                .append("whenMatched", "merge")
                                .append("whenNotMatched", "discard")
                )
        );

        mongoTemplate.getCollection("Result")
                .aggregate(pipeline)
                .toCollection();


        mongoTemplate.getCollection("Result").updateMany(
                new Document("departmentId", department)
                        .append("semester", semester),
                new Document("$set", new Document("departmentTopper", false))
        );

        mongoTemplate.getCollection("Result").updateMany(
                new Document("departmentId", department)
                        .append("semester", semester)
                        .append("departmentRank", 1),
                new Document("$set", new Document("departmentTopper", true))
        );
    }


}
