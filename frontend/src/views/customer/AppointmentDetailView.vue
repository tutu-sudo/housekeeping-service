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
              
              <!-- 状态流转提示 -->
              <el-alert
                v-if="canPay && isPaymentExpired"
                title="订单已过期，无法支付"
                type="error"
                :closable="false"
                show-icon
                style="margin: 20px 0;"
              >
                <template #default>
                  <div>该订单已超过支付期限（{{ PAYMENT_EXPIRE_MINUTES }}分钟），订单将自动取消。如有疑问，请联系客服。</div>
                </template>
              </el-alert>
              
              <el-alert
                v-if="canPay && !isPaymentExpired"
                :title="`请在 ${formatPaymentDeadline(paymentDeadline)} 前完成支付`"
                type="warning"
                :closable="false"
                show-icon
                style="margin: 20px 0;"
              />
              
              <el-alert
                v-if="appointment.status === 'pending_confirm' || (appointment.status === 0 && appointment.paymentStatus === 1)"
                title="支付成功，等待服务人员接单"
                type="info"
                :closable="false"
                show-icon
                style="margin: 20px 0;"
              />
              
              <el-alert
                v-if="appointment.status === 'confirmed' || appointment.status === 1"
                title="服务人员已接单，请等待服务开始"
                type="success"
                :closable="false"
                show-icon
                style="margin: 20px 0;"
              />
              
              <el-alert
                v-if="appointment.status === 'in_service' || appointment.status === 2"
                title="服务进行中"
                type="info"
                :closable="false"
                show-icon
                style="margin: 20px 0;"
              />
              
              <el-alert
                v-if="(appointment.status === 'completed' || appointment.status === 3) && !appointment.reviewed"
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAppointmentDetail, updateAppointmentStatus } from '@/api/appointments'
import { getServiceDetail } from '@/api/services'
import { getStaffDetail } from '@/api/staff'
import { APPOINTMENT_STATUS_CONFIG, PAYMENT_STATUS_CONFIG } from '@/utils/constants'
import Navigation from '@/components/common/Navigation.vue'
import { CreditCard } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

const appointment = ref(null)
let statusPollingTimer = null

const canEdit = computed(() => {
  if (!appointment.value) return false
  const status = appointment.value.status
  // 状态为待确认(0)或待支付状态
  return status === 0 || status === 'pending' || status === 'pending_payment'
})

const canCancel = computed(() => {
  if (!appointment.value) return false
  const status = appointment.value.status
  // 状态为待确认(0)、已确认(1)可以取消
  return status === 0 || status === 1 || status === 'pending_payment' || status === 'pending_confirm' || status === 'confirmed'
})

const canPay = computed(() => {
  if (!appointment.value) return false
  const status = appointment.value.status
  const paymentStatus = appointment.value.paymentStatus
  // 状态为待确认(0)且支付状态为待支付(0)
  return (status === 0 || status === 'pending') && 
         (paymentStatus === 0 || paymentStatus === 'unpaid' || !paymentStatus)
})

// 支付过期时间（创建订单后10分钟内需支付，可根据实际业务调整）
const PAYMENT_EXPIRE_MINUTES = 10

// 计算支付截止时间
const paymentDeadline = computed(() => {
  if (!appointment.value || !appointment.value.createTime) return null
  if (!canPay.value) return null
  
  try {
    const createTime = dayjs(appointment.value.createTime)
    if (!createTime.isValid()) return null
    return createTime.add(PAYMENT_EXPIRE_MINUTES, 'minute')
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
  const status = appointment.value.status
  // 状态为已完成(3)且还没有评价
  return (status === 3 || status === 'completed') && 
         appointment.value.reviewed !== true && 
         !appointment.value.hasReview
})

const getStatusType = (status) => {
  return APPOINTMENT_STATUS_CONFIG[status]?.type || 'info'
}

const getStatusText = (status) => {
  return APPOINTMENT_STATUS_CONFIG[status]?.text || status
}

const getPaymentStatusType = (paymentStatus) => {
  // 处理 null、undefined 或数字字符串的情况
  if (paymentStatus === null || paymentStatus === undefined) {
    return 'info'
  }
  // 确保是数字类型
  const status = typeof paymentStatus === 'string' ? parseInt(paymentStatus, 10) : paymentStatus
  return PAYMENT_STATUS_CONFIG[status]?.type || 'info'
}

const getPaymentStatusText = (paymentStatus) => {
  // 处理 null、undefined 或数字字符串的情况
  if (paymentStatus === null || paymentStatus === undefined) {
    return '未知'
  }
  // 确保是数字类型
  const status = typeof paymentStatus === 'string' ? parseInt(paymentStatus, 10) : paymentStatus
  return PAYMENT_STATUS_CONFIG[status]?.text || '未知'
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
      // 处理支付状态（确保正确映射，支持多种字段名格式）
      paymentStatus: data.paymentStatus !== undefined && data.paymentStatus !== null 
        ? (typeof data.paymentStatus === 'string' ? parseInt(data.paymentStatus, 10) : data.paymentStatus)
        : (data.payment_status !== undefined && data.payment_status !== null 
          ? (typeof data.payment_status === 'string' ? parseInt(data.payment_status, 10) : data.payment_status)
          : 0),
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
  }, 3000) // 每3秒轮询一次
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

