package com.volunteer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.volunteer.mapper.*;
import com.volunteer.entity.*;

import java.util.*;
import java.util.List;

/**
 * 积分计算服务
 * 统一的积分计算逻辑：累计积分 = 活动积分 + 补录积分 - 兑换积分 + 退款积分
 */
@Service
public class PointsCalculationService {

    @Autowired
    private ActivityCheckinMapper checkinMapper;
    
    @Autowired
    private ServiceHourReplenishMapper replenishMapper;
    
    @Autowired
    private ExchangeOrderMapper exchangeOrderMapper;
    
    @Autowired
    private VolunteerMapper volunteerMapper;

    /**
     * 计算志愿者的累计积分
     * @param volunteerId 志愿者ID
     * @return 积分计算结果
     */
    public Map<String, Object> calculateVolunteerPoints(Long volunteerId) {
        System.out.println("[PointsCalculation] 开始计算志愿者积分，ID: " + volunteerId);
        
        int servicePoints = 0;      // 活动服务积分
        int replenishPoints = 0;    // 补录积分
        int exchangePoints = 0;     // 兑换扣减积分（负数）
        int refundPoints = 0;       // 退款退还积分（正数）
        
        // 1. 计算活动服务积分（已签退的活动）
        List<ActivityCheckin> checkins = checkinMapper.selectByVolunteerId(volunteerId);
        if (checkins != null) {
            for (ActivityCheckin checkin : checkins) {
                if (checkin.getCheckoutTime() != null && checkin.getEarnedPoints() != null) {
                    servicePoints += checkin.getEarnedPoints();
                    System.out.println("[PointsCalculation] 活动签退积分: +" + checkin.getEarnedPoints());
                }
            }
        }
        
        // 2. 计算补录积分（已通过的补录申请）
        List<ServiceHourReplenish> replenishes = replenishMapper.selectByVolunteerId(volunteerId);
        if (replenishes != null) {
            for (ServiceHourReplenish replenish : replenishes) {
                if ("已通过".equals(replenish.getStatus()) && replenish.getEarnedPoints() != null) {
                    replenishPoints += replenish.getEarnedPoints();
                    System.out.println("[PointsCalculation] 补录积分: +" + replenish.getEarnedPoints());
                }
            }
        }
        
        // 3. 计算兑换和退款积分
        List<ExchangeOrder> orders = exchangeOrderMapper.selectByVolunteerId(volunteerId);
        if (orders != null) {
            for (ExchangeOrder order : orders) {
                if (order.getTotalPoints() != null) {
                    boolean isCancelled = "已取消".equals(order.getStatus());
                    boolean isRefunded = "已通过".equals(order.getRefundStatus());
                    
                    // 退款成功 - 积分退还（正数）
                    if (isRefunded) {
                        refundPoints += order.getTotalPoints();
                        System.out.println("[PointsCalculation] 退款退还积分: +" + order.getTotalPoints());
                    }
                    
                    // 正常兑换 - 积分扣减（负数）
                    // 只有非取消且非退款的订单才扣减积分
                    if (!isCancelled && !isRefunded) {
                        exchangePoints -= order.getTotalPoints();
                        System.out.println("[PointsCalculation] 兑换扣减积分: -" + order.getTotalPoints());
                    }
                }
            }
        }
        
        // 4. 计算累计积分
        int totalPoints = servicePoints + replenishPoints + exchangePoints + refundPoints;
        
        System.out.println("[PointsCalculation] 积分计算完成:");
        System.out.println("  活动积分: " + servicePoints);
        System.out.println("  补录积分: " + replenishPoints);
        System.out.println("  兑换扣减: " + exchangePoints);
        System.out.println("  退款退还: " + refundPoints);
        System.out.println("  累计积分: " + totalPoints);
        
        // 返回详细的积分信息
        Map<String, Object> result = new HashMap<>();
        result.put("servicePoints", servicePoints);      // 活动服务积分
        result.put("replenishPoints", replenishPoints);  // 补录积分
        result.put("exchangePoints", exchangePoints);    // 兑换扣减（负数）
        result.put("refundPoints", refundPoints);        // 退款退还
        result.put("totalPoints", totalPoints);          // 累计积分
        
        // 附加统计信息
        result.put("formula", String.format("%d + %d + (%d) + %d = %d", 
            servicePoints, replenishPoints, exchangePoints, refundPoints, totalPoints));
        
        return result;
    }
    
