// src/api/ai.js
import request from '@/utils/request'

// AI 对话
export const aiChat = (question, userId) => request.post('/ai/chat', { question, userId })

// 获取 AI 服务状态
export const getAIStatus = () => request.get('/ai/status')

// 获取可用模型列表
export const getAIModels = () => request.get('/ai/models')

// 获取用户对话历史
export const getAIHistory = (userId, limit = 20) => request.get(`/ai/history/${userId}`, { params: { limit } })

// 清空用户对话历史
export const clearAIHistory = (userId) => request.delete(`/ai/history/clear/${userId}`)

// 删除单条对话记录
export const deleteAIHistory = (id, userId) => request.delete(`/ai/history/delete/${id}?userId=${userId}`)

// 获取非遗专属提示词
export const getAIPrompts = () => request.get('/ai/prompts')