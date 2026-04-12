package com.project.ExamResultBackend.controller;


import com.project.ExamResultBackend.DTO.ResultSaveResponse;
import com.project.ExamResultBackend.DTO.ResultDTO;
import com.project.ExamResultBackend.service.RedisService;
import com.project.ExamResultBackend.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/results/")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;
    private final RedisService redisService;

    @PostMapping("/saveResult")
    public ResponseEntity<List<ResultSaveResponse>> bulkSaveResults(@RequestBody List<ResultDTO> ResultDTOS) {
        return ResponseEntity.status(HttpStatus.OK).body(resultService.bulkSaveResults(ResultDTOS));
    }


    @GetMapping("/test")
    public String test(){
        String res = redisService.get("name",String.class);
        if(res!=null){
            System.out.println("redis hit");
            return res;
        }else{
            redisService.set("name", new String("Mecklon Fernandes from redis"),(long)333);
            System.out.println("db hit");
            return "Mecklon Fernandes from db";
        }
    }

}
