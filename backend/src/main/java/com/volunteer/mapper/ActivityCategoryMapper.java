package com.volunteer.mapper;

import com.volunteer.entity.ActivityCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ActivityCategoryMapper {
    List<ActivityCategory> selectAll();
    ActivityCategory selectById(@Param("id") Long id);
    int insert(ActivityCategory category);
    int update(ActivityCategory category);
    int deleteById(@Param("id") Long id);
}