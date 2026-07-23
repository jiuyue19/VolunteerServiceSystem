package com.volunteer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.volunteer.mapper.*;
import com.volunteer.entity.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 志愿者统计服务
 * 统一的统计数据计算逻辑
 */
@Service
public class VolunteerStatsService {

    @Autowired
    private ActivityCheckinMapper checkinMapper;
    
    @Autowired
    private ServiceHourReplenishMapper replenishMapper;
    
    @Autowired
    private CertificateLibraryMapper certificateMapper;
    
    @Autowired
    private PointsCalculationService pointsCalculationService;
    
    @Autowired
    private ActivityApplicationMapper applicationMapper;
    
    @Autowired
    private ForumPostMapper forumPostMapper;
    
    @Autowired
    private ExchangeOrderMapper exchangeOrderMapper;
    
    @Autowired
    private VolunteerMapper volunteerMapper;

    /**
     * 获取志愿者的完整统计数据
     * @param volunteerId 志愿者ID
     * @return 统计数据
     */
    public Map<String, Object> getVolunteerStats(Long volunteerId) {
        System.out.println("========================================");
        System.out.println("[VolunteerStats] 开始计算志愿者统计数据，志愿者ID: " + volunteerId);
        System.out.println("========================================");
        
        // 1. 计算累计服务时长
        BigDecimal totalHours = calculateTotalHours(volunteerId);
        
        // 2. 计算累计积分（调用积分计算服务）
        Map<String, Object> pointsData = pointsCalculationService.calculateVolunteerPoints(volunteerId);
        int totalPoints = (int) pointsData.get("totalPoints");
        
        // 3. 计算参与活动数：已完成志愿活动数 + 补录活动数
        int activityCount = calculateActivityCount(volunteerId);
        
        // 4. 计算获得证书数：只记录已颁发的证书
        int certificateCount = calculateCertificateCount(volunteerId);
        
        System.out.println("[VolunteerStats] 统计数据计算完成:");
        System.out.println("  累计时长: " + totalHours + " 小时");
        System.out.println("  累计积分: " + totalPoints);
        System.out.println("  参与活动数: " + activityCount);
        System.out.println("  获得证书数: " + certificateCount);
        
        // 返回统计数据
        Map<String, Object> result = new HashMap<>();
        result.put("totalHours", totalHours);
        result.put("totalPoints", totalPoints);
        result.put("activityCount", activityCount);
        result.put("certificateCount", certificateCount);
        
        // 附加详细的积分信息
        result.put("pointsDetail", pointsData);
        
        return result;
    }
    
    public int getActivityCount(Long volunteerId) {
        return calculateActivityCount(volunteerId);
    }
    
    /**
     * 计算累计服务时长
     * 包括：已签退的活动时长 + 已通过的补录时长
     */
    private BigDecimal calculateTotalHours(Long volunteerId) {
        BigDecimal totalHours = BigDecimal.ZERO;
        
        // 1. 已签退的活动时长
        List<ActivityCheckin> checkins = checkinMapper.selectByVolunteerId(volunteerId);
        if (checkins != null) {
            for (ActivityCheckin checkin : checkins) {
                if (checkin.getCheckoutTime() != null && checkin.getServiceHours() != null) {
                    totalHours = totalHours.add(checkin.getServiceHours());
                    System.out.println("[VolunteerStats] 活动签退时长: +" + checkin.getServiceHours());
                }
            }
        }
        
        // 2. 已通过的补录时长
        List<ServiceHourReplenish> replenishes = replenishMapper.selectByVolunteerId(volunteerId);
        if (replenishes != null) {
            for (ServiceHourReplenish replenish : replenishes) {
                if ("已通过".equals(replenish.getStatus()) && replenish.getHours() != null) {
                    totalHours = totalHours.add(replenish.getHours());
                    System.out.println("[VolunteerStats] 补录时长: +" + replenish.getHours());
                }
            }
        }
        
        return totalHours;
    }
    
