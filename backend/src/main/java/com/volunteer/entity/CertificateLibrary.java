package com.volunteer.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 志愿服务证书库实体
 */
@Data
public class CertificateLibrary {
    
    private Long id;
    
    /**
     * 证书编号（唯一）
     */
    private String certificateNo;
    
    /**
     * 证书哈希（SHA-256，用于防伪验证）
     */
    private String certificateHash;
    
    /**
     * 志愿者ID
     */
    private Long volunteerId;
    
    /**
     * 志愿者姓名
     */
    private String volunteerName;
    
    /**
     * 钱包地址
     */
    private String walletAddress;
    
    /**
     * 总服务时长（小时）
     */
    private BigDecimal totalHours;
    
    /**
     * 补录时长（小时）
     */
    private BigDecimal replenishHours;
    
    /**
     * 参与活动数量
     */
    private Integer activityCount;
    
    /**
     * 链上总时长
     */
    private BigDecimal chainTotalHours;
    
    /**
     * 链上补录时长
     */
    private BigDecimal chainReplenishHours;
    
    /**
     * 上链交易哈希
     */
    private String txHash;
    
    /**
     * 证书状态：pending待发放，issued已发放，revoked已撤销
     */
    private String status;
    
    /**
     * 发放时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date issueDate;
    
    /**
     * 发放人ID（管理员）
     */
    private Long issuerId;
    
    /**
     * 证书图片URL
     */
    private String certificateImageUrl;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}
