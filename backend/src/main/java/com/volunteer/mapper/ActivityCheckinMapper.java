package com.volunteer.mapper;

import com.volunteer.entity.ActivityCheckin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ActivityCheckinMapper {
    List<ActivityCheckin> selectAll();
    List<Map<String, Object>> selectCheckoutRecords();
    List<ActivityCheckin> selectByVolunteerId(@Param("volunteerId") Long volunteerId);
    void insert(ActivityCheckin checkin);
    void update(ActivityCheckin checkin);
    void deleteById(Long id);
    ActivityCheckin selectByActivityAndVolunteer(@Param("activityId") Long activityId, @Param("volunteerId") Long volunteerId);
    
    // 统计方法
    BigDecimal getTotalHoursByVolunteer(@Param("volunteerId") Long volunteerId);
    Integer getTotalPointsByVolunteer(@Param("volunteerId") Long volunteerId);
    Double getTotalServiceHours();
    Double getHoursByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 图表数据查询方法
    // 按日统计志愿者服务时长（用于成长轨迹图）
    List<Map<String, Object>> getDailyHoursByVolunteer(@Param("volunteerId") Long volunteerId, @Param("days") int days);
    
    // 按周统计志愿者服务时长
    List<Map<String, Object>> getWeeklyHoursByVolunteer(@Param("volunteerId") Long volunteerId, @Param("weeks") int weeks);
    
    // 按月统计志愿者服务时长
    List<Map<String, Object>> getMonthlyHoursByVolunteer(@Param("volunteerId") Long volunteerId, @Param("months") int months);
    
    // 按年统计志愿者服务时长
    List<Map<String, Object>> getYearlyHoursByVolunteer(@Param("volunteerId") Long volunteerId, @Param("years") int years);
    
    // 获取志愿者参与的活动总数
    Integer getActivityCountByVolunteer(@Param("volunteerId") Long volunteerId);
    
    // 获取志愿者准时签到次数（签到时间在活动开始时间前后30分钟内）
    Integer getOnTimeCheckinCount(@Param("volunteerId") Long volunteerId);
    
    // 获取志愿者总签到次数
    Integer getTotalCheckinCount(@Param("volunteerId") Long volunteerId);
    
    // 全局统计方法（用于能力雷达图比率计算）
    // 获取所有志愿者的准时签到总次数
    Integer getTotalOnTimeCheckins();
    
    // 获取所有志愿者的正常签退总次数（status=2且is_counted=1）
    Integer getTotalCompletedCheckins();
    
    // 获取所有志愿者的签到积分总和
    Integer getTotalCheckinPoints();
}