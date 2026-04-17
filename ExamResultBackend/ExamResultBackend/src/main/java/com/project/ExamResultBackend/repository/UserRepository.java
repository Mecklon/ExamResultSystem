package com.project.ExamResultBackend.repository;

import com.project.ExamResultBackend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByRegistrationNumber(Long registrationNumber);
}