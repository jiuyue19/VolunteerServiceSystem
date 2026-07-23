import request from '@/utils/request'

// 管理员登录
export const adminLogin = (data) => {
  return request.post('/auth/admin/login', data)
}

// 志愿者登录
export const volunteerLogin = (data) => {
  return request.post('/auth/volunteer/login', data)
}

// 志愿者注册
export const volunteerRegister = (data) => {
  return request.post('/auth/volunteer/register', data)
}

// 刷新令牌
export const refreshToken = (data) => {
  return request.post('/auth/refresh', data)
}

// 发送志愿者重置密码验证码（手机号或邮箱）
export const sendVolunteerResetCode = (data) => {
  return request.post('/auth/volunteer/send-reset-code', data)
}

// 通过验证码重置志愿者密码
export const resetVolunteerPasswordByCode = (data) => {
  return request.post('/auth/volunteer/reset-password', data)
}

// 志愿者通过当前密码修改密码
export const changeVolunteerPassword = (data) => {
  return request.post('/auth/volunteer/change-password', data)
}