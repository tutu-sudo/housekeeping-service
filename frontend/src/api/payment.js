import request from '@/utils/request'

// 创建支付订单
export function createPayment(data) {
  return request({
    url: '/payments',
    method: 'post',
    data
  })
}

// 获取支付详情
export function getPaymentDetail(paymentId) {
  return request({
    url: `/payments/${paymentId}`,
    method: 'get'
  })
}

// 获取订单支付信息
export function getPaymentByAppointment(appointmentId) {
  // 确保appointmentId是有效的数字
  const id = typeof appointmentId === 'string' ? parseInt(appointmentId, 10) : Number(appointmentId)
  if (isNaN(id) || id <= 0) {
    return Promise.reject(new Error('无效的预约ID'))
  }
  
  return request({
    url: `/payments/appointment/${id}`,
    method: 'get'
  })
}

// 支付回调（前端处理）
export function handlePaymentCallback(data) {
  return request({
    url: '/payments/callback',
    method: 'post',
    data
  })
}

// 查询支付状态
export function queryPaymentStatus(paymentId) {
  return request({
    url: `/payments/${paymentId}/status`,
    method: 'get'
  })
}

// ========== 支付宝支付相关接口 ==========

/**
 * 创建支付宝支付（GET方式，返回支付URL）
 * @param {number|string} appointmentId 预约ID
 * @returns {Promise} 返回支付URL
 */
export function createAlipayPayment(appointmentId) {
  // 确保appointmentId是有效的数字
  const id = typeof appointmentId === 'string' ? parseInt(appointmentId, 10) : Number(appointmentId)
  if (isNaN(id) || id <= 0) {
    return Promise.reject(new Error('无效的预约ID'))
  }
  
  return request({
    url: `/payment/alipay/create/${id}`,
    method: 'get'
  })
}

/**
 * 创建支付宝支付（POST方式，返回HTML表单）
 * @param {number|string} appointmentId 预约ID
 * @returns {Promise} 返回HTML字符串
 */
export function createAlipayPaymentForm(appointmentId) {
  // 确保appointmentId是有效的数字
  const id = typeof appointmentId === 'string' ? parseInt(appointmentId, 10) : Number(appointmentId)
  if (isNaN(id) || id <= 0) {
    return Promise.reject(new Error('无效的预约ID'))
  }
  
  return request({
    url: `/payment/alipay/create/${id}`,
    method: 'post',
    responseType: 'text' // 返回HTML文本
  })
}

/**
 * 查询支付状态（根据预约ID）
 * @param {number|string} appointmentId 预约ID
 * @returns {Promise} 返回支付状态信息
 */
export function queryPaymentStatusByAppointment(appointmentId) {
  // 确保appointmentId是有效的数字
  const id = typeof appointmentId === 'string' ? parseInt(appointmentId, 10) : Number(appointmentId)
  if (isNaN(id) || id <= 0) {
    return Promise.reject(new Error('无效的预约ID'))
  }
  
  return request({
    url: `/payment/status/${id}`,
    method: 'get'
  })
}

