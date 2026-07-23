package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.service.AdminStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员数据统计Controller
 * 提供数据大屏所需的所有实时统计数据
 */
@RestController
@RequestMapping("/api/admin/stats")
@CrossOrigin
public class AdminStatsController {

    @Autowired
    private AdminStatsService adminStatsService;

    /**
     * 获取KPI数据（顶部数字看板）
     * 包括：累计志愿时长、累计志愿活动、累计补录时长、累计积分总额、已发放证书
     * @return KPI数据
     */
    @GetMapping("/kpi")
    public Result<Map<String, Object>> getKpiData() {
        try {
            Map<String, Object> kpiData = adminStatsService.getKpiData();
            return Result.success(kpiData);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[AdminStatsController] 获取KPI数据失败: " + e.getMessage());
            return Result.error("获取KPI数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取活动参与人数统计（按活动类型）
     * @return 活动参与人数数据
     */
    @GetMapping("/activity-participants")
    public Result<Map<String, Object>> getActivityParticipants() {
        try {
            Map<String, Object> data = adminStatsService.getActivityParticipantsStats();
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[AdminStatsController] 获取活动参与人数失败: " + e.getMessage());
            return Result.error("获取活动参与人数失败: " + e.getMessage());
        }
    }

    /**
     * 获取志愿时长趋势数据（按月统计）
     * @return 志愿时长趋势数据
     */
    @GetMapping("/hours-trend")
    public Result<Map<String, Object>> getHoursTrend() {
        try {
            Map<String, Object> data = adminStatsService.getHoursTrendStats();
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[AdminStatsController] 获取志愿时长趋势失败: " + e.getMessage());
            return Result.error("获取志愿时长趋势失败: " + e.getMessage());
        }
    }

    /**
     * 获取补录时长趋势数据（按月统计）
     * @return 补录时长趋势数据
     */
    @GetMapping("/replenish-trend")
    public Result<Map<String, Object>> getReplenishTrend() {
        try {
            Map<String, Object> data = adminStatsService.getReplenishTrendStats();
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[AdminStatsController] 获取补录时长趋势失败: " + e.getMessage());
            return Result.error("获取补录时长趋势失败: " + e.getMessage());
        }
    }

    /**
     * 获取活动类型占比数据
     * @return 活动类型占比数据
     */
    @GetMapping("/activity-distribution")
    public Result<Map<String, Object>> getActivityDistribution() {
        try {
            Map<String, Object> data = adminStatsService.getActivityDistributionStats();
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[AdminStatsController] 获取活动类型占比失败: " + e.getMessage());
            return Result.error("获取活动类型占比失败: " + e.getMessage());
        }
    }

    /**
     * 获取积分排行榜数据（Top 10）
     * @return 积分排行榜数据
     */
    @GetMapping("/points-ranking")
    public Result<Map<String, Object>> getPointsRanking() {
        try {
            Map<String, Object> data = adminStatsService.getPointsRankingStats();
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[AdminStatsController] 获取积分排行榜失败: " + e.getMessage());
            return Result.error("获取积分排行榜失败: " + e.getMessage());
        }
    }

    /**
     * 获取论坛帖子分布数据（按分类）
     * @return 论坛帖子分布数据
     */
    @GetMapping("/forum-distribution")
    public Result<Map<String, Object>> getForumDistribution() {
        try {
            Map<String, Object> data = adminStatsService.getForumDistributionStats();
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[AdminStatsController] 获取论坛帖子分布失败: " + e.getMessage());
            return Result.error("获取论坛帖子分布失败: " + e.getMessage());
        }
    }

    /**
     * 获取志愿者地域分布数据
     * @return 志愿者地域分布数据
     */
    @GetMapping("/volunteer-location")
    public Result<Map<String, Object>> getVolunteerLocation() {
        try {
            Map<String, Object> data = adminStatsService.getVolunteerLocationStats();
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[AdminStatsController] 获取志愿者地域分布失败: " + e.getMessage());
            return Result.error("获取志愿者地域分布失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近的链上事件（实时事件流）
     * @param limit 返回数量限制，默认10条
     * @return 最近的链上事件
     */
    @GetMapping("/recent-events")
    public Result<Map<String, Object>> getRecentEvents(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            Map<String, Object> data = adminStatsService.getRecentEventsStats(limit);
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[AdminStatsController] 获取最近事件失败: " + e.getMessage());
            return Result.error("获取最近事件失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有统计数据（一次性获取所有数据）
     * @return 所有统计数据
     */
    @GetMapping("/all")
    public Result<Map<String, Object>> getAllStats() {
        try {
            Map<String, Object> allStats = adminStatsService.getAllStats();
            return Result.success(allStats);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[AdminStatsController] 获取所有统计数据失败: " + e.getMessage());
            return Result.error("获取所有统计数据失败: " + e.getMessage());
        }
    }
}
