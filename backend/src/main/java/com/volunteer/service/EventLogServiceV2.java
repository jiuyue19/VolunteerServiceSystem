package com.volunteer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.volunteer.mapper.*;
import com.volunteer.entity.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 事件日志服务 - 优化版
 * 支持积分、时长、活动、证书等多维度事件记录
 */
@Service("eventLogServiceV2")
public class EventLogServiceV2 {

    @Autowired
    private ActivityApplicationMapper applicationMapper;

    @Autowired
    private ActivityCheckinMapper checkinMapper;

    @Autowired
    private CertificateLibraryMapper certificateLibraryMapper;

    @Autowired
    private ActivityMapper activityMapper;
    
    @Autowired
    private ExchangeOrderMapper exchangeOrderMapper;
    
    @Autowired
    private ServiceHourReplenishMapper replenishMapper;

    /**
     * 获取志愿者的所有事件日志
     */
    public List<Map<String, Object>> getVolunteerEventLogs(Long volunteerId) {
        System.out.println("[EventLogServiceV2] 开始获取志愿者事件日志，ID: " + volunteerId);
        List<Map<String, Object>> eventLogs = new ArrayList<>();

        try {
            // 1. 活动签退事件（积分+时长）
            addCheckinEvents(volunteerId, eventLogs);
            
            // 2. 商品兑换事件（积分扣减）
            addExchangeEvents(volunteerId, eventLogs);
            
            // 3. 补录时长事件（时长增加）
            addReplenishEvents(volunteerId, eventLogs);
            
            // 4. 活动申请事件
            addApplicationEvents(volunteerId, eventLogs);
            
            // 5. 证书获取事件
            addCertificateEvents(volunteerId, eventLogs);

            // 按时间倒序排序（支持Date和LocalDateTime两种类型）
            eventLogs.sort((a, b) -> {
                Object timeObjA = a.get("time");
                Object timeObjB = b.get("time");
                
                LocalDateTime timeA = convertToLocalDateTime(timeObjA);
                LocalDateTime timeB = convertToLocalDateTime(timeObjB);
                
                if (timeA == null && timeB == null) return 0;
                if (timeA == null) return 1;
                if (timeB == null) return -1;
                
                return timeB.compareTo(timeA);
            });
            
            System.out.println("[EventLogServiceV2] 总事件数量: " + eventLogs.size());
        } catch (Exception e) {
            System.err.println("[EventLogServiceV2] 错误: " + e.getMessage());
            e.printStackTrace();
        }

        return eventLogs;
    }
    
    /**
     * 添加签到签退事件
     */
    private void addCheckinEvents(Long volunteerId, List<Map<String, Object>> eventLogs) {
        List<ActivityCheckin> checkins = checkinMapper.selectByVolunteerId(volunteerId);
        System.out.println("[EventLogServiceV2] 签到记录数量: " + (checkins != null ? checkins.size() : 0));
        
        if (checkins != null) {
            for (ActivityCheckin checkin : checkins) {
                Activity activity = activityMapper.selectById(checkin.getActivityId());
                String activityTitle = activity != null ? activity.getTitle() : "志愿活动";
                
                // 签退事件（同时记录积分和时长）
                if (checkin.getCheckoutTime() != null) {
                    Map<String, Object> event = new HashMap<>();
                    event.put("category", "points");  // 积分类别
                    event.put("time", checkin.getCheckoutTime());
                    event.put("title", "活动签退获得积分");
                    event.put("description", "完成活动：" + activityTitle);
                    event.put("pointsChange", checkin.getEarnedPoints() != null ? checkin.getEarnedPoints() : 0);
                    event.put("status", "已完成");
                    eventLogs.add(event);
                    
                    // 同时添加时长记录
                    Map<String, Object> hoursEvent = new HashMap<>();
                    hoursEvent.put("category", "hours");
                    hoursEvent.put("time", checkin.getCheckoutTime());
                    hoursEvent.put("title", "活动签退累计时长");
                    hoursEvent.put("description", "完成活动：" + activityTitle);
                    hoursEvent.put("hoursChange", checkin.getServiceHours() != null ? checkin.getServiceHours().doubleValue() : 0.0);
                    hoursEvent.put("status", "已完成");
                    eventLogs.add(hoursEvent);
                }
            }
        }
    }
    
