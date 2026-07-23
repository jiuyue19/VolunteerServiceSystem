package com.volunteer.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Goods {
    private Long id;
    private String name;
    private String description;
    private Integer points; 
    private Integer stock;
    private String image;
    private Integer status; 
    private String material; // 材质
    private String color; // 颜色
    private String targetGroup; // 适用人群
    private String season; // 适用季节
    private String features; // 其他特性（多个用逗号分隔）
    private LocalDateTime createdAt;
}