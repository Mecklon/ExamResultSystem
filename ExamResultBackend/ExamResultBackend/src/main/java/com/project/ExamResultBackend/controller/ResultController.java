package com.project.ExamResultBackend.controller;


import com.project.ExamResultBackend.DTO.*;
import com.project.ExamResultBackend.service.RedisService;
import com.project.ExamResultBackend.service.ResultService;
import com.project.ExamResultBackend.service.UtilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.event.MouseInputListener;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;
    private final RedisService redisService;
    private final UtilityService utilityService;

    @PostMapping("/admin/publish-results")
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




    @GetMapping("/stats/department/{departmentCode}/{joiningYear}")
    public ResponseEntity<List<DepartmentSubjectAnalyticsResponse>> getDepartmentAnalytics(
            @PathVariable String departmentCode,
            @PathVariable Integer joiningYear
    ) {
        return ResponseEntity.ok(
                utilityService.getDepartmentAnalytics(departmentCode, joiningYear)
        );
    }


    @GetMapping("/result/{studentId}")
    public ResponseEntity<List<ResultOutputDTO>> getStudentResult(@PathVariable("studentId") Long registrationNumber){
            return ResponseEntity.status(HttpStatus.OK).body(resultService.getStudentResult(registrationNumber));

    }

    @GetMapping("/leaderboard/{departmentId}/{joiningYear}/{semester}")
    public ResponseEntity<Void> getLeaderBoard(String departmentId, Integer joiningYear, Integer semester){
        return ResponseEntity.status(HttpStatus.OK).build();
    }



}
