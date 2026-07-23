package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivityApplication;
import com.volunteer.entity.Volunteer;
import com.volunteer.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
@CrossOrigin
public class ActivityController {
    
    @Autowired
    private ActivityService activityService;
    
    @GetMapping("/list")
    public Result<List<Activity>> getActivityList(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district) {
        try {
            return Result.success(activityService.getActivitiesByFilter(date, province, city, district));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/detail/{id}")
    public Result<Activity> getActivityDetail(@PathVariable Long id) {
        try {
            return Result.success(activityService.getActivityById(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/add")
    public Result<Void> createActivity(@RequestBody Activity activity) {
        try {
            activityService.createActivity(activity);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PutMapping("/update")
    public Result<Void> updateActivity(@RequestBody Activity activity) {
        try {
            activityService.updateActivity(activity);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteActivity(@PathVariable Long id) {
        try {
            activityService.deleteActivity(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/apply")
    public Result<Void> applyActivity(@RequestBody Map<String, Object> request) {
        try {
            if (request.get("activityId") == null) {
                return Result.error("活动ID不能为空");
            }
            if (request.get("volunteerId") == null) {
                return Result.error("志愿者ID不能为空");
            }
            
            Long activityId = Long.valueOf(request.get("activityId").toString());
            Long volunteerId = Long.valueOf(request.get("volunteerId").toString());
            String reason = request.get("reason") != null ? request.get("reason").toString() : null;
            activityService.applyActivity(activityId, volunteerId, reason);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/apply/review")
    public Result<Void> reviewApplication(@RequestBody Map<String, Object> request) {
        try {
            Long id = Long.valueOf(request.get("id").toString());
            String status = request.get("status").toString();
            String rejectReason = request.get("rejectReason") != null ? request.get("rejectReason").toString() : null;
            activityService.reviewApplication(id, status, rejectReason);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/participants/{activityId}")
    public Result<List<Volunteer>> getParticipants(@PathVariable Long activityId) {
        try {
            return Result.success(activityService.getParticipatingVolunteers(activityId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/applications")
    public Result<List<ActivityApplication>> getAllApplications() {
        try {
            return Result.success(activityService.getAllApplications());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/applications/my/{volunteerId}")
    public Result<List<ActivityApplication>> getMyApplications(@PathVariable Long volunteerId) {
        try {
            return Result.success(activityService.getApplicationsByVolunteer(volunteerId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/applications/my-with-checkin/{volunteerId}")
    public Result<List<Map<String, Object>>> getMyApplicationsWithCheckin(@PathVariable Long volunteerId) {
        try {
            return Result.success(activityService.getApplicationsWithCheckinByVolunteer(volunteerId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/applications/my-with-replenish/{volunteerId}")
    public Result<List<Map<String, Object>>> getMyApplicationsWithReplenish(@PathVariable Long volunteerId) {
        try {
            return Result.success(activityService.getApplicationsWithCheckinAndReplenishByVolunteer(volunteerId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/apply/delete/{id}")
    public Result<Void> deleteApplication(@PathVariable Long id) {
        try {
            activityService.deleteApplication(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/apply/batch-delete")
    public Result<Void> batchDeleteApplications(@RequestBody List<Long> ids) {
        try {
            activityService.batchDeleteApplications(ids);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}