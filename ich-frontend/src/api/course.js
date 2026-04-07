// src/api/course.js
import request from '@/utils/request'

export const getCourseList = (params) => request.get('/course/list', { params })
export const getCourseDetail = (id) => request.get(`/course/detail/${id}`)
export const getHotCourses = (limit = 6) => request.get('/course/hot', { params: { limit } })
export const getCourseCategories = () => request.get('/course/categories')
export const updateCourseProgress = (data) => request.post('/course/progress', data)
export const getCourseProgress = (userId, courseId) => request.get(`/course/progress/${userId}/${courseId}`)
export const getUserCourseStats = (userId) => request.get(`/course/stats/${userId}`)