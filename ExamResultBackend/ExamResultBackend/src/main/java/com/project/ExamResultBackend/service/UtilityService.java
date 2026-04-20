package com.project.ExamResultBackend.service;


import com.project.ExamResultBackend.DTO.*;
import com.project.ExamResultBackend.model.*;
import com.project.ExamResultBackend.repository.ResultRepository;
import com.project.ExamResultBackend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilityService {
    private final StudentRepository studentRepository;
    private final ResultRepository resultRepository;
    private final MongoTemplate mongoTemplate;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate redisTemplate;



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
                return 0;
        }
    }


    @Transactional
    public void saveSingleResult(ResultDTO resultDTO, List<ResultSaveResponse> resultSaveResponse, HashMap<String, Subject> subjectMap, HashMap<String, Department> departmentMap){

        if (resultDTO.getMarksList() == null || resultDTO.getMarksList().isEmpty()) {
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Empty result list","",null));return;
        }
        if (resultDTO.getRegistrationNumber()==null) {
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",null,"null registration number","",null));return;
        }
        Optional<Student> studentRequest = studentRepository.findByRegistrationNumber(resultDTO.getRegistrationNumber());
        if(studentRequest.isEmpty()){
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Student does not exist","",null));return;
        }
        Student savedStudent = studentRequest.get();
        if(!departmentMap.containsKey(savedStudent.getDepartmentId())){
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),"Department does not exist",savedStudent.getDepartmentId(),savedStudent.getJoiningYear()));return;
        }
        ArrayList<ArrayList<String>> allSemesterSubjectList = departmentMap.get(savedStudent.getDepartmentId()).getSubjectCodes();
        if (resultDTO.getSemester() <= 0 || resultDTO.getSemester() > allSemesterSubjectList.size()) {
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),resultDTO.getSemester()+"Invalid semester index",savedStudent.getDepartmentId(),savedStudent.getJoiningYear()));return;
        }
        ArrayList<String> currentSemesterSubjectList = allSemesterSubjectList.get(resultDTO.getSemester()-1);
        HashSet<String> currSemesterSubjetHashMap = new HashSet<>(currentSemesterSubjectList);
        if(currentSemesterSubjectList.size()!=resultDTO.getMarksList().size()){
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),resultDTO.getSemester()+"Missing subject details",savedStudent.getDepartmentId(),savedStudent.getJoiningYear()));return;
        }
        ArrayList<Marks> marksList = new ArrayList<>();
        Set<String> seenSubjects = new HashSet<>();
        for(MarksDTO marks: resultDTO.getMarksList()){
            if(marks.getCode()==null  || marks.getInternalMarks()== null || marks.getExternalMarks() == null){
                resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),resultDTO.getSemester()+"Incomplete Marks detail",savedStudent.getDepartmentId(),savedStudent.getJoiningYear()));return;
            }
            if (!seenSubjects.add(marks.getCode())) {
                resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),resultDTO.getSemester()+"Duplicate subject details",savedStudent.getDepartmentId(),savedStudent.getJoiningYear()));return;
            }
            if(!subjectMap.containsKey(marks.getCode())){
                resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),resultDTO.getSemester()+"Subject does not exist",savedStudent.getDepartmentId(),savedStudent.getJoiningYear()));return;
            }
            if(!currSemesterSubjetHashMap.contains(marks.getCode())){
                resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),resultDTO.getSemester()+"Department subject miss match",savedStudent.getDepartmentId(),savedStudent.getJoiningYear()));return;
            }
            Subject currentSubject = subjectMap.get(marks.getCode());
            if (currentSubject.getTotalExternalMarks() < marks.getExternalMarks() ||
                    currentSubject.getTotalInternalMarks() < marks.getInternalMarks() ||
                    marks.getInternalMarks() < 0 || marks.getExternalMarks() < 0) {
                resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),resultDTO.getSemester()+"Invalid marks range",savedStudent.getDepartmentId(),savedStudent.getJoiningYear()));
                return;
            }
            marksList.add(Marks.builder()
                    .internalMarks(marks.getInternalMarks())
                    .externalMarks(marks.getExternalMarks())
                    .grade(calculateGrade(marks.getInternalMarks() + marks.getExternalMarks(), currentSubject.getTotalExternalMarks() + currentSubject.getTotalInternalMarks()))
                    .subjectSnapshot(new SubjectSnapshot(currentSubject.getName(), currentSubject.getCode(), currentSubject.getTotalInternalMarks(), currentSubject.getTotalExternalMarks(), currentSubject.getCredits(),resultDTO.getSemester()))
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
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),resultDTO.getSemester()+"Total credits cannot be zero",savedStudent.getDepartmentId(),savedStudent.getJoiningYear()));
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
                .joiningYear(savedStudent.getJoiningYear())
                .name(savedStudent.getName())
                .registrationNumber(savedStudent.getRegistrationNumber())
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
            studentRepository.save(savedStudent);
        } catch (DataIntegrityViolationException e) {
            resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "FAILED",resultDTO.getRegistrationNumber(),resultDTO.getSemester()+"Duplicate entry of semester",savedStudent.getDepartmentId(),savedStudent.getJoiningYear()));
            return;
        }
            resultRepository.save(newResult);
        resultSaveResponse.add(new ResultSaveResponse(resultDTO.getStudentId(), "SUCCESS",resultDTO.getRegistrationNumber(),"",savedStudent.getDepartmentId(),savedStudent.getJoiningYear()));
        updateLeaderboards(savedStudent, newResult);
        redisService.delete("result:"+resultDTO.getRegistrationNumber());
    }

    public void updateLeaderboards(Student student, Result result) {

        String studentId = student.getRegistrationNumber().toString();
        String member = student.getRegistrationNumber() + "|" + student.getName();

        String deptKey = "leaderboard:dept:" +
                student.getDepartmentId() + ":" +
                student.getJoiningYear();

        redisTemplate.opsForZSet().add(
                deptKey,
                member,
                student.getCgpa()
        );

        String semKey = "leaderboard:sem:" +
                student.getDepartmentId() + ":" +
                student.getJoiningYear() + ":" +
                result.getSemester();

        redisTemplate.opsForZSet().add(
                semKey,
                member,
                result.getSgpa()
        );
    }

    @Transactional
    public void saveSingleStudent(StudentSaveRequestDTO studentSaveRequestDTO, List<StudentSaveResponse> studentSaveResponses){


        if(studentSaveRequestDTO.getName()==null|| studentSaveRequestDTO.getRegistrationNumber()==null ||studentSaveRequestDTO.getSection()==null || studentSaveRequestDTO.getDepartmentId()==null|| studentSaveRequestDTO.getJoiningYear()==null){
            studentSaveResponses.add(new StudentSaveResponse(null, "FAIlED", studentSaveRequestDTO.getRegistrationNumber(), "Incomplete student details"));
            return;
        }
        int currentYear = java.time.Year.now().getValue();

        if (studentSaveRequestDTO.getJoiningYear() < 2000 ||
                studentSaveRequestDTO.getJoiningYear() > currentYear) {

            studentSaveResponses.add(
                    new StudentSaveResponse(
                            null,
                            "FAILED",
                            studentSaveRequestDTO.getRegistrationNumber(),
                            "Invalid joining year"
                    )
            );
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
                .joiningYear(studentSaveRequestDTO.getJoiningYear())
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
    public void recomputeOverallRank(String department, Integer joiningYear) {

        List<Document> deptPipeline = List.of(
                new Document("$match",
                        new Document("departmentId", department)
                                .append("joiningYear", joiningYear)
                ),
                new Document("$setWindowFields",
                        new Document("sortBy",
                                new Document("cgpa", -1)   // ✅ ONLY ONE FIELD
                        )
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
                .aggregate(deptPipeline)
                .toCollection();


        List<Document> classPipeline = List.of(
                new Document("$match",
                        new Document("departmentId", department)
                                .append("joiningYear", joiningYear)
                ),
                new Document("$setWindowFields",
                        new Document("partitionBy", "$section")   // ✅ CORRECT
                                .append("sortBy",
                                        new Document("cgpa", -1)
                                )
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
                .aggregate(classPipeline)
                .toCollection();


        mongoTemplate.getCollection("Student").updateMany(
                new Document("departmentId", department)
                        .append("joiningYear", joiningYear),
                new Document("$set", new Document("departmentTopper", false))
        );


        mongoTemplate.getCollection("Student").updateMany(
                new Document("departmentId", department)
                        .append("joiningYear", joiningYear)
                        .append("departmentRank", 1),
                new Document("$set", new Document("departmentTopper", true))
        );
    }
    @Transactional
    public void recomputeSemesterWiseRank(String department, Integer semester, Integer joiningYear) {

        List<Document> deptPipeline = List.of(
                new Document("$match",
                        new Document("departmentId", department)
                                .append("semester", semester)
                                .append("joiningYear", joiningYear)
                ),
                new Document("$setWindowFields",
                        new Document("sortBy",
                                new Document("sgpa", -1)   // ✅ ONLY ONE FIELD (IMPORTANT)
                        )
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
                .aggregate(deptPipeline)
                .toCollection();


        List<Document> classPipeline = List.of(
                new Document("$match",
                        new Document("departmentId", department)
                                .append("semester", semester)
                                .append("joiningYear", joiningYear)
                ),
                new Document("$setWindowFields",
                        new Document("partitionBy", "$section")   // ✅ FIXED
                                .append("sortBy",
                                        new Document("sgpa", -1)
                                )
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
                .aggregate(classPipeline)
                .toCollection();


        mongoTemplate.getCollection("Result").updateMany(
                new Document("departmentId", department)
                        .append("semester", semester)
                        .append("joiningYear", joiningYear),
                new Document("$set",
                        new Document("departmentTopper", false)
                )
        );


        mongoTemplate.getCollection("Result").updateMany(
                new Document("departmentId", department)
                        .append("semester", semester)
                        .append("joiningYear", joiningYear)
                        .append("departmentRank", 1),
                new Document("$set",
                        new Document("departmentTopper", true)
                )
        );
    }
    public List<DepartmentSubjectAnalyticsResponse> getDepartmentAnalytics(
            String departmentCode,
            Integer joiningYear
    ) {

        Object cachedResultObject = redisService.get("analytics:"+departmentCode+":"+joiningYear, Object.class);
        if(cachedResultObject!=null){
            List<DepartmentSubjectAnalyticsResponse> cachedAnalytics = objectMapper.convertValue(
                    cachedResultObject,
                    new TypeReference<ArrayList<DepartmentSubjectAnalyticsResponse>>() {}
            );
            return cachedAnalytics;
        }

        List<Document> pipeline = List.of(

                new Document("$match",
                        new Document("departmentId", departmentCode)
                                .append("joiningYear", joiningYear)
                ),

                new Document("$unwind", "$marksList"),

                new Document("$addFields",
                        new Document("totalMarks",
                                new Document("$add", List.of(
                                        "$marksList.internalMarks",
                                        "$marksList.externalMarks"
                                ))
                        )
                ),

                new Document("$group",
                        new Document("_id",
                                new Document("departmentCode", "$departmentId")
                                        .append("joiningYear", "$joiningYear")
                                        .append("subjectCode", "$marksList.subjectSnapshot.code")
                                        .append("semester", "$marksList.subjectSnapshot.semester")
                        )
                                .append("averageMarks", new Document("$avg", "$totalMarks"))
                                .append("highestMarks", new Document("$max", "$totalMarks"))
                                .append("passCount",
                                        new Document("$sum",
                                                new Document("$cond", List.of(
                                                        new Document("$gte", List.of("$totalMarks", 40)),
                                                        1,
                                                        0
                                                ))
                                        )
                                )
                                .append("failCount",
                                        new Document("$sum",
                                                new Document("$cond", List.of(
                                                        new Document("$lt", List.of("$totalMarks", 40)),
                                                        1,
                                                        0
                                                ))
                                        )
                                )
                )
        );

        List<Document> docs = mongoTemplate.getCollection("Result")
                .aggregate(pipeline)
                .into(new ArrayList<>());

        List<DepartmentSubjectAnalyticsResponse> response = new ArrayList<>();

        for (Document doc : docs) {

            Document id = (Document) doc.get("_id");

            response.add(
                    DepartmentSubjectAnalyticsResponse.builder()
                            .departmentCode(id.getString("departmentCode"))
                            .joiningYear(id.getInteger("joiningYear"))
                            .subjectCode(id.getString("subjectCode"))
                            .averageMarks(doc.getDouble("averageMarks"))
                            .highestMarks(doc.getInteger("highestMarks"))
                            .passCount(doc.getInteger("passCount"))
                            .failCount(doc.getInteger("failCount"))
                            .semester(id.getInteger("semester"))
                            .build()
            );
        }
        redisService.set("analytics:"+departmentCode+":"+joiningYear,response, 60*10L);
        return response;
    }

    public List<LeaderboardEntry> getLeaderboard(String departmentId,
                                                 Integer joiningYear,
                                                 Integer semester,
                                                 int limit) {

        String key = (semester != null)
                ? "leaderboard:sem:" + departmentId + ":" + joiningYear + ":" + semester
                : "leaderboard:dept:" + departmentId + ":" + joiningYear;

        Long size = redisTemplate.opsForZSet().zCard(key);

        if (size == null || size == 0) {

            if (semester != null) {

                List<Result> results = mongoTemplate.find(
                        Query.query(
                                Criteria.where("departmentId").is(departmentId)
                                        .and("joiningYear").is(joiningYear)
                                        .and("semester").is(semester)
                        ).with(Sort.by(Sort.Direction.DESC, "sgpa")),
                        Result.class
                );

                for (Result r : results) {

                    String member = r.getRegistrationNumber() + "|" + r.getName();

                    redisTemplate.opsForZSet().add(
                            key,
                            member,
                            r.getSgpa()
                    );
                }

            } else {

                List<Student> students = mongoTemplate.find(
                        Query.query(
                                Criteria.where("departmentId").is(departmentId)
                                        .and("joiningYear").is(joiningYear)
                        ).with(Sort.by(Sort.Direction.DESC, "cgpa")),
                        Student.class
                );

                for (Student s : students) {

                    String member = s.getRegistrationNumber() + "|" + s.getName();

                    redisTemplate.opsForZSet().add(
                            key,
                            member,
                            s.getCgpa()
                    );
                }
            }
        }

        Set<ZSetOperations.TypedTuple<String>> result =
                redisTemplate.opsForZSet()
                        .reverseRangeWithScores(key, 0, limit - 1);

        if (result == null || result.isEmpty()) {
            return Collections.emptyList();
        }

        List<LeaderboardEntry> leaderboard = new ArrayList<>();

        for (ZSetOperations.TypedTuple<String> entry : result) {

            String[] parts = entry.getValue().split("\\|");

            String studentId = parts[0];
            String name = (parts.length > 1) ? parts[1] : "UNKNOWN";

            leaderboard.add(new LeaderboardEntry(
                    studentId,
                    entry.getScore(),
                    name
            ));
        }

        return leaderboard;
    }

    public  Integer getLiveCount(String departmentCode, Integer joiningYear, Integer semester) {
        String key = departmentCode+":"+joiningYear;
        if(semester!=null){
            key+=semester;
        }
        Integer count = redisService.get(key, Integer.class);
        if(count==null){
            redisService.set(key, 1, 60*10*10L);
        }else{
            redisService.set(key, count+1, 60*10*10L);
        }
        return count+1;
    }

    public void decrementLiveCount(String departmentCode, Integer joiningYear, Integer semester) {
        String key = departmentCode+":"+joiningYear;
        if(semester!=null){
            key+=semester;
        }
        Integer count = redisService.get(key, Integer.class);
        if(count==null)return;
        redisService.set(key, count-1, 60*10*10L);
    }
}

