import request from '@/utils/request'

export function getReplenishList() {
  return request({
    url: '/replenish/list',
    method: 'get'
  })
}

export function addReplenish(data) {
  return request({
    url: '/replenish/add',
    method: 'post',
    data
  })
}

export function updateReplenish(data) {
  return request({
    url: '/replenish/update',
    method: 'put',
    data
  })
}

export function deleteReplenish(id) {
  return request({
    url: `/replenish/delete/${id}`,
    method: 'delete'
  })
}

export function batchDeleteReplenish(ids) {
  return request({
    url: '/replenish/batch-delete',
    method: 'delete',
    data: ids
  })
}

// 志愿者获取自己的补录申请
export function getMyReplenish() {
  return request({
    url: '/replenish/my',
    method: 'get'
  })
}

// 志愿者提交补录申请
export function applyReplenish(data) {
  return request({
    url: '/replenish/apply',
    method: 'post',
    data
  })
}

// 管理员审核补录申请
export function reviewReplenish(data) {
  return request({
    url: '/replenish/review',
    method: 'post',
    data
  })
}