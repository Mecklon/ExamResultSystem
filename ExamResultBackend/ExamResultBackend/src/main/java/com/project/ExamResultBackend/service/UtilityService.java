package com.project.ExamResultBackend.service;


import com.project.ExamResultBackend.DTO.*;
import com.project.ExamResultBackend.model.Marks;
import com.project.ExamResultBackend.model.Result;
import com.project.ExamResultBackend.model.Student;
import com.project.ExamResultBackend.model.SubjectSnapshot;
import com.project.ExamResultBackend.repository.ResultRepository;
import com.project.ExamResultBackend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UtilityService {
    private final StudentRepository studentRepository;
    private final ResultRepository resultRepository;
    public static final Map<String, SubjectSnapshot> SUBJECT_MAP = Map.of(
            "CS101", SubjectSnapshot.builder()
                    .name("DBMS")
                    .code("CS101")
                    .totalInternalMarks(40)
                    .totalExternalMarks(60)
                    .credits(4)
                    .build(),

            "CS102", SubjectSnapshot.builder()
                    .name("Operating Systems")
                    .code("CS102")
                    .totalInternalMarks(40)
                    .totalExternalMarks(60)
                    .credits(3)
                    .build(),

            "CS103", SubjectSnapshot.builder()
                    .name("Computer Networks")
                    .code("CS103")
                    .totalInternalMarks(40)
                    .totalExternalMarks(60)
                    .credits(3)
                    .build()
    );

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
    public void saveSingleResult(ResultDTO resultDTO, List<ResultSaveResponse> resultSaveResponse){

        if (resultDTO.getMarksList() == null || resultDTO.getMarksList().isEmpty()) {
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Empty result list",""));
            return;
        }
        if (resultDTO.getRegistrationNumber()==null) {
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",null,"null registration number",""));
            return;
        }
        Optional<Student> studentRequest = studentRepository.findByRegistrationNumber(resultDTO.getRegistrationNumber());
        if(studentRequest.isEmpty()){
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Student does not exist",""));
            return;
        }
        Student savedStudent = studentRequest.get();

        ArrayList<Marks> marksList = new ArrayList<>();
        for (MarksDTO marks : resultDTO.getMarksList()) {
            if (!SUBJECT_MAP.containsKey(marks.getSubjectId())) {
                resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"This subject does not exist",""));
                return;
            }
            SubjectSnapshot currentSubjectSnapshot = SUBJECT_MAP.get(marks.getSubjectId());
            if (currentSubjectSnapshot.getTotalExternalMarks() < marks.getExternalMarks() ||
                    currentSubjectSnapshot.getTotalInternalMarks() < marks.getInternalMarks() ||
                    marks.getInternalMarks() < 0 || marks.getExternalMarks() < 0) {
                resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Invalid marks range",""));
                return;
            }
            marksList.add(Marks.builder()
                    .internalMarks(marks.getInternalMarks())
                    .externalMarks(marks.getExternalMarks())
                    .grade(calculateGrade(marks.getInternalMarks() + marks.getExternalMarks(), currentSubjectSnapshot.getTotalExternalMarks() + currentSubjectSnapshot.getTotalInternalMarks()))
                    .subjectSnapshot(currentSubjectSnapshot)
                    .build());
        }
        double weightedSum = 0;
        int totalCredits = 0;

        for (Marks marks : marksList) {
            int gradePoint = getGradePoint(marks.getGrade());
            int credits = marks.getSubjectSnapshot().getCredits();
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
                .department(savedStudent.getDepartment())
                .totalCredits(totalCredits)
                .class_rank(0)
                .department_rank(0)
                .department_topper(false)
                .build();

        Result oldResult = resultRepository.findByStudentIdAndSemester(savedStudent.getId(), resultDTO.getSemester());
        if(oldResult!=null){
            savedStudent.setTotalCredits(savedStudent.getTotalCredits()- oldResult.getTotalCredits());
            savedStudent.setTotalWeightGradeSum(savedStudent.getTotalWeightGradeSum()- oldResult.getSgpa()* oldResult.getTotalCredits());
            resultRepository.delete(oldResult);
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
        resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "SUCCESS",resultDTO.getRegistrationNumber(),"",savedStudent.getDepartment()));
    }

    @Transactional
    public void saveSingleStudent(StudentSaveRequestDTO studentSaveRequestDTO, List<StudentSaveResponse> studentSaveResponses){
        if(studentSaveRequestDTO.getName()==null|| studentSaveRequestDTO.getRegistrationNumber()==null ||studentSaveRequestDTO.getSection()==null || studentSaveRequestDTO.getDepartment()==null){
            studentSaveResponses.add(new StudentSaveResponse(null, "FAIlED", studentSaveRequestDTO.getRegistrationNumber(), "Incomplete student details"));
            return;
        }
        Student newStudent = Student.builder()
                .name(studentSaveRequestDTO.getName())
                .registrationNumber(studentSaveRequestDTO.getRegistrationNumber())
                .section(studentSaveRequestDTO.getSection())
                .department(studentSaveRequestDTO.getDepartment())
                .cgpa(0.0)
                .isTopper(false)
                .totalCredits(0)
                .totalWeightGradeSum(0.0)
                .class_rank(0)
                .department_rank(0)
                .department_topper(false)
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


    }

    @Transactional
    public void recomputeSemesterWiseRank(String department, Integer semester) {
    }
}
