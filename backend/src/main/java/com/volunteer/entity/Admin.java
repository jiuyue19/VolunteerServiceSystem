package com.volunteer.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Admin {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String phone;
    private String email;
    private String avatar;
    private String role;
    private Integer status;
    private String permissions; // 权限列表(JSON格式)
    private LocalDateTime createdAt;
    private LocalDateTime updateTime;
}