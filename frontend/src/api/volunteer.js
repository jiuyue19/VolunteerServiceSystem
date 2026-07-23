import request from '@/utils/request'

export const getVolunteerInfo = () => {
  return request({
    url: '/volunteer/info',
    method: 'get'
  })
}

export const getInfo = () => {
  return request({
    url: '/volunteer/info',
    method: 'get'
  })
}

export const updateVolunteerInfo = (data) => {
  return request({
    url: '/volunteer/update',
    method: 'put',
    data
  })
}

export const addVolunteer = (data) => {
  return request({
    url: '/volunteer/add',
    method: 'post',
    data
  })
}

export const deleteVolunteer = (id) => {
  return request({
    url: `/volunteer/delete/${id}`,
    method: 'delete'
  })
}

export const batchDeleteVolunteer = (ids) => {
  return request({
    url: '/volunteer/batch-delete',
    method: 'delete',
    data: ids
  })
}

export const getVolunteerList = () => {
  return request({
    url: '/volunteer/list',
    method: 'get'
  })
}

export const recalculateStats = () => {
  return request({
    url: '/volunteer/recalculate-stats',
    method: 'post'
  })
}

export const adminUpdateVolunteer = (data) => {
  return request({
    url: '/volunteer/admin-update',
    method: 'put',
    data
  })
}