package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.service.ForumPostFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/post-favorites")
public class ForumPostFavoriteController {

    @Autowired
    private ForumPostFavoriteService forumPostFavoriteService;

    /**
     * 获取所有帖子收藏记录（管理员）
     */
    @GetMapping
    public Result<List<Map<String, Object>>> getAllFavorites() {
        try {
            List<Map<String, Object>> favorites = forumPostFavoriteService.getAllFavoritesForAdmin();
            return Result.success(favorites);
        } catch (Exception e) {
            return Result.error("获取收藏记录失败：" + e.getMessage());
        }
    }

    /**
     * 删除收藏记录（管理员）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFavorite(@PathVariable Long id) {
        try {
            forumPostFavoriteService.deleteFavorite(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除收藏记录失败：" + e.getMessage());
        }
    }
}
