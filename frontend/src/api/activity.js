import request from '@/utils/request'

// 获取活动列表
export const getActivityList = (params) => {
  return request.get('/activity/list', { params })
}

// 获取活动详情
export const getActivityDetail = (id) => {
  return request.get(`/activity/detail/${id}`)
}

// 创建活动
export const createActivity = (data) => {
  return request.post('/activity/add', data)
}

// 更新活动
export const updateActivity = (data) => {
  return request.put('/activity/update', data)
}

// 删除活动
export const deleteActivity = (id) => {
  return request.delete(`/activity/delete/${id}`)
}

// 申请活动
export const applyActivity = (data) => {
  return request.post('/activity/apply', data)
}

// 审核申请
export const reviewApplication = (data) => {
  return request.post('/activity/apply/review', data)
}

// 签到
export const checkin = (data) => {
  return request.post('/checkin/in', data)
}

// 签退
export const checkout = (data) => {
  return request.post('/checkin/out', data)
}

// 获取活动的待参与志愿者
export const getParticipants = (activityId) => {
  return request.get(`/activity/participants/${activityId}`)
}

// 获取所有申请列表
export const getApplications = () => {
  return request.get('/activity/applications')
}

// 获取当前志愿者的申请列表
export const getMyApplications = (volunteerId) => {
  return request.get(`/activity/applications/my/${volunteerId}`)
}

// 获取当前志愿者的申请列表（包含签退信息）
export const getMyApplicationsWithCheckin = (volunteerId) => {
  return request.get(`/activity/applications/my-with-checkin/${volunteerId}`)
}

// 获取当前志愿者的申请列表（包含签退信息和补录信息）
export const getMyApplicationsWithReplenish = (volunteerId) => {
  return request.get(`/activity/applications/my-with-replenish/${volunteerId}`)
}

// 删除申请
export const deleteApplication = (id) => {
  return request.delete(`/activity/apply/delete/${id}`)
}

// 批量删除申请
export const batchDeleteApplications = (ids) => {
  return request.delete('/activity/apply/batch-delete', { data: ids })
}