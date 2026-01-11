import request from '@/utils/request'

// ========== 服务管理 ==========
// 服务类型管理
export function getAdminServiceTypes() {
  return request({
    url: '/admin/services/types',
    method: 'get'
  })
}

export function createServiceType(data) {
  return request({
    url: '/admin/services/types',
    method: 'post',
    data
  })
}

export function updateServiceType(serviceTypeId, data) {
  return request({
    url: `/admin/services/types/${serviceTypeId}`,
    method: 'put',
    data
  })
}

export function deleteServiceType(serviceTypeId) {
  return request({
    url: `/admin/services/types/${serviceTypeId}`,
    method: 'delete'
  })
}

// 服务项管理
export function getAdminServices(params) {
  return request({
    url: '/admin/services/items',
    method: 'get',
    params
  })
}

export function createService(data) {
  return request({
    url: '/admin/services/items',
    method: 'post',
    data
  })
}

export function updateService(serviceId, data) {
  return request({
    url: `/admin/services/items/${serviceId}`,
    method: 'put',
    data
  })
}

export function updateServiceStatus(serviceId, availableStatus) {
  return request({
    url: `/admin/services/items/${serviceId}/status`,
    method: 'patch',
    params: { availableStatus }
  })
}

export function deleteService(serviceId) {
  return request({
    url: `/admin/services/items/${serviceId}`,
    method: 'delete'
  })
}

// ========== 预约/订单管理 ==========
export function getAdminAppointmentDetail(appointmentId) {
  return request({
    url: `/admin/appointments/${appointmentId}`,
    method: 'get'
  })
}

export function getAdminAppointments(params) {
  return request({
    url: '/admin/appointments',
    method: 'get',
    params
  })
}

export function updateAdminAppointmentStatus(appointmentId, status) {
  return request({
    url: `/admin/appointments/${appointmentId}/status`,
    method: 'patch',
    params: { status }
  })
}

// ========== 支付管理 ==========
export function getPaymentDetail(paymentId) {
  return request({
    url: `/admin/payments/${paymentId}`,
    method: 'get'
  })
}

export function getPaymentByAppointment(appointmentId) {
  return request({
    url: `/admin/payments/appointment/${appointmentId}`,
    method: 'get'
  })
}

export function getPayments(params) {
  return request({
    url: '/admin/payments',
    method: 'get',
    params
  })
}

// ========== 评价管理 ==========
export function getAdminReviewDetail(reviewId) {
  return request({
    url: `/admin/reviews/${reviewId}`,
    method: 'get'
  })
}

export function getAdminReviews(params) {
  return request({
    url: '/admin/reviews',
    method: 'get',
    params
  })
}

export function approveReview(reviewId) {
  return request({
    url: `/admin/reviews/${reviewId}/approve`,
    method: 'patch'
  })
}

export function maskReview(reviewId, reason) {
  return request({
    url: `/admin/reviews/${reviewId}/mask`,
    method: 'patch',
    params: { reason }
  })
}

// ========== 排班管理 ==========
export function getScheduleDetail(scheduleId) {
  return request({
    url: `/admin/schedules/${scheduleId}`,
    method: 'get'
  })
}

export function getSchedules(params) {
  return request({
    url: '/admin/schedules',
    method: 'get',
    params
  })
}

export function createSchedule(data) {
  return request({
    url: '/admin/schedules',
    method: 'post',
    data
  })
}

export function updateSchedule(scheduleId, data) {
  return request({
    url: `/admin/schedules/${scheduleId}`,
    method: 'put',
    data
  })
}

export function deleteSchedule(scheduleId) {
  return request({
    url: `/admin/schedules/${scheduleId}`,
    method: 'delete'
  })
}

// ========== 公司信息管理 ==========
export function getCompanyDetail(companyId) {
  return request({
    url: `/admin/companies/${companyId}`,
    method: 'get'
  })
}

export function getCompanies() {
  return request({
    url: '/admin/companies',
    method: 'get'
  })
}

export function createCompany(data) {
  return request({
    url: '/admin/companies',
    method: 'post',
    data
  })
}

export function updateCompany(companyId, data) {
  return request({
    url: `/admin/companies/${companyId}`,
    method: 'put',
    data
  })
}

export function deleteCompany(companyId) {
  return request({
    url: `/admin/companies/${companyId}`,
    method: 'delete'
  })
}

// ========== 服务人员管理 ==========
export function getStaffList(params) {
  return request({
    url: '/admin/staff',
    method: 'get',
    params
  })
}

export function getStaffDetail(staffId) {
  return request({
    url: `/admin/staff/${staffId}`,
    method: 'get'
  })
}

export function updateStaffVerificationStatus(staffId, status) {
  return request({
    url: `/admin/staff/${staffId}/verification`,
    method: 'patch',
    params: { status }
  })
}

// ========== 数据统计分析 ==========
export function getStatisticsData(params) {
  return request({
    url: '/admin/statistics',
    method: 'get',
    params
  })
}

// 获取收入统计
export function getRevenueStatistics(params) {
  return request({
    url: '/admin/statistics/revenue',
    method: 'get',
    params
  })
}

// 获取订单统计
export function getOrderStatistics(params) {
  return request({
    url: '/admin/statistics/orders',
    method: 'get',
    params
  })
}

// 获取服务类型占比统计
export function getServiceTypeStatistics(params) {
  return request({
    url: '/admin/statistics/service-types',
    method: 'get',
    params
  })
}

// 获取服务质量分析
export function getQualityAnalysis(params) {
  return request({
    url: '/admin/statistics/quality',
    method: 'get',
    params
  })
}

// 获取客户行为分析
export function getCustomerBehaviorAnalysis(params) {
  return request({
    url: '/admin/statistics/customer-behavior',
    method: 'get',
    params
  })
}