    /**
     * 只返回累计积分数值
     * @param volunteerId 志愿者ID
     * @return 累计积分
     */
    public int getTotalPoints(Long volunteerId) {
        Map<String, Object> result = calculateVolunteerPoints(volunteerId);
        return (int) result.get("totalPoints");
    }
    
    /**
     * 同步志愿者积分到数据库
     * 将计算得到的积分更新到Volunteer表
     * @param volunteerId 志愿者ID
     */
    public void syncVolunteerPoints(Long volunteerId) {
        System.out.println("[PointsCalculation] 开始同步志愿者积分到数据库，ID: " + volunteerId);
        
        // 计算最新积分
        int totalPoints = getTotalPoints(volunteerId);
        
        // 更新到数据库（直接设置，不是累加）
        volunteerMapper.syncPoints(volunteerId, totalPoints);
        
        System.out.println("[PointsCalculation] 积分同步完成: " + totalPoints);
    }
    
    /**
     * 批量同步所有志愿者的积分
     */
    public void syncAllVolunteerPoints() {
        System.out.println("[PointsCalculation] 开始批量同步所有志愿者积分");
        
        // 获取所有志愿者ID
        List<Long> volunteerIds = volunteerMapper.selectAllVolunteerIds();
        
        int successCount = 0;
        for (Long volunteerId : volunteerIds) {
            try {
                syncVolunteerPoints(volunteerId);
                successCount++;
            } catch (Exception e) {
                System.err.println("[PointsCalculation] 同步志愿者 " + volunteerId + " 积分失败: " + e.getMessage());
            }
        }
        
        System.out.println("[PointsCalculation] 批量同步完成，成功: " + successCount + "/" + volunteerIds.size());
    }
    
    /**
     * 计算志愿者的累计服务时长
     * 总时长 = 签到时长 + 补录时长（保留2位小数，四舍五入）
     * @param volunteerId 志愿者ID
     * @return 累计服务时长
     */
    public java.math.BigDecimal calculateTotalHours(Long volunteerId) {
        System.out.println("[PointsCalculation] 开始计算志愿者服务时长，ID: " + volunteerId);
        
        // 1. 获取签到时长
        java.math.BigDecimal checkinHours = checkinMapper.getTotalHoursByVolunteer(volunteerId);
        if (checkinHours == null) {
            checkinHours = java.math.BigDecimal.ZERO;
        }
        
        // 2. 获取补录时长
        java.math.BigDecimal replenishHours = replenishMapper.getTotalHoursByVolunteer(volunteerId);
        if (replenishHours == null) {
            replenishHours = java.math.BigDecimal.ZERO;
        }
        
        // 3. 计算总时长（保留2位小数，四舍五入）
        java.math.BigDecimal totalHours = checkinHours.add(replenishHours).setScale(2, java.math.BigDecimal.ROUND_HALF_UP);
        
        System.out.println("[PointsCalculation] 时长计算完成:");
        System.out.println("  签到时长: " + checkinHours);
        System.out.println("  补录时长: " + replenishHours);
        System.out.println("  累计时长: " + totalHours);
        
        return totalHours;
    }
    
