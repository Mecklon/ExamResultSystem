package com.project.ExamResultBackend.repository;

import com.project.ExamResultBackend.model.Result;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface ResultRepository extends MongoRepository<Result, String> {

    @Query("{studentId: ?0}")
    ArrayList<Result> findByStudentId(String studentId);


    @Query("{studentId: ?0, semester: ?1}")
    Result findByStudentIdAndSemester(String id, Integer semester);
}
