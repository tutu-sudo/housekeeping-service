import request from '@/utils/request'

// 获取用户信息
export function getUserInfo(userId) {
  return request({
    url: `/users/${userId}`,
    method: 'get'
  })
}

// 更新用户信息
export function updateUserInfo(userId, data) {
  return request({
    url: `/users/${userId}`,
    method: 'put',
    data
  })
}

// 上传头像
export function uploadAvatar(userId, file) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('category', 'avatar')
  formData.append('relatedId', userId)
  
  return request({
    url: '/files/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

