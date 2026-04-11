package com.project.ExamResultBackend.service;


import com.project.ExamResultBackend.DTO.ResultSaveResponse;
import com.project.ExamResultBackend.DTO.ResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultService {
    private final UtilityService utilityService;

    public ArrayList<ResultSaveResponse> bulkSaveResults(List<ResultDTO> ResultDTOS) {
        ArrayList<ResultSaveResponse> resultSaveResponse = new ArrayList<>();
        for(ResultDTO resultDTO: ResultDTOS){
            utilityService.saveSingleResult(resultDTO, resultSaveResponse);
        }
        HashSet<String> recomputedRankSet = new HashSet<>();
        for(int i =0;i< resultSaveResponse.size();i++){
            ResultSaveResponse currResponse = resultSaveResponse.get(i);
            ResultDTO currResult = ResultDTOS.get(i);
            if(currResponse.getStatus().equals("SUCCESS") && !recomputedRankSet.contains(currResponse.getDepartment()+":"+currResult.getSemester())){
                recomputedRankSet.add(currResponse.getDepartment()+":"+currResult.getSemester());
                utilityService.recomputeSemesterWiseRank(currResponse.getDepartment(), currResult.getSemester());
            }
            if(currResponse.getStatus().equals("SUCCESS") && !recomputedRankSet.contains(currResponse.getDepartment())){
                recomputedRankSet.add(currResponse.getDepartment());
                utilityService.recomputeOverallRank(currResponse.getDepartment());
            }
        }
        return resultSaveResponse;
    }


}
