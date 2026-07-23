import request from '@/utils/request'

// 获取所有权限列表（仅超级管理员）
export const getAllPermissions = () => {
  return request({
    url: '/permission/list',
    method: 'get'
  })
}

// 获取当前登录管理员的权限列表
export const getMyPermissions = () => {
  return request({
    url: '/permission/my-permissions',
    method: 'get'
  })
}

// 获取指定管理员的权限
export const getAdminPermissions = (adminId) => {
  return request({
    url: `/permission/admin/${adminId}`,
    method: 'get'
  })
}

// 为管理员分配权限
export const assignPermissions = (data) => {
  return request({
    url: '/permission/assign',
    method: 'post',
    data
  })
}

// 检查是否有指定权限
export const checkPermission = (permissionKey) => {
  return request({
    url: `/permission/check/${permissionKey}`,
    method: 'get'
  })
}
