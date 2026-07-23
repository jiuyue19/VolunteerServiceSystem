package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.Admin;
import com.volunteer.entity.Permission;
import com.volunteer.mapper.AdminMapper;
import com.volunteer.mapper.PermissionMapper;
import com.volunteer.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permission")
@CrossOrigin
public class PermissionController {

    @Autowired
    private PermissionMapper permissionMapper;
    
    @Autowired
    private AdminMapper adminMapper;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取所有权限列表（仅超级管理员可用）
     */
    @GetMapping("/list")
    public Result<List<Permission>> getAllPermissions(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(actualToken);
            Long userId = claims.get("userId", Long.class);
            Admin admin = adminMapper.selectById(userId);
            
            // 验证是否为超级管理员
            if (admin == null || !"SUPER_ADMIN".equals(admin.getRole())) {
                return Result.error("无权限访问");
            }
            
            List<Permission> permissions = permissionMapper.selectAll();
            return Result.success(permissions);
        } catch (Exception e) {
            return Result.error("获取权限列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录管理员的权限列表
     */
    @GetMapping("/my-permissions")
    public Result<List<Permission>> getMyPermissions(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(actualToken);
            Long userId = claims.get("userId", Long.class);
            Admin admin = adminMapper.selectById(userId);
            
            if (admin == null) {
                return Result.error("管理员不存在");
            }
            
            // 超级管理员返回所有权限
            List<Permission> permissions;
            if ("SUPER_ADMIN".equals(admin.getRole())) {
                permissions = permissionMapper.selectAll();
            } else {
                permissions = permissionMapper.selectByAdminId(userId);
            }
            
            return Result.success(permissions);
        } catch (Exception e) {
            return Result.error("获取权限列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定管理员的权限列表（仅超级管理员可用）
     */
    @GetMapping("/admin/{adminId}")
    public Result<Map<String, Object>> getAdminPermissions(
            @RequestHeader("Authorization") String token,
            @PathVariable Long adminId) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(actualToken);
            Long userId = claims.get("userId", Long.class);
            Admin currentAdmin = adminMapper.selectById(userId);
            
            // 验证是否为超级管理员
            if (currentAdmin == null || !"SUPER_ADMIN".equals(currentAdmin.getRole())) {
                return Result.error("无权限访问");
            }
            
            Admin targetAdmin = adminMapper.selectById(adminId);
            if (targetAdmin == null) {
                return Result.error("管理员不存在");
            }
            
            // 超级管理员拥有所有权限
            List<Permission> permissions;
            if ("SUPER_ADMIN".equals(targetAdmin.getRole())) {
                permissions = permissionMapper.selectAll();
            } else {
                permissions = permissionMapper.selectByAdminId(adminId);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("admin", targetAdmin);
            result.put("permissions", permissions);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取管理员权限失败: " + e.getMessage());
        }
    }

    /**
     * 为管理员分配权限（仅超级管理员可用）
     */
    @PostMapping("/assign")
    public Result<Void> assignPermissions(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> params) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(actualToken);
            Long userId = claims.get("userId", Long.class);
            Admin currentAdmin = adminMapper.selectById(userId);
            
            // 验证是否为超级管理员
            if (currentAdmin == null || !"SUPER_ADMIN".equals(currentAdmin.getRole())) {
                return Result.error("无权限操作");
            }
            
            Long adminId = Long.parseLong(params.get("adminId").toString());
            @SuppressWarnings("unchecked")
            List<Long> permissionIds = (List<Long>) params.get("permissionIds");
            
            Admin targetAdmin = adminMapper.selectById(adminId);
            if (targetAdmin == null) {
                return Result.error("管理员不存在");
            }
            
            // 不能修改超级管理员的权限
            if ("SUPER_ADMIN".equals(targetAdmin.getRole())) {
                return Result.error("不能修改超级管理员的权限");
            }
            
            // 删除原有权限
            permissionMapper.deleteAdminPermissions(adminId);
            
            // 分配新权限
            if (permissionIds != null && !permissionIds.isEmpty()) {
                permissionMapper.batchInsertAdminPermissions(adminId, permissionIds);
            }
            
            return Result.success();
        } catch (Exception e) {
            return Result.error("分配权限失败: " + e.getMessage());
        }
    }

    /**
     * 检查当前管理员是否拥有指定权限
     */
    @GetMapping("/check/{permissionKey}")
    public Result<Boolean> checkPermission(
            @RequestHeader("Authorization") String token,
            @PathVariable String permissionKey) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(actualToken);
            Long userId = claims.get("userId", Long.class);
            Admin admin = adminMapper.selectById(userId);
            
            if (admin == null) {
                return Result.success(false);
            }
            
            // 超级管理员拥有所有权限
            if ("SUPER_ADMIN".equals(admin.getRole())) {
                return Result.success(true);
            }
            
            // 检查普通管理员权限
            List<Permission> permissions = permissionMapper.selectByAdminId(userId);
            boolean hasPermission = permissions.stream()
                    .anyMatch(p -> permissionKey.equals(p.getPermissionKey()));
            
            return Result.success(hasPermission);
        } catch (Exception e) {
            return Result.error("检查权限失败: " + e.getMessage());
        }
    }
}
