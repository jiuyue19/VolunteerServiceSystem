package com.volunteer.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExchangeOrder {
    private Long id;
    private Long goodsId;
    private Long volunteerId;
    private Integer quantity;
    private Integer totalPoints;
    private String address;
    private Long addressId;
    private String contactName;
    private String phone;
    private String orderNumber;
    private String trackingNumber;
    private String status;
    private LocalDateTime createdAt;
    private String goodsName;
    private String goodsDescription;
    private String goodsImage;
    private String volunteerName;
    // 退款相关字段
    private String refundStatus;
    private String refundReason;
    private String refundEvidence;
    private LocalDateTime refundApplyTime;
    private LocalDateTime refundAuditTime;
    private Long refundAdminId;
}