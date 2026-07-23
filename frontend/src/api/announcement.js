import request from '@/utils/request'

export function getAnnouncementList() {
  return request({
    url: '/announcement/list',
    method: 'get'
  })
}

export function addAnnouncement(data) {
  return request({
    url: '/announcement/add',
    method: 'post',
    data
  })
}

export function updateAnnouncement(data) {
  return request({
    url: '/announcement/update',
    method: 'put',
    data
  })
}

export function deleteAnnouncement(id) {
  return request({
    url: `/announcement/delete/${id}`,
    method: 'delete'
  })
}

export function batchDeleteAnnouncement(ids) {
  return request({
    url: '/announcement/batch-delete',
    method: 'delete',
    data: ids
  })
}