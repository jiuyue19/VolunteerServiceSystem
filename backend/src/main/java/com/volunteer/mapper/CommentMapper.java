package com.volunteer.mapper;

import com.volunteer.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> selectAll();
    Comment selectById(@Param("id") Long id);
    List<Comment> selectByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId, @Param("volunteerId") Long volunteerId);
    List<Comment> selectByVolunteerId(@Param("volunteerId") Long volunteerId);
    int insert(Comment comment);
    int update(Comment comment);
    int deleteById(@Param("id") Long id);
    int insertLike(@Param("commentId") Long commentId, @Param("volunteerId") Long volunteerId);
    int deleteLike(@Param("commentId") Long commentId, @Param("volunteerId") Long volunteerId);
}