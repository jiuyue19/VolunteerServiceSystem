package com.volunteer.mapper;

import com.volunteer.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AdminMapper {
    Admin selectByUsername(@Param("username") String username);
    Admin selectById(@Param("id") Long id);
    List<Admin> selectAll();
    int insert(Admin admin);
    int update(Admin admin);
    int deleteById(@Param("id") Long id);
}