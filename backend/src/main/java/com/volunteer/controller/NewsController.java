package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.News;
import com.volunteer.mapper.NewsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@CrossOrigin
public class NewsController {

    @Autowired
    private NewsMapper newsMapper;

    /**
     * 获取首页展示用的热点信息列表
     */
    @GetMapping("/hot")
    public Result<List<News>> getHotNews(@RequestParam(defaultValue = "8") int limit) {
        try {
            List<News> list = newsMapper.selectLatest(limit);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取热点信息失败: " + e.getMessage());
        }
    }

    /** 管理端：获取全部新闻列表 */
    @GetMapping("/list")
    public Result<List<News>> getList() {
        try {
            return Result.success(newsMapper.selectAll());
        } catch (Exception e) {
            return Result.error("获取新闻列表失败: " + e.getMessage());
        }
    }

    /** 管理端：新增新闻 */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody News news) {
        try {
            if (news.getViews() == null) {
                news.setViews(0);
            }
            newsMapper.insert(news);
            return Result.success();
        } catch (Exception e) {
            return Result.error("添加新闻失败: " + e.getMessage());
        }
    }

    /** 管理端：更新新闻 */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody News news) {
        try {
            newsMapper.update(news);
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新新闻失败: " + e.getMessage());
        }
    }

    /** 管理端：删除单条新闻 */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            newsMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除新闻失败: " + e.getMessage());
        }
    }

    /** 管理端：批量删除新闻 */
    @DeleteMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                newsMapper.deleteById(id);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error("批量删除新闻失败: " + e.getMessage());
        }
    }
}