    /**
     * 同步志愿者服务时长到数据库
     * 将计算得到的时长更新到Volunteer表，确保与累计服务时长选项卡的计算逻辑一致
     * @param volunteerId 志愿者ID
     */
    public void syncVolunteerHours(Long volunteerId) {
        System.out.println("[PointsCalculation] 开始同步志愿者服务时长到数据库，ID: " + volunteerId);
        
        try {
            // 计算最新时长（使用与累计服务时长选项卡相同的计算逻辑）
            java.math.BigDecimal totalHours = calculateTotalHours(volunteerId);
            
            // 验证计算结果
            if (totalHours == null) {
                System.err.println("[PointsCalculation] 错误：计算得到的时长为null，使用默认值0");
                totalHours = java.math.BigDecimal.ZERO;
            }
            
            // 更新到数据库（直接设置，不是累加）
            int updateResult = volunteerMapper.syncHours(volunteerId, totalHours);
            
            if (updateResult > 0) {
                System.out.println("[PointsCalculation] 时长同步成功: " + totalHours + " 小时，更新记录数: " + updateResult);
            } else {
                System.err.println("[PointsCalculation] 警告：时长同步可能失败，更新记录数: " + updateResult);
                
                // 尝试使用备用方法更新
                Volunteer volunteer = volunteerMapper.selectById(volunteerId);
                if (volunteer != null) {
                    volunteer.setTotalHours(totalHours);
                    volunteerMapper.update(volunteer);
                    System.out.println("[PointsCalculation] 使用备用方法更新时长成功: " + totalHours + " 小时");
                }
            }
        } catch (Exception e) {
            System.err.println("[PointsCalculation] 同步志愿者服务时长失败，ID: " + volunteerId + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 同步志愿者的积分和时长（一次性同步）
     * @param volunteerId 志愿者ID
     */
    public void syncVolunteerPointsAndHours(Long volunteerId) {
        System.out.println("[PointsCalculation] 开始同步志愿者积分和时长，ID: " + volunteerId);
        syncVolunteerPoints(volunteerId);
        syncVolunteerHours(volunteerId);
        System.out.println("[PointsCalculation] 积分和时长同步完成");
    }
    
    /**
     * 批量同步所有志愿者的积分和时长
     * 从签到记录和补录记录重新计算所有志愿者的统计数据
     * @return 同步结果统计
     */
    public Map<String, Object> syncAllVolunteerPointsAndHours() {
        System.out.println("[PointsCalculation] ========== 开始批量同步所有志愿者的积分和时长 ==========");
        
        // 获取所有志愿者ID
        List<Long> volunteerIds = volunteerMapper.selectAllVolunteerIds();
        System.out.println("[PointsCalculation] 共找到 " + volunteerIds.size() + " 个志愿者需要同步");
        
        int successCount = 0;
        int failCount = 0;
        List<Map<String, Object>> failedVolunteers = new java.util.ArrayList<>();
        
        for (Long volunteerId : volunteerIds) {
            try {
                System.out.println("[PointsCalculation] ========================================");
                System.out.println("[PointsCalculation] 正在处理志愿者 ID: " + volunteerId + " (" + (successCount + failCount + 1) + "/" + volunteerIds.size() + ")");
                
                // 同步积分和时长
                syncVolunteerPointsAndHours(volunteerId);
                
                successCount++;
                System.out.println("[PointsCalculation] 志愿者 " + volunteerId + " 同步成功");
            } catch (Exception e) {
                failCount++;
                System.err.println("[PointsCalculation] 志愿者 " + volunteerId + " 同步失败: " + e.getMessage());
                e.printStackTrace();
                
                Map<String, Object> failInfo = new HashMap<>();
                failInfo.put("volunteerId", volunteerId);
                failInfo.put("error", e.getMessage());
                failedVolunteers.add(failInfo);
            }
        }
        
        System.out.println("[PointsCalculation] ========================================");
        System.out.println("[PointsCalculation] 批量同步完成！");
        System.out.println("[PointsCalculation] 总计: " + volunteerIds.size() + " 个志愿者");
        System.out.println("[PointsCalculation] 成功: " + successCount + " 个");
        System.out.println("[PointsCalculation] 失败: " + failCount + " 个");
        System.out.println("[PointsCalculation] ========================================");
        
        // 返回统计结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", volunteerIds.size());
        result.put("success", successCount);
        result.put("failed", failCount);
        result.put("failedVolunteers", failedVolunteers);
        
        return result;
    }
}
