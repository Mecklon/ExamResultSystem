package com.project.ExamResultBackend.repository;


import com.project.ExamResultBackend.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends MongoRepository<Student, String> {


    @Query("{ registrationNumber: ?0}")
    Optional<Student> findByRegistrationNumber(long registrationNumber);
}
