import request from '@/utils/request'

// 帖子分类
export function getPostCategoryList() {
  return request({
    url: '/forum-category/list',
    method: 'get'
  })
}

export function addPostCategory(data) {
  return request({
    url: '/forum-category/add',
    method: 'post',
    data
  })
}

export function updatePostCategory(data) {
  return request({
    url: '/forum-category/update',
    method: 'put',
    data
  })
}

export function deletePostCategory(id) {
  return request({
    url: `/forum-category/delete/${id}`,
    method: 'delete'
  })
}

export function batchDeletePostCategory(ids) {
  return request({
    url: '/forum-category/batch-delete',
    method: 'delete',
    data: ids
  })
}

// 论坛帖子
export function getForumPostList(params) {
  return request({
    url: '/forum-post/list',
    method: 'get',
    params
  })
}

export function getMyForumPosts(params) {
  return request({
    url: '/forum-post/my',
    method: 'get',
    params
  })
}

export function getForumPostDetail(id) {
  return request({
    url: `/forum-post/detail/${id}`,
    method: 'get'
  })
}

export function addForumPost(data) {
  return request({
    url: '/forum-post/add',
    method: 'post',
    data
  })
}

export function updateForumPost(data) {
  return request({
    url: '/forum-post/update',
    method: 'put',
    data
  })
}

export function reviewForumPost(data) {
  return request({
    url: '/forum-post/review',
    method: 'post',
    data
  })
}

export function deleteForumPost(id) {
  return request({
    url: `/forum-post/delete/${id}`,
    method: 'delete'
  })
}

export function batchDeleteForumPost(ids) {
  return request({
    url: '/forum-post/batch-delete',
    method: 'delete',
    data: ids
  })
}

export function getPostInteraction(postId, volunteerId) {
  return request({
    url: `/forum-post/interaction/${postId}`,
    method: 'get',
    params: { volunteerId }
  })
}

export function likePost(data) {
  return request({
    url: '/forum-post/like',
    method: 'post',
    data
  })
}

export function unlikePost(data) {
  return request({
    url: '/forum-post/unlike',
    method: 'post',
    data
  })
}

export function favoritePost(data) {
  return request({
    url: '/forum-post/favorite',
    method: 'post',
    data
  })
}

export function unfavoritePost(data) {
  return request({
    url: '/forum-post/unfavorite',
    method: 'post',
    data
  })
}

export function sharePost(data) {
  return request({
    url: '/forum-post/share',
    method: 'post',
    data
  })
}

export function getFavoritePosts(params) {
  return request({
    url: '/forum-post/favorites',
    method: 'get',
    params
  })
}