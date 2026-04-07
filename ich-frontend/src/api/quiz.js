// src/api/quiz.js
import request from '@/utils/request'

// 获取今日题目
export const getTodayQuiz = (userId) => request.get('/quiz/today', { params: { userId } })

// 提交答案
export const submitAnswer = (data) => request.post('/quiz/submit', data)

// 获取用户答题统计
export const getUserQuizStats = (userId) => request.get(`/quiz/stats/${userId}`)