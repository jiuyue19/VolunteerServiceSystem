import request from '@/utils/request'

export function getCarouselList() {
  return request({
    url: '/carousel/list',
    method: 'get'
  })
}

export function addCarousel(data) {
  return request({
    url: '/carousel/add',
    method: 'post',
    data
  })
}

export function updateCarousel(data) {
  return request({
    url: '/carousel/update',
    method: 'put',
    data
  })
}

export function deleteCarousel(id) {
  return request({
    url: `/carousel/delete/${id}`,
    method: 'delete'
  })
}

export function batchDeleteCarousel(ids) {
  return request({
    url: '/carousel/batch-delete',
    method: 'delete',
    data: ids
  })
}