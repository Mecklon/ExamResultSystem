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
@RequestMapping("/results/")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;
    private final RedisService redisService;
    private final UtilityService utilityService;

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


    @GetMapping("/getDepartmentAverage/{departmentCode}/{joiningYear}")
    public ResponseEntity<List<DepartmentAverageResponse>> getDepartmentAverage(@PathVariable("departmentCode") String departmentCode, @PathVariable("joiningYear") int joiningYear){
        return ResponseEntity.status(HttpStatus.OK).body(utilityService.getDepartmentWiseAverage(departmentCode, joiningYear));
    }

    @GetMapping("/getMaxMarksPerSubject/{departmentCode}/{joiningYear}")
    public ResponseEntity<List<HighestMarksResponse>> getMaxMarksPerSubject(@PathVariable("departmentCode") String departmentCode, @PathVariable("joiningYear") int joiningYear){
        return ResponseEntity.status(HttpStatus.OK).body(utilityService.getHighestMarksPerSubject(departmentCode, joiningYear));
    }

    @GetMapping("/getPassFailCount/{departmentCode}/{joiningYear}")
    public ResponseEntity<List<PassFailResponse>> getPassFailCount(@PathVariable("departmentCode") String departmentCode, @PathVariable("joiningYear") int joiningYear){
        return ResponseEntity.status(HttpStatus.OK).body(utilityService.getPassFailCount(departmentCode, joiningYear));
    }

    @GetMapping("/department-analytics/{departmentCode}/{joiningYear}")
    public ResponseEntity<List<DepartmentSubjectAnalyticsResponse>> getDepartmentAnalytics(
            @PathVariable String departmentCode,
            @PathVariable Integer joiningYear
    ) {
        return ResponseEntity.ok(
                utilityService.getDepartmentAnalytics(departmentCode, joiningYear)
        );
    }

}
