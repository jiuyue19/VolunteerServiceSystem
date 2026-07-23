import request from '@/utils/request'

export const getAdminInfo = () => {
  return request({
    url: '/admin/info',
    method: 'get'
  })
}

export const updateAdminInfo = (data) => {
  return request({
    url: '/admin/update',
    method: 'put',
    data
  })
}

export const addAdmin = (data) => {
  return request({
    url: '/admin/add',
    method: 'post',
    data
  })
}

export const deleteAdmin = (id) => {
  return request({
    url: `/admin/delete/${id}`,
    method: 'delete'
  })
}

export const batchDeleteAdmin = (ids) => {
  return request({
    url: '/admin/batch-delete',
    method: 'delete',
    data: ids
  })
}

export const getAdminList = () => {
  return request({
    url: '/admin/list',
    method: 'get'
  })
}

export const updateAdminById = (data) => {
  return request({
    url: '/admin/update-by-id',
    method: 'put',
    data
  })
}