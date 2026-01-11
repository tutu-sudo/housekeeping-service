import request from '@/utils/request'

// 登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 注册
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 刷新Token
export function refreshToken() {
  return request({
    url: '/auth/refresh',
    method: 'post'
  })
}

// 发送验证码（用于密码找回）
export function sendVerificationCode(phone) {
  return request({
    url: '/auth/send-code',
    method: 'post',
    data: { phone }
  })
}

// 验证验证码
export function verifyCode(phone, code) {
  return request({
    url: '/auth/verify-code',
    method: 'post',
    data: { phone, code }
  })
}

// 重置密码
export function resetPassword(data) {
  return request({
    url: '/auth/reset-password',
    method: 'post',
    data
  })
}

