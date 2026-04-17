<template>
  <div class="payment-view">
    <Navigation />
    <div class="content">
      <el-container>
        <el-main>
          <el-card>
            <template #header>
              <h2>订单支付</h2>
            </template>
            
            <div v-if="appointment" class="payment-content">
              <el-descriptions title="订单信息" :column="2" border>
                <el-descriptions-item label="订单编号">
                  {{ appointment.id }}
                </el-descriptions-item>
                <el-descriptions-item label="服务项目">
                  {{ appointment.serviceName }}
                </el-descriptions-item>
                <el-descriptions-item label="服务人员">
                  {{ appointment.staffName }}
                </el-descriptions-item>
                <el-descriptions-item label="预约时间">
                  {{ appointment.appointmentDate }} {{ appointment.appointmentTime }}
                </el-descriptions-item>
                <el-descriptions-item label="订单金额" :span="2">
                  <span class="amount">¥ {{ appointment.totalAmount }}</span>
                </el-descriptions-item>
              </el-descriptions>
              
              <el-divider />
              
              <div class="payment-methods">
                <h3>选择支付方式</h3>
                <el-radio-group v-model="paymentMethod">
                  <el-radio label="alipay">
                    <el-icon><CreditCard /></el-icon>
                    支付宝
                  </el-radio>
                  <el-radio label="wechat">
                    <el-icon><Money /></el-icon>
                    微信支付
                  </el-radio>
                </el-radio-group>
              </div>
              
              <div class="payment-actions">
                <el-button type="primary" size="large" @click="handlePayment" :loading="paying">
                  确认支付
                </el-button>
                <el-button @click="$router.back()">取消</el-button>
              </div>
            </div>
          </el-card>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CreditCard, Money } from '@element-plus/icons-vue'
import { getAppointmentDetail } from '@/api/appointments'
import { createPayment, queryPaymentStatus, createAlipayPayment, queryPaymentStatusByAppointment } from '@/api/payment'
import { isPaidPaymentStatus } from '@/utils/payment'
import Navigation from '@/components/common/Navigation.vue'

const router = useRouter()
const route = useRoute()

const appointment = ref(null)
const paymentMethod = ref('alipay')
const paying = ref(false)
let appointmentPaymentWatcher = null
let appointmentPaymentWatcherAttempts = 0
let appointmentPaymentWatcherNotExistCount = 0
let appointmentPaymentWatcherStartTimer = null

const APPOINTMENT_POLL_INTERVAL_MS = 3000
const APPOINTMENT_POLL_MAX_ATTEMPTS = 12
const APPOINTMENT_POLL_MAX_NOT_EXIST = 3
const APPOINTMENT_POLL_START_DELAY_MS = 3000

