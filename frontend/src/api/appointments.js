import request from '@/utils/request'

// 获取预约详情
export function getAppointmentDetail(appointmentId) {
  // 确保appointmentId是有效的数字
  const id = typeof appointmentId === 'string' ? parseInt(appointmentId, 10) : Number(appointmentId)
  if (isNaN(id) || id <= 0) {
    return Promise.reject(new Error('无效的预约ID'))
  }
  
  return request({
    url: `/appointments/${id}`,
    method: 'get'
  })
}

// 获取顾客预约列表（旧接口，保留兼容）
export function getCustomerAppointments(customerId) {
  return request({
    url: `/appointments/customer/${customerId}`,
    method: 'get'
  })
}

// 获取当前登录用户的预约列表（推荐使用，自动从token获取userId）
export function getMyAppointments(params) {
  return request({
    url: '/appointments/my-appointments',
    method: 'get',
    params
  })
}

// 获取服务人员预约列表
export function getStaffAppointments(staffId) {
  return request({
    url: `/appointments/staff/${staffId}`,
    method: 'get'
  })
}

// 创建预约
export function createAppointment(data) {
  return request({
    url: '/appointments',
    method: 'post',
    data
  })
}

// 修改预约
export function updateAppointment(appointmentId, data) {
  return request({
    url: `/appointments/${appointmentId}`,
    method: 'put',
    data
  })
}

// 修改预约状态
export function updateAppointmentStatus(appointmentId, status) {
  return request({
    url: `/appointments/${appointmentId}/status`,
    method: 'patch',
    params: { status }
  })
}

// ==================== 服务人员专用预约管理接口 ====================

// 获取服务人员预约列表（支持按状态筛选）
export function getStaffAppointmentsList(params) {
  return request({
    url: '/staff/appointments',
    method: 'get',
    params
  })
}

// 获取预约详情
export function getStaffAppointmentDetail(appointmentId) {
  return request({
    url: `/staff/appointments/${appointmentId}`,
    method: 'get'
  })
}

// 接单（确认预约）
export function acceptAppointment(appointmentId) {
  return request({
    url: `/staff/appointments/${appointmentId}/accept`,
    method: 'post'
  })
}

// 拒绝预约
export function rejectAppointment(appointmentId) {
  return request({
    url: `/staff/appointments/${appointmentId}/reject`,
    method: 'post'
  })
}

// 开始服务
export function startAppointment(appointmentId) {
  return request({
    url: `/staff/appointments/${appointmentId}/start`,
    method: 'post'
  })
}

// 完成服务
export function completeAppointment(appointmentId) {
  return request({
    url: `/staff/appointments/${appointmentId}/complete`,
    method: 'post'
  })
}

