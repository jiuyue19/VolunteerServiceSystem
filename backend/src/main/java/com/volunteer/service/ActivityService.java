package com.volunteer.service;

import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivityApplication;
import com.volunteer.entity.Volunteer;
import com.volunteer.mapper.ActivityApplicationMapper;
import com.volunteer.mapper.ActivityCheckinMapper;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.mapper.ServiceHourReplenishMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivityService {
    
    @Autowired
    private ActivityMapper activityMapper;
    
    @Autowired
    private ActivityApplicationMapper applicationMapper;
    
    @Autowired
    private VolunteerMapper volunteerMapper;
    
    @Autowired
    private ActivityCheckinMapper checkinMapper;
    
    @Autowired
    private ServiceHourReplenishMapper replenishMapper;
    
    public List<Activity> getAllActivities() {
        return activityMapper.selectAll();
    }
    
    public List<Activity> getActivitiesByFilter(String date, String province, String city, String district) {
        return activityMapper.selectByFilter(date, province, city, district);
    }
    
    public Activity getActivityById(Long id) {
        return activityMapper.selectById(id);
    }
    
    public void createActivity(Activity activity) {
        // 自动计算志愿时长和积分
        calculateServiceHoursAndPoints(activity);
        activityMapper.insert(activity);
    }
    
    public void updateActivity(Activity activity) {
        // 自动计算志愿时长和积分
        calculateServiceHoursAndPoints(activity);
        activityMapper.update(activity);
    }
    
    public void deleteActivity(Long id) {
        activityMapper.deleteById(id);
    }
    
    /**
     * 根据活动起止时间自动计算志愿时长和积分
     * 业务规则：
     * - 志愿时长 = 结束时间 - 起始时间（单位：小时，保留2位小数）
     * - 积分 = 志愿时长 × 10（1小时10积分，四舍五入取整）
     */
    private void calculateServiceHoursAndPoints(Activity activity) {
        if (activity.getStartTime() != null && activity.getEndTime() != null) {
            // 计算时间差（分钟）
            long minutes = ChronoUnit.MINUTES.between(activity.getStartTime(), activity.getEndTime());
            
            // 转换为小时，保留2位小数
            BigDecimal hours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            
            // 计算积分：时长 × 10，四舍五入取整
            int points = hours.multiply(BigDecimal.TEN)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
            
            // 设置计算结果
            activity.setServiceHours(hours);
            activity.setRewardPoints(points);
        }
    }
    
    @Transactional
    public void applyActivity(Long activityId, Long volunteerId, String reason) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        
        ActivityApplication existing = applicationMapper.selectByActivityAndVolunteer(activityId, volunteerId);
        if (existing != null) {
            throw new RuntimeException("已申请过该活动");
        }
        
        if (activity.getCurrentNumber() >= activity.getTargetNumber()) {
            throw new RuntimeException("活动报名人数已满");
        }
        
        ActivityApplication application = new ActivityApplication();
        application.setActivityId(activityId);
        application.setVolunteerId(volunteerId);
        application.setStatus("待审核");
        application.setReason(reason);
        application.setApplyTime(LocalDateTime.now());
        applicationMapper.insert(application);
    }
    
    @Transactional
    public void reviewApplication(Long applicationId, String status, String rejectReason) {
        ActivityApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new RuntimeException("申请不存在");
        }
        
        // 保存原状态，用于判断是否需要调整当前人数
        String oldStatus = application.getStatus();
        String finalStatus = "已通过".equals(status) ? "待参与" : status;
        
        applicationMapper.updateStatus(applicationId, finalStatus, rejectReason);
        // 记录审核时间
        applicationMapper.updateAuditTime(applicationId, LocalDateTime.now());
        
        Activity activity = activityMapper.selectById(application.getActivityId());
        
        // 审核通过：从待审核变为待参与，当前人数+1
        if ("已通过".equals(status) && !"待参与".equals(oldStatus)) {
            activityMapper.updateCurrentNumber(activity.getId(), activity.getCurrentNumber() + 1);
        }
        // 拒绝申请：如果原来是待参与状态，需要将当前人数-1
        else if ("已拒绝".equals(status) && "待参与".equals(oldStatus)) {
            int newNumber = Math.max(0, activity.getCurrentNumber() - 1);
            activityMapper.updateCurrentNumber(activity.getId(), newNumber);
        }
    }
    
    public List<Volunteer> getParticipatingVolunteers(Long activityId) {
        List<Long> volunteerIds = applicationMapper.selectParticipatingVolunteerIds(activityId);
        if (volunteerIds == null || volunteerIds.isEmpty()) {
            return new ArrayList<>();
        }
        return volunteerMapper.selectByIds(volunteerIds);
    }
    
    public List<ActivityApplication> getAllApplications() {
        return applicationMapper.selectAll();
    }

    public List<ActivityApplication> getApplicationsByVolunteer(Long volunteerId) {
        return applicationMapper.selectByVolunteerId(volunteerId);
    }

    public List<Map<String, Object>> getApplicationsWithCheckinByVolunteer(Long volunteerId) {
        List<ActivityApplication> applications = applicationMapper.selectByVolunteerId(volunteerId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ActivityApplication app : applications) {
            Map<String, Object> appData = new HashMap<>();
            appData.put("id", app.getId());
            appData.put("activityId", app.getActivityId());
            appData.put("volunteerId", app.getVolunteerId());
            appData.put("status", app.getStatus());
            appData.put("reason", app.getReason());
            appData.put("applyTime", app.getApplyTime());
            appData.put("rejectReason", app.getRejectReason());
            
            // 获取活动信息
            Activity activity = activityMapper.selectById(app.getActivityId());
            if (activity != null) {
                appData.put("activityName", activity.getTitle());
                appData.put("activityAddress", activity.getAddress());
                appData.put("activityStartTime", activity.getStartTime());
                appData.put("activityEndTime", activity.getEndTime());
            }
            
            // 获取签退信息
            var checkin = checkinMapper.selectByActivityAndVolunteer(app.getActivityId(), volunteerId);
            if (checkin != null) {
                appData.put("checkinTime", checkin.getCheckinTime());
                appData.put("checkoutTime", checkin.getCheckoutTime());
                appData.put("serviceHours", checkin.getServiceHours() != null ? checkin.getServiceHours() : 0);
                appData.put("earnedPoints", checkin.getEarnedPoints() != null ? checkin.getEarnedPoints() : 0);
                
                // 设置签到状态
                if (checkin.getCheckoutTime() != null) {
                    appData.put("checkinStatus", "checkout");
                } else if (checkin.getCheckinTime() != null) {
                    appData.put("checkinStatus", "checkin");
                } else {
                    appData.put("checkinStatus", "not_checkin");
                }
            } else {
                appData.put("checkinTime", null);
                appData.put("checkoutTime", null);
                appData.put("serviceHours", 0);
                appData.put("earnedPoints", 0);
                appData.put("checkinStatus", "not_checkin");
            }
            
            result.add(appData);
        }
        
        return result;
    }

    public List<Map<String, Object>> getApplicationsWithCheckinAndReplenishByVolunteer(Long volunteerId) {
        // 获取正常的申请记录
        List<Map<String, Object>> result = getApplicationsWithCheckinByVolunteer(volunteerId);
        
        // 获取补录记录
        var replenishList = replenishMapper.selectByVolunteerId(volunteerId);
        
        // 将补录记录转换为统一格式并添加到结果中
        for (var replenish : replenishList) {
            if ("已通过".equals(replenish.getStatus())) {
                Map<String, Object> replenishData = new HashMap<>();
                replenishData.put("id", "replenish_" + replenish.getId());
                replenishData.put("activityId", null);
                replenishData.put("volunteerId", replenish.getVolunteerId());
                replenishData.put("status", "已通过");
                replenishData.put("reason", replenish.getReason());
                replenishData.put("applyTime", replenish.getApplyTime());
                replenishData.put("rejectReason", null);
                
                // 活动信息（补录）- 尝试根据活动名称查找真实活动
                Activity realActivity = null;
                try {
                    // 移除可能的"(补录)"后缀，查找原始活动名称
                    String originalActivityName = replenish.getActivityName().replace("(补录)", "").trim();
                    realActivity = activityMapper.selectByTitle(originalActivityName);
                } catch (Exception e) {
                    // 如果查找失败，使用默认值
                }
                
                replenishData.put("activityName", replenish.getActivityName());
                if (realActivity != null) {
                    replenishData.put("activityAddress", realActivity.getAddress());
                    replenishData.put("activityStartTime", realActivity.getStartTime());
                    replenishData.put("activityEndTime", realActivity.getEndTime());
                    replenishData.put("activityId", realActivity.getId()); // 设置真实的活动ID
                } else {
                    replenishData.put("activityAddress", "补录申请（地址未知）");
                    replenishData.put("activityStartTime", replenish.getApplyTime());
                    replenishData.put("activityEndTime", replenish.getApplyTime());
                }
                
                // 签退信息（补录）
                replenishData.put("checkinTime", replenish.getApplyTime());
                replenishData.put("checkoutTime", replenish.getAuditTime());
                replenishData.put("serviceHours", replenish.getHours());
                replenishData.put("earnedPoints", replenish.getEarnedPoints());
                replenishData.put("checkinStatus", "checkout");
                
                // 标记为补录
                replenishData.put("isReplenish", true);
                replenishData.put("replenishId", replenish.getId());
                
                result.add(replenishData);
            }
        }
        
        return result;
    }
    
    @Transactional
    public void deleteApplication(Long applicationId) {
        ActivityApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new RuntimeException("申请不存在");
        }
        
        // 如果申请状态是"待参与"，需要减少活动当前人数
        if ("待参与".equals(application.getStatus())) {
            Activity activity = activityMapper.selectById(application.getActivityId());
            if (activity != null) {
                int newNumber = Math.max(0, activity.getCurrentNumber() - 1);
                activityMapper.updateCurrentNumber(activity.getId(), newNumber);
            }
        }
        
        applicationMapper.deleteById(applicationId);
    }
    
    @Transactional
    public void batchDeleteApplications(List<Long> ids) {
        for (Long id : ids) {
            deleteApplication(id);
        }
    }
}