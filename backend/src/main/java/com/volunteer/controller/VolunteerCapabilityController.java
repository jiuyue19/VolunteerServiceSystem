package com.volunteer.controller;

import com.volunteer.service.VolunteerCapabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 志愿者能力评估Controller
 * 提供志愿者综合贡献能力的评估接口
 */
@RestController
@RequestMapping("/api/volunteer/capability")
@CrossOrigin
public class VolunteerCapabilityController {
    
    @Autowired
    private VolunteerCapabilityService capabilityService;
    
    /**
     * 获取指定志愿者的能力评估
     * GET /api/volunteer/capability/{volunteerId}
     * 
     * @param volunteerId 志愿者ID
     * @return 包含6个维度评分和综合评分的能力评估数据
     */
    @GetMapping("/{volunteerId}")
    public ResponseEntity<Map<String, Object>> getVolunteerCapability(@PathVariable Long volunteerId) {
        try {
            System.out.println("========================================");
            System.out.println("[VolunteerCapabilityController] 接收到能力评估请求，志愿者ID: " + volunteerId);
            System.out.println("[VolunteerCapabilityController] API路径: /api/volunteer/capability/" + volunteerId);
            System.out.println("========================================");
            
            Map<String, Object> capability = capabilityService.calculateVolunteerCapability(volunteerId);
            System.out.println("[VolunteerCapabilityController] 能力评估数据返回成功");
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取能力评估成功");
            response.put("data", capability);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("[VolunteerCapabilityController] 获取能力评估失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取能力评估失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取能力评估说明文档
     * GET /api/volunteer/capability/info
     * 
     * @return 能力评估算法说明
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getCapabilityInfo() {
        Map<String, Object> info = new HashMap<>();
        
        Map<String, String> metrics = new HashMap<>();
        metrics.put("participationRate", "参与频率 = 个人通过审核的申请数 / 所有人通过审核的申请总数 × 100%");
        metrics.put("punctualityRate", "服务准时率 = 个人准时签到次数 / 所有人准时签到总次数 × 100%");
        metrics.put("completionRate", "活动完成率 = 个人正常签退次数 / 所有人正常签退总次数 × 100%");
        metrics.put("pointsRate", "积分获得率 = 个人实际积分获得 / 所有人实际积分获得总和 × 100%");
        metrics.put("forumActivityRate", "论坛活跃率 = 个人发布帖子数 / 所有人发布帖子总数 × 100%");
        metrics.put("exchangeRate", "积分兑换率 = 个人兑换成功次数 / 所有人兑换成功总次数 × 100%");
        
        Map<String, Double> weights = new HashMap<>();
        weights.put("participationRate", 0.25);
        weights.put("punctualityRate", 0.20);
        weights.put("completionRate", 0.25);
        weights.put("pointsRate", 0.15);
        weights.put("forumActivityRate", 0.10);
        weights.put("exchangeRate", 0.05);
        
        info.put("metrics", metrics);
        info.put("weights", weights);
        info.put("comprehensiveScoreFormula", "综合评分 = Σ(各维度评分 × 对应权重)");
        info.put("dataSource", "所有数据均从业务表实时计算，不使用volunteer表的static字段");
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取能力评估说明成功");
        response.put("data", info);
        
        return ResponseEntity.ok(response);
    }
}
