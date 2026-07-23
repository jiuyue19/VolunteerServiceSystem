package com.volunteer.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Volunteer {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String gender;
    private String phone;
    private String email;
    private String idCard;
    private String avatar;
    private String province;
    private String city;
    private String district;
    private String detailedAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal totalHours;
    private Integer points;
    private Integer certificationStatus;
    private String walletAddress;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updateTime;
}