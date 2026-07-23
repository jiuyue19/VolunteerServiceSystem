import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getRoleItem, getRoleTypeByPath } from './storage'

// 刷新令牌并重试失败请求的支持
let isRefreshing = false
let refreshPromise = null

// 统一处理未授权/登录过期：清理本地凭据并强制跳转登录页
const forceLogout = (roleType) => {
  try {
    const storage = require('./storage')
    if (storage.clearRoleStorage) {
      storage.clearRoleStorage(roleType)
    } else if (storage.removeRoleItem) {
      storage.removeRoleItem('token', roleType)
      storage.removeRoleItem('refreshToken', roleType)
    }
  } catch (e) {
    // 忽略本地清理异常，继续跳转
  }

  // 优先使用 SPA 路由跳转
  router.push('/login').catch(() => {})

  // 兜底：如果 100ms 后仍不在登录页，强制刷新到登录路由
  setTimeout(() => {
    try {
      if (router.currentRoute.value.path !== '/login') {
        window.location.href = '/login'
      }
    } catch (e) {
      window.location.href = '/login'
    }
  }, 100)
}

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 根据当前路由获取对应角色的token
    const currentPath = router.currentRoute.value.path
    const roleType = getRoleTypeByPath(currentPath)
    const token = getRoleItem('token', roleType)
    
    console.log('[Request] 路径:', currentPath, '角色类型:', roleType, 'Token存在:', !!token)
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    } else {
      console.warn('[Request] Token不存在，请求可能失败:', config.url)
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    if (error.response?.status === 401) {
      const originalConfig = error.config || {}
      const currentPath = router.currentRoute.value.path
      const roleType = getRoleTypeByPath(currentPath)
      
      // 避免对刷新接口再次触发刷新逻辑导致死循环
      if (originalConfig.url && originalConfig.url.includes('/auth/refresh')) {
        ElMessage.error('登录已过期，请重新登录')
        forceLogout(roleType)
        return Promise.reject(error)
      }

      const rt = getRoleItem('refreshToken', roleType)
      if (!rt) {
        ElMessage.error('登录已过期，请重新登录')
        forceLogout(roleType)
        return Promise.reject(error)
      }

      if (!isRefreshing) {
        isRefreshing = true
        // 直接用 axios，避免拦截器嵌套造成重复处理
        refreshPromise = axios.post('/api/auth/refresh', { refreshToken: rt })
      }

      return refreshPromise
        .then(res => {
          isRefreshing = false
          refreshPromise = null
          const newToken = res?.data?.data?.token
          if (!newToken) {
            throw new Error('刷新令牌失败')
          }
          // 更新 token 并重试原请求
          const { setRoleItem } = require('./storage')
          setRoleItem('token', newToken, roleType)
          originalConfig.headers = originalConfig.headers || {}
          originalConfig.headers.Authorization = `Bearer ${newToken}`
          return request(originalConfig)
        })
        .catch(err => {
          isRefreshing = false
          refreshPromise = null
          ElMessage.error('登录已过期，请重新登录')
          forceLogout(roleType)
          return Promise.reject(err)
        })
    } else {
      ElMessage.error(error.message || '网络错误')
      return Promise.reject(error)
    }
  }
)

export default request