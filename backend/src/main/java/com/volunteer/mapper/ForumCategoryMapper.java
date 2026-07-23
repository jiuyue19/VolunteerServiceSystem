package com.volunteer.mapper;

import com.volunteer.entity.ForumCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ForumCategoryMapper {
    List<ForumCategory> selectAll();
    ForumCategory selectById(@Param("id") Long id);
    int insert(ForumCategory category);
    int update(ForumCategory category);
    int deleteById(@Param("id") Long id);
}