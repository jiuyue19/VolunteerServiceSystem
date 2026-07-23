package com.volunteer.mapper;

import com.volunteer.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ActivityMapper {
    Activity selectById(@Param("id") Long id);
    List<Activity> selectAll();
    List<Activity> selectByCategory(@Param("categoryId") Long categoryId);
    List<Activity> selectByStatus(@Param("status") String status);
    List<Activity> selectByFilter(@Param("date") String date, @Param("province") String province,
                                   @Param("city") String city, @Param("district") String district);
    int insert(Activity activity);
    int update(Activity activity);
    int updateCurrentNumber(@Param("id") Long id, @Param("number") Integer number);
    int deleteById(@Param("id") Long id);
    Activity selectByTitle(@Param("title") String title);
    
    // 统计方法
    Integer countAll();
    Integer countByCategory(@Param("categoryId") Long categoryId);
}