import request from '@/utils/request'

export const addCollect = (userId, heritageId) => request.post('/collect/add', { userId, heritageId })
export const cancelCollect = (userId, heritageId) => request.post('/collect/cancel', { userId, heritageId })
export const getMyCollects = (userId) => request.get(`/collect/my/${userId}`)