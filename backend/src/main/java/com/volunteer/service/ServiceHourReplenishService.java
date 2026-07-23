package com.volunteer.service;

import com.volunteer.entity.ServiceHourReplenish;
import com.volunteer.mapper.ServiceHourReplenishMapper;
import com.volunteer.mapper.VolunteerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ServiceHourReplenishService {
    
    @Autowired
    private ServiceHourReplenishMapper replenishMapper;
    
    @Autowired
    private VolunteerMapper volunteerMapper;
    
    @Autowired
    private PointsCalculationService pointsCalculationService;
    
    public List<ServiceHourReplenish> getAllReplenish() {
        return replenishMapper.selectAll();
    }
    
    public List<Map<String, Object>> getAllReplenishWithVolunteerInfo() {
        return replenishMapper.selectAllWithVolunteerInfo();
    }
    
    public List<ServiceHourReplenish> getReplenishByVolunteer(Long volunteerId) {
        return replenishMapper.selectByVolunteerId(volunteerId);
    }
    
    public ServiceHourReplenish getReplenishById(Long id) {
        return replenishMapper.selectById(id);
    }
    
    @Transactional
    public void createReplenish(ServiceHourReplenish replenish) {
        replenish.setStatus("待审核");
        replenish.setApplyTime(LocalDateTime.now());
        replenishMapper.insert(replenish);
    }
    
    @Transactional
    public void updateReplenish(ServiceHourReplenish replenish) {
        replenishMapper.update(replenish);
    }
    
    @Transactional
    public void reviewReplenish(Long id, String status, String rejectReason, Long adminId) {
        ServiceHourReplenish replenish = replenishMapper.selectById(id);
        if (replenish == null) {
            throw new RuntimeException("补录申请不存在");
        }
        
        replenishMapper.updateStatus(id, status, rejectReason, adminId);
        
        // 如果审核通过，即时同步积分和时长（不再使用累加方式）
        if ("已通过".equals(status)) {
            // volunteerMapper.updateTotalHours(replenish.getVolunteerId(), replenish.getHours()); // 不再使用累加方式
            // volunteerMapper.updatePoints(replenish.getVolunteerId(), replenish.getEarnedPoints()); // 不再使用累加方式
            
            // 补录通过后自动同步积分和时长（使用后端统一计算）
            pointsCalculationService.syncVolunteerPointsAndHours(replenish.getVolunteerId());
        }
    }
    
    public void deleteReplenish(Long id) {
        replenishMapper.deleteById(id);
    }
    
    public void batchDeleteReplenish(List<Long> ids) {
        replenishMapper.batchDelete(ids);
    }
    
    // 统计方法
    public BigDecimal getTotalHoursByVolunteer(Long volunteerId) {
        return replenishMapper.getTotalHoursByVolunteer(volunteerId);
    }
    
    public Integer getTotalPointsByVolunteer(Long volunteerId) {
        return replenishMapper.getTotalPointsByVolunteer(volunteerId);
    }
    
    public Integer getCountByVolunteer(Long volunteerId) {
        return replenishMapper.getCountByVolunteer(volunteerId);
    }
}
