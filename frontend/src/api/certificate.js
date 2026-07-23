import request from '@/utils/request'

/**
 * 证书管理API
 */

// 生成证书
export function generateCertificate(data) {
  return request({
    url: '/certificate/generate',
    method: 'post',
    data
  })
}

// 发放证书（单个）
export function issueCertificate(data) {
  return request({
    url: '/certificate/issue',
    method: 'post',
    data
  })
}

// 批量发放证书
export function batchIssueCertificates(data) {
  return request({
    url: '/certificate/batch-issue',
    method: 'post',
    data
  })
}

// 根据证书编号查询
export function getCertificateByCertificateNo(certificateNo) {
  return request({
    url: `/certificate/by-no/${certificateNo}`,
    method: 'get'
  })
}

// 根据姓名和钱包地址查询证书
export function queryCertificates(params) {
  return request({
    url: '/certificate/query',
    method: 'get',
    params
  })
}

// 查询志愿者的所有证书
export function getCertificatesByVolunteerId(volunteerId) {
  return request({
    url: `/certificate/volunteer/${volunteerId}`,
    method: 'get'
  })
}

// 查询所有证书
export function getAllCertificates(status) {
  return request({
    url: '/certificate/list',
    method: 'get',
    params: { status }
  })
}

// 统计证书数量
export function getCertificateStats() {
  return request({
    url: '/certificate/stats',
    method: 'get'
  })
}

// 证书验伪（基础版本 - 仅证书编号）
export function verifyCertificate(certificateNo) {
  return request({
    url: `/certificate/verify/${certificateNo}`,
    method: 'get'
  })
}

// 证书验伪（增强版本 - 姓名+编号+哈希）
export function verifyCertificateEnhanced(data) {
  return request({
    url: '/certificate/verify-enhanced',
    method: 'post',
    data
  })
}

// 撤销证书
export function revokeCertificate(data) {
  return request({
    url: '/certificate/revoke',
    method: 'post',
    data
  })
}

// 删除证书
export function deleteCertificate(certificateId) {
  return request({
    url: `/certificate/${certificateId}`,
    method: 'delete'
  })
}
