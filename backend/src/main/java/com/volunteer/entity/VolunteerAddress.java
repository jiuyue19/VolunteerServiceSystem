package com.volunteer.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VolunteerAddress {
    private Long id;
    private Long volunteerId;
    private String contactName;
    private String contactPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String label;
    private Integer isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updateTime;
}
