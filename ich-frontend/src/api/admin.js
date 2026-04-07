// src/api/admin.js
import request from '@/utils/request'

// ========== 管理员登录 ==========
export const adminLogin = (data) => request.post('/admin/login', data)
export const adminCheck = () => request.get('/admin/check')

// 非遗管理
export const adminGetHeritageList = (params) => request.get('/admin/heritage/list', { params })
export const adminSaveHeritage = (data) => request.post('/admin/heritage/save', data)
export const adminDeleteHeritage = (id) => request.delete(`/admin/heritage/delete/${id}`)
export const adminGetHeritageDetail = (id) => request.get(`/admin/heritage/detail/${id}`)

// 传承人管理
export const adminGetInheritorList = (params) => request.get('/admin/inheritor/list', { params })
export const adminSaveInheritor = (data) => request.post('/admin/inheritor/save', data)
export const adminDeleteInheritor = (id) => request.delete(`/admin/inheritor/delete/${id}`)

// 商品管理
export const adminGetProductList = (params) => request.get('/admin/product/list', { params })
export const adminSaveProduct = (data) => request.post('/admin/product/save', data)
export const adminDeleteProduct = (id) => request.delete(`/admin/product/delete/${id}`)

// 评论管理
export const adminGetCommentList = (params) => request.get('/admin/comment/list', { params })
export const adminAuditComment = (id, status) => request.post(`/admin/comment/audit/${id}?status=${status}`)
export const adminDeleteComment = (id) => request.delete(`/admin/comment/delete/${id}`)

// 用户管理
export const adminGetUserList = (params) => request.get('/admin/user/list', { params })
export const adminUpdateUserStatus = (id, status) => request.post(`/admin/user/status/${id}?status=${status}`)

// 分类管理
export const adminGetCategoryList = (params) => request.get('/admin/category/list', { params })
export const adminGetAllCategories = () => request.get('/admin/category/all')
export const adminSaveCategory = (data) => request.post('/admin/category/save', data)
export const adminDeleteCategory = (id) => request.delete(`/admin/category/delete/${id}`)

// 统计
export const adminGetStatistics = () => request.get('/admin/statistics')

// 课程管理
export const adminGetCourseList = (params) => request.get('/admin/course/list', { params })
export const adminSaveCourse = (data) => request.post('/admin/course/save', data)
export const adminDeleteCourse = (id) => request.delete(`/admin/course/delete/${id}`)

// 添加模型管理接口
export const adminGetModelList = (params) => request.get('/admin/model/list', { params })
export const adminSaveModel = (data) => request.post('/admin/model/save', data)
export const adminDeleteModel = (id) => request.delete(`/admin/model/delete/${id}`)
export const adminGetModelDetail = (id) => request.get(`/admin/model/detail/${id}`)

// 获取待审核知识列表
export const adminGetPendingKnowledge = (params) => request.get('/admin/knowledge/pending', { params })

// 审核知识（1:通过, 2:驳回）
export const adminAuditKnowledge = (id, status) => request.post(`/admin/knowledge/audit/${id}?status=${status}`)

// 每日一题管理
export const adminGetQuizList = (params) => request.get('/admin/quiz/list', { params })
export const adminSaveQuiz = (data) => request.post('/admin/quiz/save', data)
export const adminDeleteQuiz = (id) => request.delete(`/admin/quiz/delete/${id}`)