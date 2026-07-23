package com.volunteer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ForumPostFavoriteMapper {
    int insert(@Param("postId") Long postId, @Param("volunteerId") Long volunteerId);
    int delete(@Param("postId") Long postId, @Param("volunteerId") Long volunteerId);
    int countByPost(@Param("postId") Long postId);
    int exists(@Param("postId") Long postId, @Param("volunteerId") Long volunteerId);
    
    List<Map<String, Object>> getAllFavoritesForAdmin();
    int deleteFavorite(@Param("id") Long id);
}
