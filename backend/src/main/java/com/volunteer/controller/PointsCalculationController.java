package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.service.PointsCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 积分计算Controller
 * 统一的积分计算API接口
 */
@RestController
@RequestMapping("/api/points")
@CrossOrigin
public class PointsCalculationController {

    @Autowired
    private PointsCalculationService pointsCalculationService;

    /**
     * 获取志愿者的详细积分信息
     * @param volunteerId 志愿者ID
     * @return 积分详细信息（包括各部分明细）
     */
    @GetMapping("/calculate/{volunteerId}")
    public Result<Map<String, Object>> calculatePoints(@PathVariable Long volunteerId) {
        try {
            Map<String, Object> pointsData = pointsCalculationService.calculateVolunteerPoints(volunteerId);
            return Result.success(pointsData);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("计算积分失败: " + e.getMessage());
        }
    }
    
    /**
     * 只获取累计积分数值
     * @param volunteerId 志愿者ID
     * @return 累计积分
     */
    @GetMapping("/total/{volunteerId}")
    public Result<Integer> getTotalPoints(@PathVariable Long volunteerId) {
        try {
            int totalPoints = pointsCalculationService.getTotalPoints(volunteerId);
            return Result.success(totalPoints);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取积分失败: " + e.getMessage());
        }
    }
    
    /**
     * 同步单个志愿者的积分到数据库
     * @param volunteerId 志愿者ID
     * @return 同步结果
     */
    @PostMapping("/sync/{volunteerId}")
    public Result<String> syncVolunteerPoints(@PathVariable Long volunteerId) {
        try {
            pointsCalculationService.syncVolunteerPoints(volunteerId);
            return Result.success("积分同步成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("积分同步失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量同步所有志愿者的积分到数据库
     * @return 同步结果
     */
    @PostMapping("/sync/all")
    public Result<String> syncAllVolunteerPoints() {
        try {
            pointsCalculationService.syncAllVolunteerPoints();
            return Result.success("批量同步完成");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量同步失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量同步所有志愿者的积分和时长到数据库
     * 从签到记录和补录记录重新计算所有志愿者的统计数据
     * @return 同步结果统计
     */
    @PostMapping("/sync/all-points-hours")
    public Result<Map<String, Object>> syncAllVolunteerPointsAndHours() {
        try {
            Map<String, Object> result = pointsCalculationService.syncAllVolunteerPointsAndHours();
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量同步失败: " + e.getMessage());
        }
    }
}
