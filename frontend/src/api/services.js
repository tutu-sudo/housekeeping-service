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
  // 确保 serviceTypeId 是数字类型
  const id = typeof serviceTypeId === 'string' ? parseInt(serviceTypeId, 10) : Number(serviceTypeId)
  if (isNaN(id) || id <= 0) {
    return Promise.reject(new Error('无效的服务类型ID'))
  }
  return request({
    url: `/services/type/${id}`,
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
  // 确保 serviceId 是数字类型
  const id = typeof serviceId === 'string' ? parseInt(serviceId, 10) : Number(serviceId)
  if (isNaN(id) || id <= 0) {
    return Promise.reject(new Error('无效的服务ID'))
  }
  return request({
    url: `/services/${id}`,
    method: 'get'
  })
}

// 服务人员筛选
export function searchStaff(params) {
  // 确保 serviceId 是数字类型
  const processedParams = { ...params }
  if (processedParams.serviceId !== undefined && processedParams.serviceId !== null) {
    const id = typeof processedParams.serviceId === 'string' 
      ? parseInt(processedParams.serviceId, 10) 
      : Number(processedParams.serviceId)
    if (!isNaN(id) && id > 0) {
      processedParams.serviceId = id
    } else {
      // 如果 serviceId 无效，移除该参数
      delete processedParams.serviceId
    }
  }
  return request({
    url: '/services/staff/search',
    method: 'get',
    params: processedParams
  })
}

// 获取能提供该服务的人员
export function getStaffByService(serviceId) {
  // 确保 serviceId 是数字类型
  const id = typeof serviceId === 'string' ? parseInt(serviceId, 10) : Number(serviceId)
  if (isNaN(id) || id <= 0) {
    return Promise.reject(new Error('无效的服务ID'))
  }
  return request({
    url: `/services/${id}/staff`,
    method: 'get'
  })
}

