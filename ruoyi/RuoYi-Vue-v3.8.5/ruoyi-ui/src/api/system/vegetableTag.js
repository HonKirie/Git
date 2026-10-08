import request from '@/utils/request'

export function listVegetableTag(query) {
  return request({ url: '/system/vegetable-tag/list', method: 'get', params: query })
}

export function getVegetableTag(dictCode) {
  return request({ url: '/system/vegetable-tag/' + dictCode, method: 'get' })
}

export function addVegetableTag(data) {
  return request({ url: '/system/vegetable-tag', method: 'post', data })
}

export function updateVegetableTag(data) {
  return request({ url: '/system/vegetable-tag', method: 'put', data })
}

export function delVegetableTag(dictCodes) {
  return request({ url: '/system/vegetable-tag/' + dictCodes, method: 'delete' })
}
