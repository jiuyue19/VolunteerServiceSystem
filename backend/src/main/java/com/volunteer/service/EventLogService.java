package com.volunteer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.volunteer.mapper.ActivityApplicationMapper;
import com.volunteer.mapper.ActivityCheckinMapper;
import com.volunteer.mapper.CertificateLibraryMapper;
import com.volunteer.mapper.ExchangeOrderMapper;
import com.volunteer.mapper.ServiceHourReplenishMapper;
import com.volunteer.entity.ActivityApplication;
import com.volunteer.entity.ActivityCheckin;
import com.volunteer.entity.CertificateLibrary;
import com.volunteer.entity.Activity;
import com.volunteer.entity.EventLog;
import com.volunteer.entity.Volunteer;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.VolunteerMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 事件日志服务
 */
@Service
public class EventLogService {

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
    
    @Autowired
    private VolunteerMapper volunteerMapper;

    /**
     * 获取志愿者的所有事件日志
     */
    public List<Map<String, Object>> getVolunteerEventLogs(Long volunteerId) {
        System.out.println("[EventLogService] 开始获取志愿者事件日志，ID: " + volunteerId);
        List<Map<String, Object>> eventLogs = new ArrayList<>();

        try {
            // 1. 获取活动申请事件
            System.out.println("[EventLogService] 获取活动申请事件...");
            List<ActivityApplication> applications = applicationMapper.selectByVolunteerId(volunteerId);
            System.out.println("[EventLogService] 申请记录数量: " + (applications != null ? applications.size() : 0));
            
            if (applications != null) {
                for (ActivityApplication app : applications) {
                    if (app.getApplyTime() != null) {
                        Activity activity = activityMapper.selectById(app.getActivityId());
                        Map<String, Object> event = new HashMap<>();
                        event.put("type", "activity");
                        event.put("time", app.getApplyTime());
                        event.put("title", "申请参与活动");
                        event.put("description", activity != null ? activity.getTitle() : "活动");
                        
                        Map<String, Object> meta = new HashMap<>();
                        if (app.getStatus() != null) {
                            if ("已通过".equals(app.getStatus())) {
                                meta.put("status", "已通过");
                            } else if ("待审核".equals(app.getStatus())) {
                                meta.put("status", "待审核");
                            } else if ("已拒绝".equals(app.getStatus())) {
                                meta.put("status", "已拒绝");
                            }
                        }
                        event.put("meta", meta);
                        eventLogs.add(event);
                    }
                }
            }

            // 2. 获取签到签退事件
            System.out.println("[EventLogService] 获取签到签退事件...");
            List<ActivityCheckin> checkins = checkinMapper.selectByVolunteerId(volunteerId);
            System.out.println("[EventLogService] 签到记录数量: " + (checkins != null ? checkins.size() : 0));
            
            if (checkins != null) {
                for (ActivityCheckin checkin : checkins) {
                    Activity activity = activityMapper.selectById(checkin.getActivityId());
                    String activityTitle = activity != null ? activity.getTitle() : "活动";
                    
                    // 签到事件
                    if (checkin.getCheckinTime() != null) {
                        Map<String, Object> event = new HashMap<>();
                        event.put("type", "checkin");
                        event.put("time", checkin.getCheckinTime());
                        event.put("title", "活动签到");
                        event.put("description", "签到：" + activityTitle);
                        
                        Map<String, Object> meta = new HashMap<>();
                        meta.put("status", "已签到");
                        event.put("meta", meta);
                        eventLogs.add(event);
                    }
                    
                    // 签退事件
                    if (checkin.getCheckoutTime() != null) {
                        Map<String, Object> event = new HashMap<>();
                        event.put("type", "checkin");
                        event.put("time", checkin.getCheckoutTime());
                        event.put("title", "活动签退");
                        event.put("description", "签退：" + activityTitle);
                        
                        Map<String, Object> meta = new HashMap<>();
                        meta.put("status", "已签退");
                        if (checkin.getServiceHours() != null) {
                            meta.put("hours", checkin.getServiceHours());
                        }
                        if (checkin.getEarnedPoints() != null) {
                            meta.put("points", checkin.getEarnedPoints());
                        }
                        event.put("meta", meta);
                        eventLogs.add(event);
                    }
                }
            }

            // 3. 获取证书事件
            System.out.println("[EventLogService] 获取证书事件...");
            List<CertificateLibrary> certificates = certificateLibraryMapper.selectByVolunteerId(volunteerId);
            System.out.println("[EventLogService] 证书记录数量: " + (certificates != null ? certificates.size() : 0));
            
            if (certificates != null) {
                for (CertificateLibrary cert : certificates) {
                    if (cert.getCreatedAt() != null) {
                        Map<String, Object> event = new HashMap<>();
                        event.put("type", "certificate");
                        // 将 java.util.Date 转换为 LocalDateTime
                        LocalDateTime certTime = cert.getCreatedAt().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDateTime();
                        event.put("time", certTime);
                        event.put("title", "获得志愿服务证书");
                        event.put("description", "证书编号: " + cert.getCertificateNo());
                        
                        Map<String, Object> meta = new HashMap<>();
                        meta.put("status", "已完成");
                        if (cert.getTotalHours() != null) {
                            meta.put("hours", cert.getTotalHours());
                        }
                        event.put("meta", meta);
                        eventLogs.add(event);
                    }
                }
            }

            // 按时间倒序排序
            eventLogs.sort((a, b) -> {
                LocalDateTime timeA = (LocalDateTime) a.get("time");
                LocalDateTime timeB = (LocalDateTime) b.get("time");
                return timeB.compareTo(timeA);
            });
            
            System.out.println("[EventLogService] 总事件数量: " + eventLogs.size());
        } catch (Exception e) {
            System.err.println("[EventLogService] 错误: " + e.getMessage());
            e.printStackTrace();
        }

        return eventLogs;
    }

