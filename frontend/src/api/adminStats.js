import request from '@/utils/request'

/**
 * 管理员数据统计API
 * 用于数据大屏展示
 */

/**
 * 获取KPI数据（顶部数字看板）
 */
export function getKpiData() {
  return request({
    url: '/admin/stats/kpi',
    method: 'get'
  })
}

/**
 * 获取活动参与人数统计
 */
export function getActivityParticipants() {
  return request({
    url: '/admin/stats/activity-participants',
    method: 'get'
  })
}

/**
 * 获取志愿时长趋势
 */
export function getHoursTrend() {
  return request({
    url: '/admin/stats/hours-trend',
    method: 'get'
  })
}

/**
 * 获取补录时长趋势
 */
export function getReplenishTrend() {
  return request({
    url: '/admin/stats/replenish-trend',
    method: 'get'
  })
}

/**
 * 获取活动类型占比
 */
export function getActivityDistribution() {
  return request({
    url: '/admin/stats/activity-distribution',
    method: 'get'
  })
}

/**
 * 获取积分排行榜
 */
export function getPointsRanking() {
  return request({
    url: '/admin/stats/points-ranking',
    method: 'get'
  })
}

/**
 * 获取论坛帖子分布
 */
export function getForumDistribution() {
  return request({
    url: '/admin/stats/forum-distribution',
    method: 'get'
  })
}

/**
 * 获取志愿者地域分布
 */
export function getVolunteerLocation() {
  return request({
    url: '/admin/stats/volunteer-location',
    method: 'get'
  })
}

/**
 * 获取最近的链上事件
 * @param {Number} limit 返回数量限制
 */
export function getRecentEvents(limit = 10) {
  return request({
    url: '/admin/stats/recent-events',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取所有统计数据（一次性获取）
 */
export function getAllStats() {
  return request({
    url: '/admin/stats/all',
    method: 'get'
  })
}
