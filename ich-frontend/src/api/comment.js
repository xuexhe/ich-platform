import request from '@/utils/request'

export const addComment = (heritageId, userId, content) => 
  request.post('/comment/add', { heritageId, userId, content })
export const getComments = (heritageId) => request.get(`/comment/heritage/${heritageId}`)
export const likeComment = (commentId, userId) => request.post('/comment/like', { commentId, userId })
export const unlikeComment = (commentId, userId) => request.post('/comment/unlike', { commentId, userId })
export const addReply = (data) => request.post('/comment/reply', data)
export const deleteComment = (commentId, userId) => 
  request.delete(`/comment/delete/${commentId}?userId=${userId}`)
export const deleteReply = (replyId, userId) => 
  request.delete(`/comment/reply/delete/${replyId}?userId=${userId}`)