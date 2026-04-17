import { PAYMENT_STATUS, PAYMENT_STATUS_CONFIG } from '@/utils/constants'

// 统一支付状态口径：兼容 paymentStatus / payment_status / payStatus / pay_status
export const normalizePaymentStatus = (input) => {
  const raw = typeof input === 'object' && input !== null
    ? (input.paymentStatus ?? input.payment_status ?? input.payStatus ?? input.pay_status)
    : input

  if (raw === null || raw === undefined || raw === '') return PAYMENT_STATUS.UNPAID
  if (typeof raw === 'number') return raw

  if (typeof raw === 'string') {
    const v = raw.trim().toLowerCase()
    if (v === '') return PAYMENT_STATUS.UNPAID
    if (/^\d+$/.test(v)) return Number(v)
    if (v === 'paid' || v === 'success') return PAYMENT_STATUS.PAID
    if (v === 'unpaid' || v === 'pending' || v === 'paying') return PAYMENT_STATUS.UNPAID
    if (v === 'failed') return PAYMENT_STATUS.FAILED
    if (v === 'refunded') return PAYMENT_STATUS.REFUNDED
  }

  return raw
}

export const isPaidPaymentStatus = (input) =>
  normalizePaymentStatus(input) === PAYMENT_STATUS.PAID

export const isUnpaidPaymentStatus = (input) =>
  normalizePaymentStatus(input) === PAYMENT_STATUS.UNPAID

export const getPaymentStatusType = (input) =>
  PAYMENT_STATUS_CONFIG[normalizePaymentStatus(input)]?.type || 'info'

export const getPaymentStatusText = (input) =>
  PAYMENT_STATUS_CONFIG[normalizePaymentStatus(input)]?.text || '未知'

export const normalizePaymentFields = (row) => {
  if (!row || typeof row !== 'object') return row
  const paymentStatus = normalizePaymentStatus(row)
  return {
    ...row,
    paymentStatus,
    payment_status: paymentStatus
  }
}
