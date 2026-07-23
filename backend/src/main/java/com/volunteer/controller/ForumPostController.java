package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.ForumPost;
import com.volunteer.mapper.ForumPostMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/forum-post")
@CrossOrigin
public class ForumPostController {

    @Autowired
    private ForumPostMapper postMapper;

    @Data
    public static class ReviewRequest {
        private Long id;
        private String status;
        private String reviewReason;
    }

    @Data
    public static class ShareRequest {
        private Long postId;
        private Long volunteerId;
    }

    @GetMapping("/list")
    public Result<List<ForumPost>> getList(@RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) String keyword) {
        try {
            List<ForumPost> posts;
            if (keyword != null && !keyword.trim().isEmpty()) {
                posts = postMapper.search(keyword);
            } else if (categoryId != null) {
                posts = postMapper.selectByCategory(categoryId);
            } else {
                posts = postMapper.selectAll();
            }
            return Result.success(posts);
        } catch (Exception e) {
            return Result.error("获取论坛帖子列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public Result<ForumPost> getDetail(@PathVariable Long id) {
        try {
            postMapper.incrementViews(id);
            ForumPost post = postMapper.selectById(id);
            return Result.success(post);
        } catch (Exception e) {
            return Result.error("获取帖子详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/my")
    public Result<List<ForumPost>> getMyPosts(@RequestParam Long volunteerId) {
        try {
            List<ForumPost> posts = postMapper.selectByVolunteerId(volunteerId);
            return Result.success(posts);
        } catch (Exception e) {
            return Result.error("获取我的帖子列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/favorites")
    public Result<List<ForumPost>> getFavoritePosts(@RequestParam Long volunteerId) {
        try {
            List<ForumPost> posts = postMapper.selectFavoritedByVolunteerId(volunteerId);
            return Result.success(posts);
        } catch (Exception e) {
            return Result.error("获取收藏列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody ForumPost post) {
        try {
            postMapper.insert(post);
            return Result.success();
        } catch (Exception e) {
            return Result.error("添加论坛帖子失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody ForumPost post) {
        try {
            postMapper.update(post);
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新论坛帖子失败: " + e.getMessage());
        }
    }

    @PostMapping("/review")
    public Result<Void> review(@RequestBody ReviewRequest request) {
        try {
            if (request.getId() == null || request.getStatus() == null) {
                return Result.error("帖子ID和状态不能为空");
            }
            postMapper.reviewPost(request.getId(), request.getStatus(), request.getReviewReason());
            return Result.success();
        } catch (Exception e) {
            return Result.error("审核帖子失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            postMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除论坛帖子失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                postMapper.deleteById(id);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error("批量删除论坛帖子失败: " + e.getMessage());
        }
    }

    @PostMapping("/share")
    public Result<Void> sharePost(@RequestBody ShareRequest request) {
        try {
            if (request.getPostId() == null || request.getVolunteerId() == null) {
                return Result.error("帖子ID和志愿者ID不能为空");
            }
            
            // 获取原帖信息
            ForumPost originalPost = postMapper.selectById(request.getPostId());
            if (originalPost == null) {
                return Result.error("原帖不存在");
            }
            
            // 检查是否已经转发过
            if (postMapper.checkIfShared(request.getPostId(), request.getVolunteerId()) > 0) {
                return Result.error("您已经转发过该帖子");
            }
            
            // 创建转发帖子
            postMapper.sharePost(request.getPostId(), request.getVolunteerId());
            return Result.success();
        } catch (Exception e) {
            return Result.error("转发帖子失败: " + e.getMessage());
        }
    }
}