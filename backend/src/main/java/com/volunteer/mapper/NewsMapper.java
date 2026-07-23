package com.volunteer.mapper;

import com.volunteer.entity.News;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NewsMapper {
    /** 获取最新的热点信息列表（首页使用） */
    List<News> selectLatest(@Param("limit") int limit);

    /** 管理端：获取全部新闻列表 */
    List<News> selectAll();

    /** 管理端：按ID查询 */
    News selectById(@Param("id") Long id);

    /** 管理端：新增 */
    int insert(News news);

    /** 管理端：更新 */
    int update(News news);

    /** 管理端：删除 */
    int deleteById(@Param("id") Long id);
}
