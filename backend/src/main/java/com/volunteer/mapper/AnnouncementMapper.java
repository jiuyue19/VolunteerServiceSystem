package com.volunteer.mapper;

import com.volunteer.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AnnouncementMapper {
    List<Announcement> selectAll();
    Announcement selectById(@Param("id") Long id);
    int insert(Announcement announcement);
    int update(Announcement announcement);
    int deleteById(@Param("id") Long id);
}