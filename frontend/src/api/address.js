import request from '@/utils/request'

export function getAddressList() {
  return request({
    url: '/address/list',
    method: 'get'
  })
}

export function addAddress(data) {
  return request({
    url: '/address/add',
    method: 'post',
    data
  })
}

export function updateAddress(data) {
  return request({
    url: '/address/update',
    method: 'put',
    data
  })
}

export function deleteAddress(id) {
  return request({
    url: `/address/delete/${id}`,
    method: 'delete'
  })
}

export function batchDeleteAddresses(ids) {
  return request({
    url: '/address/batch-delete',
    method: 'post',
    data: { ids }
  })
}

export function setDefaultAddress(id) {
  return request({
    url: `/address/set-default/${id}`,
    method: 'post'
  })
}
