package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.Volunteer;
import com.volunteer.service.AuthService;
import com.volunteer.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/admin/login")
    public Result<Map<String, String>> adminLogin(@RequestBody Map<String, String> request) {
        try {
            String token = authService.adminLogin(request.get("username"), request.get("password"));
            // 基于 access token 解析用户信息，签发 refresh token
            io.jsonwebtoken.Claims claims = jwtUtil.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            String refreshToken = jwtUtil.generateRefreshToken(userId, username, role, "admin");
            Map<String, String> data = new HashMap<>();
            data.put("token", token);
            data.put("refreshToken", refreshToken);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/volunteer/login")
    public Result<Map<String, String>> volunteerLogin(@RequestBody Map<String, String> request) {
        try {
            String token = authService.volunteerLogin(request.get("username"), request.get("password"));
            io.jsonwebtoken.Claims claims = jwtUtil.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            String refreshToken = jwtUtil.generateRefreshToken(userId, username, role, "volunteer");
            Map<String, String> data = new HashMap<>();
            data.put("token", token);
            data.put("refreshToken", refreshToken);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/volunteer/register")
    public Result<Void> volunteerRegister(@RequestBody Volunteer volunteer) {
        try {
            authService.volunteerRegister(volunteer);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/refresh")
    public Result<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            io.jsonwebtoken.Claims claims = jwtUtil.parseToken(refreshToken);
            String tokenType = claims.get("tokenType", String.class);
            if (!"REFRESH".equals(tokenType)) {
                throw new io.jsonwebtoken.JwtException("令牌类型错误");
            }
            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            String userType = claims.get("userType", String.class);
            String newAccessToken = jwtUtil.generateAccessToken(userId, username, role, userType);
            Map<String, String> data = new HashMap<>();
            data.put("token", newAccessToken);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/volunteer/send-reset-code")
    public Result<Map<String, String>> sendVolunteerResetCode(@RequestBody Map<String, String> request) {
        try {
            String type = request.get("type");
            String phone = request.get("phone");
            String email = request.get("email");
            String code = authService.sendVolunteerResetCode(type, phone, email);
            Map<String, String> data = new HashMap<>();
            // 提示：当前接口会返回验证码仅用于开发调试，生产环境请移除
            data.put("code", code);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/volunteer/reset-password")
    public Result<Void> resetVolunteerPassword(@RequestBody Map<String, String> request) {
        try {
            String type = request.get("type");
            String phone = request.get("phone");
            String email = request.get("email");
            String code = request.get("code");
            String newPassword = request.get("newPassword");
            authService.resetVolunteerPasswordByCode(type, phone, email, code, newPassword);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/volunteer/change-password")
    public Result<Void> changeVolunteerPassword(@RequestHeader("Authorization") String token,
                                                @RequestBody Map<String, String> request) {
        try {
            String actualToken = token.replace("Bearer ", "");
            io.jsonwebtoken.Claims claims = jwtUtil.parseToken(actualToken);
            Long userId = claims.get("userId", Long.class);
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            authService.changeVolunteerPassword(userId, currentPassword, newPassword);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}