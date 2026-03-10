import request from '@/utils/request'

// ========== 服务管理 ==========
// 服务类型管理 - 大类型
export function getMainCategories() {
  return request({
    url: '/admin/services/main-categories',
    method: 'get',
    skipErrorHandler: true // 跳过自动错误提示，因为有降级方案
  })
}

// 服务类型管理 - 小类型（子类型）
export function getSubTypes(parentTypeId) {
  return request({
    url: `/admin/services/sub-types/${parentTypeId}`,
    method: 'get'
  })
}

export function createSubType(data) {
  return request({
    url: '/admin/services/sub-types',
    method: 'post',
    data
  })
}

// 服务类型管理 - 通用接口（兼容旧代码）
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

// 服务内容管理（用于编辑服务详情页面内容）
export function getServiceContent(serviceId) {
  return request({
    url: `/admin/services/items/${serviceId}`,
    method: 'get'
  })
}

export function updateServiceContent(serviceId, data) {
  return request({
    url: `/admin/services/items/${serviceId}/content`,
    method: 'put',
    data
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

// 管理员新增对顾客/家政的评价（review_extra）
// Body: { appointmentId, reviewTarget, overallRating, reviewContent }
export function createAdminReviewExtra(data) {
  return request({
    url: '/admin/review-extras',
    method: 'post',
    data
  })
}

// 按预约获取三方评价包（管理员视角）
// 返回 AppointmentReviewBundleDTO：{ appointmentId, customerReview, staffSelfReview, adminReviewForCustomer, adminReviewForStaff }
export function getAdminAppointmentReviewBundle(appointmentId) {
  return request({
    url: `/admin/review-extras/appointment/${appointmentId}/bundle`,
    method: 'get'
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
// 人员状态监控 / 一般服务人员列表
export function getStaffList(params) {
  return request({
    url: '/admin/staff',
    method: 'get',
    params
  })
}

// 人员审核列表（服务人员申请）
export function getStaffApplications(params) {
  return request({
    url: '/admin/staff/applications',
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

// 证书审核列表
export function getStaffCertificates(params) {
  return request({
    url: '/admin/staff/certificates',
    method: 'get',
    params
  })
}

// 证书审核通过
export function approveStaffCertificate(skillId, remark) {
  return request({
    url: `/admin/staff/certificates/${skillId}/approve`,
    method: 'post',
    params: remark ? { remark } : {}
  })
}

// 证书审核驳回
export function rejectStaffCertificate(skillId, reason) {
  return request({
    url: `/admin/staff/certificates/${skillId}/reject`,
    method: 'post',
    params: reason ? { reason } : {}
  })
}

// 更新服务人员工作状态（正常 / 不可服务 / 警告 / 黑名单）
export function updateStaffWorkStatus(staffId, workStatus, reason) {
  return request({
    url: `/admin/staff/${staffId}/work-status`,
    method: 'patch',
    params: { workStatus, reason }
  })
}

// ========== 数据统计分析 ==========
// 仪表盘统计
export function getDashboardStatistics() {
  return request({
    url: '/admin/statistics/dashboard',
    method: 'get'
  })
}

// 预约统计 - 按日期
export function getAppointmentStatisticsByDate(params) {
  return request({
    url: '/admin/statistics/appointments/date',
    method: 'get',
    params
  })
}

// 预约统计 - 按服务类型
export function getAppointmentStatisticsByServiceType(params) {
  return request({
    url: '/admin/statistics/appointments/service-type',
    method: 'get',
    params
  })
}

// 预约统计 - 按状态
export function getAppointmentStatisticsByStatus(params) {
  return request({
    url: '/admin/statistics/appointments/status',
    method: 'get',
    params
  })
}

// 收入统计 - 按日期
export function getRevenueStatisticsByDate(params) {
  return request({
    url: '/admin/statistics/revenue/date',
    method: 'get',
    params
  })
}

// 收入统计 - 按支付方式
export function getRevenueStatisticsByPaymentMethod(params) {
  return request({
    url: '/admin/statistics/revenue/payment-method',
    method: 'get',
    params
  })
}

// 服务人员工作量统计
export function getStaffWorkloadStatistics(params) {
  return request({
    url: '/admin/statistics/staff/workload',
    method: 'get',
    params
  })
}

// 顾客活跃度统计
export function getCustomerActivityStatistics(params) {
  return request({
    url: '/admin/statistics/customers/activity',
    method: 'get',
    params
  })
}

// 服务质量统计 - 按服务类型
export function getQualityStatisticsByServiceType(params) {
  return request({
    url: '/admin/statistics/quality/service-type',
    method: 'get',
    params
  })
}

// 服务质量统计 - 按服务人员
export function getQualityStatisticsByStaff(params) {
  return request({
    url: '/admin/statistics/quality/staff',
    method: 'get',
    params
  })
}

// ========== 兼容旧接口（保留，避免影响现有代码） ==========
// 获取综合统计数据（兼容旧接口）
export function getStatisticsData(params) {
  return request({
    url: '/admin/statistics',
    method: 'get',
    params
  })
}

// 获取收入统计（兼容旧接口）
export function getRevenueStatistics(params) {
  return request({
    url: '/admin/statistics/revenue',
    method: 'get',
    params
  })
}

// 获取订单统计（兼容旧接口）
export function getOrderStatistics(params) {
  return request({
    url: '/admin/statistics/orders',
    method: 'get',
    params
  })
}

// 获取服务类型占比统计（兼容旧接口）
export function getServiceTypeStatistics(params) {
  return request({
    url: '/admin/statistics/service-types',
    method: 'get',
    params
  })
}

// 获取服务质量分析（兼容旧接口）
export function getQualityAnalysis(params) {
  return request({
    url: '/admin/statistics/quality',
    method: 'get',
    params
  })
}

// 获取客户行为分析（兼容旧接口）
export function getCustomerBehaviorAnalysis(params) {
  return request({
    url: '/admin/statistics/customer-behavior',
    method: 'get',
    params
  })
}