    /**
     * 计算参与活动数
     * 业务逻辑：参与活动数 = 已完成志愿活动数（已签退）+ 补录活动数（已通过）
     */
    private int calculateActivityCount(Long volunteerId) {
        int activityCount = 0;
        
        // 1. 已完成志愿活动数（已签退）
        List<ActivityCheckin> checkins = checkinMapper.selectByVolunteerId(volunteerId);
        if (checkins != null) {
            for (ActivityCheckin checkin : checkins) {
                if (checkin.getCheckoutTime() != null) {
                    activityCount++;
                    System.out.println("[VolunteerStats] 已完成活动: 活动ID " + checkin.getActivityId());
                }
            }
        }
        
        // 2. 补录活动数（已通过）
        List<ServiceHourReplenish> replenishes = replenishMapper.selectByVolunteerId(volunteerId);
        if (replenishes != null) {
            for (ServiceHourReplenish replenish : replenishes) {
                if ("已通过".equals(replenish.getStatus())) {
                    activityCount++;
                    System.out.println("[VolunteerStats] 补录活动: 补录ID " + replenish.getId());
                }
            }
        }
        
        return activityCount;
    }
    
    /**
     * 计算获得证书数
     * 业务逻辑：只记录已颁发的证书
     */
    private int calculateCertificateCount(Long volunteerId) {
        int certificateCount = 0;
        
        // 查询志愿者的所有证书
        List<CertificateLibrary> certificates = certificateMapper.selectByVolunteerId(volunteerId);
        if (certificates != null) {
            for (CertificateLibrary cert : certificates) {
                // 只统计已颁发的证书（status = "issued"）
                if ("issued".equals(cert.getStatus())) {
                    certificateCount++;
                    System.out.println("[VolunteerStats] 已颁发证书: 证书编号 " + cert.getCertificateNo());
                }
            }
        }
        
        return certificateCount;
    }
    
    /**
     * 获取志愿者成长轨迹数据（按时间周期）
     * 包含签到签退时长 + 补录时长
     * @param volunteerId 志愿者ID
     * @param period 周期类型: day/week/month/year
     * @return 成长轨迹数据
     */
    public Map<String, Object> getGrowthTrendData(Long volunteerId, String period) {
        System.out.println("[VolunteerStats] 获取成长轨迹数据，period: " + period);
        
        List<Map<String, Object>> checkinData;
        List<Map<String, Object>> replenishData;
        
        if ("day".equals(period)) {
            // 获取最近1天（24小时）的数据
            checkinData = checkinMapper.getDailyHoursByVolunteer(volunteerId, 1);
            replenishData = replenishMapper.getDailyReplenishHours(volunteerId, 1);
        } else if ("week".equals(period)) {
            // 获取最近7天的数据
            checkinData = checkinMapper.getWeeklyHoursByVolunteer(volunteerId, 7);
            replenishData = replenishMapper.getWeeklyReplenishHours(volunteerId, 7);
        } else if ("year".equals(period)) {
            // 获取最近12个月的数据
            checkinData = checkinMapper.getYearlyHoursByVolunteer(volunteerId, 1);
            replenishData = replenishMapper.getYearlyReplenishHours(volunteerId, 1);
        } else {
            // 默认按月，获取最近1个月的数据
            checkinData = checkinMapper.getMonthlyHoursByVolunteer(volunteerId, 1);
            replenishData = replenishMapper.getMonthlyReplenishHours(volunteerId, 1);
        }
        
        // 合并签到和补录数据
        List<Map<String, Object>> mergedData = mergeHoursData(checkinData, replenishData, period);
        
        Map<String, Object> result = new HashMap<>();
        result.put("period", period);
        result.put("data", mergedData);
        
        return result;
    }
    
    /**
     * 合并签到和补录的时长数据
     */
    private List<Map<String, Object>> mergeHoursData(
            List<Map<String, Object>> checkinData, 
            List<Map<String, Object>> replenishData,
            String period) {
        
        Map<String, Double> mergedMap = new HashMap<>();
        
        // 处理签到数据
        for (Map<String, Object> item : checkinData) {
            String key = getTimeKey(item, period);
            double hours = getHoursValue(item);
            mergedMap.put(key, mergedMap.getOrDefault(key, 0.0) + hours);
        }
        
        // 处理补录数据
        for (Map<String, Object> item : replenishData) {
            String key = getTimeKey(item, period);
            double hours = getHoursValue(item);
            mergedMap.put(key, mergedMap.getOrDefault(key, 0.0) + hours);
        }
        
        // 转换回List格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Double> entry : mergedMap.entrySet()) {
            Map<String, Object> dataPoint = new HashMap<>();
            setTimeKey(dataPoint, entry.getKey(), period);
            dataPoint.put("hours", entry.getValue());
            result.add(dataPoint);
        }
        
