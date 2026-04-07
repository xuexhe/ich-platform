// src/api/achievement.js
import request from '@/utils/request'

export const getAllAchievements = () => request.get('/achievement/list')
export const getUserAchievements = (userId) => request.get(`/achievement/user/${userId}`)
export const getUserAchievementStats = (userId) => request.get(`/achievement/stats/${userId}`)