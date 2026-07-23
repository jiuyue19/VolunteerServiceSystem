package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.service.VolunteerStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 志愿者统计Controller
 * 提供统计数据API接口
 */
@RestController
@RequestMapping("/api/volunteer/stats")
@CrossOrigin
public class VolunteerStatsController {

    @Autowired
    private VolunteerStatsService statsService;

    /**
     * 获取志愿者的完整统计数据
     * 包括：累计时长、累计积分、参与活动数、获得证书数
     * @param volunteerId 志愿者ID
     * @return 统计数据
     */
    @GetMapping("/{volunteerId}")
    public Result<Map<String, Object>> getVolunteerStats(@PathVariable Long volunteerId) {
        try {
            Map<String, Object> stats = statsService.getVolunteerStats(volunteerId);
            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[VolunteerStatsController] 获取统计数据失败: " + e.getMessage());
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取志愿者成长轨迹数据
     * @param volunteerId 志愿者ID
     * @param period 周期类型: day/week/month/year，默认month
     * @return 成长轨迹数据
     */
    @GetMapping("/{volunteerId}/growth-trend")
    public Result<Map<String, Object>> getGrowthTrend(
            @PathVariable Long volunteerId,
            @RequestParam(defaultValue = "month") String period) {
        try {
            Map<String, Object> trendData = statsService.getGrowthTrendData(volunteerId, period);
            return Result.success(trendData);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[VolunteerStatsController] 获取成长轨迹数据失败: " + e.getMessage());
            return Result.error("获取成长轨迹数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取志愿者能力雷达图数据
     * @param volunteerId 志愿者ID
     * @return 能力雷达图数据
     */
    @GetMapping("/{volunteerId}/ability-radar")
    public Result<Map<String, Object>> getAbilityRadar(@PathVariable Long volunteerId) {
        System.out.println("========================================");
        System.out.println("[VolunteerStatsController] 接收到能力雷达图请求，志愿者ID: " + volunteerId);
        System.out.println("========================================");
        try {
            Map<String, Object> radarData = statsService.getAbilityRadarData(volunteerId);
            System.out.println("[VolunteerStatsController] 能力雷达图数据返回成功");
            return Result.success(radarData);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[VolunteerStatsController] 获取能力雷达图数据失败: " + e.getMessage());
            return Result.error("获取能力雷达图数据失败: " + e.getMessage());
        }
    }
}
