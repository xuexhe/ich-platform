import request from '@/utils/request'

export const getInheritorList = () => request.get('/inheritor/list')
export const getInheritorDetail = (id) => request.get(`/inheritor/detail/${id}`)