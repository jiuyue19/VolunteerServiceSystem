import request from '@/utils/request'

export function getCheckinList() {
  return request({
    url: '/checkin/list',
    method: 'get'
  })
}

export function addCheckin(data) {
  return request({
    url: '/checkin/add',
    method: 'post',
    data
  })
}

export function updateCheckin(data) {
  return request({
    url: '/checkin/update',
    method: 'put',
    data
  })
}

export function deleteCheckin(id) {
  return request({
    url: `/checkin/delete/${id}`,
    method: 'delete'
  })
}

export function batchDeleteCheckin(ids) {
  return request({
    url: '/checkin/batch-delete',
    method: 'delete',
    data: ids
  })
}

// 生成活动验证码（管理员使用）
export function generateCode(activityId) {
  return request({
    url: `/checkin/generate-code/${activityId}`,
    method: 'post'
  })
}

// 志愿者签到
export function checkin(data) {
  return request({
    url: '/checkin/checkin',
    method: 'post',
    data
  })
}

// 志愿者签退
export function checkout(data) {
  return request({
    url: '/checkin/checkout',
    method: 'post',
    data
  })
}

// 获取签到状态
export function getCheckinStatus(activityId, volunteerId) {
  return request({
    url: `/checkin/status/${activityId}/${volunteerId}`,
    method: 'get'
  })
}