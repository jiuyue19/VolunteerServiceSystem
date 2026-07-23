package com.volunteer.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Activity {
    private Long id;
    private Long categoryId;
    private String title;
    private String content;
    private String address;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer targetNumber;
    private Integer currentNumber;
    private BigDecimal serviceHours;
    private Integer rewardPoints;
    private String status;
    private String coverImage;
    private String venueImage; // JSON格式存储多张场所图片，如：["url1", "url2", "url3"]
    private LocalDateTime createdAt;
    private LocalDateTime updateTime;
    
    // 详情页新增字段
    private String detailedAddress;
    private String latitude;
    private String longitude;
    private String volunteerField;
    private String volunteerTarget;
    private String serviceLocation;
    private String organizationName;
    private String organizationImage;
    private String contactPerson;
    private String contactPhone;
}