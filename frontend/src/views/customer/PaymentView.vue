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
import { getAppointmentDetail, updateAppointmentStatus } from '@/api/appointments'
import { createPayment, queryPaymentStatus, createAlipayPayment } from '@/api/payment'
import Navigation from '@/components/common/Navigation.vue'

const router = useRouter()
const route = useRoute()

const appointment = ref(null)
const paymentMethod = ref('alipay')
const paying = ref(false)

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
      
      // 支付宝支付：使用GET方式获取支付URL
      const response = await createAlipayPayment(appointmentId)
      
      // 处理响应：根据后端修复后的格式，URL在 result.data 字段中
      // 响应格式: { code: 200, message: "success", data: "支付URL", success: true }
      const result = response.data
      
      // ✅ 检查响应是否成功，并从data字段获取支付URL
      if (result && result.code === 200 && result.success && result.data) {
        const paymentUrl = result.data
        
        if (typeof paymentUrl === 'string' && paymentUrl.trim()) {
          // 跳转到支付宝支付页面
          window.location.href = paymentUrl
          // 注意：这里不设置paying.value = false，因为页面会跳转
        } else {
          localStorage.removeItem('pendingPaymentAppointmentId')
          ElMessage.error('获取支付链接失败：支付URL为空')
          paying.value = false
        }
      } else {
        localStorage.removeItem('pendingPaymentAppointmentId')
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
        ElMessage.success('支付成功')
        await updateAppointmentStatusAfterPayment()
        router.push({
          path: '/appointment/' + appointmentId,
          query: { paymentSuccess: true }
        })
        paying.value = false
      }
    }
  } catch (error) {
    localStorage.removeItem('pendingPaymentAppointmentId')
    console.error('支付失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '支付失败，请重试'
    ElMessage.error(errorMessage)
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
        await updateAppointmentStatusAfterPayment()
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

// 支付成功后更新预约状态
const updateAppointmentStatusAfterPayment = async () => {
  try {
    // 更新预约状态为待接单
    await updateAppointmentStatus(appointment.value.id, 'pending_confirm')
  } catch (error) {
    console.error('更新预约状态失败:', error)
  }
}

onMounted(() => {
  loadAppointment()
})

onUnmounted(() => {
  stopPaymentPolling()
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

