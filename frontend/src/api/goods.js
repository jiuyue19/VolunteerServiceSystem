import request from '@/utils/request'

export function getGoodsList() {
  return request({
    url: '/goods/list',
    method: 'get'
  })
}

export function addGoods(data) {
  return request({
    url: '/goods/add',
    method: 'post',
    data
  })
}

export function updateGoods(data) {
  return request({
    url: '/goods/update',
    method: 'put',
    data
  })
}

export function deleteGoods(id) {
  return request({
    url: `/goods/delete/${id}`,
    method: 'delete'
  })
}

export function batchDeleteGoods(ids) {
  return request({
    url: '/goods/batch-delete',
    method: 'delete',
    data: ids
  })
}