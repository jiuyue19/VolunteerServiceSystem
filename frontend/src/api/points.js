import request from '@/utils/request'

/**
 * 获取志愿者的详细积分信息
 * 包括：活动积分、补录积分、兑换扣减、退款退还、累计积分
 * @param {Number} volunteerId 志愿者ID
 * @returns {Promise} 积分详细信息
 */
export function calculatePoints(volunteerId) {
  return request({
    url: `/points/calculate/${volunteerId}`,
    method: 'get'
  })
}

/**
 * 只获取累计积分数值
 * @param {Number} volunteerId 志愿者ID
 * @returns {Promise} 累计积分
 */
export function getTotalPoints(volunteerId) {
  return request({
    url: `/points/total/${volunteerId}`,
    method: 'get'
  })
}
