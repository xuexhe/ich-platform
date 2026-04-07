import axios from 'axios'
import { ElMessage } from 'element-plus'

// 使用环境变量，更灵活
const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'

const request = axios.create({
  baseURL: baseURL,
  timeout: 60000  // 延长超时时间，更稳定
})

// 请求拦截器 - 添加token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      // console.log('请求携带token:', config.url)
    } else {
      // console.log('请求无token:', config.url)
    }
    return config
  },
  error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401) {
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        window.location.href = '/login'
      } else {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    console.error('响应错误:', error)
    
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('未登录，请先登录')
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('权限不足')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(error.response.data?.message || '网络错误')
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查后端是否启动')
    } else {
      ElMessage.error('请求配置错误')
    }
    return Promise.reject(error)
  }
)

export default request