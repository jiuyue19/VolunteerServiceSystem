package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.entity.Comment;
import com.volunteer.mapper.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/comment")
@CrossOrigin
public class CommentController {

    @Autowired
    private CommentMapper commentMapper;

    @GetMapping("/list")
    public Result<List<Comment>> getList() {
        try {
            return Result.success(commentMapper.selectAll());
        } catch (Exception e) {
            return Result.error("获取评论列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/list-by-target")
    public Result<List<Comment>> getByTarget(@RequestParam String targetType, @RequestParam Long targetId, @RequestParam(required = false) Long volunteerId) {
        try {
            return Result.success(commentMapper.selectByTarget(targetType, targetId, volunteerId));
        } catch (Exception e) {
            return Result.error("获取评论列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Comment comment) {
        try {
            commentMapper.insert(comment);
            return Result.success();
        } catch (Exception e) {
            return Result.error("添加评论失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Comment comment) {
        try {
            commentMapper.update(comment);
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新评论失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            commentMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除评论失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                commentMapper.deleteById(id);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error("批量删除评论失败: " + e.getMessage());
        }
    }

    @GetMapping("/my/{volunteerId}")
    public Result<List<Comment>> getMyComments(@PathVariable Long volunteerId) {
        try {
            return Result.success(commentMapper.selectByVolunteerId(volunteerId));
        } catch (Exception e) {
            return Result.error("获取我的评论失败: " + e.getMessage());
        }
    }

    @PostMapping("/like")
    public Result<Void> likeComment(@RequestBody LikeRequest request) {
        try {
            commentMapper.insertLike(request.getCommentId(), request.getVolunteerId());
            return Result.success();
        } catch (Exception e) {
            return Result.error("点赞失败: " + e.getMessage());
        }
    }

    @PostMapping("/unlike")
    public Result<Void> unlikeComment(@RequestBody LikeRequest request) {
        try {
            commentMapper.deleteLike(request.getCommentId(), request.getVolunteerId());
            return Result.success();
        } catch (Exception e) {
            return Result.error("取消点赞失败: " + e.getMessage());
        }
    }

    public static class LikeRequest {
        private Long commentId;
        private Long volunteerId;

        public Long getCommentId() { return commentId; }
        public void setCommentId(Long commentId) { this.commentId = commentId; }
        public Long getVolunteerId() { return volunteerId; }
        public void setVolunteerId(Long volunteerId) { this.volunteerId = volunteerId; }
    }
}