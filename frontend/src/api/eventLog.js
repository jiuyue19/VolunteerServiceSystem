import request from '@/utils/request'

/**
 * 获取志愿者事件日志
 * @param {Number} volunteerId 志愿者ID
 * @returns {Promise}
 */
export function getVolunteerEventLogs(volunteerId) {
  return request({
    url: `/eventLog/volunteer/${volunteerId}`,
    method: 'get'
  })
}

/**
 * 获取指定类型的事件日志
 * @param {Number} volunteerId 志愿者ID
 * @param {String} type 事件类型 (activity/checkin/certificate)
 * @returns {Promise}
 */
export function getEventLogsByType(volunteerId, type) {
  return request({
    url: `/eventLog/volunteer/${volunteerId}/type/${type}`,
    method: 'get'
  })
}
