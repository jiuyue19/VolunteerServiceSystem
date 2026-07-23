package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.service.EventLogServiceV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

/**
 * 事件日志控制器
 */
@RestController
@RequestMapping("/api/eventLog")
@CrossOrigin
public class EventLogController {

    @Autowired
    @Qualifier("eventLogServiceV2")
    private EventLogServiceV2 eventLogService;

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public Result test() {
        return Result.success("EventLog Controller is working!");
    }

    /**
     * 获取志愿者的所有事件日志
     * @param volunteerId 志愿者ID
     * @return 事件日志列表
     */
    @GetMapping("/volunteer/{volunteerId}")
    public Result getVolunteerEventLogs(@PathVariable Long volunteerId) {
        try {
            System.out.println("[EventLogController] 接收到请求，志愿者ID: " + volunteerId);
            return Result.success(eventLogService.getVolunteerEventLogs(volunteerId));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[EventLogController] 错误: " + e.getMessage());
            return Result.error("获取事件日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定类别的事件日志
     * @param volunteerId 志愿者ID
     * @param category 事件类别 (points/hours/activity/certificate)
     * @return 事件日志列表
     */
    @GetMapping("/volunteer/{volunteerId}/category/{category}")
    public Result getEventLogsByCategory(@PathVariable Long volunteerId, @PathVariable String category) {
        try {
            return Result.success(eventLogService.getEventLogsByCategory(volunteerId, category));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取事件日志失败: " + e.getMessage());
        }
    }
}
