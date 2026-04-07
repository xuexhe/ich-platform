import request from '@/utils/request'

export const getHeritageList = (params) => request.get('/heritage/list', { params })
export const getHeritageDetail = (id) => request.get(`/heritage/detail/${id}`)
export const getCategories = () => request.get('/heritage/categories')
export const getHotHeritage = (limit = 6) => request.get('/heritage/hot', { params: { limit } })