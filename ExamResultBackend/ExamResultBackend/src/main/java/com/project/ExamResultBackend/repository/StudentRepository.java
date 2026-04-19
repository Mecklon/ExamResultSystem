package com.project.ExamResultBackend.repository;


import com.project.ExamResultBackend.model.Student;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends MongoRepository<Student, String> {


    @Query("{ registrationNumber: ?0}")
    Optional<Student> findByRegistrationNumber(long registrationNumber);

    @Query("{ 'registrationNumber': { $regex: '^?0' } }")
    List<Student> findByPrefix(String prefix, Pageable pageable);

    List<Student> findByRegistrationNumberBetween(long start, long end, PageRequest of);
}