const loadAppointment = async () => {
  let appointmentId = route.query.appointmentId
  if (!appointmentId) {
    ElMessage.error('缺少订单信息')
    router.push('/profile')
    return
  }
  
  // 确保appointmentId是有效的数字
  const id = typeof appointmentId === 'string' ? parseInt(appointmentId, 10) : Number(appointmentId)
  if (isNaN(id) || id <= 0) {
    ElMessage.error('无效的订单ID')
    router.push('/profile')
    return
  }
  appointmentId = id
  
  try {
    const response = await getAppointmentDetail(appointmentId)
    // 处理响应数据：可能是 response.data.data 或 response.data
    const data = response.data?.data || response.data
    appointment.value = {
      ...data,
      // 确保有appointmentId字段
      appointmentId: data.appointmentId || data.id || appointmentId,
      id: data.id || data.appointmentId || appointmentId
    }

    // 按新的业务规则：仅当预约状态为「待付款」（status = 1）时才允许进入支付流程
    const status = typeof appointment.value.status === 'string'
      ? parseInt(appointment.value.status, 10)
      : appointment.value.status
    if (status !== 1) {
      ElMessage.warning('当前订单状态不支持支付，请刷新订单或重新下单')
      router.push(`/appointment/${appointmentId}`)
    }
  } catch (error) {
    console.error('获取订单信息失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '获取订单信息失败'
    ElMessage.error(errorMsg)
    router.push('/profile')
  }
}

const handlePayment = async () => {
  if (!appointment.value) return
  
  const appointmentId = appointment.value.appointmentId || appointment.value.id
  if (!appointmentId) {
    ElMessage.error('订单信息不完整')
    return
  }
  
  paying.value = true
  try {
    if (paymentMethod.value === 'alipay') {
      // 保存appointmentId到localStorage，以便支付成功后页面使用
      localStorage.setItem('pendingPaymentAppointmentId', appointmentId.toString())
      
      console.log('🔄 开始创建支付宝支付，预约ID:', appointmentId)
      
      // 支付宝支付：使用GET方式获取支付URL
      const response = await createAlipayPayment(appointmentId)
      
      console.log('📥 支付宝支付响应:', response)
      
      // 处理响应：根据后端修复后的格式，URL在 result.data 字段中
      // 响应格式: { code: 200, message: "success", data: "支付URL", success: true }
      const result = response.data
      
      console.log('📦 解析后的响应数据:', result)
      
      // ✅ 检查响应是否成功，并从data字段获取支付URL
      if (result && result.code === 200 && result.success && result.data) {
        const paymentUrl = result.data
        
        console.log('✅ 获取到支付URL:', paymentUrl)
        
        if (typeof paymentUrl === 'string' && paymentUrl.trim()) {
          // 验证URL格式
          if (paymentUrl.startsWith('http://') || paymentUrl.startsWith('https://')) {
            // 在“新标签页”打开支付宝收银台，当前站点页面不跳走
            console.log('🚀 在新标签页打开支付宝支付页面')
            const payWindow = window.open(paymentUrl, '_blank')
            if (!payWindow) {
              localStorage.removeItem('pendingPaymentAppointmentId')
              ElMessage.warning('浏览器拦截了支付弹窗，请允许弹窗后重试')
              paying.value = false
              return
            }
            // 避免“刚拿到支付链接就高频查询”，延迟后再开始轮询
            startAppointmentPaymentWatcher(appointmentId, {
              initialDelayMs: APPOINTMENT_POLL_START_DELAY_MS
            })
            // 当前页面仍然保留在订单支付页，恢复按钮状态
            paying.value = false
          } else {
            localStorage.removeItem('pendingPaymentAppointmentId')
            console.error('❌ 支付URL格式不正确:', paymentUrl)
            ElMessage.error('获取支付链接失败：支付URL格式不正确')
            paying.value = false
          }
        } else {
          localStorage.removeItem('pendingPaymentAppointmentId')
          console.error('❌ 支付URL为空或格式错误:', paymentUrl)
          ElMessage.error('获取支付链接失败：支付URL为空')
          paying.value = false
        }
      } else if (result && result.code === 3006) {
        // 支付超时，后端已经将订单关闭/删除
        localStorage.removeItem('pendingPaymentAppointmentId')
        console.warn('⚠️ 支付超时，订单已关闭')
        ElMessage.error(result.message || '支付超时，订单已关闭')
        // 返回预约详情页，展示最新状态
        router.push(`/appointment/${appointmentId}`)
        paying.value = false
      } else {
        localStorage.removeItem('pendingPaymentAppointmentId')
        console.error('❌ 获取支付链接失败，响应数据:', result)
        const errorMsg = result?.message || '获取支付链接失败，请重试'
        ElMessage.error(errorMsg)
        paying.value = false
      }
    } else if (paymentMethod.value === 'wechat') {
      // 微信支付（暂未实现）
      ElMessage.warning('微信支付功能暂未开放，请选择支付宝支付')
      paying.value = false
    } else {
      // 其他支付方式，使用原有逻辑
      const response = await createPayment({
        appointmentId: appointmentId,
        paymentMethod: paymentMethod.value,
        amount: appointment.value.totalAmount
      })
      
      if (response.data?.paymentUrl) {
        window.location.href = response.data.paymentUrl
      } else if (response.data?.paymentId) {
        startPaymentPolling(response.data.paymentId)
      } else {
        // 其余场景认为由后端直接完成扣款并更新预约状态，这里只做跳转展示
        ElMessage.success('支付成功')
        router.push({
          path: '/appointment/' + appointmentId,
          query: { paymentSuccess: true }
        })
        paying.value = false
      }
    }
  } catch (error) {
    localStorage.removeItem('pendingPaymentAppointmentId')
    console.error('❌ 支付失败，错误详情:', {
      message: error.message,
      response: error.response,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      url: error.config?.url,
      method: error.config?.method
    })

    const resp = error.response?.data
    const status = error.response?.status
    
    // 根据不同的错误类型给出更详细的提示
    if (status === 404) {
      const requestUrl = error.config?.url || '未知'
      console.error('❌ 404错误，请求的URL:', requestUrl)
      ElMessage.error(`支付接口不存在 (404)。请检查后端接口路径是否正确: ${requestUrl}`)
    } else if (status === 500) {
      ElMessage.error('服务器内部错误，请稍后重试或联系客服')
    } else if (resp && resp.code === 3006) {
      // 后端返回的支付超时错误
      ElMessage.error(resp.message || '支付超时，订单已关闭')
      router.push(`/appointment/${appointmentId}`)
    } else {
      const errorMessage = resp?.message || error.message || '支付失败，请重试'
    ElMessage.error(errorMessage)
    }
    paying.value = false
  }
}

// 支付状态轮询
let paymentPollingTimer = null

const startPaymentPolling = async (paymentId) => {
  if (paymentPollingTimer) return
  
  paymentPollingTimer = setInterval(async () => {
    try {
      const response = await queryPaymentStatus(paymentId)
      const status = response.data?.status
      
      if (status === 'paid' || status === 'success') {
        stopPaymentPolling()
        ElMessage.success('支付成功')
        // 由后端回调更新状态，这里仅跳转到详情页让用户查看最新状态
        router.push({
          path: '/appointment/' + appointment.value.id,
          query: { paymentSuccess: true }
        })
      } else if (status === 'failed' || status === 'cancelled') {
        stopPaymentPolling()
        ElMessage.error('支付失败')
      }
    } catch (error) {
      console.error('查询支付状态失败:', error)
    }
  }, 2000) // 每2秒查询一次
  
  // 30分钟后停止轮询（支付超时）
  setTimeout(() => {
    stopPaymentPolling()
    ElMessage.warning('支付超时，请重新支付')
  }, 30 * 60 * 1000)
}

const stopPaymentPolling = () => {
  if (paymentPollingTimer) {
    clearInterval(paymentPollingTimer)
    paymentPollingTimer = null
  }
}

const normalizeQueryStatus = (payload) => {
  const raw =
    payload?.paymentStatus ??
    payload?.payment_status ??
    payload?.payStatus ??
    payload?.pay_status
  if (raw === null || raw === undefined || raw === '') return null
  const n = Number(raw)
  return Number.isFinite(n) ? n : null
}

const isTradeNotExistError = (error) => {
  const data = error?.response?.data || {}
  const text = [
    data?.subCode,
    data?.subMsg,
    data?.message,
    error?.message
  ].filter(Boolean).join(' ').toUpperCase()
  return text.includes('TRADE_NOT_EXIST')
}

const resetAppointmentWatcherState = () => {
  appointmentPaymentWatcherAttempts = 0
  appointmentPaymentWatcherNotExistCount = 0
}

const startAppointmentPaymentWatcher = (appointmentId, options = {}) => {
  const id = Number(appointmentId)
  if (!Number.isFinite(id) || id <= 0) return
  if (appointmentPaymentWatcher) return
  if (appointmentPaymentWatcherStartTimer) return

  const initialDelayMs = Number(options.initialDelayMs)
  const delayMs = Number.isFinite(initialDelayMs) && initialDelayMs >= 0
    ? initialDelayMs
    : 0

  resetAppointmentWatcherState()

  const startPolling = () => {
    appointmentPaymentWatcherStartTimer = null
    if (appointmentPaymentWatcher) return

    appointmentPaymentWatcher = setInterval(async () => {
      appointmentPaymentWatcherAttempts += 1
      if (appointmentPaymentWatcherAttempts > APPOINTMENT_POLL_MAX_ATTEMPTS) {
        stopAppointmentPaymentWatcher()
        localStorage.removeItem('pendingPaymentAppointmentId')
        ElMessage.warning('支付结果确认超时，请重新发起支付')
        return
      }

      try {
        const response = await queryPaymentStatusByAppointment(id, { skipErrorHandler: true })
        const payload = response?.data?.data || response?.data || {}
        const normalizedStatus = normalizeQueryStatus(payload)

        if (isPaidPaymentStatus(payload) || normalizedStatus === 1) {
          stopAppointmentPaymentWatcher()
          localStorage.removeItem('pendingPaymentAppointmentId')
          ElMessage.success('支付成功，正在返回订单详情')
          router.replace({
            path: `/appointment/${id}`,
            query: { from: 'payment-success', refresh: 'true' }
          })
          return
        }

        if (normalizedStatus === 2) {
          stopAppointmentPaymentWatcher()
          localStorage.removeItem('pendingPaymentAppointmentId')
          ElMessage.error('支付失败，请重新发起支付')
          return
        }

        // payment_status = 0/null 视为未确认成功，继续等待
        appointmentPaymentWatcherNotExistCount = 0
      } catch (error) {
        if (isTradeNotExistError(error)) {
          appointmentPaymentWatcherNotExistCount += 1
          if (appointmentPaymentWatcherNotExistCount >= APPOINTMENT_POLL_MAX_NOT_EXIST) {
            stopAppointmentPaymentWatcher()
            localStorage.removeItem('pendingPaymentAppointmentId')
            ElMessage.warning('连续多次未查询到交易，请重新发起支付')
            return
          }
        }
        // 静默重试，避免“暂无支付记录”等提示干扰用户
        console.debug('轮询预约支付状态失败:', error)
      }
    }, APPOINTMENT_POLL_INTERVAL_MS)
  }

  if (delayMs > 0) {
    appointmentPaymentWatcherStartTimer = setTimeout(startPolling, delayMs)
    return
  }
  startPolling()
}

const stopAppointmentPaymentWatcher = () => {
  if (appointmentPaymentWatcherStartTimer) {
    clearTimeout(appointmentPaymentWatcherStartTimer)
    appointmentPaymentWatcherStartTimer = null
  }
  if (appointmentPaymentWatcher) {
    clearInterval(appointmentPaymentWatcher)
    appointmentPaymentWatcher = null
  }
  resetAppointmentWatcherState()
}

const handleWindowFocusForPendingPayment = () => {
  const pendingId = localStorage.getItem('pendingPaymentAppointmentId')
  if (!pendingId) return
  startAppointmentPaymentWatcher(pendingId)
}

onMounted(() => {
  loadAppointment()
  // 兜底：如果用户刷新支付页且仍有待确认订单，继续自动检测支付结果
  const pendingId = route.query.appointmentId || localStorage.getItem('pendingPaymentAppointmentId')
  if (pendingId) {
    startAppointmentPaymentWatcher(pendingId, { initialDelayMs: APPOINTMENT_POLL_START_DELAY_MS })
  }
  window.addEventListener('focus', handleWindowFocusForPendingPayment)
})

onUnmounted(() => {
  stopPaymentPolling()
  stopAppointmentPaymentWatcher()
  window.removeEventListener('focus', handleWindowFocusForPendingPayment)
})
</script>

<style scoped lang="scss">
.payment-view {
  min-height: 100vh;
}

.content {
  padding: 20px;
}

.payment-content {
  .amount {
    font-size: 24px;
    color: #ff6600;
    font-weight: bold;
  }
}

.payment-methods {
  margin: 30px 0;
  
  h3 {
    margin-bottom: 20px;
  }
  
  .el-radio-group {
    display: flex;
    gap: 20px;
    
    .el-radio {
      padding: 20px;
      border: 2px solid #ddd;
      border-radius: 8px;
      cursor: pointer;
      
      &:hover {
        border-color: #4CAF50;
      }
      
      &.is-checked {
        border-color: #4CAF50;
        background-color: #f0f9ff;
      }
    }
  }
}

.payment-actions {
  text-align: center;
  margin-top: 30px;
  
  .el-button {
    margin: 0 10px;
  }
}
</style>

