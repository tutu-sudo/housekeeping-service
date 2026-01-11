import request from '@/utils/request'

// 获取评价详情
export function getReviewDetail(reviewId) {
  return request({
    url: `/reviews/${reviewId}`,
    method: 'get'
  })
}

// 按预约查询评价
export function getReviewByAppointment(appointmentId) {
  return request({
    url: `/reviews/appointment/${appointmentId}`,
    method: 'get'
  })
}

// 按服务人员查询评价
export function getReviewsByStaff(staffId) {
  return request({
    url: `/reviews/staff/${staffId}`,
    method: 'get'
  })
}

// 提交评价
export function createReview(data) {
  return request({
    url: '/reviews',
    method: 'post',
    data
  })
}

// 更新评价
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

