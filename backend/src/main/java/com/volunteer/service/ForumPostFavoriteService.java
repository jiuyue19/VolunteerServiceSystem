package com.volunteer.service;

import com.volunteer.mapper.ForumPostFavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ForumPostFavoriteService {

    @Autowired
    private ForumPostFavoriteMapper forumPostFavoriteMapper;

    /**
     * 获取所有帖子收藏记录（管理员）
     */
    public List<Map<String, Object>> getAllFavoritesForAdmin() {
        return forumPostFavoriteMapper.getAllFavoritesForAdmin();
    }

    /**
     * 删除收藏记录
     */
    public void deleteFavorite(Long id) {
        forumPostFavoriteMapper.deleteFavorite(id);
    }
}
