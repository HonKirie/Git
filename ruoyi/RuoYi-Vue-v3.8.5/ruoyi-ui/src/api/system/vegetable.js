import request from '@/utils/request'

export const listCategories = () => request({ url: '/system/vegetable/categories', method: 'get' })
export const getCategory = id => request({ url: '/system/vegetable/category/' + id, method: 'get' })
export const addCategory = data => request({ url: '/system/vegetable/category', method: 'post', data })
export const updateCategory = data => request({ url: '/system/vegetable/category', method: 'put', data })
export const deleteCategory = id => request({ url: '/system/vegetable/category/' + id, method: 'delete' })
export const listRecords = query => request({ url: '/system/vegetable/records', method: 'get', params: query })
export const getRecord = id => request({ url: '/system/vegetable/record/' + id, method: 'get' })
export const addRecord = data => request({ url: '/system/vegetable/record', method: 'post', data })
export const updateRecord = data => request({ url: '/system/vegetable/record', method: 'put', data })
export const deleteRecord = id => request({ url: '/system/vegetable/record/' + id, method: 'delete' })
export const listGrowers = query => request({ url: '/system/vegetable/growers', method: 'get', params: query })
