package com.volunteer.mapper;

import com.volunteer.entity.Carousel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CarouselMapper {
    List<Carousel> selectAll();
    Carousel selectById(@Param("id") Long id);
    int insert(Carousel carousel);
    int update(Carousel carousel);
    int deleteById(@Param("id") Long id);
}