package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.Carousel;
import com.volunteer.mapper.CarouselMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/carousel")
@CrossOrigin
public class CarouselController {

    @Autowired
    private CarouselMapper carouselMapper;

    @GetMapping("/list")
    public Result<List<Carousel>> getList() {
        try {
            return Result.success(carouselMapper.selectAll());
        } catch (Exception e) {
            return Result.error("获取轮播图列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Carousel carousel) {
        try {
            System.out.println("接收到轮播图数据: " + carousel); // 添加调试日志
            carouselMapper.insert(carousel);
            System.out.println("轮播图插入成功"); // 添加调试日志
            return Result.success();
        } catch (Exception e) {
            System.err.println("添加轮播图失败: " + e.getMessage()); // 添加错误日志
            e.printStackTrace();
            return Result.error("添加轮播图失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Carousel carousel) {
        try {
            carouselMapper.update(carousel);
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新轮播图失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            carouselMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除轮播图失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                carouselMapper.deleteById(id);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error("批量删除轮播图失败: " + e.getMessage());
        }
    }
}