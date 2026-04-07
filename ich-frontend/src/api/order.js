// src/api/order.js
import request from '@/utils/request'

export const createOrder = (data) => request.post('/order/create', data)
export const payOrder = (orderId) => request.post('/order/pay', { orderId })
export const cancelOrder = (orderId) => request.post('/order/cancel', { orderId })
export const confirmOrder = (orderId) => request.post('/order/confirm', { orderId })
export const deleteOrder = (orderId, userId) => request.delete(`/order/delete/${orderId}`, { params: { userId } })
export const getOrders = (userId, params) => request.get(`/order/list/${userId}`, { params })
export const getOrderDetail = (orderId) => request.get(`/order/detail/${orderId}`)