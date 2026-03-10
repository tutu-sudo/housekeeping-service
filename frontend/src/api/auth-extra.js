import request from '@/utils/request'

// 修改密码（需登录）
export function changePassword(data) {
  return request({
    url: '/auth/change-password',
    method: 'post',
    data
  })
}

