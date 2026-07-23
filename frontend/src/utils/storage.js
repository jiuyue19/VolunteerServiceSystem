/**
 * 角色存储工具
 * 根据不同角色使用不同的localStorage key，避免角色信息互相影响
 */

/**
 * 获取当前角色类型（根据路由路径判断）
 * @param {string} path - 当前路由路径
 * @returns {string} 'volunteer' | 'admin'
 */
export const getRoleTypeByPath = (path) => {
  if (path.startsWith('/admin')) {
    return 'admin'
  } else if (path.startsWith('/volunteer')) {
    return 'volunteer'
  }
  // 默认返回 localStorage 中存储的 userType，如果都没有则返回 volunteer
  return localStorage.getItem('currentUserType') || 'volunteer'
}

/**
 * 生成带角色前缀的key
 * @param {string} key - 原始key
 * @param {string} roleType - 角色类型 'volunteer' | 'admin'
 * @returns {string} 带前缀的key
 */
const getRoleKey = (key, roleType) => {
  return `${roleType}_${key}`
}

/**
 * 设置角色相关的存储项
 * @param {string} key - 存储键
 * @param {*} value - 存储值
 * @param {string} roleType - 角色类型
 */
export const setRoleItem = (key, value, roleType) => {
  const roleKey = getRoleKey(key, roleType)
  if (typeof value === 'object') {
    localStorage.setItem(roleKey, JSON.stringify(value))
  } else {
    localStorage.setItem(roleKey, value)
  }
}

/**
 * 获取角色相关的存储项
 * @param {string} key - 存储键
 * @param {string} roleType - 角色类型
 * @returns {*} 存储值
 */
export const getRoleItem = (key, roleType) => {
  const roleKey = getRoleKey(key, roleType)
  const value = localStorage.getItem(roleKey)
  
  // 尝试解析JSON
  if (value && (value.startsWith('{') || value.startsWith('['))) {
    try {
      return JSON.parse(value)
    } catch (e) {
      return value
    }
  }
  return value
}

/**
 * 移除角色相关的存储项
 * @param {string} key - 存储键
 * @param {string} roleType - 角色类型
 */
export const removeRoleItem = (key, roleType) => {
  const roleKey = getRoleKey(key, roleType)
  localStorage.removeItem(roleKey)
}

/**
 * 清除指定角色的所有存储数据
 * @param {string} roleType - 角色类型
 */
export const clearRoleStorage = (roleType) => {
  const keysToRemove = ['token', 'refreshToken', 'user', 'userId', 'avatar', 'username']
  keysToRemove.forEach(key => {
    removeRoleItem(key, roleType)
  })
  
  // 如果清除的是当前用户类型，也清除 currentUserType
  if (localStorage.getItem('currentUserType') === roleType) {
    localStorage.removeItem('currentUserType')
  }
}

/**
 * 设置当前激活的用户类型
 * @param {string} roleType - 角色类型
 */
export const setCurrentUserType = (roleType) => {
  localStorage.setItem('currentUserType', roleType)
}

/**
 * 获取当前激活的用户类型
 * @returns {string|null} 角色类型
 */
export const getCurrentUserType = () => {
  return localStorage.getItem('currentUserType')
}
