package com.project.ExamResultBackend.repository;

import com.project.ExamResultBackend.model.Subject;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SubjectRepository extends MongoRepository<Subject, String> {
}
