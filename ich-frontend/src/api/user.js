import request from '@/utils/request'

export const login = (data) => request.post('/user/login', data)
export const register = (data) => request.post('/user/register', data)
export const getUserInfo = (id) => request.get(`/user/info/${id}`)
export const updateUser = (data) => request.put('/user/update', data)

// 新增：上传头像
export const uploadAvatar = (userId, file) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('userId', userId)
  return request.post('/user/uploadAvatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}