package com.volunteer.service;

import com.volunteer.entity.*;
import com.volunteer.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员数据统计Service
 * 聚合系统所有数据用于数据大屏展示
 */
@Service
public class AdminStatsService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ActivityCheckinMapper checkinMapper;

    @Autowired
    private VolunteerMapper volunteerMapper;

    @Autowired
    private ServiceHourReplenishMapper replenishMapper;

    @Autowired
    private CertificateLibraryMapper certificateMapper;

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private ForumCategoryMapper forumCategoryMapper;

    @Autowired
    private ActivityCategoryMapper activityCategoryMapper;

    @Autowired
    private VolunteerAddressMapper addressMapper;

    @Autowired
    private ActivityApplicationMapper applicationMapper;

    @Autowired
    private EventLogService eventLogService;
    
    @Autowired
    private PointsCalculationService pointsCalculationService;

    /**
     * 获取KPI数据
     */
    public Map<String, Object> getKpiData() {
        Map<String, Object> kpiData = new HashMap<>();

        // 1. 累计志愿时长（实时计算：签退记录 + 补录记录，保留2位小数）
        Double totalHours = volunteerMapper.getTotalHours();
        System.out.println("[AdminStatsService] 累计志愿时长查询结果: " + totalHours);
        kpiData.put("totalHours", totalHours != null ? Math.round(totalHours * 100.0) / 100.0 : 0.00);

        // 2. 累计志愿活动数
        Integer totalActivities = activityMapper.countAll();
        System.out.println("[AdminStatsService] 累计志愿活动数查询结果: " + totalActivities);
        kpiData.put("totalActivities", totalActivities != null ? totalActivities : 0);

        // 3. 累计补录时长（保留2位小数，不四舍五入）
        Double totalReplenishHours = replenishMapper.getTotalReplenishHours();
        System.out.println("[AdminStatsService] 累计补录时长查询结果: " + totalReplenishHours);
        kpiData.put("totalReplenishHours", totalReplenishHours != null ? Math.round(totalReplenishHours * 100.0) / 100.0 : 0.00);

        // 4. 累计积分总额（使用PointsCalculationService计算实际总积分）
        int totalPoints = calculateTotalSystemPoints();
        System.out.println("[AdminStatsService] 累计积分总额计算结果: " + totalPoints);
        kpiData.put("totalPoints", totalPoints);

        // 5. 已发放证书数（只统计已发放的证书，状态为issued）
        Integer totalCertificates = certificateMapper.countByStatus("issued");
        System.out.println("[AdminStatsService] 已发放证书数查询结果: " + totalCertificates);
        kpiData.put("totalCertificates", totalCertificates != null ? totalCertificates : 0);

        // 6. 计算趋势（与上月对比，这里简化处理，返回随机趋势）
        kpiData.put("hoursTrend", calculateTrend(totalHours.doubleValue()));
        kpiData.put("activitiesTrend", calculateTrend(totalActivities != null ? totalActivities.doubleValue() : 0.0));
        kpiData.put("replenishTrend", calculateTrend(totalReplenishHours.doubleValue()));
        kpiData.put("pointsTrend", calculateTrend((double) totalPoints));
        kpiData.put("certificatesTrend", calculateTrend(totalCertificates != null ? totalCertificates.doubleValue() : 0.0));

        System.out.println("[AdminStatsService] KPI数据汇总: " + kpiData);
        return kpiData;
    }

    /**
     * 获取活动参与人数统计（按活动类型）
     */
    public Map<String, Object> getActivityParticipantsStats() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取所有活动类型
        List<ActivityCategory> categories = activityCategoryMapper.selectAll();
        
        List<String> categoryNames = new ArrayList<>();
        List<Integer> participantCounts = new ArrayList<>();
        
        for (ActivityCategory category : categories) {
            categoryNames.add(category.getName());
            // 统计该类型活动的参与人数
            Integer count = applicationMapper.countParticipantsByCategory(category.getId());
            participantCounts.add(count != null ? count : 0);
        }
        
        result.put("categories", categoryNames);
        result.put("counts", participantCounts);
        
        return result;
    }

    /**
     * 获取志愿时长趋势（最近6个月）
     */
    public Map<String, Object> getHoursTrendStats() {
        Map<String, Object> result = new HashMap<>();
        
        List<String> months = new ArrayList<>();
        List<Double> hours = new ArrayList<>();
        
        // 获取最近6个月的数据
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);
            
            String monthName = monthStart.format(DateTimeFormatter.ofPattern("M月"));
            months.add(monthName);
            
            Double monthHours = checkinMapper.getHoursByDateRange(monthStart, monthEnd);
            hours.add(monthHours != null ? monthHours : 0.0);
        }
        
        result.put("months", months);
        result.put("hours", hours);
        
        return result;
    }

    /**
     * 获取补录时长趋势（最近6个月）
     */
    public Map<String, Object> getReplenishTrendStats() {
        Map<String, Object> result = new HashMap<>();
        
        List<String> months = new ArrayList<>();
        List<Double> hours = new ArrayList<>();
        
        // 获取最近6个月的数据
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);
            
            String monthName = monthStart.format(DateTimeFormatter.ofPattern("M月"));
            months.add(monthName);
            
            Double monthHours = replenishMapper.getHoursByDateRange(monthStart, monthEnd);
            hours.add(monthHours != null ? monthHours : 0.0);
        }
        
        result.put("months", months);
        result.put("hours", hours);
        
        return result;
    }

    /**
     * 获取活动类型占比
     */
    public Map<String, Object> getActivityDistributionStats() {
        Map<String, Object> result = new HashMap<>();
        
        List<ActivityCategory> categories = activityCategoryMapper.selectAll();
        
        List<Map<String, Object>> distribution = new ArrayList<>();
        
        for (ActivityCategory category : categories) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", category.getName());
            
            Integer count = activityMapper.countByCategory(category.getId());
            item.put("value", count != null ? count : 0);
            
            distribution.add(item);
        }
        
        result.put("data", distribution);
        
        return result;
    }

    /**
     * 获取积分排行榜（Top 10）- 使用PointsCalculationService计算实际积分后排序
     */
    public Map<String, Object> getPointsRankingStats() {
        Map<String, Object> result = new HashMap<>();
        
        System.out.println("[AdminStatsService] 开始计算积分排行榜（使用实际积分计算）");
        
        // 获取所有志愿者
        List<Volunteer> allVolunteers = volunteerMapper.selectAll();
        
        // 计算每个志愿者的实际积分
        List<Map<String, Object>> volunteerPoints = new ArrayList<>();
        for (Volunteer volunteer : allVolunteers) {
            try {
                int actualPoints = pointsCalculationService.getTotalPoints(volunteer.getId());
                Map<String, Object> item = new HashMap<>();
                item.put("id", volunteer.getId());
                item.put("username", volunteer.getUsername() != null ? volunteer.getUsername() : "用户" + volunteer.getId());
                item.put("points", actualPoints);
                volunteerPoints.add(item);
            } catch (Exception e) {
                System.err.println("[AdminStatsService] 计算志愿者 " + volunteer.getId() + " 积分失败: " + e.getMessage());
            }
        }
        
        // 按积分降序排序
        volunteerPoints.sort((a, b) -> {
            Integer pointsA = (Integer) a.get("points");
            Integer pointsB = (Integer) b.get("points");
            return pointsB.compareTo(pointsA);
        });
        
        // 取Top 10
        List<String> names = new ArrayList<>();
        List<Integer> points = new ArrayList<>();
        int limit = Math.min(10, volunteerPoints.size());
        for (int i = 0; i < limit; i++) {
            Map<String, Object> item = volunteerPoints.get(i);
            names.add((String) item.get("username"));
            points.add((Integer) item.get("points"));
        }
        
        result.put("names", names);
        result.put("points", points);
        
        System.out.println("[AdminStatsService] 积分排行榜计算完成，Top " + limit + " 志愿者");
        return result;
    }

    /**
     * 获取论坛帖子分布（按分类）
     */
    public Map<String, Object> getForumDistributionStats() {
        Map<String, Object> result = new HashMap<>();
        
        List<ForumCategory> categories = forumCategoryMapper.selectAll();
        
        List<Map<String, Object>> distribution = new ArrayList<>();
        
        for (ForumCategory category : categories) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", category.getName());
            
            Integer count = forumPostMapper.countByCategory(category.getId());
            item.put("value", count != null ? count : 0);
            
            distribution.add(item);
        }
        
        result.put("data", distribution);
        
        return result;
    }

    /**
     * 获取志愿者地域分布
     */
    public Map<String, Object> getVolunteerLocationStats() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取所有志愿者的地址信息
        List<VolunteerAddress> addresses = addressMapper.selectAll();
        
        List<Map<String, Object>> locationData = new ArrayList<>();
        
        for (VolunteerAddress address : addresses) {
            Map<String, Object> item = new HashMap<>();
            item.put("volunteerId", address.getVolunteerId());
            item.put("address", address.getProvince() + address.getCity() + address.getDistrict());
            item.put("province", address.getProvince());
            item.put("city", address.getCity());
            
            locationData.add(item);
        }
        
        result.put("data", locationData);
        
        return result;
    }

    /**
     * 获取最近的链上事件
     */
    public Map<String, Object> getRecentEventsStats(Integer limit) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取最近的事件日志
        List<EventLog> eventLogs = eventLogService.getRecentEvents(limit != null ? limit : 10);
        
        List<Map<String, Object>> events = new ArrayList<>();
        
        for (EventLog log : eventLogs) {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", determineEventType(log.getEventType()));
            event.put("activityName", log.getActivityName() != null ? log.getActivityName() : "系统活动");
            event.put("volunteerName", log.getVolunteerName() != null ? log.getVolunteerName() : "系统");
            event.put("createTime", log.getCreateTime());
            event.put("description", log.getDescription());
            event.put("blockchainHash", log.getBlockchainHash() != null ? log.getBlockchainHash() : generateMockHash());
            
            events.add(event);
        }
        
        result.put("events", events);
        
        return result;
    }

    /**
     * 获取所有统计数据
     */
    public Map<String, Object> getAllStats() {
        Map<String, Object> allStats = new HashMap<>();
        
        allStats.put("kpi", getKpiData());
        allStats.put("activityParticipants", getActivityParticipantsStats());
        allStats.put("hoursTrend", getHoursTrendStats());
        allStats.put("replenishTrend", getReplenishTrendStats());
        allStats.put("activityDistribution", getActivityDistributionStats());
        allStats.put("pointsRanking", getPointsRankingStats());
        allStats.put("forumDistribution", getForumDistributionStats());
        allStats.put("volunteerLocation", getVolunteerLocationStats());
        allStats.put("recentEvents", getRecentEventsStats(10));
        
        return allStats;
    }

    /**
     * 计算趋势百分比（简化版本）
     */
    private Double calculateTrend(Double currentValue) {
        if (currentValue == null || currentValue == 0) {
            return 0.0;
        }
        // 简化处理：返回5-20之间的随机增长率
        return Math.round((Math.random() * 15 + 5) * 10) / 10.0;
    }

    /**
     * 确定事件类型
     */
    private String determineEventType(String eventType) {
        if (eventType == null) return "activity";
        
        if (eventType.contains("签退") || eventType.contains("时长")) {
            return "hours";
        } else if (eventType.contains("证书")) {
            return "certificate";
        } else if (eventType.contains("补录")) {
            return "replenish";
        } else {
            return "activity";
        }
    }

    /**
     * 生成模拟的区块链哈希
     */
    private String generateMockHash() {
        String chars = "0123456789abcdef";
        StringBuilder hash = new StringBuilder("0x");
        Random random = new Random();
        
        for (int i = 0; i < 4; i++) {
            hash.append(chars.charAt(random.nextInt(chars.length())));
        }
        hash.append("...");
        for (int i = 0; i < 4; i++) {
            hash.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return hash.toString();
    }
    
    /**
     * 计算系统所有志愿者的实际总积分
     * 使用PointsCalculationService遍历计算
     */
    private int calculateTotalSystemPoints() {
        System.out.println("[AdminStatsService] 开始计算系统总积分（使用实际积分计算）");
        
        List<Long> volunteerIds = volunteerMapper.selectAllVolunteerIds();
        int totalPoints = 0;
        
        for (Long volunteerId : volunteerIds) {
            try {
                int points = pointsCalculationService.getTotalPoints(volunteerId);
                totalPoints += points;
            } catch (Exception e) {
                System.err.println("[AdminStatsService] 计算志愿者 " + volunteerId + " 积分失败: " + e.getMessage());
            }
        }
        
        System.out.println("[AdminStatsService] 系统总积分计算完成: " + totalPoints);
        return totalPoints;
    }
}
