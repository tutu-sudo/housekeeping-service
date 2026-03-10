import request from '@/utils/request'

// 获取当前登录用户的站内消息列表
export function getMessages(params) {
  return request({
    url: '/messages',
    method: 'get',
    params
  })
}

// 将消息标记为已读
export function markMessageAsRead(messageId) {
  return request({
    url: `/messages/${messageId}/read`,
    method: 'post'
  })
}

