package com.volunteer.mapper;

import com.volunteer.entity.ForumPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ForumPostMapper {
    List<ForumPost> selectAll();
    List<ForumPost> selectByCategory(@Param("categoryId") Long categoryId);
    List<ForumPost> search(@Param("keyword") String keyword);
    ForumPost selectById(@Param("id") Long id);
    List<ForumPost> selectByVolunteerId(@Param("volunteerId") Long volunteerId);
    int insert(ForumPost post);
    int update(ForumPost post);
    int deleteById(@Param("id") Long id);
    int incrementViews(@Param("id") Long id);
    int reviewPost(@Param("id") Long id, @Param("status") String status, @Param("reviewReason") String reviewReason);
    int checkIfShared(@Param("postId") Long postId, @Param("volunteerId") Long volunteerId);
    int sharePost(@Param("postId") Long postId, @Param("volunteerId") Long volunteerId);
    // 统计方法
    Integer countByCategory(@Param("categoryId") Long categoryId);
    // 获取志愿者发布的帖子总数
    Integer countByVolunteerId(@Param("volunteerId") Long volunteerId);
    // 获取所有志愿者发布的帖子总数
    Integer countAllPosts();
    // 获取志愿者收藏的帖子列表
    List<ForumPost> selectFavoritedByVolunteerId(@Param("volunteerId") Long volunteerId);
}