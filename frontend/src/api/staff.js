import request from '@/utils/request'

// ==================== 个人资料相关接口 ====================

// 获取服务人员个人资料（包含用户信息和服务人员详细信息）
export function getStaffProfile() {
  return request({
    url: '/staff/profile',
    method: 'get'
  })
}

// 更新基本信息
// 可更新：姓名、性别、出生日期、籍贯、身份证号、工作经验、手机号、邮箱
export function updateStaffBasicInfo(data) {
  return request({
    url: '/staff/profile/basic',
    method: 'put',
    data
  })
}

// 更新简历
// 可更新个人简介、工作经历等
export function updateStaffResume(data) {
  return request({
    url: '/staff/profile/resume',
    method: 'put',
    data
  })
}

// 更新头像
export function updateStaffAvatar(avatarUrl) {
  return request({
    url: '/staff/profile/avatar',
    method: 'put',
    data: { avatar: avatarUrl }
  })
}

// ==================== 服务技能相关接口 ====================

// 获取服务技能列表
export function getStaffSkills() {
  return request({
    url: '/staff/skills',
    method: 'get'
  })
}

// 添加服务技能
export function addStaffSkill(data) {
  return request({
    url: '/staff/skills',
    method: 'post',
    data
  })
}

// 更新服务技能
export function updateStaffSkill(skillId, data) {
  return request({
    url: `/staff/skills/${skillId}`,
    method: 'put',
    data
  })
}

// 删除服务技能
export function deleteStaffSkill(skillId) {
  return request({
    url: `/staff/skills/${skillId}`,
    method: 'delete'
  })
}

// ==================== 兼容旧接口（保留用于其他页面） ====================

// 获取服务人员详情（用于顾客端查看）
// 注意：此API可能不存在，调用时会静默失败，不会显示错误消息
export function getStaffDetail(staffId) {
  return request({
    url: `/staff/${staffId}`,
    method: 'get',
    skipErrorHandler: true // 静默失败，不显示全局错误消息
  })
}

// 更新服务人员基本信息（兼容旧接口）
export function updateStaffInfo(staffId, data) {
  return request({
    url: `/staff/${staffId}`,
    method: 'put',
    data
  })
}

// 获取服务人员技能列表（兼容旧接口）
export function getStaffSkillsByStaffId(staffId) {
  return request({
    url: `/staff/${staffId}/skills`,
    method: 'get'
  })
}

// 添加服务技能（兼容旧接口）
export function addStaffSkillByStaffId(staffId, data) {
  return request({
    url: `/staff/${staffId}/skills`,
    method: 'post',
    data
  })
}

// 更新服务技能（兼容旧接口）
export function updateStaffSkillByStaffId(staffId, skillId, data) {
  return request({
    url: `/staff/${staffId}/skills/${skillId}`,
    method: 'put',
    data
  })
}

// 删除服务技能（兼容旧接口）
export function deleteStaffSkillByStaffId(staffId, skillId) {
  return request({
    url: `/staff/${staffId}/skills/${skillId}`,
    method: 'delete'
  })
}
