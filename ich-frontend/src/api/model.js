// src/api/model.js
import request from '@/utils/request'

// 前台接口
export const getModelList = () => request.get('/model/list')
export const getModelDetail = (id) => request.get(`/model/detail/${id}`)

// 上传接口
export const uploadModelFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/model/upload/model', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
}

export const uploadModelCover = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/model/upload/cover', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}