    /**
     * 获取指定类型的事件日志
     */
    public List<Map<String, Object>> getEventLogsByType(Long volunteerId, String type) {
        List<Map<String, Object>> allLogs = getVolunteerEventLogs(volunteerId);
        return allLogs.stream()
                .filter(log -> type.equals(log.get("type")))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取最近的事件日志（用于数据大屏）
     */
    public List<EventLog> getRecentEvents(Integer limit) {
        List<EventLog> events = new ArrayList<>();
        
        try {
            // 1. 获取最近的签退记录
            List<ActivityCheckin> recentCheckins = checkinMapper.selectAll();
            if (recentCheckins != null) {
                recentCheckins.stream()
                    .filter(c -> c.getCheckoutTime() != null)
                    .sorted((a, b) -> b.getCheckoutTime().compareTo(a.getCheckoutTime()))
                    .limit(limit / 2)
                    .forEach(checkin -> {
                        Activity activity = activityMapper.selectById(checkin.getActivityId());
                        Volunteer volunteer = volunteerMapper.selectById(checkin.getVolunteerId());
                        
                        EventLog event = new EventLog();
                        event.setEventType("签退");
                        event.setActivityName(activity != null ? activity.getTitle() : "活动");
                        event.setVolunteerName(volunteer != null ? volunteer.getRealName() : "志愿者");
                        event.setCreateTime(checkin.getCheckoutTime());
                        event.setDescription("完成志愿服务，获得 " + 
                            (checkin.getServiceHours() != null ? checkin.getServiceHours() : 0) + " 小时");
                        event.setBlockchainHash(null);
                        
                        events.add(event);
                    });
            }
            
            // 2. 获取最近的证书记录
            List<CertificateLibrary> recentCerts = certificateLibraryMapper.selectAll(null);
            if (recentCerts != null) {
                recentCerts.stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .limit(limit / 3)
                    .forEach(cert -> {
                        EventLog event = new EventLog();
                        event.setEventType("证书");
                        event.setActivityName("志愿服务证书");
                        event.setVolunteerName(cert.getVolunteerName());
                        // 将 java.util.Date 转换为 LocalDateTime
                        LocalDateTime createTime = cert.getCreatedAt() != null 
                            ? cert.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                            : LocalDateTime.now();
                        event.setCreateTime(createTime);
                        event.setDescription("获得志愿服务证书");
                        event.setBlockchainHash(cert.getCertificateHash());
                        
                        events.add(event);
                    });
            }
            
            // 3. 获取最近的补录记录
            List<com.volunteer.entity.ServiceHourReplenish> recentReplenish = replenishMapper.selectAll();
            if (recentReplenish != null) {
                recentReplenish.stream()
                    .filter(r -> "已通过".equals(r.getStatus()))
                    .filter(r -> r.getCreatedAt() != null)
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .limit(limit / 3)
                    .forEach(replenish -> {
                        Volunteer volunteer = volunteerMapper.selectById(replenish.getVolunteerId());
                        
                        EventLog event = new EventLog();
                        event.setEventType("补录");
                        event.setActivityName(replenish.getActivityName());
                        event.setVolunteerName(volunteer != null ? volunteer.getRealName() : "志愿者");
                        event.setCreateTime(replenish.getCreatedAt());
                        event.setDescription("补录志愿时长 " + 
                            (replenish.getHours() != null ? replenish.getHours() : BigDecimal.ZERO) + " 小时");
                        event.setBlockchainHash(null);
                        
                        events.add(event);
                    });
            }
            
            // 按时间倒序排序并限制数量
            events.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
            
        } catch (Exception e) {
            System.err.println("[EventLogService] 获取最近事件失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 限制返回数量
        if (events.size() > limit) {
            return events.subList(0, limit);
        }
        return events;
    }
}
