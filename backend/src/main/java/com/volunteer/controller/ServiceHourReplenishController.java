package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.ServiceHourReplenish;
import com.volunteer.service.ServiceHourReplenishService;
import com.volunteer.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/replenish")
@CrossOrigin
public class ServiceHourReplenishController {

    @Autowired
    private ServiceHourReplenishService replenishService;
    
    @Autowired
    private JwtUtil jwtUtil;

    // 管理员获取所有补录申请
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getList() {
        try {
            return Result.success(replenishService.getAllReplenishWithVolunteerInfo());
        } catch (Exception e) {
            return Result.error("获取补录申请列表失败: " + e.getMessage());
        }
    }

    // 志愿者获取自己的补录申请
    @GetMapping("/my")
    public Result<List<ServiceHourReplenish>> getMyReplenish(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(actualToken);
            Long volunteerId = claims.get("userId", Long.class);
            return Result.success(replenishService.getReplenishByVolunteer(volunteerId));
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("获取补录申请失败: " + e.getMessage());
        }
    }

    // 志愿者提交补录申请
    @PostMapping("/apply")
    public Result<Void> applyReplenish(@RequestHeader("Authorization") String token, @RequestBody ServiceHourReplenish replenish) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(actualToken);
            Long volunteerId = claims.get("userId", Long.class);
            replenish.setVolunteerId(volunteerId);
            replenishService.createReplenish(replenish);
            return Result.success();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("提交补录申请失败: " + e.getMessage());
        }
    }

    // 管理员审核补录申请
    @PostMapping("/review")
    public Result<Void> reviewReplenish(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> request) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(actualToken);
            Long adminId = claims.get("userId", Long.class);
            
            Long id = Long.valueOf(request.get("id").toString());
            String status = request.get("status").toString();
            String rejectReason = request.get("rejectReason") != null ? request.get("rejectReason").toString() : null;
            
            replenishService.reviewReplenish(id, status, rejectReason, adminId);
            return Result.success();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("审核补录申请失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            replenishService.deleteReplenish(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除补录申请失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        try {
            replenishService.batchDeleteReplenish(ids);
            return Result.success();
        } catch (Exception e) {
            return Result.error("批量删除补录申请失败: " + e.getMessage());
        }
    }
}