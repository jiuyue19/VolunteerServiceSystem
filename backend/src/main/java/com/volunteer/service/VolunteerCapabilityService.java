package com.volunteer.service;

import com.volunteer.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 志愿者能力评估服务
 * 基于数据大屏统计数据计算志愿者的综合贡献能力
 * 不使用volunteer表的static字段，而是从实际业务表实时计算
 */
@Service
public class VolunteerCapabilityService {

    @Autowired
    private ActivityApplicationMapper applicationMapper;

    @Autowired
    private ActivityCheckinMapper checkinMapper;

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private ExchangeOrderMapper exchangeOrderMapper;

    @Autowired
    private ServiceHourReplenishMapper replenishMapper;

    @Autowired
    private VolunteerMapper volunteerMapper;

    @Autowired
    private PointsCalculationService pointsCalculationService;

    /**
     * 计算志愿者的综合能力评估
     * @param volunteerId 志愿者ID
     * @return 包含6个维度评分和综合评分的Map
     */
    public Map<String, Object> calculateVolunteerCapability(Long volunteerId) {
        Map<String, Object> capability = new HashMap<>();

        System.out.println("[VolunteerCapabilityService] 开始计算志愿者 " + volunteerId + " 的能力评估");

        // 1. 参与频率：个人报名参与活动 / 所有人报名参与活动
        double participationRate = calculateParticipationRate(volunteerId);
        capability.put("participationRate", participationRate);

        // 2. 服务准时率：个人准时签到 / 所有人准时签到
        double punctualityRate = calculatePunctualityRate(volunteerId);
        capability.put("punctualityRate", punctualityRate);

        // 3. 活动完成率：个人正常签退 / 所有人正常签退
        double completionRate = calculateCompletionRate(volunteerId);
        capability.put("completionRate", completionRate);

        // 4. 积分获得率：个人积分获得 / 所有人积分获得（实时计算，不用static字段）
        double pointsRate = calculatePointsRate(volunteerId);
        capability.put("pointsRate", pointsRate);

        // 5. 论坛活跃率：个人发布帖子 / 所有人发布帖子
        double forumActivityRate = calculateForumActivityRate(volunteerId);
        capability.put("forumActivityRate", forumActivityRate);

        // 6. 积分兑换率：个人兑换成功 / 所有人兑换成功
        double exchangeRate = calculateExchangeRate(volunteerId);
        capability.put("exchangeRate", exchangeRate);

        // 计算综合评分（6个维度的加权平均）
        double comprehensiveScore = calculateComprehensiveScore(
                participationRate, punctualityRate, completionRate,
                pointsRate, forumActivityRate, exchangeRate
        );
        capability.put("comprehensiveScore", comprehensiveScore);

        // 添加绝对值统计（用于展示详细信息）
        Map<String, Object> absoluteStats = getAbsoluteStats(volunteerId);
        capability.put("absoluteStats", absoluteStats);

        System.out.println("[VolunteerCapabilityService] 志愿者 " + volunteerId + " 能力评估完成: " + capability);

        return capability;
    }

    /**
     * 计算参与频率
     * 个人通过审核的申请数 / 所有人通过审核的申请总数
     */
    private double calculateParticipationRate(Long volunteerId) {
        Integer personalApplications = applicationMapper.getApprovedApplicationCount(volunteerId);
        Integer totalApplications = applicationMapper.getTotalApprovedApplications();

        System.out.println("[VolunteerCapabilityService] 参与频率 - 个人: " + personalApplications + ", 总数: " + totalApplications);

        if (totalApplications == null || totalApplications == 0) {
            return 0.0;
        }

        double rate = (personalApplications != null ? personalApplications : 0) * 100.0 / totalApplications;
        return round(rate, 2);
    }

    /**
     * 计算服务准时率
     * 个人准时签到次数 / 所有人准时签到总次数
     */
    private double calculatePunctualityRate(Long volunteerId) {
        Integer personalOnTime = checkinMapper.getOnTimeCheckinCount(volunteerId);
        Integer totalOnTime = checkinMapper.getTotalOnTimeCheckins();

        System.out.println("[VolunteerCapabilityService] 服务准时率 - 个人: " + personalOnTime + ", 总数: " + totalOnTime);

        if (totalOnTime == null || totalOnTime == 0) {
            return 0.0;
        }

        double rate = (personalOnTime != null ? personalOnTime : 0) * 100.0 / totalOnTime;
        return round(rate, 2);
    }

    /**
     * 计算活动完成率
     * 个人活动签退数 / 个人总申请成功数 * 100（个人完成率，非全局占比）
     */
    private double calculateCompletionRate(Long volunteerId) {
        Integer personalCheckouts = checkinMapper.getTotalCheckinCount(volunteerId);
        Integer personalApplications = applicationMapper.getApprovedApplicationCount(volunteerId);

        System.out.println("[VolunteerCapabilityService] 活动完成率 - 个人签退数: " + personalCheckouts + ", 个人申请成功数: " + personalApplications);

        if (personalApplications == null || personalApplications == 0) {
            return 0.0;
        }

        double rate = (personalCheckouts != null ? personalCheckouts : 0) * 100.0 / personalApplications;
        return round(rate, 2);
    }

