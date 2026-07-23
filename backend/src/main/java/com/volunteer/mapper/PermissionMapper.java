package com.volunteer.mapper;

import com.volunteer.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PermissionMapper {
    // 获取所有权限
    List<Permission> selectAll();
    
    // 获取指定管理员的权限列表
    List<Permission> selectByAdminId(@Param("adminId") Long adminId);
    
    // 根据ID查询权限
    Permission selectById(@Param("id") Long id);
    
    // 添加权限
    int insert(Permission permission);
    
    // 更新权限
    int update(Permission permission);
    
    // 删除权限
    int deleteById(@Param("id") Long id);
    
    // 批量为管理员分配权限
    int batchInsertAdminPermissions(@Param("adminId") Long adminId, @Param("permissionIds") List<Long> permissionIds);
    
    // 删除管理员的所有权限
    int deleteAdminPermissions(@Param("adminId") Long adminId);
}
