// 预约状态常量（对应数据库status字段：0-待接单，1-已接单（待付款）、2-服务中，3-已完成，4/5-已拒单/已关闭）
export const APPOINTMENT_STATUS = {
  PENDING: 0, // 待接单（家政服务人员尚未接单）
  CONFIRMED: 1, // 已接单（等待用户支付）
  IN_PROGRESS: 2, // 服务中（已付款）
  COMPLETED: 3, // 已完成
  CANCELLED: 4, // 已拒单/已关闭（包含部分取消场景，前端统一展示为已拒单/已关闭）
  REJECTED: 5 // 已拒单/已关闭（拒单、超时关闭等）
}

// 预约状态显示配置
export const APPOINTMENT_STATUS_CONFIG = {
  [APPOINTMENT_STATUS.PENDING]: {
    text: '待接单',
    type: 'warning',
    color: '#E6A23C',
    actions: [] // 待接单阶段用户不能支付
  },
  [APPOINTMENT_STATUS.CONFIRMED]: {
    text: '已接单',
    type: 'warning',
    color: '#E6A23C',
    actions: ['pay', 'cancel']
  },
  [APPOINTMENT_STATUS.IN_PROGRESS]: {
    text: '服务中',
    type: 'primary',
    color: '#409EFF',
    actions: []
  },
  [APPOINTMENT_STATUS.COMPLETED]: {
    text: '已完成',
    type: 'success',
    color: '#67C23A',
    actions: ['review']
  },
  [APPOINTMENT_STATUS.CANCELLED]: {
    text: '已拒单/已关闭',
    type: 'danger',
    color: '#F56C6C',
    actions: []
  },
  [APPOINTMENT_STATUS.REJECTED]: {
    text: '已拒单/已关闭',
    type: 'danger',
    color: '#F56C6C',
    actions: []
  }
}

// 兼容旧的状态映射（用于向后兼容）
export const APPOINTMENT_STATUS_MAP = {
  pending: { text: '待确认', type: 'warning' },
  confirmed: { text: '已确认', type: 'success' },
  in_progress: { text: '进行中', type: 'primary' },
  completed: { text: '已完成', type: 'success' },
  cancelled: { text: '已取消', type: 'danger' },
  rejected: { text: '已拒绝', type: 'danger' }
}

// 支付状态常量（对应数据库payment_status字段：0-待支付，1-支付成功，2-支付失败，3-已退款）
export const PAYMENT_STATUS = {
  UNPAID: 0, // 待支付
  PAID: 1, // 支付成功
  FAILED: 2, // 支付失败
  REFUNDED: 3 // 已退款
}

// 支付状态显示配置（支持数字和字符串）
export const PAYMENT_STATUS_CONFIG = {
  // 数字状态
  0: { text: '待支付', type: 'warning' },
  1: { text: '已支付', type: 'success' },
  2: { text: '支付失败', type: 'danger' },
  3: { text: '已退款', type: 'info' },
  // 字符串状态（兼容）
  'unpaid': { text: '待支付', type: 'warning' },
  'paid': { text: '已支付', type: 'success' },
  'paying': { text: '支付中', type: 'info' },
  'failed': { text: '支付失败', type: 'danger' },
  'refunded': { text: '已退款', type: 'info' },
  'success': { text: '已支付', type: 'success' }
}

