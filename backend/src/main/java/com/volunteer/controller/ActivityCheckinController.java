package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.Activity;
import com.volunteer.entity.ActivityCheckin;
import com.volunteer.entity.Volunteer;
import com.volunteer.mapper.ActivityCheckinMapper;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.service.PointsCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/checkin")
@CrossOrigin
public class ActivityCheckinController {

    @Autowired
    private ActivityCheckinMapper checkinMapper;
    
    @Autowired
    private ActivityMapper activityMapper;
    
    @Autowired
    private VolunteerMapper volunteerMapper;
    
    @Autowired
    private PointsCalculationService pointsCalculationService;
    
    // 存储活动验证码的内存缓存
    private static final Map<Long, String> activityCodes = new ConcurrentHashMap<>();

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getList() {
        try {
            return Result.success(checkinMapper.selectCheckoutRecords());
        } catch (Exception e) {
            return Result.error("获取签退记录列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody ActivityCheckin checkin) {
        try {
            checkinMapper.insert(checkin);
            return Result.success();
        } catch (Exception e) {
            return Result.error("添加打卡记录失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody ActivityCheckin checkin) {
        try {
            checkinMapper.update(checkin);
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新打卡记录失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            checkinMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除打卡记录失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                checkinMapper.deleteById(id);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error("批量删除打卡记录失败: " + e.getMessage());
        }
    }
    
    // 生成活动验证码（管理员使用）
    @PostMapping("/generate-code/{activityId}")
    public Result<Map<String, String>> generateCode(@PathVariable Long activityId) {
        try {
            String code = generateRandomCode();
            activityCodes.put(activityId, code);
            
            Map<String, String> result = new HashMap<>();
            result.put("code", code);
            result.put("activityId", activityId.toString());
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("生成验证码失败: " + e.getMessage());
        }
    }
    
    // 志愿者签到
    @PostMapping("/checkin")
    public Result<Void> checkin(@RequestBody Map<String, Object> request) {
        try {
            Long activityId = Long.valueOf(request.get("activityId").toString());
            Long volunteerId = Long.valueOf(request.get("volunteerId").toString());
            String inputCode = request.get("code").toString();
            
            // 验证验证码
            String correctCode = activityCodes.get(activityId);
            if (correctCode == null || !correctCode.equals(inputCode)) {
                return Result.error("验证码错误或已过期");
            }
            
            // 获取活动信息
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                return Result.error("活动不存在");
            }
            
            // 获取志愿者信息（使用志愿者登记的经纬度）
            Volunteer volunteer = volunteerMapper.selectById(volunteerId);
            if (volunteer == null) {
                return Result.error("志愿者不存在");
            }
            
            // 验证志愿者是否登记了经纬度
            if (volunteer.getLatitude() == null || volunteer.getLongitude() == null) {
                return Result.error("您尚未登记地理位置信息，请联系管理员添加");
            }
            
            // 验证地理位置（使用志愿者登记的经纬度，要求在1公里范围内）
            Double volunteerLat = volunteer.getLatitude().doubleValue();
            Double volunteerLng = volunteer.getLongitude().doubleValue();
            if (!isLocationValid(volunteerLat, volunteerLng, activity)) {
                return Result.error("您登记的地理位置不在活动地点1公里范围内，无法签到");
            }
            
            // 检查是否已经签到
            ActivityCheckin existing = checkinMapper.selectByActivityAndVolunteer(activityId, volunteerId);
            if (existing != null && existing.getCheckinTime() != null) {
                return Result.error("您已经签到过了");
            }
            
            // 创建签到记录
            ActivityCheckin checkin = new ActivityCheckin();
            checkin.setActivityId(activityId);
            checkin.setVolunteerId(volunteerId);
            checkin.setCheckinTime(LocalDateTime.now());
            checkin.setStatus(1); // 1-已签到
            checkin.setIsValid(1);
            checkin.setIsCounted(0);
            checkin.setCreatedAt(LocalDateTime.now());
            
            if (existing != null) {
                checkin.setId(existing.getId());
                checkinMapper.update(checkin);
            } else {
                checkinMapper.insert(checkin);
            }
            
            return Result.success();
        } catch (Exception e) {
            return Result.error("签到失败: " + e.getMessage());
        }
    }
    
    // 志愿者签退
    @PostMapping("/checkout")
    @Transactional
    public Result<Map<String, Object>> checkout(@RequestBody Map<String, Object> request) {
        try {
            Long activityId = Long.valueOf(request.get("activityId").toString());
            Long volunteerId = Long.valueOf(request.get("volunteerId").toString());
            
            // 查找签到记录
            ActivityCheckin checkin = checkinMapper.selectByActivityAndVolunteer(activityId, volunteerId);
            if (checkin == null || checkin.getCheckinTime() == null) {
                return Result.error("请先签到");
            }
            
            if (checkin.getCheckoutTime() != null) {
                return Result.error("您已经签退过了");
            }
            
            // 计算服务时长
            LocalDateTime checkinTime = checkin.getCheckinTime();
            LocalDateTime checkoutTime = LocalDateTime.now();
            long minutes = ChronoUnit.MINUTES.between(checkinTime, checkoutTime);
            BigDecimal serviceHours = BigDecimal.valueOf(minutes / 60.0).setScale(1, BigDecimal.ROUND_HALF_UP);
            
            // 获取活动信息计算积分
            Activity activity = activityMapper.selectById(activityId);
            int earnedPoints = serviceHours.multiply(BigDecimal.valueOf(10)).intValue(); // 1小时=10积分
            
            // 更新签退信息
            checkin.setCheckoutTime(checkoutTime);
            checkin.setServiceHours(serviceHours);
            checkin.setEarnedPoints(earnedPoints);
            checkin.setStatus(2); // 2-已签退
            checkin.setIsCounted(1);
            checkin.setUpdateTime(LocalDateTime.now());
            
            checkinMapper.update(checkin);
            
            // 更新志愿者总时长和积分 - 立即同步到数据库
            syncVolunteerStats(volunteerId);
            
            // 返回详细的签退信息
            Map<String, Object> result = new HashMap<>();
            result.put("serviceHours", serviceHours);
            result.put("earnedPoints", earnedPoints);
            result.put("checkinTime", checkin.getCheckinTime());
            result.put("checkoutTime", checkoutTime);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("签退失败: " + e.getMessage());
        }
    }
    
    // 获取志愿者的签到状态
    @GetMapping("/status/{activityId}/{volunteerId}")
    public Result<Map<String, Object>> getCheckinStatus(@PathVariable Long activityId, @PathVariable Long volunteerId) {
        try {
            ActivityCheckin checkin = checkinMapper.selectByActivityAndVolunteer(activityId, volunteerId);
            
            Map<String, Object> result = new HashMap<>();
            if (checkin == null) {
                result.put("status", "not_checkin");
                result.put("canCheckin", true);
            } else if (checkin.getCheckinTime() != null && checkin.getCheckoutTime() == null) {
                result.put("status", "checkin");
                result.put("canCheckout", true);
                result.put("checkinTime", checkin.getCheckinTime());
            } else if (checkin.getCheckoutTime() != null) {
                result.put("status", "checkout");
                result.put("checkinTime", checkin.getCheckinTime());
                result.put("checkoutTime", checkin.getCheckoutTime());
                result.put("serviceHours", checkin.getServiceHours());
                result.put("earnedPoints", checkin.getEarnedPoints());
            }
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取签到状态失败: " + e.getMessage());
        }
    }
    
    // 生成6位随机验证码
    private String generateRandomCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return code.toString();
    }
    
    // 验证地理位置（使用Haversine公式计算球面距离，要求在1公里范围内）
    private boolean isLocationValid(Double userLat, Double userLng, Activity activity) {
        if (activity.getLatitude() == null || activity.getLongitude() == null) {
            return true; // 如果活动没有设置坐标，则不验证位置
        }

        try {
            double activityLat = Double.parseDouble(activity.getLatitude());
            double activityLng = Double.parseDouble(activity.getLongitude());

            double distance = calculateDistance(userLat, userLng, activityLat, activityLng);

            System.out.println("[位置验证] 用户位置: (" + userLat + ", " + userLng +
                    "), 活动位置: (" + activityLat + ", " + activityLng +
                    "), 距离: " + distance + "米");

            return distance <= 1000; // 允许1000米（1公里）范围内
        } catch (Exception e) {
            System.err.println("[位置验证] 坐标格式错误: " + e.getMessage());
            return true; // 坐标格式错误时不验证
        }
    }

    /**
     * 使用 Haversine 公式计算两点之间的球面距离，单位：米
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double EARTH_RADIUS = 6371000; // 地球半径，单位：米

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
    
    // 同步志愿者统计信息（时长和积分）- 立即同步到数据库
    private void syncVolunteerStats(Long volunteerId) {
        try {
            System.out.println("[ActivityCheckinController] 开始同步志愿者统计信息，ID: " + volunteerId);
            
            // 使用 PointsCalculationService 同步积分和时长
            pointsCalculationService.syncVolunteerPointsAndHours(volunteerId);
            
            // 验证同步结果，确保数据库字段已更新
            Volunteer volunteer = volunteerMapper.selectById(volunteerId);
            if (volunteer != null) {
                System.out.println("[ActivityCheckinController] 验证同步结果 - 数据库中的总时长: " + volunteer.getTotalHours() + " 小时");
                
                // 重新计算实时累计时长进行对比
                java.math.BigDecimal realTimeHours = pointsCalculationService.calculateTotalHours(volunteerId);
                System.out.println("[ActivityCheckinController] 验证同步结果 - 实时计算的累计时长: " + realTimeHours + " 小时");
                
                // 检查数据一致性
                if (volunteer.getTotalHours() != null && volunteer.getTotalHours().compareTo(realTimeHours) == 0) {
                    System.out.println("[ActivityCheckinController] 数据一致性验证通过");
                } else {
                    System.err.println("[ActivityCheckinController] 警告：数据库字段与实时计算不一致，尝试重新同步");
                    
                    // 尝试重新同步
                    pointsCalculationService.syncVolunteerHours(volunteerId);
                }
            }
            
            System.out.println("[ActivityCheckinController] 志愿者统计信息同步完成，ID: " + volunteerId);
        } catch (Exception e) {
            System.err.println("[ActivityCheckinController] 同步志愿者统计信息失败，ID: " + volunteerId + ", 错误: " + e.getMessage());
            e.printStackTrace();
            
            // 记录错误但继续执行，不影响签退操作
            System.err.println("[ActivityCheckinController] 同步失败，但签退操作继续完成");
        }
    }
    
    // 更新志愿者统计信息（时长和积分）- 使用实时同步方式
    private void updateVolunteerStats(Long volunteerId, BigDecimal serviceHours, Integer earnedPoints) {
        // 此方法已废弃，不再需要手动更新
        // 签退记录已保存到数据库，通过实时计算即可获取最新数据
        System.out.println("[ActivityCheckinController] updateVolunteerStats 方法已废弃，请使用 PointsCalculationService.syncVolunteerPointsAndHours");
    }
}