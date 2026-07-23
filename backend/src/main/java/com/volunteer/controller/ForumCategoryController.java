package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.ForumCategory;
import com.volunteer.mapper.ForumCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/forum-category")
@CrossOrigin
public class ForumCategoryController {

    @Autowired
    private ForumCategoryMapper categoryMapper;

    @GetMapping("/list")
    public Result<List<ForumCategory>> getList() {
        try {
            return Result.success(categoryMapper.selectAll());
        } catch (Exception e) {
            return Result.error("获取帖子分类列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody ForumCategory category) {
        try {
            categoryMapper.insert(category);
            return Result.success();
        } catch (Exception e) {
            return Result.error("添加帖子分类失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody ForumCategory category) {
        try {
            categoryMapper.update(category);
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新帖子分类失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            categoryMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除帖子分类失败: " + e.getMessage());
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
            return Result.error("批量删除帖子分类失败: " + e.getMessage());
        }
    }
}