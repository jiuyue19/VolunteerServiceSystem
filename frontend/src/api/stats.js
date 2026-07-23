import request from '@/utils/request'

/**
 * 获取志愿者的完整统计数据
 * 包括：累计时长、累计积分、参与活动数、获得证书数
 * @param {Number} volunteerId 志愿者ID
 * @returns {Promise} 统计数据
 */
export function getVolunteerStats(volunteerId) {
  return request({
    url: `/volunteer/stats/${volunteerId}`,
    method: 'get'
  })
}

/**
 * 获取志愿者成长轨迹数据
 * @param {Number} volunteerId 志愿者ID
 * @param {String} period 周期类型: week/month/year
 * @returns {Promise} 成长轨迹数据
 */
export function getGrowthTrend(volunteerId, period = 'month') {
  return request({
    url: `/volunteer/stats/${volunteerId}/growth-trend`,
    method: 'get',
    params: { period }
  })
}

/**
 * 获取志愿者能力雷达图数据
 * @param {Number} volunteerId 志愿者ID
 * @returns {Promise} 能力雷达图数据
 */
export function getAbilityRadar(volunteerId) {
  return request({
    url: `/volunteer/stats/${volunteerId}/ability-radar`,
    method: 'get'
  })
}
