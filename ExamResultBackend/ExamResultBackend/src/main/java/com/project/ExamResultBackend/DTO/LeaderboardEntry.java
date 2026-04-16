package com.project.ExamResultBackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardEntry {
    private String studentId;
    private Double score;
    String name;
}