    /**
     * 添加商品兑换事件（积分扣减和退还）
     */
    private void addExchangeEvents(Long volunteerId, List<Map<String, Object>> eventLogs) {
        List<ExchangeOrder> orders = exchangeOrderMapper.selectByVolunteerId(volunteerId);
        System.out.println("[EventLogServiceV2] 兑换订单数量: " + (orders != null ? orders.size() : 0));
        
        if (orders != null) {
            int exchangeCount = 0;
            int refundCount = 0;
            
            for (ExchangeOrder order : orders) {
                if (order.getTotalPoints() != null) {
                    // 情况1: 退款成功 - 积分退还（正数）
                    if ("已通过".equals(order.getRefundStatus()) && order.getRefundAuditTime() != null) {
                        Map<String, Object> refundEvent = new HashMap<>();
                        refundEvent.put("category", "points");
                        refundEvent.put("time", order.getRefundAuditTime());
                        refundEvent.put("title", "退款退还积分");
                        refundEvent.put("description", "退款商品：" + (order.getGoodsName() != null ? order.getGoodsName() : "商品"));
                        refundEvent.put("pointsChange", order.getTotalPoints()); // 正数表示退还
                        refundEvent.put("status", "退款成功");
                        refundEvent.put("isRefund", true); // 退款标识
                        eventLogs.add(refundEvent);
                        refundCount++;
                    }
                    
                    // 情况2: 正常兑换 - 积分扣减（负数）
                    // 只记录成功兑换且未取消、未退款的订单
                    if (order.getCreatedAt() != null) {
                        // 判断订单是否已取消或已退款
                        boolean isCancelled = "已取消".equals(order.getStatus());
                        boolean isRefunded = "已通过".equals(order.getRefundStatus());
                        
                        // 只有非取消且非退款的订单才扣减积分
                        if (!isCancelled && !isRefunded) {
                            Map<String, Object> exchangeEvent = new HashMap<>();
                            exchangeEvent.put("category", "points");
                            exchangeEvent.put("time", order.getCreatedAt());
                            exchangeEvent.put("title", "兑换商品");
                            exchangeEvent.put("description", "兑换商品：" + (order.getGoodsName() != null ? order.getGoodsName() : "商品"));
                            exchangeEvent.put("pointsChange", -order.getTotalPoints()); // 负数表示扣减
                            exchangeEvent.put("status", order.getStatus() != null ? order.getStatus() : "已兑换");
                            exchangeEvent.put("isExchange", true); // 兑换标识
                            eventLogs.add(exchangeEvent);
                            exchangeCount++;
                        }
                    }
                }
            }
            
            System.out.println("[EventLogServiceV2] 兑换记录数量: " + exchangeCount + ", 退款记录数量: " + refundCount);
        }
    }
    
    /**
     * 添加补录时长事件
     */
    private void addReplenishEvents(Long volunteerId, List<Map<String, Object>> eventLogs) {
        List<ServiceHourReplenish> replenishes = replenishMapper.selectByVolunteerId(volunteerId);
        System.out.println("[EventLogServiceV2] 补录记录数量: " + (replenishes != null ? replenishes.size() : 0));
        
        if (replenishes != null) {
            int approvedCount = 0;
            for (ServiceHourReplenish replenish : replenishes) {
                // 只记录已通过的补录申请
                if ("已通过".equals(replenish.getStatus())) {
                    // 补录积分事件
                    if (replenish.getEarnedPoints() != null && replenish.getEarnedPoints() > 0) {
                        Map<String, Object> pointsEvent = new HashMap<>();
                        pointsEvent.put("category", "points");
                        pointsEvent.put("time", replenish.getAuditTime() != null ? replenish.getAuditTime() : replenish.getCreatedAt());
                        pointsEvent.put("title", "补录获得积分");
                        pointsEvent.put("description", "活动：" + replenish.getActivityName() + " (补录)");
                        pointsEvent.put("pointsChange", replenish.getEarnedPoints());
                        pointsEvent.put("status", "已通过");
                        pointsEvent.put("isReplenish", true); // 补录标识
                        eventLogs.add(pointsEvent);
                    }
                    
                    // 补录时长事件
                    if (replenish.getHours() != null && replenish.getHours().doubleValue() > 0) {
                        Map<String, Object> hoursEvent = new HashMap<>();
                        hoursEvent.put("category", "hours");
                        hoursEvent.put("time", replenish.getAuditTime() != null ? replenish.getAuditTime() : replenish.getCreatedAt());
                        hoursEvent.put("title", "补录服务时长");
                        hoursEvent.put("description", "活动：" + replenish.getActivityName() + " (补录)");
                        hoursEvent.put("hoursChange", replenish.getHours().doubleValue());
                        hoursEvent.put("status", "已通过");
                        hoursEvent.put("isReplenish", true); // 补录标识
                        eventLogs.add(hoursEvent);
                    }
                    
                    // 补录活动参与事件
                    Map<String, Object> activityEvent = new HashMap<>();
                    activityEvent.put("category", "activity");
                    activityEvent.put("time", replenish.getAuditTime() != null ? replenish.getAuditTime() : replenish.getCreatedAt());
                    activityEvent.put("title", "补录活动参与");
                    activityEvent.put("description", "活动：" + replenish.getActivityName() + " (补录)");
                    activityEvent.put("status", "已通过");
                    activityEvent.put("isReplenish", true); // 补录标识
                    eventLogs.add(activityEvent);
                    
                    approvedCount++;
                }
            }
            System.out.println("[EventLogServiceV2] 已通过补录数量: " + approvedCount);
        }
    }
    
