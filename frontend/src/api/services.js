import request from '@/utils/request'

// 获取服务类型列表
export function getServiceTypes() {
  return request({
    url: '/services/types',
    method: 'get'
  })
}

// 按类型查询服务
export function getServicesByType(serviceTypeId) {
  return request({
    url: `/services/type/${serviceTypeId}`,
    method: 'get'
  })
}

// 获取可用服务列表
export function getAvailableServices() {
  return request({
    url: '/services/available',
    method: 'get'
  })
}

// 获取服务详情
export function getServiceDetail(serviceId) {
  return request({
    url: `/services/${serviceId}`,
    method: 'get'
  })
}

// 服务人员筛选
export function searchStaff(params) {
  return request({
    url: '/services/staff/search',
    method: 'get',
    params
  })
}

// 获取能提供该服务的人员
export function getStaffByService(serviceId) {
  return request({
    url: `/services/${serviceId}/staff`,
    method: 'get'
  })
}

