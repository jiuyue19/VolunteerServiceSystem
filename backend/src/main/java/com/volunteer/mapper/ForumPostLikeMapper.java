package com.volunteer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ForumPostLikeMapper {
    int insert(@Param("postId") Long postId, @Param("volunteerId") Long volunteerId);
    int delete(@Param("postId") Long postId, @Param("volunteerId") Long volunteerId);
    int countByPost(@Param("postId") Long postId);
    int exists(@Param("postId") Long postId, @Param("volunteerId") Long volunteerId);
}
