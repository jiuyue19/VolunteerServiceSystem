import request from '@/utils/request'

export function getCategoryList() {
  return request({
    url: '/activity-category/list',
    method: 'get'
  })
}

export function addCategory(data) {
  return request({
    url: '/activity-category/add',
    method: 'post',
    data
  })
}

export function updateCategory(data) {
  return request({
    url: '/activity-category/update',
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: `/activity-category/delete/${id}`,
    method: 'delete'
  })
}

export function batchDeleteCategory(ids) {
  return request({
    url: '/activity-category/batch-delete',
    method: 'delete',
    data: ids
  })
}