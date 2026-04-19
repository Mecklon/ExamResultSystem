package com.project.ExamResultBackend.repository;


import com.project.ExamResultBackend.model.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends MongoRepository<Department, String> {
    Optional<Department> findByCode(String code);

    boolean existsByCode(String departmentCode);

    void deleteByCode(String departmentCode);
}
