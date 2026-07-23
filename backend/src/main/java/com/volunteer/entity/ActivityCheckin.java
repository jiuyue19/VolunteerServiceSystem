package com.volunteer.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivityCheckin {
    private Long id;
    private Long activityId;
    private Long volunteerId;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private BigDecimal serviceHours;
    private Integer earnedPoints;
    private Integer status;
    private Integer isValid;
    private Integer isCounted;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updateTime;
}