import request from '@/utils/request'

// ==================== 服务人员专用日程管理接口 ====================

// 获取可服务时间列表
// 支持按日期范围和状态筛选
export function getSchedules(params) {
  return request({
    url: '/staff/schedules',
    method: 'get',
    params
  })
}

// 获取可服务时间详情
export function getScheduleDetail(scheduleId) {
  return request({
    url: `/staff/schedules/${scheduleId}`,
    method: 'get'
  })
}

// 添加可服务时间
export function createSchedule(data) {
  return request({
    url: '/staff/schedules',
    method: 'post',
    data
  })
}

// 更新可服务时间
export function updateSchedule(scheduleId, data) {
  return request({
    url: `/staff/schedules/${scheduleId}`,
    method: 'put',
    data
  })
}

// 删除可服务时间
export function deleteSchedule(scheduleId) {
  return request({
    url: `/staff/schedules/${scheduleId}`,
    method: 'delete'
  })
}
