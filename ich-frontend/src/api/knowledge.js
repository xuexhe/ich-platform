// src/api/knowledge.js
import request from '@/utils/request'

// 基础查询
export const getKnowledgeList = (params) => request.get('/knowledge/list', { params })
export const getKnowledgeDetail = (id) => request.get(`/knowledge/detail/${id}`)
export const getKnowledgeCategories = () => request.get('/knowledge/categories')

// 点赞功能
export const likeKnowledge = (knowledgeId, userId) => request.post('/knowledge/like', { knowledgeId, userId })
export const unlikeKnowledge = (knowledgeId, userId) => request.post('/knowledge/unlike', { knowledgeId, userId })
export const checkLiked = (knowledgeId, userId) => request.get(`/knowledge/liked/${knowledgeId}`, { params: { userId } })
export const getLikeCount = (knowledgeId) => request.get(`/knowledge/likeCount/${knowledgeId}`)

// 用户添加知识
export const addKnowledge = (data, userId) => request.post(`/knowledge/add?userId=${userId}`, data)
export const getUserKnowledge = (userId, params) => request.get(`/knowledge/user/${userId}`, { params })
export const updateKnowledge = (id, data, userId) => request.put(`/knowledge/update/${id}?userId=${userId}`, data)
export const deleteKnowledge = (id, userId) => request.delete(`/knowledge/delete/${id}?userId=${userId}`)