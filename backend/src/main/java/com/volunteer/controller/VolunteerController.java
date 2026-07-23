package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.Volunteer;
import com.volunteer.entity.Admin;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.mapper.ActivityCheckinMapper;
import com.volunteer.mapper.ServiceHourReplenishMapper;
import com.volunteer.mapper.AdminMapper;
import com.volunteer.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/volunteer")
@CrossOrigin
public class VolunteerController {
    
    @Autowired
    private VolunteerMapper volunteerMapper;
    
    @Autowired
    private ActivityCheckinMapper checkinMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AdminMapper adminMapper;
    
    /**
     * 检查管理员是否有志愿者管理权限
     */
    private boolean checkAdminPermission(String token) {
        try {
            System.out.println("[VolunteerController] 开始权限检查，token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
            
            if (token == null || token.trim().isEmpty()) {
                System.err.println("[VolunteerController] Token为空");
                return false;
            }
            
            String actualToken = token.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(actualToken);
            Long userId = claims.get("userId", Long.class);
            String userType = claims.get("userType", String.class);
            
            System.out.println("[VolunteerController] Token解析成功 - userId: " + userId + ", userType: " + userType);
            
            // 只有管理员才能访问管理接口
            if (!"admin".equals(userType)) {
                System.err.println("[VolunteerController] 用户类型不是管理员: " + userType);
                return false;
            }
            
            Admin admin = adminMapper.selectById(userId);
            if (admin == null) {
                System.err.println("[VolunteerController] 管理员不存在: " + userId);
                return false;
            }
            
            System.out.println("[VolunteerController] 管理员信息 - ID: " + admin.getId() + ", Role: " + admin.getRole() + ", Name: " + admin.getName());
            
            // 超级管理员拥有所有权限
            if ("SUPER_ADMIN".equals(admin.getRole())) {
                System.out.println("[VolunteerController] 超级管理员访问，权限通过");
                return true;
            }
            
            // 检查普通管理员是否有 volunteers 权限
            // 从admin.permissions字段读取（JSON数组格式）
            String permissionsStr = admin.getPermissions();
            System.out.println("[VolunteerController] 管理员权限字符串: " + permissionsStr);
            
            if (permissionsStr == null || permissionsStr.trim().isEmpty()) {
                System.err.println("[VolunteerController] 管理员权限为空");
                return false;
            }
            
            // 检查permissions字段中是否包含 "volunteers"
            boolean hasPermission = permissionsStr.contains("\"volunteers\"");
            System.out.println("[VolunteerController] 是否有volunteers权限: " + hasPermission);
            return hasPermission;
        } catch (Exception e) {
            System.err.println("[VolunteerController] 权限检查异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @GetMapping("/info")
    public Result<Volunteer> getInfo(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(actualToken);
            Long userId = claims.get("userId", Long.class);
            Volunteer volunteer = volunteerMapper.selectById(userId);
            if (volunteer != null) {
                volunteer.setPassword(null);
            }
            return Result.success(volunteer);
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/update")
    public Result<Void> updateInfo(@RequestHeader("Authorization") String token, @RequestBody Volunteer volunteer) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(actualToken);
            Long userId = claims.get("userId", Long.class);
            volunteer.setId(userId);
            // 志愿者可以修改自己的用户名和其他个人信息
            // 但不允许修改密码（密码需要通过专门的修改密码接口）
            volunteer.setPassword(null);
            // 不允许修改真实姓名（需要实名认证）
            volunteer.setRealName(null);
            volunteerMapper.update(volunteer);
            return Result.success();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<Volunteer>> getList(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("[VolunteerController] 开始获取志愿者列表");
            // 检查管理员权限
            if (!checkAdminPermission(token)) {
                System.err.println("[VolunteerController] 权限检查失败，拒绝访问");
                return Result.error("无权限访问");
            }
            
            System.out.println("[VolunteerController] 权限检查通过，开始查询数据");
            List<Volunteer> volunteers = volunteerMapper.selectAll();
            volunteers.forEach(volunteer -> volunteer.setPassword(null));
            System.out.println("[VolunteerController] 成功获取 " + volunteers.size() + " 个志愿者");
            return Result.success(volunteers);
        } catch (Exception e) {
            System.err.println("[VolunteerController] 获取志愿者列表异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取志愿者列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Void> addVolunteer(@RequestHeader("Authorization") String token, @RequestBody Volunteer volunteer) {
        try {
            System.out.println("[VolunteerController] 开始添加志愿者");
            // 检查管理员权限
            if (!checkAdminPermission(token)) {
                System.err.println("[VolunteerController] 添加志愿者权限检查失败");
                return Result.error("无权限操作");
            }
            
            System.out.println("[VolunteerController] 添加志愿者权限检查通过");
            volunteerMapper.insert(volunteer);
            return Result.success();
        } catch (Exception e) {
            System.err.println("[VolunteerController] 添加志愿者异常: " + e.getMessage());
            return Result.error("添加志愿者失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteVolunteer(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        try {
            System.out.println("[VolunteerController] 开始删除志愿者 ID: " + id);
            // 检查管理员权限
            if (!checkAdminPermission(token)) {
                System.err.println("[VolunteerController] 删除志愿者权限检查失败");
                return Result.error("无权限操作");
            }
            
            System.out.println("[VolunteerController] 删除志愿者权限检查通过");
            volunteerMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            System.err.println("[VolunteerController] 删除志愿者异常: " + e.getMessage());
            return Result.error("删除志愿者失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestHeader("Authorization") String token, @RequestBody List<Long> ids) {
        try {
            // 检查管理员权限
            if (!checkAdminPermission(token)) {
                return Result.error("无权限操作");
            }
            
            for (Long id : ids) {
                volunteerMapper.deleteById(id);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error("批量删除志愿者失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/admin-update")
    public Result<Void> adminUpdateVolunteer(@RequestHeader("Authorization") String token, @RequestBody Volunteer volunteer) {
        try {
            System.out.println("[VolunteerController] 超级管理员开始修改志愿者信息");
            // 只有超级管理员才能修改志愿者信息
            String actualToken = token.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(actualToken);
            Long userId = claims.get("userId", Long.class);
            String userType = claims.get("userType", String.class);
            
            System.out.println("[VolunteerController] 当前用户 - userId: " + userId + ", userType: " + userType);
            
            // 验证是否为管理员
            if (!"admin".equals(userType)) {
                System.err.println("[VolunteerController] 用户类型不是管理员");
                return Result.error("无权限操作");
            }
            
            Admin admin = adminMapper.selectById(userId);
            if (admin == null) {
                System.err.println("[VolunteerController] 管理员不存在");
                return Result.error("管理员不存在");
            }
            
            System.out.println("[VolunteerController] 管理员角色: " + admin.getRole());
            
            // 只有超级管理员才能修改志愿者信息
            if (!"SUPER_ADMIN".equals(admin.getRole())) {
                System.err.println("[VolunteerController] 不是超级管理员，无权修改");
                return Result.error("只有超级管理员才能修改志愿者信息");
            }
            
            if (volunteer.getId() == null) {
                return Result.error("志愿者ID不能为空");
            }
            
            System.out.println("[VolunteerController] 准备更新志愿者 ID: " + volunteer.getId() + ", 用户名: " + volunteer.getUsername());
            
            // 管理员更新志愿者信息，允许更新所有字段（包括用户名，除了密码需要单独接口）
            volunteer.setPassword(null); // 不允许通过此接口修改密码
            volunteerMapper.update(volunteer);
            
            System.out.println("[VolunteerController] 志愿者信息更新成功");
            return Result.success();
        } catch (JwtException e) {
            System.err.println("[VolunteerController] JWT异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("[VolunteerController] 更新志愿者信息异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("更新志愿者信息失败: " + e.getMessage());
        }
    }
    
    @Autowired
    private ServiceHourReplenishMapper replenishMapper;
    
    @PostMapping("/recalculate-stats")
    public Result<Void> recalculateStats(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(actualToken);
            Long userId = claims.get("userId", Long.class);
            
            // 从签到签退记录和补录记录重新计算统计数据
            BigDecimal hoursFromCheckin = checkinMapper.getTotalHoursByVolunteer(userId);
            Integer pointsFromCheckin = checkinMapper.getTotalPointsByVolunteer(userId);
            BigDecimal hoursFromReplenish = replenishMapper.getTotalHoursByVolunteer(userId);
            Integer pointsFromReplenish = replenishMapper.getTotalPointsByVolunteer(userId);
            
            BigDecimal totalHours = (hoursFromCheckin != null ? hoursFromCheckin : BigDecimal.ZERO)
                    .add(hoursFromReplenish != null ? hoursFromReplenish : BigDecimal.ZERO);
            int totalPoints = (pointsFromCheckin != null ? pointsFromCheckin : 0)
                    + (pointsFromReplenish != null ? pointsFromReplenish : 0);
            
            // 直接同步统计数据（使用新的sync方法，不是累加）
            volunteerMapper.syncHours(userId, totalHours);
            volunteerMapper.syncPoints(userId, totalPoints);
            
            return Result.success();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("重新计算统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 调试接口：检查当前管理员的权限信息
     */
    @GetMapping("/debug-permission")
    public Result<Map<String, Object>> debugPermission(@RequestHeader("Authorization") String token) {
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            
            String actualToken = token.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(actualToken);
            Long userId = claims.get("userId", Long.class);
            String userType = claims.get("userType", String.class);
            
            debugInfo.put("userId", userId);
            debugInfo.put("userType", userType);
            
            Admin admin = adminMapper.selectById(userId);
            if (admin != null) {
                debugInfo.put("adminId", admin.getId());
                debugInfo.put("adminUsername", admin.getUsername());
                debugInfo.put("adminName", admin.getName());
                debugInfo.put("adminRole", admin.getRole());
                debugInfo.put("adminPermissions", admin.getPermissions());
                debugInfo.put("adminStatus", admin.getStatus());
                
                // 检查权限
                boolean isSuperAdmin = "SUPER_ADMIN".equals(admin.getRole());
                debugInfo.put("isSuperAdmin", isSuperAdmin);
                
                if (!isSuperAdmin) {
                    String permissionsStr = admin.getPermissions();
                    boolean hasVolunteersPermission = permissionsStr != null && permissionsStr.contains("\"volunteers\"");
                    debugInfo.put("hasVolunteersPermission", hasVolunteersPermission);
                }
            } else {
                debugInfo.put("error", "管理员不存在");
            }
            
            return Result.success(debugInfo);
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return Result.error("调试失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取志愿者地域分布统计数据
     * 用于管理端首页地图热力图展示
     */
    @GetMapping("/location-stats")
    public Result<List<Map<String, Object>>> getLocationStats() {
        try {
            List<Volunteer> volunteers = volunteerMapper.selectAll();
            
            // 提取地址信息，组合省市区字段
            List<Map<String, Object>> locationData = volunteers.stream()
                .filter(v -> v.getProvince() != null && !v.getProvince().trim().isEmpty())
                .map(v -> {
                    Map<String, Object> map = new HashMap<>();
                    // 组合完整地址：省 + 市 + 区 + 详细地址
                    StringBuilder address = new StringBuilder();
                    if (v.getProvince() != null) address.append(v.getProvince());
                    if (v.getCity() != null) address.append(v.getCity());
                    if (v.getDistrict() != null) address.append(v.getDistrict());
                    if (v.getDetailedAddress() != null) address.append(v.getDetailedAddress());
                    
                    map.put("address", address.toString());
                    return map;
                })
                .filter(map -> !((String)map.get("address")).isEmpty())
                .collect(Collectors.toList());
            
            return Result.success(locationData);
        } catch (Exception e) {
            return Result.error("获取地址统计失败: " + e.getMessage());
        }
    }
}