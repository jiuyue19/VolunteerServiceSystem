package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.ActivityCategory;
import com.volunteer.mapper.ActivityCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/activity-category")
@CrossOrigin
public class ActivityCategoryController {

    @Autowired
    private ActivityCategoryMapper categoryMapper;

    @GetMapping("/list")
    public Result<List<ActivityCategory>> getList() {
        try {
            List<ActivityCategory> list = categoryMapper.selectAll();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取活动分类列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody ActivityCategory category) {
        try {
            categoryMapper.insert(category);
            return Result.success();
        } catch (Exception e) {
            return Result.error("添加活动分类失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody ActivityCategory category) {
        try {
            categoryMapper.update(category);
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新活动分类失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            categoryMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除活动分类失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                categoryMapper.deleteById(id);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error("批量删除活动分类失败: " + e.getMessage());
        }
    }
}