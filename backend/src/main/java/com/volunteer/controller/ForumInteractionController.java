package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.mapper.CommentMapper;
import com.volunteer.mapper.ForumPostFavoriteMapper;
import com.volunteer.mapper.ForumPostLikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum-post")
@CrossOrigin
public class ForumInteractionController {

    @Autowired
    private ForumPostLikeMapper likeMapper;

    @Autowired
    private ForumPostFavoriteMapper favoriteMapper;

    @Autowired
    private CommentMapper commentMapper;

    @GetMapping("/interaction/{postId}")
    public Result<Map<String, Object>> getInteraction(@PathVariable Long postId,
                                                      @RequestParam(required = false) Long volunteerId) {
        try {
            int likes = likeMapper.countByPost(postId);
            int favorites = favoriteMapper.countByPost(postId);
            List<?> comments = commentMapper.selectByTarget("帖子", postId, volunteerId);
            int commentsCount = comments != null ? comments.size() : 0;

            Map<String, Object> data = new HashMap<>();
            data.put("likes", likes);
            data.put("favorites", favorites);
            data.put("comments", commentsCount);

            if (volunteerId != null) {
                boolean liked = likeMapper.exists(postId, volunteerId) > 0;
                boolean favorited = favoriteMapper.exists(postId, volunteerId) > 0;
                data.put("liked", liked);
                data.put("favorited", favorited);
            }

            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取帖子互动数据失败: " + e.getMessage());
        }
    }

    @PostMapping("/like")
    public Result<Void> like(@RequestBody Map<String, Long> body) {
        try {
            Long postId = body.get("postId");
            Long volunteerId = body.get("volunteerId");
            likeMapper.insert(postId, volunteerId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("点赞失败: " + e.getMessage());
        }
    }

    @PostMapping("/unlike")
    public Result<Void> unlike(@RequestBody Map<String, Long> body) {
        try {
            Long postId = body.get("postId");
            Long volunteerId = body.get("volunteerId");
            likeMapper.delete(postId, volunteerId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("取消点赞失败: " + e.getMessage());
        }
    }

    @PostMapping("/favorite")
    public Result<Void> favorite(@RequestBody Map<String, Long> body) {
        try {
            Long postId = body.get("postId");
            Long volunteerId = body.get("volunteerId");
            favoriteMapper.insert(postId, volunteerId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("收藏失败: " + e.getMessage());
        }
    }

    @PostMapping("/unfavorite")
    public Result<Void> unfavorite(@RequestBody Map<String, Long> body) {
        try {
            Long postId = body.get("postId");
            Long volunteerId = body.get("volunteerId");
            favoriteMapper.delete(postId, volunteerId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("取消收藏失败: " + e.getMessage());
        }
    }
}
