package com.project.ExamResultBackend.controller;


import com.project.ExamResultBackend.DTO.ResultSaveResponse;
import com.project.ExamResultBackend.DTO.ResultDTO;
import com.project.ExamResultBackend.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/results/")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;


    @PostMapping("/saveResult")
    public ResponseEntity<List<ResultSaveResponse>> bulkSaveResults(@RequestBody List<ResultDTO> ResultDTOS) {
        return ResponseEntity.status(HttpStatus.OK).body(resultService.bulkSaveResults(ResultDTOS));
    }


}
