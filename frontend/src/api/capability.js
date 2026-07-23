import request from '@/utils/request'

/**
 * 获取志愿者能力评估
 * @param {Number} volunteerId - 志愿者ID
 * @returns {Promise}
 */
export function getVolunteerCapability(volunteerId) {
  return request({
    url: `/volunteer/capability/${volunteerId}`,
    method: 'get'
  })
}

/**
 * 获取能力评估算法说明
 * @returns {Promise}
 */
export function getCapabilityInfo() {
  return request({
    url: '/volunteer/capability/info',
    method: 'get'
  })
}