    /**
     * 计算积分获得率
     * 个人实际积分获得 / 所有人实际积分获得总和
     * 注意：使用与数据大屏相同的计算逻辑，遍历所有志愿者实时计算总积分
     */
    private double calculatePointsRate(Long volunteerId) {
        try {
            // 使用PointsCalculationService实时计算个人积分
            int personalPoints = pointsCalculationService.getTotalPoints(volunteerId);

            // 使用与数据大屏相同的逻辑计算系统总积分
            // 遍历所有志愿者，使用PointsCalculationService计算每个人的积分并求和
            List<Long> allVolunteerIds = volunteerMapper.selectAllVolunteerIds();
            int totalPoints = 0;
            for (Long vid : allVolunteerIds) {
                try {
                    totalPoints += pointsCalculationService.getTotalPoints(vid);
                } catch (Exception e) {
                    System.err.println("[VolunteerCapabilityService] 计算志愿者 " + vid + " 积分失败: " + e.getMessage());
                }
            }

            System.out.println("[VolunteerCapabilityService] 积分获得率 - 个人积分: " + personalPoints + ", 系统总积分: " + totalPoints);
            System.out.println("[VolunteerCapabilityService] 积分获得率计算: " + personalPoints + " / " + totalPoints + " * 100 = " + (personalPoints * 100.0 / totalPoints));

            if (totalPoints == 0) {
                return 0.0;
            }

            double rate = personalPoints * 100.0 / totalPoints;
            return round(rate, 2);
        } catch (Exception e) {
            System.err.println("[VolunteerCapabilityService] 计算积分获得率失败: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * 计算论坛活跃率
     * 个人发布帖子数 / 所有人发布帖子总数
     */
    private double calculateForumActivityRate(Long volunteerId) {
        Integer personalPosts = forumPostMapper.countByVolunteerId(volunteerId);
        Integer totalPosts = forumPostMapper.countAllPosts();

        System.out.println("[VolunteerCapabilityService] 论坛活跃率 - 个人: " + personalPosts + ", 总数: " + totalPosts);

        if (totalPosts == null || totalPosts == 0) {
            return 0.0;
        }

        double rate = (personalPosts != null ? personalPosts : 0) * 100.0 / totalPosts;
        return round(rate, 2);
    }

    /**
     * 计算积分兑换率
     * 个人兑换成功次数 / 所有人兑换成功总次数
     */
    private double calculateExchangeRate(Long volunteerId) {
        Integer personalExchanges = exchangeOrderMapper.countSuccessfulOrdersByVolunteer(volunteerId);
        Integer totalExchanges = exchangeOrderMapper.countTotalSuccessfulOrders();

        System.out.println("[VolunteerCapabilityService] 积分兑换率 - 个人: " + personalExchanges + ", 总数: " + totalExchanges);

        if (totalExchanges == null || totalExchanges == 0) {
            return 0.0;
        }

        double rate = (personalExchanges != null ? personalExchanges : 0) * 100.0 / totalExchanges;
        return round(rate, 2);
    }

    /**
     * 计算综合评分
     * 使用加权平均算法，各维度权重可调整
     */
    private double calculateComprehensiveScore(
            double participationRate, double punctualityRate, double completionRate,
            double pointsRate, double forumActivityRate, double exchangeRate) {

        // 权重配置（总和为1）
        double w1 = 0.25; // 参与频率权重
        double w2 = 0.20; // 服务准时率权重
        double w3 = 0.25; // 活动完成率权重
        double w4 = 0.15; // 积分获得率权重
        double w5 = 0.10; // 论坛活跃率权重
        double w6 = 0.05; // 积分兑换率权重

        double score = participationRate * w1 +
                punctualityRate * w2 +
                completionRate * w3 +
                pointsRate * w4 +
                forumActivityRate * w5 +
                exchangeRate * w6;

        return round(score, 2);
    }

    /**
     * 获取志愿者的绝对值统计数据
     */
    private Map<String, Object> getAbsoluteStats(Long volunteerId) {
        Map<String, Object> stats = new HashMap<>();

        try {
            stats.put("approvedApplications", applicationMapper.getApprovedApplicationCount(volunteerId));
            stats.put("onTimeCheckins", checkinMapper.getOnTimeCheckinCount(volunteerId));
            stats.put("completedActivities", checkinMapper.getTotalCheckinCount(volunteerId));
            stats.put("totalPoints", pointsCalculationService.getTotalPoints(volunteerId));
            stats.put("forumPosts", forumPostMapper.countByVolunteerId(volunteerId));
            stats.put("successfulExchanges", exchangeOrderMapper.countSuccessfulOrdersByVolunteer(volunteerId));
        } catch (Exception e) {
            System.err.println("[VolunteerCapabilityService] 获取绝对值统计失败: " + e.getMessage());
        }

        return stats;
    }

    /**
     * 批量计算所有志愿者的能力评估（用于排行榜）
     */
    public Map<Long, Map<String, Object>> calculateAllVolunteersCapability() {
        // TODO: 实现批量计算逻辑
        return new HashMap<>();
    }

    /**
     * 四舍五入到指定小数位
     */
    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
