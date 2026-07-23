import request from '@/utils/request'

// 管理端：兑换订单管理
export function getOrderList() {
  return request({
    url: '/order/list',
    method: 'get'
  })
}

export function addOrder(data) {
  return request({
    url: '/order/add',
    method: 'post',
    data
  })
}

export function updateOrder(data) {
  return request({
    url: '/order/update',
    method: 'put',
    data
  })
}

export function deleteOrder(id) {
  return request({
    url: `/order/delete/${id}`,
    method: 'delete'
  })
}

export function batchDeleteOrder(ids) {
  return request({
    url: '/order/batch-delete',
    method: 'delete',
    data: ids
  })
}

// 志愿者端：积分兑换相关
export function exchangeGoods(data) {
  return request({
    url: '/order/exchange',
    method: 'post',
    data
  })
}

export function getMyExchanges() {
  return request({
    url: '/order/my-exchanges',
    method: 'get'
  })
}

export function confirmExchange(id) {
  return request({
    url: `/order/${id}/confirm`,
    method: 'post'
  })
}

export function cancelExchange(id) {
  return request({
    url: `/order/${id}/cancel`,
    method: 'post'
  })
}

// 志愿者端：兑换订单退款申请
export function applyExchangeRefund(id, data) {
  return request({
    url: `/order/${id}/refund/apply`,
    method: 'post',
    data
  })
}

// 管理端：兑换订单退款审核
export function reviewExchangeRefund(id, data) {
  return request({
    url: `/order/${id}/refund/review`,
    method: 'post',
    data
  })
}