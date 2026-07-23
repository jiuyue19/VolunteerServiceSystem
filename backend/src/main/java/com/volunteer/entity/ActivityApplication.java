package com.volunteer.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityApplication {
    private Long id;
    private Long activityId;
    private Long volunteerId;
    private String status;
    private String reason;
    private String rejectReason;
    private LocalDateTime applyTime;
    private LocalDateTime auditTime;
    private String activityName;
    private String volunteerName;
    private String activityAddress;
    private LocalDateTime activityStartTime;
    private LocalDateTime activityEndTime;
}