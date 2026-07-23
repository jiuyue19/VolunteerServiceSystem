package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.Announcement;
import com.volunteer.mapper.AnnouncementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/announcement")
@CrossOrigin
public class AnnouncementController {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @GetMapping("/list")
    public Result<List<Announcement>> getList() {
        try {
            return Result.success(announcementMapper.selectAll());
        } catch (Exception e) {
            return Result.error("获取公告列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Announcement announcement) {
        try {
            announcementMapper.insert(announcement);
            return Result.success();
        } catch (Exception e) {
            return Result.error("添加公告失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Announcement announcement) {
        try {
            announcementMapper.update(announcement);
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新公告失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            announcementMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除公告失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                announcementMapper.deleteById(id);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error("批量删除公告失败: " + e.getMessage());
        }
    }
}