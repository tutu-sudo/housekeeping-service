import request from '@/utils/request'

// 获取评价详情（顾客对订单的评价，旧接口）
export function getReviewDetail(reviewId) {
  return request({
    url: `/reviews/${reviewId}`,
    method: 'get'
  })
}

// 按预约查询顾客评价（旧接口，仅返回顾客评价）
export function getReviewByAppointment(appointmentId) {
  return request({
    url: `/reviews/appointment/${appointmentId}`,
    method: 'get'
  })
}

// 获取某预约的三方评价包（顾客视角）
// 返回 AppointmentReviewBundleDTO：{ appointmentId, customerReview, staffSelfReview, adminReviewForCustomer, adminReviewForStaff }
export function getAppointmentReviewBundle(appointmentId) {
  return request({
    url: `/reviews/appointment/${appointmentId}/bundle`,
    method: 'get'
  })
}

// 按服务人员查询评价（旧接口，按 staffId 查顾客对其的评价列表）
export function getReviewsByStaff(staffId) {
  return request({
    url: `/reviews/staff/${staffId}`,
    method: 'get'
  })
}

// 提交顾客评价
export function createReview(data) {
  return request({
    url: '/reviews',
    method: 'post',
    data
  })
}

// 更新顾客评价
export function updateReview(reviewId, data) {
  return request({
    url: `/reviews/${reviewId}`,
    method: 'put',
    data
  })
}

// 获取顾客的评价列表（根据customerId，旧接口，保留兼容）
export function getCustomerReviews(customerId) {
  return request({
    url: `/reviews/customer/${customerId}`,
    method: 'get'
  })
}

// 获取当前登录用户的评价列表（推荐使用，自动从token获取userId）
export function getMyReviews() {
  return request({
    url: '/reviews/my-reviews',
    method: 'get'
  })
}

// ===================== 服务人员端：自评与三方评价 =====================

// 提交服务人员自评
export function createStaffSelfReview(data) {
  return request({
    url: '/staff/reviews/self',
    method: 'post',
    data
  })
}

// 获取当前服务人员的自评列表（可选，用于“我的自评”列表）
export function getMySelfReviews(params) {
  return request({
    url: '/staff/reviews/my-self-reviews',
    method: 'get',
    params
  })
}

// 获取某预约的三方评价包（服务人员视角）
export function getStaffAppointmentReviewBundle(appointmentId) {
  return request({
    url: `/staff/reviews/appointment/${appointmentId}/bundle`,
    method: 'get'
  })
}
