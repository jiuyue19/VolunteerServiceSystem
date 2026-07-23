package com.volunteer.mapper;

import com.volunteer.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface GoodsMapper {
    List<Goods> selectAll();
    Goods selectById(@Param("id") Long id);
    void insert(Goods goods);
    void update(Goods goods);
    void deleteById(Long id);
    int reduceStock(@Param("id") Long id, @Param("quantity") Integer quantity);
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}