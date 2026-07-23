package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.service.CheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
@CrossOrigin
public class CheckinController {
    
    @Autowired
    private CheckinService checkinService;
    
    @PostMapping("/in")
    public Result<Void> checkin(@RequestBody Map<String, Long> request) {
        try {
            checkinService.checkin(request.get("activityId"), request.get("volunteerId"));
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/out")
    public Result<Void> checkout(@RequestBody Map<String, Long> request) {
        try {
            checkinService.checkout(request.get("activityId"), request.get("volunteerId"));
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}