package com.volunteer.entity;

import java.time.LocalDateTime;

/**
 * 事件日志实体类（用于数据传输）
 */
public class EventLog {
    private String eventType;
    private String activityName;
    private String volunteerName;
    private LocalDateTime createTime;
    private String description;
    private String blockchainHash;

    public EventLog() {
    }

    public EventLog(String eventType, String activityName, String volunteerName, 
                    LocalDateTime createTime, String description, String blockchainHash) {
        this.eventType = eventType;
        this.activityName = activityName;
        this.volunteerName = volunteerName;
        this.createTime = createTime;
        this.description = description;
        this.blockchainHash = blockchainHash;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getVolunteerName() {
        return volunteerName;
    }

    public void setVolunteerName(String volunteerName) {
        this.volunteerName = volunteerName;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBlockchainHash() {
        return blockchainHash;
    }

    public void setBlockchainHash(String blockchainHash) {
        this.blockchainHash = blockchainHash;
    }
}
