package com.volunteer.mapper;

import com.volunteer.entity.ServiceHourReplenish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ServiceHourReplenishMapper {
    List<ServiceHourReplenish> selectAll();
    List<Map<String, Object>> selectAllWithVolunteerInfo();
    List<ServiceHourReplenish> selectByVolunteerId(@Param("volunteerId") Long volunteerId);
    ServiceHourReplenish selectById(@Param("id") Long id);
    void insert(ServiceHourReplenish replenish);
    void update(ServiceHourReplenish replenish);
    void updateStatus(@Param("id") Long id, @Param("status") String status, @Param("rejectReason") String rejectReason, @Param("adminId") Long adminId);
    void deleteById(Long id);
    void batchDelete(@Param("ids") List<Long> ids);
    
    // 统计方法
    BigDecimal getTotalHoursByVolunteer(@Param("volunteerId") Long volunteerId);
    Integer getTotalPointsByVolunteer(@Param("volunteerId") Long volunteerId);
    Integer getCountByVolunteer(@Param("volunteerId") Long volunteerId);
    Double getTotalReplenishHours();
    Double getHoursByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 图表数据查询方法 - 补录时长统计
    // 按日统计补录时长
    List<Map<String, Object>> getDailyReplenishHours(@Param("volunteerId") Long volunteerId, @Param("days") int days);
    
    // 按周统计补录时长
    List<Map<String, Object>> getWeeklyReplenishHours(@Param("volunteerId") Long volunteerId, @Param("weeks") int weeks);
    
    // 按月统计补录时长
    List<Map<String, Object>> getMonthlyReplenishHours(@Param("volunteerId") Long volunteerId, @Param("months") int months);
    
    // 按年统计补录时长
    List<Map<String, Object>> getYearlyReplenishHours(@Param("volunteerId") Long volunteerId, @Param("years") int years);
    
    // 获取所有志愿者的补录积分总和
    Integer getTotalReplenishPoints();
}