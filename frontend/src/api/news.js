import request from '@/utils/request'

// 获取首页热点信息列表
export function getHotNews(limit = 8) {
  return request({
    url: '/news/hot',
    method: 'get',
    params: { limit }
  })
}

// 管理端：获取新闻列表
export function getNewsList() {
  return request({
    url: '/news/list',
    method: 'get'
  })
}

// 管理端：新增新闻
export function addNews(data) {
  return request({
    url: '/news/add',
    method: 'post',
    data
  })
}

// 管理端：更新新闻
export function updateNews(data) {
  return request({
    url: '/news/update',
    method: 'put',
    data
  })
}

// 管理端：删除单条新闻
export function deleteNews(id) {
  return request({
    url: `/news/delete/${id}`,
    method: 'delete'
  })
}

// 管理端：批量删除新闻
export function batchDeleteNews(ids) {
  return request({
    url: '/news/batch-delete',
    method: 'delete',
    data: ids
  })
}
