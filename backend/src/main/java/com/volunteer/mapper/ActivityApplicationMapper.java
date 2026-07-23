package com.volunteer.mapper;

import com.volunteer.entity.ActivityApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ActivityApplicationMapper {
    ActivityApplication selectById(@Param("id") Long id);
    List<ActivityApplication> selectByVolunteerId(@Param("volunteerId") Long volunteerId);
    List<ActivityApplication> selectByActivityId(@Param("activityId") Long activityId);
    List<ActivityApplication> selectAll();
    List<Long> selectParticipatingVolunteerIds(@Param("activityId") Long activityId);
    ActivityApplication selectByActivityAndVolunteer(@Param("activityId") Long activityId, @Param("volunteerId") Long volunteerId);
    int insert(ActivityApplication application);
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("rejectReason") String rejectReason);
    int updateAuditTime(@Param("id") Long id, @Param("auditTime") LocalDateTime auditTime);
    int deleteById(@Param("id") Long id);
    
    // 统计方法
    Integer countParticipantsByCategory(@Param("categoryId") Long categoryId);
    // 统计方法
    // 获取志愿者通过审核的申请总数
    Integer getApprovedApplicationCount(@Param("volunteerId") Long volunteerId);
    
    // 获取志愿者参与的活动类型数量（去重）
    Integer getActivityTypeCoverage(@Param("volunteerId") Long volunteerId);
    
    // 全局统计方法（用于能力雷达图比率计算）
    // 获取所有志愿者的通过审核申请总数
    Integer getTotalApprovedApplications();
}