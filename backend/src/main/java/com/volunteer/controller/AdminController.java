package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.Admin;
import com.volunteer.entity.Permission;
import com.volunteer.mapper.AdminMapper;
import com.volunteer.mapper.PermissionMapper;
import com.volunteer.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private AdminMapper adminMapper;
    
    @Autowired
    private PermissionMapper permissionMapper;
    
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/info")
    public Result<Admin> getInfo(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(actualToken);
            Long userId = claims.get("userId", Long.class);
            Admin admin = adminMapper.selectById(userId);
            if (admin != null) {
                admin.setPassword(null);
                
                // 超级管理员自动拥有所有权限
                if ("SUPER_ADMIN".equals(admin.getRole())) {
                    // 获取所有权限并转换为JSON字符串
                    List<Permission> permissions = permissionMapper.selectAll();
                    List<String> permissionKeys = permissions.stream()
                            .map(Permission::getPermissionKey)
                            .collect(Collectors.toList());
                    
                    ObjectMapper mapper = new ObjectMapper();
                    admin.setPermissions(mapper.writeValueAsString(permissionKeys));
                }
                // 普通管理员的权限已经存储在admin.permissions字段中，直接返回即可
                // 如果permissions字段为null或空，前端会认为没有权限
            }
            return Result.success(admin);
        } catch (JwtException e) {
            System.err.println("[AdminController] JWT解析失败: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("[AdminController] 获取管理员信息失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取管理员信息失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Void> updateInfo(@RequestHeader("Authorization") String token, @RequestBody Admin admin) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(actualToken);
            Long userId = claims.get("userId", Long.class);
            
            // 只更新当前登录管理员的信息
            admin.setId(userId);
            // 不允许通过此接口修改密码、姓名、角色和状态
            admin.setPassword(null);
            admin.setName(null);
            admin.setRole(null);
            admin.setStatus(null);
            
            adminMapper.update(admin);
            return Result.success();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("更新管理员信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<Admin>> getList() {
        try {
            List<Admin> admins = adminMapper.selectAll();
            admins.forEach(admin -> admin.setPassword(null));
            return Result.success(admins);
        } catch (Exception e) {
            return Result.error("获取管理员列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Void> addAdmin(@RequestBody Admin admin) {
        try {
            adminMapper.insert(admin);
            return Result.success();
        } catch (Exception e) {
            return Result.error("添加管理员失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteAdmin(@PathVariable Long id) {
        try {
            adminMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除管理员失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                adminMapper.deleteById(id);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error("批量删除管理员失败: " + e.getMessage());
        }
    }

    @PutMapping("/update-by-id")
    public Result<Void> updateById(@RequestHeader("Authorization") String token, @RequestBody Admin admin) {
        try {
            // 只有超级管理员才能修改其他管理员的信息
            String actualToken = token.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(actualToken);
            Long userId = claims.get("userId", Long.class);
            String userType = claims.get("userType", String.class);
            
            // 验证是否为管理员
            if (!"admin".equals(userType)) {
                return Result.error("无权限操作");
            }
            
            Admin currentAdmin = adminMapper.selectById(userId);
            if (currentAdmin == null) {
                return Result.error("管理员不存在");
            }
            
            // 只有超级管理员才能修改其他管理员的信息
            if (!"SUPER_ADMIN".equals(currentAdmin.getRole())) {
                return Result.error("只有超级管理员才能修改管理员信息");
            }
            
            if (admin.getId() == null) {
                return Result.error("管理员ID不能为空");
            }
            
            // 管理员列表页面的编辑功能，允许修改指定ID的管理员信息（包括用户名）
            // 不允许修改密码（密码应该通过专门的修改密码接口）
            admin.setPassword(null);
            adminMapper.update(admin);
            return Result.success();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            return Result.error("更新管理员信息失败: " + e.getMessage());
        }
    }
}