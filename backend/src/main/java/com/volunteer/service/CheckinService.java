package com.volunteer.service;

import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivityCheckin;
import com.volunteer.entity.Volunteer;
import com.volunteer.mapper.ActivityCheckinMapper;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.VolunteerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class CheckinService {
    
    @Autowired
    private ActivityCheckinMapper checkinMapper;
    
    @Autowired
    private ActivityMapper activityMapper;
    
    @Autowired
    private VolunteerMapper volunteerMapper;
    
    @Autowired
    private PointsCalculationService pointsCalculationService;
    
    @Transactional
    public void checkin(Long activityId, Long volunteerId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = activity.getStartTime();
        
        if (now.isBefore(startTime.minusMinutes(15)) || now.isAfter(startTime)) {
            throw new RuntimeException("不在签到时间范围内");
        }
        
        ActivityCheckin existing = checkinMapper.selectByActivityAndVolunteer(activityId, volunteerId);
        if (existing != null) {
            throw new RuntimeException("已签到");
        }
        
        ActivityCheckin checkin = new ActivityCheckin();
        checkin.setActivityId(activityId);
        checkin.setVolunteerId(volunteerId);
        checkin.setCheckinTime(now);
        checkin.setStatus(1);
        checkinMapper.insert(checkin);
    }
    
    @Transactional
    public void checkout(Long activityId, Long volunteerId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = activity.getEndTime();
        
        if (now.isBefore(endTime.minusMinutes(15))) {
            throw new RuntimeException("未到签退时间");
        }
        
        ActivityCheckin checkin = checkinMapper.selectByActivityAndVolunteer(activityId, volunteerId);
        if (checkin == null || checkin.getStatus() != 1) {
            throw new RuntimeException("未签到或已签退");
        }
        
        checkin.setCheckoutTime(now);
        checkin.setStatus(2);
        
        Duration duration = Duration.between(checkin.getCheckinTime(), now);
        BigDecimal hours = BigDecimal.valueOf(duration.toMinutes()).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
        checkin.setServiceHours(hours);
        checkin.setEarnedPoints(activity.getRewardPoints());
        checkin.setIsCounted(1);
        
        checkinMapper.update(checkin);
        
        // 签退后即时同步积分和时长（不再使用累加方式）
        // volunteerMapper.updateTotalHours(volunteerId, hours); // 不再使用累加方式
        // volunteerMapper.updatePoints(volunteerId, activity.getRewardPoints()); // 不再使用累加方式
        
        // 签退后自动同步积分和时长（使用后端统一计算）
        pointsCalculationService.syncVolunteerPointsAndHours(volunteerId);
    }
}