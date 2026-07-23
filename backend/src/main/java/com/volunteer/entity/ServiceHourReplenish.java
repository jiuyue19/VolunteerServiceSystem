package com.volunteer.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ServiceHourReplenish {
    private Long id;
    private Long volunteerId;
    private String activityName;
    private BigDecimal hours;
    private Integer earnedPoints;
    private String reason;
    private String evidenceFiles;
    private String status;
    private String rejectReason;
    private Long adminId;
    private LocalDateTime applyTime;
    private LocalDateTime auditTime;
    private LocalDateTime createdAt;
    private LocalDateTime updateTime;
}