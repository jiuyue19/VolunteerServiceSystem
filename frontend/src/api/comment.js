import request from '@/utils/request'

export function getCommentList() {
  return request({
    url: '/comment/list',
    method: 'get'
  })
}

export function getCommentsByTarget(targetType, targetId, volunteerId = null) {
  const params = { targetType, targetId }
  if (volunteerId) {
    params.volunteerId = volunteerId
  }
  return request({
    url: '/comment/list-by-target',
    method: 'get',
    params
  })
}

export function addComment(data) {
  return request({
    url: '/comment/add',
    method: 'post',
    data
  })
}

export function updateComment(data) {
  return request({
    url: '/comment/update',
    method: 'put',
    data
  })
}

export function deleteComment(id) {
  return request({
    url: `/comment/delete/${id}`,
    method: 'delete'
  })
}

export function batchDeleteComment(ids) {
  return request({
    url: '/comment/batch-delete',
    method: 'delete',
    data: ids
  })
}

export function likeComment(commentId, volunteerId) {
  return request({
    url: '/comment/like',
    method: 'post',
    data: { commentId, volunteerId }
  })
}

export function unlikeComment(commentId, volunteerId) {
  return request({
    url: '/comment/unlike',
    method: 'post',
    data: { commentId, volunteerId }
  })
}

export function getMyComments(volunteerId) {
  return request({
    url: `/comment/my/${volunteerId}`,
    method: 'get'
  })
}