        return result;
    }
    
    /**
     * 获取时间键值
     */
    private String getTimeKey(Map<String, Object> item, String period) {
        if ("day".equals(period)) {
            return String.valueOf(item.get("hour"));
        } else if ("week".equals(period)) {
            return String.valueOf(item.get("dayOfWeek"));
        } else if ("month".equals(period)) {
            return String.valueOf(item.get("day"));
        } else if ("year".equals(period)) {
            return String.valueOf(item.get("monthNum"));
        }
        return "";
    }
    
    /**
     * 设置时间键值
     */
    private void setTimeKey(Map<String, Object> dataPoint, String key, String period) {
        if ("day".equals(period)) {
            dataPoint.put("hour", Integer.parseInt(key));
        } else if ("week".equals(period)) {
            dataPoint.put("dayOfWeek", Integer.parseInt(key));
        } else if ("month".equals(period)) {
            dataPoint.put("day", Integer.parseInt(key));
        } else if ("year".equals(period)) {
            dataPoint.put("monthNum", Integer.parseInt(key));
        }
    }
    
    /**
     * 获取小时数值
     */
    private double getHoursValue(Map<String, Object> item) {
        Object hours = item.get("hours");
        if (hours instanceof Number) {
            return ((Number) hours).doubleValue();
        }
        return 0.0;
    }
    
    /**
     * 获取志愿者能力雷达图数据（基于全局比率计算）
     * @param volunteerId 志愿者ID
     * @return 能力雷达图数据
     */
    public Map<String, Object> getAbilityRadarData(Long volunteerId) {
        System.out.println("[VolunteerStats] 计算能力雷达图数据（基于比率）");
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> indicators = new ArrayList<>();
        
        // 1. 参与频率 (0-100)
        // 个人报名参与活动 / 所有人报名参与活动
        Integer personalApplications = applicationMapper.getApprovedApplicationCount(volunteerId);
        Integer totalApplications = applicationMapper.getTotalApprovedApplications();
        int participationRate = calculateRate(personalApplications, totalApplications);
        indicators.add(createIndicator("参与频率", participationRate, "个人报名活动占比"));
        
        // 2. 服务准时率 (0-100)
        // 个人准时签到 / 所有人准时签到
        Integer personalOnTime = checkinMapper.getOnTimeCheckinCount(volunteerId);
        Integer totalOnTime = checkinMapper.getTotalOnTimeCheckins();
        int punctualityRate = calculateRate(personalOnTime, totalOnTime);
        indicators.add(createIndicator("服务准时率", punctualityRate, "个人准时签到占比"));
        
        // 3. 活动完成率 (0-100)
        // 个人活动签退数 / 个人总申请成功数（个人完成率，非全局占比）
        Integer personalCheckouts = checkinMapper.getTotalCheckinCount(volunteerId);
        Integer personalApprovedApps = applicationMapper.getApprovedApplicationCount(volunteerId);
        int completionRate = calculatePersonalCompletionRate(personalCheckouts, personalApprovedApps);
        indicators.add(createIndicator("活动完成率", completionRate, "个人活动完成度"));
        
        // 4. 积分获得率 (0-100)
        // 个人积分 / 所有人积分
        try {
            System.out.println("[VolunteerStats] === 开始计算积分获得率 ===");
            Map<String, Object> pointsData = pointsCalculationService.calculateVolunteerPoints(volunteerId);
            int personalPoints = (int) pointsData.get("totalPoints");
            System.out.println("[VolunteerStats] 个人积分: " + personalPoints);
            
            // 获取所有志愿者总积分（需要遍历计算或从数据库统计）
            System.out.println("[VolunteerStats] 开始计算系统总积分...");
            int totalPoints = getTotalSystemPoints();
            System.out.println("[VolunteerStats] ✓ 系统总积分获取成功: " + totalPoints);
            
            int pointsRate = calculateRate(personalPoints, totalPoints);
            System.out.println("[VolunteerStats] ✓ 积分获得率计算成功: " + pointsRate + "%");
            indicators.add(createIndicator("积分获得率", pointsRate, "个人积分获得占比"));
            System.out.println("[VolunteerStats] === 积分获得率计算完成 ===");
        } catch (Exception e) {
            System.err.println("[VolunteerStats] ❌ 积分获得率计算失败: " + e.getMessage());
            e.printStackTrace();
            // 降级处理：使用0作为默认值
            indicators.add(createIndicator("积分获得率", 0, "个人积分获得占比"));
        }
        
        // 5. 论坛活跃率 (0-100)
        // 个人发帖数 / 所有人发帖数
        System.out.println("[VolunteerStats] === 开始计算论坛活跃率 ===");
        Integer personalPosts = forumPostMapper.countByVolunteerId(volunteerId);
        Integer totalPosts = forumPostMapper.countAllPosts();
        System.out.println("[VolunteerStats] 论坛活跃率 - 个人: " + personalPosts + ", 总数: " + totalPosts);
        int forumActivityRate = calculateRate(personalPosts, totalPosts);
        indicators.add(createIndicator("论坛活跃率", forumActivityRate, "个人发帖数占比"));
        
        // 6. 积分兑换率 (0-100)
        // 个人兑换成功次数 / 所有人兑换成功次数
        System.out.println("[VolunteerStats] === 开始计算积分兑换率 ===");
        Integer personalExchanges = exchangeOrderMapper.countSuccessfulOrdersByVolunteer(volunteerId);
        Integer totalExchanges = exchangeOrderMapper.countTotalSuccessfulOrders();
        System.out.println("[VolunteerStats] 积分兑换率 - 个人: " + personalExchanges + ", 总数: " + totalExchanges);
        int exchangeRate = calculateRate(personalExchanges, totalExchanges);
        indicators.add(createIndicator("积分兑换率", exchangeRate, "个人兑换成功次数占比"));
        
        result.put("indicators", indicators);
        
        System.out.println("[VolunteerStats] 能力雷达图数据计算完成");
        return result;
    }
    
    /**
     * 计算比率并转换为0-100分数
     * @param personal 个人数值
     * @param total 全局总数
     * @return 0-100的分数
     */
    private int calculateRate(Integer personal, Integer total) {
        if (total == null || total == 0) {
            return 0;
        }
        if (personal == null) {
            personal = 0;
        }
        // 计算比率并乘以100，转换为百分制
        double rate = (personal * 100.0) / total;
        // 限制最大值为100
        return Math.min(100, (int) Math.round(rate));
    }
    
    /**
     * 计算个人完成率（用于活动完成率）
     * 个人签退数 / 个人申请成功数 * 100
     * @param personalCheckouts 个人签退次数
     * @param personalApplications 个人申请成功数
     * @return 0-100的分数
     */
    private int calculatePersonalCompletionRate(Integer personalCheckouts, Integer personalApplications) {
        if (personalApplications == null || personalApplications == 0) {
            return 0;
        }
        if (personalCheckouts == null) {
            personalCheckouts = 0;
        }
        // 计算个人完成率
        double rate = (personalCheckouts * 100.0) / personalApplications;
        // 限制最大值为100
        return Math.min(100, (int) Math.round(rate));
    }
    
    /**
     * 获取系统所有志愿者的总积分
     * 使用与数据大屏相同的计算逻辑：遍历所有志愿者，实时计算每个人的积分并求和
     */
    private int getTotalSystemPoints() {
        try {
            System.out.println("[VolunteerStats] 开始计算系统总积分（与数据大屏保持一致）");
            
            // 获取所有志愿者ID
            List<Long> allVolunteerIds = volunteerMapper.selectAllVolunteerIds();
            int totalPoints = 0;
            
            // 遍历每个志愿者，使用PointsCalculationService实时计算积分
            for (Long volunteerId : allVolunteerIds) {
                try {
                    int points = pointsCalculationService.getTotalPoints(volunteerId);
                    totalPoints += points;
                } catch (Exception e) {
                    System.err.println("[VolunteerStats] 计算志愿者 " + volunteerId + " 积分失败: " + e.getMessage());
                }
            }
            
            System.out.println("[VolunteerStats] 系统总积分计算完成: " + totalPoints);
            
            // 避免除零
            return totalPoints > 0 ? totalPoints : 1;
        } catch (Exception e) {
            // 如果查询失败，返回一个默认值避免除零
            System.err.println("[VolunteerStats] 获取系统总积分失败: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }
    
    /**
     * 创建雷达图指标
     */
    private Map<String, Object> createIndicator(String name, int value, String description) {
        Map<String, Object> indicator = new HashMap<>();
        indicator.put("name", name);
        indicator.put("value", value);
        indicator.put("max", 100);
        indicator.put("description", description);
        return indicator;
    }
}