    /**
     * 添加活动申请事件（仅已签退的活动）
     */
    private void addApplicationEvents(Long volunteerId, List<Map<String, Object>> eventLogs) {
        List<ActivityApplication> applications = applicationMapper.selectByVolunteerId(volunteerId);
        System.out.println("[EventLogServiceV2] 申请记录数量: " + (applications != null ? applications.size() : 0));
        
        if (applications != null) {
            int participatedCount = 0;
            for (ActivityApplication app : applications) {
                if (app.getApplyTime() != null) {
                    // 检查该活动是否已签退
                    ActivityCheckin checkin = checkinMapper.selectByActivityAndVolunteer(app.getActivityId(), volunteerId);
                    
                    // 只添加已签退的活动
                    if (checkin != null && checkin.getCheckoutTime() != null) {
                        Activity activity = activityMapper.selectById(app.getActivityId());
                        Map<String, Object> event = new HashMap<>();
                        event.put("category", "activity");
                        event.put("time", checkin.getCheckoutTime()); // 使用签退时间而不是申请时间
                        event.put("title", "完成志愿活动");
                        event.put("description", activity != null ? activity.getTitle() : "志愿活动");
                        event.put("status", "已完成");
                        eventLogs.add(event);
                        participatedCount++;
                    }
                }
            }
            System.out.println("[EventLogServiceV2] 已完成活动数量: " + participatedCount);
        }
    }
    
    /**
     * 添加证书获取事件
     * 业务逻辑：只记录已发放的证书（status = "issued"）
     * 不记录待发放（pending）和已撤销（revoked）的证书
     */
    private void addCertificateEvents(Long volunteerId, List<Map<String, Object>> eventLogs) {
        List<CertificateLibrary> certificates = certificateLibraryMapper.selectByVolunteerId(volunteerId);
        System.out.println("[EventLogServiceV2] 证书记录数量: " + (certificates != null ? certificates.size() : 0));
        
        if (certificates != null) {
            int issuedCount = 0;
            int skippedCount = 0;
            
            for (CertificateLibrary cert : certificates) {
                // 只记录已发放的证书
                if ("issued".equals(cert.getStatus())) {
                    // 使用issueDate作为事件时间（证书发放时间）
                    if (cert.getIssueDate() != null) {
                        Map<String, Object> event = new HashMap<>();
                        event.put("category", "certificate");
                        event.put("time", cert.getIssueDate());  // 使用发放时间而不是创建时间
                        event.put("title", "获得志愿服务证书");
                        event.put("description", "证书编号: " + cert.getCertificateNo());
                        event.put("hoursChange", cert.getTotalHours() != null ? cert.getTotalHours().doubleValue() : 0.0);
                        event.put("status", "已发放");
                        eventLogs.add(event);
                        issuedCount++;
                        System.out.println("[EventLogServiceV2] 已发放证书: " + cert.getCertificateNo());
                    }
                } else {
                    skippedCount++;
                    System.out.println("[EventLogServiceV2] 跳过证书（状态: " + cert.getStatus() + "）: " + cert.getCertificateNo());
                }
            }
            
            System.out.println("[EventLogServiceV2] 证书事件统计 - 已发放: " + issuedCount + ", 跳过: " + skippedCount);
        }
    }

    /**
     * 转换时间对象为LocalDateTime
     * 支持Date和LocalDateTime两种类型
     */
    private LocalDateTime convertToLocalDateTime(Object timeObj) {
        if (timeObj == null) {
            return null;
        }
        
        if (timeObj instanceof LocalDateTime) {
            return (LocalDateTime) timeObj;
        } else if (timeObj instanceof Date) {
            return ((Date) timeObj).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        } else {
            System.err.println("[EventLogServiceV2] 未知的时间类型: " + timeObj.getClass().getName());
            return null;
        }
    }

    /**
     * 获取指定类别的事件日志
     */
    public List<Map<String, Object>> getEventLogsByCategory(Long volunteerId, String category) {
        List<Map<String, Object>> allLogs = getVolunteerEventLogs(volunteerId);
        if (category == null || category.isEmpty()) {
            return allLogs;
        }
        
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> log : allLogs) {
            if (category.equals(log.get("category"))) {
                filtered.add(log);
            }
        }
        return filtered;
    }
}
