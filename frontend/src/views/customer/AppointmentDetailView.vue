<template>
  <div class="appointment-detail-view">
    <Navigation />
    <div class="content">
      <el-container>
        <el-main>
          <el-card>
            <template #header>
              <div class="header-actions">
                <h2>预约详情</h2>
                <el-button v-if="canEdit" @click="handleEdit">修改预约</el-button>
              </div>
            </template>
            
            <div v-if="appointment" class="detail-content">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="预约编号">
                  {{ appointment.id }}
                </el-descriptions-item>
                <el-descriptions-item label="预约状态">
                  <el-tag :type="getStatusType(appointment.status)" size="large">
                    {{ getStatusText(appointment.status) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="服务项目">
                  {{ appointment.serviceName || (appointment.serviceId ? `服务#${appointment.serviceId}` : '-') }}
                </el-descriptions-item>
                <el-descriptions-item label="服务人员">
                  {{ appointment.staffName || (appointment.staffId ? `员工#${appointment.staffId}` : '-') }}
                </el-descriptions-item>
                <el-descriptions-item label="预约日期">
                  {{ appointment.appointmentDate || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="预约时间">
                  {{ appointment.appointmentTime || (appointment.startTime && appointment.endTime ? `${appointment.startTime} - ${appointment.endTime}` : '-') }}
                </el-descriptions-item>
                <el-descriptions-item label="服务地址" :span="2" v-if="appointment.serviceAddress">
                  {{ appointment.serviceAddress }}
                </el-descriptions-item>
                <el-descriptions-item label="订单金额">
                  ¥{{ (appointment.totalAmount || 0).toFixed(2) }}
                </el-descriptions-item>
                <el-descriptions-item label="支付状态">
                  <div style="display: flex; align-items: center; gap: 10px;">
                  <el-tag :type="getPaymentStatusType(appointment.paymentStatus)">
                    {{ getPaymentStatusText(appointment.paymentStatus) }}
                  </el-tag>
                    <el-link
                      v-if="canPay && !isPaymentExpired"
                      type="primary"
                      :underline="false"
                      @click="goToPayment"
                      style="font-size: 14px;"
                    >
                      <el-icon><CreditCard /></el-icon> 立即支付
                    </el-link>
                    <el-tooltip
                      v-if="canPay && isPaymentExpired"
                      content="订单已过期，无法支付"
                      placement="top"
                    >
                      <el-tag type="danger" size="small">已过期</el-tag>
                    </el-tooltip>
                  </div>
                  <div v-if="canPay && !isPaymentExpired && paymentDeadline" style="margin-top: 5px; font-size: 12px; color: #909399;">
                    请在 {{ formatPaymentDeadline(paymentDeadline) }} 前完成支付
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="备注" :span="2">
                  {{ appointment.notes || appointment.specialRequirements || '无' }}
                </el-descriptions-item>
                <el-descriptions-item label="待付款" :span="2" v-if="canPay && !isPaymentExpired">
                  <el-link type="primary" :underline="false" @click="goToPayment" style="font-size: 14px;">
                    <el-icon><CreditCard /></el-icon> 前往付款页面
                  </el-link>
                  <span style="margin-left: 10px; color: #909399; font-size: 12px;">
                    （请在 {{ formatPaymentDeadline(paymentDeadline) }} 前完成支付）
                  </span>
                </el-descriptions-item>
              </el-descriptions>
              
              <!-- 三方互评展示：顾客 / 服务人员自评 / 管理员评价 -->
              <el-divider />

              <h3 style="margin-bottom: 10px;">本次服务评价</h3>

              <!-- 顾客评价（当前登录顾客对本单的评价） -->
              <h4 style="margin: 6px 0;">我的评价</h4>
              <div v-if="customerReviewBlock">
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="总体评分">
                    <el-rate v-model="customerReviewBlock.overallRating" disabled show-score text-color="#ff9900" />
                  </el-descriptions-item>
                  <el-descriptions-item label="评价时间">
                    {{ customerReviewBlock.reviewTime || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="服务态度" v-if="customerReviewBlock.serviceAttitudeRating !== null && customerReviewBlock.serviceAttitudeRating !== undefined">
                    <el-rate v-model="customerReviewBlock.serviceAttitudeRating" disabled />
                    <span style="margin-left:8px;color:#666;">{{ customerReviewBlock.serviceAttitudeRating }}分</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="专业能力" v-if="customerReviewBlock.professionalAbilityRating !== null && customerReviewBlock.professionalAbilityRating !== undefined">
                    <el-rate v-model="customerReviewBlock.professionalAbilityRating" disabled />
                    <span style="margin-left:8px;color:#666;">{{ customerReviewBlock.professionalAbilityRating }}分</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="服务质量" v-if="customerReviewBlock.serviceQualityRating !== null && customerReviewBlock.serviceQualityRating !== undefined">
                    <el-rate v-model="customerReviewBlock.serviceQualityRating" disabled />
                    <span style="margin-left:8px;color:#666;">{{ customerReviewBlock.serviceQualityRating }}分</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="评价内容" :span="2">
                    {{ customerReviewBlock.reviewContent || '（无）' }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>
              <el-alert
                v-else
                title="您还没有对本次服务进行评价，可以在订单完成后点击“评价服务”进行评价。"
                type="info"
                :closable="false"
                show-icon
                style="margin-bottom: 16px;"
              />

              <!-- 服务人员自评 -->
              <h4 style="margin: 6px 0;">服务人员自评</h4>
              <div v-if="staffSelfReviewBlock">
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="总体评分">
                    <el-rate v-model="staffSelfReviewBlock.overallRating" disabled show-score text-color="#ff9900" />
                  </el-descriptions-item>
                  <el-descriptions-item label="评价时间">
                    {{ staffSelfReviewBlock.reviewTime || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="自评内容" :span="2">
                    {{ staffSelfReviewBlock.reviewContent || '（无）' }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>
              <el-empty
                v-else
                description="服务人员暂未对本次服务进行自评"
                style="margin-bottom: 16px;"
              />

              <!-- 管理员评价（公司视角） -->
              <h4 style="margin: 6px 0;">平台评价</h4>
              <div class="admin-review-blocks">
                <div class="admin-review-item">
                  <h5>平台对顾客的评价</h5>
                  <div v-if="adminReviewForCustomerBlock">
                    <el-descriptions :column="2" border>
                      <el-descriptions-item label="总体评分">
                        <el-rate v-model="adminReviewForCustomerBlock.overallRating" disabled show-score text-color="#ff9900" />
                      </el-descriptions-item>
                      <el-descriptions-item label="评价时间">
                        {{ adminReviewForCustomerBlock.reviewTime || '-' }}
                      </el-descriptions-item>
                      <el-descriptions-item label="评价内容" :span="2">
                        {{ adminReviewForCustomerBlock.reviewContent || '（无）' }}
                      </el-descriptions-item>
                    </el-descriptions>
                  </div>
                  <el-empty v-else description="平台暂未对您本单进行评价" />
                </div>

                <div class="admin-review-item">
                  <h5>平台对服务人员的评价</h5>
                  <div v-if="adminReviewForStaffBlock">
                    <el-descriptions :column="2" border>
                      <el-descriptions-item label="总体评分">
                        <el-rate v-model="adminReviewForStaffBlock.overallRating" disabled show-score text-color="#ff9900" />
                      </el-descriptions-item>
                      <el-descriptions-item label="评价时间">
                        {{ adminReviewForStaffBlock.reviewTime || '-' }}
                      </el-descriptions-item>
                      <el-descriptions-item label="评价内容" :span="2">
                        {{ adminReviewForStaffBlock.reviewContent || '（无）' }}
                      </el-descriptions-item>
                    </el-descriptions>
                  </div>
                  <el-empty v-else description="平台暂未对本次服务进行评价" />
                </div>
              </div>
              
              <!-- 状态流转提示（基于新的数值状态含义） -->
              <el-alert
                v-if="appointment.status === 0"
                title="预约成功，等待家政服务人员接单"
                type="info"
                :closable="false"
                show-icon
                style="margin: 20px 0;"
              />

              <el-alert
                v-else-if="appointment.status === 1 && !isPaymentExpired"
                :title="`服务人员已接单，请在 ${PAYMENT_EXPIRE_MINUTES} 分钟内完成支付`"
                type="warning"
                :closable="false"
                show-icon
                style="margin: 20px 0;"
              >
                <template #default>
                  <div>请在 {{ formatPaymentDeadline(paymentDeadline) }} 前完成支付，逾期订单将自动关闭。</div>
                </template>
              </el-alert>

              <el-alert
                v-else-if="appointment.status === 1 && isPaymentExpired"
                title="支付超时，订单已关闭"
                type="error"
                :closable="false"
                show-icon
                style="margin: 20px 0;"
              >
                <template #default>
                  <div>该订单已超过支付有效期（{{ PAYMENT_EXPIRE_MINUTES }}分钟），如需继续服务请重新下单。</div>
                </template>
              </el-alert>
              
              <el-alert
                v-else-if="appointment.status === 2 && (appointment.paymentStatus === 1 || appointment.paymentStatus === 'paid' || appointment.paymentStatus === 'success')"
                title="已付款，等待服务开始或服务进行中"
                type="success"
                :closable="false"
                show-icon
                style="margin: 20px 0;"
              />
              
              <el-alert
                v-else-if="appointment.status === 3 && (appointment.paymentStatus === 1 || appointment.paymentStatus === 'paid' || appointment.paymentStatus === 'success')"
                title="服务已完成，感谢您的使用"
                type="success"
                :closable="false"
                show-icon
                style="margin: 20px 0;"
              />
              
              <!-- 状态不一致的提示：已完成但未支付 -->
              <el-alert
                v-else-if="appointment.status === 3 && appointment.paymentStatus !== 1 && appointment.paymentStatus !== 'paid' && appointment.paymentStatus !== 'success'"
                title="订单状态异常：服务已完成但未支付，请联系客服"
                type="warning"
                :closable="false"
                show-icon
                style="margin: 20px 0;"
              />
              
              <!-- 状态不一致的提示：服务中但未支付 -->
              <el-alert
                v-else-if="appointment.status === 2 && appointment.paymentStatus !== 1 && appointment.paymentStatus !== 'paid' && appointment.paymentStatus !== 'success'"
                title="订单状态异常：服务中但未支付，请联系客服"
                type="warning"
                :closable="false"
                show-icon
                style="margin: 20px 0;"
              />
              
              <el-alert
                v-else-if="appointment.status === 4"
                title="订单已取消"
                type="info"
                :closable="false"
                show-icon
                style="margin: 20px 0;"
              />
              
              <el-alert
                v-else-if="appointment.status === 5"
                title="订单已关闭（服务人员拒单或超时未支付）"
                type="warning"
                :closable="false"
                show-icon
                style="margin: 20px 0;"
              />
              
              <el-alert
                v-if="appointment.status === 3 && !appointment.reviewed && (appointment.paymentStatus === 1 || appointment.paymentStatus === 'paid' || appointment.paymentStatus === 'success')"
                title="服务已完成，请对本次服务进行评价"
                type="success"
                :closable="false"
                show-icon
                style="margin: 20px 0;"
              />
              
              <!-- 操作按钮 -->
              <div class="actions">
                <el-button
                  v-if="canPay && !isPaymentExpired"
                  type="primary"
                  size="large"
                  @click="goToPayment"
                >
                  <el-icon><CreditCard /></el-icon> 立即支付
                </el-button>
                
                <el-button
                  v-if="canPay && isPaymentExpired"
                  type="danger"
                  size="large"
                  disabled
                >
                  订单已过期
                </el-button>
                
                <el-button
                  v-if="canEdit"
                  @click="handleEdit"
                >
                  修改预约
                </el-button>
                
                <el-button
                  v-if="canCancel"
                  @click="handleCancel"
                >
                  取消预约
                </el-button>
                
                <el-button
                  v-if="canReview"
                  type="success"
                  size="large"
                  @click="goToReview"
                >
                  评价服务
                </el-button>
                
                <el-button
                  v-if="appointment.reviewed"
                  @click="viewReview"
                >
                  查看评价
                </el-button>
              </div>
              
              <!-- 状态流转时间线 -->
              <el-timeline v-if="appointment.statusHistory" style="margin-top: 30px;">
                <el-timeline-item
                  v-for="(history, index) in appointment.statusHistory"
                  :key="index"
                  :timestamp="history.timestamp"
                  placement="top"
                >
                  <el-card>
                    <h4>{{ history.statusText }}</h4>
                    <p>{{ history.description }}</p>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-card>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAppointmentDetail, updateAppointmentStatus } from '@/api/appointments'
import * as reviewsApi from '@/api/reviews'
import { getServiceDetail } from '@/api/services'
import { getStaffDetail } from '@/api/staff'
import { queryPaymentStatusByAppointment } from '@/api/payment'
import { APPOINTMENT_STATUS_CONFIG } from '@/utils/constants'
import {
  normalizePaymentStatus,
  isPaidPaymentStatus,
  isUnpaidPaymentStatus,
  getPaymentStatusType as getUnifiedPaymentStatusType,
  getPaymentStatusText as getUnifiedPaymentStatusText
} from '@/utils/payment'
import { normalizeVisibleReview } from '@/utils/review'
import Navigation from '@/components/common/Navigation.vue'
import { CreditCard } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

const appointment = ref(null)
let statusPollingTimer = null
const customerReviewBlock = ref(null)
const staffSelfReviewBlock = ref(null)
const adminReviewForCustomerBlock = ref(null)
const adminReviewForStaffBlock = ref(null)

const canPay = computed(() => {
  if (!appointment.value) return false

  // 只有当订单处于「待付款」状态（1），且支付状态为未支付时，才允许用户发起支付。
  // 是否真正允许支付（包括是否超时等），仍由后端的 validatePayable 做最终校验。
  const rawStatus = appointment.value.status
  const status = typeof rawStatus === 'string' ? parseInt(rawStatus, 10) : rawStatus
  return status === 1 && isUnpaidPaymentStatus(appointment.value.paymentStatus)
})

// 支付过期时间（创建订单后10分钟内需支付，可根据实际业务调整）
const PAYMENT_EXPIRE_MINUTES = 10

// 计算支付截止时间：从服务人员「接单时间」开始计时10分钟
const paymentDeadline = computed(() => {
  if (!appointment.value) return null
  if (!canPay.value) return null
  
  try {
    // 优先使用后端返回的 acceptTime / accept_time，没有则退回到 createTime
    const baseTimeStr =
      appointment.value.acceptTime ||
      appointment.value.accept_time ||
      appointment.value.createTime ||
      appointment.value.create_time ||
      appointment.value.createDate ||
      appointment.value.create_date

    if (!baseTimeStr) return null

    const baseTime = dayjs(baseTimeStr)
    if (!baseTime.isValid()) return null
    return baseTime.add(PAYMENT_EXPIRE_MINUTES, 'minute')
  } catch (error) {
    console.warn('计算支付截止时间失败:', error)
    return null
  }
})

// 判断是否已过期
const isPaymentExpired = computed(() => {
  if (!paymentDeadline.value) return false
  return dayjs().isAfter(paymentDeadline.value)
})

// 格式化支付截止时间
const formatPaymentDeadline = (deadline) => {
  if (!deadline) return ''
  return deadline.format('YYYY-MM-DD HH:mm:ss')
}

const canReview = computed(() => {
  if (!appointment.value) return false
  
  // 检查预约状态：必须是已完成（status === 3）
  const rawStatus = appointment.value.status
  const status = typeof rawStatus === 'string' ? parseInt(rawStatus, 10) : rawStatus
  if (status !== 3 && status !== 'completed') return false
  
  // 检查支付状态：必须是已支付（paymentStatus === 1）
  if (!isPaidPaymentStatus(appointment.value.paymentStatus)) return false
  
  // 检查是否已经评价过
  if (appointment.value.reviewed === true || appointment.value.hasReview) return false
  
  return true
})

const getStatusType = (status) => {
  return APPOINTMENT_STATUS_CONFIG[status]?.type || 'info'
}

const getStatusText = (status) => {
  return APPOINTMENT_STATUS_CONFIG[status]?.text || status
}

const getPaymentStatusType = (paymentStatus) => {
  return getUnifiedPaymentStatusType(paymentStatus)
}

const getPaymentStatusText = (paymentStatus) => {
  return getUnifiedPaymentStatusText(paymentStatus)
}

const loadAppointment = async () => {
  const appointmentId = route.params.id
  if (!appointmentId) {
    ElMessage.error('预约ID无效')
    router.push('/profile')
    return
  }
  
  try {
    const response = await getAppointmentDetail(appointmentId)
    // 处理响应数据：可能是 response.data.data 或 response.data
    let data = response.data?.data || response.data
    
    if (!data) {
      ElMessage.error('预约详情不存在')
      router.push('/profile')
      return
    }
    
    // 数据清洗和增强：确保所有字段都正确映射
    // 调试：打印原始数据（仅开发环境）
    if (import.meta.env.DEV) {
      console.debug('📥 预约详情原始数据:', data)
      console.debug('📥 paymentStatus 原始值:', data.paymentStatus, data.payment_status)
    }
    
    appointment.value = {
      ...data,
      // 确保ID字段存在
      id: data.appointmentId || data.id,
      appointmentId: data.appointmentId || data.id,
      // 处理服务名称和服务ID
      serviceName: data.serviceName || data.service?.serviceName || data.service?.name || null,
      serviceId: data.serviceId || data.service?.serviceId || data.service?.id || null,
      // 处理服务人员名称和ID（优先使用直接返回的字段）
      staffName: data.staffName || data.staff?.name || data.staff?.staffName || data.staff?.realName || data.staff?.username || null,
      staffId: data.staffId || data.staff?.staffId || data.staff?.id || null,
      // 处理日期和时间
      appointmentDate: data.appointmentDate || data.appointment_date || null,
      appointmentTime: data.appointmentTime || data.appointment_time || 
        (data.startTime && data.endTime ? `${data.startTime} - ${data.endTime}` : null) ||
        (data.start_time && data.end_time ? `${data.start_time} - ${data.end_time}` : null) ||
        null,
      startTime: data.startTime || data.start_time || null,
      endTime: data.endTime || data.end_time || null,
      // 处理金额
      totalAmount: data.totalAmount || data.total_amount || 0,
      // 处理状态（统一为数字）
      status: data.status !== undefined && data.status !== null ? data.status : 0,
      // 接单时间（用于计算支付倒计时）
      acceptTime: data.acceptTime || data.accept_time || null,
      // 处理支付状态（确保正确映射，支持多种字段名格式）
      paymentStatus: normalizePaymentStatus(data),
      // 其他字段
      serviceAddress: data.serviceAddress || data.service_address || null,
      specialRequirements: data.specialRequirements || data.special_requirements || data.notes || null,
      notes: data.notes || data.specialRequirements || data.special_requirements || null,
      reviewed: data.reviewed || data.hasReview || false,
      // 处理创建时间（用于计算支付过期时间）
      createTime: data.createTime || data.create_time || data.createDate || data.create_date || null
    }
    
    // 如果缺少服务名称或服务人员名称，尝试补充
    await enhanceAppointmentDetail(appointment.value)

    // 加载本订单的三方互评信息
    await loadReviewBundle(appointment.value.appointmentId)
    
    // 如果支付状态为"待支付"或"未知"，主动查询支付状态以触发后端自动同步
    const paymentStatus = normalizePaymentStatus(appointment.value)
    const isUnpaid = isUnpaidPaymentStatus(paymentStatus)
    
    if (isUnpaid && appointment.value.appointmentId) {
      try {
        // 调用支付状态查询接口，触发后端自动同步支付宝状态
        await queryPaymentStatusByAppointment(appointment.value.appointmentId, { skipErrorHandler: true })
        // 同步后重新加载预约详情以获取最新状态
        const refreshResponse = await getAppointmentDetail(appointmentId)
        const refreshData = refreshResponse.data?.data || refreshResponse.data
        if (refreshData) {
          // 更新整个预约对象，确保所有字段都是最新的
          const newPaymentStatus = normalizePaymentStatus(refreshData)
          
          const oldPaymentStatus = normalizePaymentStatus(appointment.value)
          appointment.value.paymentStatus = newPaymentStatus
          
          // 如果支付状态已更新为已支付，提示用户
          if (newPaymentStatus === 1 && oldPaymentStatus !== 1) {
            ElMessage.success('支付状态已更新：订单已支付')
          } else if (newPaymentStatus === 0 && oldPaymentStatus !== 0) {
            // 如果状态从其他值变为待支付，也更新显示
            console.debug('支付状态已同步为待支付')
          }
        }
      } catch (error) {
        // 静默处理错误，不影响页面正常显示
        console.debug('自动同步支付状态失败:', error)
      }
    }
    
    // 如果是待支付或支付中状态，启动轮询（使用数字状态码判断）
    const status = appointment.value.status
    if (status === 0 || status === 'pending' || status === 'pending_payment' || status === 'pending_confirm') {
      startStatusPolling()
    }
  } catch (error) {
    console.error('获取预约详情失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '获取预约详情失败'
    ElMessage.error(errorMsg)
    router.push('/profile')
  }
}

// 增强预约详情：补充缺失的服务名称和服务人员姓名
const enhanceAppointmentDetail = async (appointment) => {
  try {
    // 如果缺少服务名称，尝试查询
    if (!appointment.serviceName && appointment.serviceId) {
      try {
        const response = await getServiceDetail(appointment.serviceId)
        const service = response.data?.data || response.data
        if (service) {
          appointment.serviceName = service.serviceName || service.name || service.service_name || `服务#${appointment.serviceId}`
        }
      } catch (error) {
        // 静默处理，不抛出错误
        console.debug('获取服务详情失败:', error.message)
        appointment.serviceName = appointment.serviceName || `服务#${appointment.serviceId}`
      }
    }
    
    // 如果缺少服务人员名称，尝试查询
    if (!appointment.staffName && appointment.staffId) {
      try {
        const response = await getStaffDetail(appointment.staffId)
        const staff = response.data?.data || response.data
        if (staff) {
          // 尝试多种字段名格式获取姓名
          const name = staff.name || staff.staffName || staff.realName || staff.username
          if (name) {
            appointment.staffName = name
          }
        }
      } catch (error) {
        // 静默处理，不抛出错误（API可能不存在或权限不足）
        console.debug(`获取服务人员详情失败 (staffId: ${appointment.staffId}):`, error.message)
        appointment.staffName = appointment.staffName || `员工#${appointment.staffId}`
      }
    }
  } catch (error) {
    // 捕获所有未预期的错误，静默处理
    console.debug('增强预约详情失败:', error.message)
  }
}

// 加载三方评价包（顾客视角）
const loadReviewBundle = async (appointmentId) => {
  if (!appointmentId) return
  // 兼容老版本：如果后端或前端还未提供 getAppointmentReviewBundle，则直接跳过，不影响详情页
  const fn = reviewsApi.getAppointmentReviewBundle
  if (typeof fn !== 'function') {
    return
  }
  try {
    const res = await fn(appointmentId, { skipErrorHandler: true })
    const payload = res.data?.data || res.data || {}
    const normalize = (r) => {
      const visible = normalizeVisibleReview(r)
      if (!visible) return null
      return {
        overallRating: visible.overallRating ?? visible.rating ?? visible.overall_rating ?? null,
        serviceAttitudeRating: visible.serviceAttitudeRating ?? visible.service_attitude_rating ?? null,
        professionalAbilityRating: visible.professionalAbilityRating ?? visible.professional_ability_rating ?? null,
        serviceQualityRating: visible.serviceQualityRating ?? visible.service_quality_rating ?? null,
        reviewContent: visible.reviewContent ?? visible.content ?? visible.review_content ?? '',
        reviewTime: visible.createTime ?? visible.createdAt ?? visible.created_at ?? visible.reviewTime ?? ''
      }
    }
    customerReviewBlock.value = normalize(payload.customerReview)
    staffSelfReviewBlock.value = normalize(payload.staffSelfReview)
    adminReviewForCustomerBlock.value = normalize(payload.adminReviewForCustomer)
    adminReviewForStaffBlock.value = normalize(payload.adminReviewForStaff)
  } catch (e) {
    // 不影响主详情展示，静默记录错误
    console.error('加载三方评价包失败:', e)
  }
}

// 状态轮询（用于实时更新状态）
const startStatusPolling = () => {
  if (statusPollingTimer) return
  
  statusPollingTimer = setInterval(async () => {
    try {
      const response = await getAppointmentDetail(route.params.id)
      const data = response.data?.data || response.data
      const newStatus = data.status
      const oldStatus = appointment.value.status
      
      // 更新数据（保持数据清洗逻辑）
      appointment.value = {
        ...appointment.value,
        ...data,
        serviceName: data.serviceName || data.service?.serviceName || appointment.value.serviceName,
        staffName: data.staffName || data.staff?.name || appointment.value.staffName
      }
      
      // 如果状态发生变化，停止轮询
      if (newStatus !== oldStatus) {
        stopStatusPolling()
        
        // 状态变更提示
        if (newStatus === 'pending_confirm') {
          ElMessage.success('支付成功，等待服务人员接单')
        } else if (newStatus === 'confirmed') {
          ElMessage.success('服务人员已接单')
        } else if (newStatus === 'in_service') {
          ElMessage.info('服务已开始')
        } else if (newStatus === 'completed') {
          ElMessage.success('服务已完成')
        }
      }
      
      // 如果状态不再是需要轮询的状态，停止轮询
      if (![0, 'pending', 'pending_payment', 'paying', 'pending_confirm'].includes(newStatus)) {
        stopStatusPolling()
      }
    } catch (error) {
      console.error('轮询状态失败:', error)
    }
  }, 15000) // 每15秒轮询一次，减少页面刷新频率
}

const stopStatusPolling = () => {
  if (statusPollingTimer) {
    clearInterval(statusPollingTimer)
    statusPollingTimer = null
  }
}

const goToPayment = () => {
  router.push({
    path: '/payment',
    query: { appointmentId: appointment.value.id }
  })
}

const handleEdit = () => {
  router.push({
    path: '/appointment',
    query: { appointmentId: appointment.value.id, edit: true }
  })
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消此预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await updateAppointmentStatus(appointment.value.id, 'cancelled')
    ElMessage.success('预约已取消')
    stopStatusPolling()
    loadAppointment()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消预约失败')
    }
  }
}

const goToReview = () => {
  router.push({
    path: '/review',
    query: { appointmentId: appointment.value.id }
  })
}

const viewReview = () => {
  // TODO: 跳转到评价详情页
  ElMessage.info('查看评价功能开发中')
}

onMounted(() => {
  loadAppointment()
})

// 支付成功回跳后：进入详情页立刻刷新一次（避免等轮询/缓存）
watch(
  () => route.query,
  async (q) => {
    const needRefresh =
      q?.refresh === 'true' ||
      q?.from === 'payment-success' ||
      q?.from === 'alipay-return'

    if (!needRefresh) return

    // 先触发一次支付状态同步，再拉详情
    const id = route.params.id
    if (id) {
      try {
        // 短轮询确认：避免回跳后瞬间仍读到旧状态
        for (let i = 0; i < 8; i++) {
          try {
            await queryPaymentStatusByAppointment(id, { skipErrorHandler: true })
          } catch (e) {
            // ignore
          }
          // 重新拉取一次详情，如果已变更则提前结束
          await loadAppointment()
          const ps = normalizePaymentStatus(appointment.value)
          if (ps === 1) break
          await new Promise((r) => setTimeout(r, 700))
        }
      } catch (e) {
        // ignore
      }
    }

    // 清掉 query，避免重复触发
    router.replace({ query: {} })
  },
  { immediate: true }
)

onUnmounted(() => {
  stopStatusPolling()
})
</script>

<style scoped lang="scss">
.appointment-detail-view {
  min-height: 100vh;
}

.content {
  padding: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  h2 {
    margin: 0;
  }
}

.detail-content {
  .actions {
    margin-top: 30px;
    text-align: center;
    display: flex;
    justify-content: center;
    gap: 10px;
    flex-wrap: wrap;
    
    .el-button {
      min-width: 120px;
    }
  }
}
</style>

