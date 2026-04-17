<template>
  <div class="payment-success-view">
    <Navigation />
    <div class="content">
      <el-container>
        <el-main>
          <el-card class="success-card">
            <div class="success-content">
              <el-result
                icon="success"
                title="支付成功"
                sub-title="您的订单已成功支付，我们将尽快为您安排服务"
              >
                <template #extra>
                  <el-descriptions v-if="paymentInfo" title="支付信息" :column="1" border>
                    <el-descriptions-item label="订单号">
                      {{ paymentInfo.outTradeNo || '-' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="交易号">
                      {{ paymentInfo.tradeNo || '-' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="支付金额">
                      <span class="amount">¥{{ paymentInfo.paymentAmount?.toFixed(2) || '0.00' }}</span>
                    </el-descriptions-item>
                    <el-descriptions-item label="支付方式">
                      {{ paymentInfo.paymentMethod === 'alipay' ? '支付宝' : '其他' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="支付时间">
                      {{ paymentInfo.paymentTime || new Date().toLocaleString() }}
                    </el-descriptions-item>
                  </el-descriptions>
                  
                  <div class="action-buttons">
                    <el-button type="primary" @click="goToAppointmentDetail">
                      查看订单详情
                    </el-button>
                    <el-button @click="goToHome">
                      返回首页
                    </el-button>
                  </div>
                </template>
              </el-result>
            </div>
          </el-card>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { queryPaymentStatusByAppointment } from '@/api/payment'
import { getAppointmentDetail } from '@/api/appointments'
import Navigation from '@/components/common/Navigation.vue'

const router = useRouter()
const route = useRoute()

const paymentInfo = ref(null)
const appointmentId = ref(null)

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

const extractPaidFlag = (payload) => {
  const data = payload?.data || payload || {}
  const status =
    data.paymentStatus ??
    data.payment_status ??
    data.status ??
    data.paymentState ??
    data.payment_state
  const s = typeof status === 'string' ? parseInt(status, 10) : status
  return s === 1 || status === 'paid' || status === 'success'
}

// 支付回跳后：短轮询确认（兼容 notify/return 延迟），确认已支付后再刷新订单视图
const confirmPaymentStatus = async (id, maxAttempts = 10, intervalMs = 800) => {
  for (let i = 0; i < maxAttempts; i++) {
    try {
      const res = await queryPaymentStatusByAppointment(id)
      const payload = res?.data?.data || res?.data || res
      if (extractPaidFlag(payload)) return { ok: true, payload }
    } catch (e) {
      // ignore and retry
    }
    await sleep(intervalMs)
  }
  return { ok: false, payload: null }
}

const loadPaymentInfo = async () => {
  // 从URL参数获取订单号和预约ID
  const outTradeNo = route.query.outTradeNo || ''
  const tradeNo = route.query.tradeNo || ''
  
  // 优先使用URL参数中的appointmentId
  let appointmentIdValue = route.query.appointmentId
  if (!appointmentIdValue) {
    // 如果没有appointmentId，尝试从localStorage获取（支付前保存的）
    const savedAppointmentId = localStorage.getItem('pendingPaymentAppointmentId')
    if (savedAppointmentId) {
      appointmentIdValue = savedAppointmentId
      localStorage.removeItem('pendingPaymentAppointmentId')
    }
  }
  
  // 确保appointmentId是有效的数字
  if (appointmentIdValue) {
    const id = typeof appointmentIdValue === 'string' ? parseInt(appointmentIdValue, 10) : Number(appointmentIdValue)
    if (!isNaN(id) && id > 0) {
      appointmentId.value = id
    } else {
      appointmentId.value = null
    }
  } else {
    appointmentId.value = null
  }
  
  // 如果有有效的appointmentId，查询支付状态
  if (appointmentId.value) {
    try {
      // 先做一次短轮询确认，尽可能让前端“瞬间”拿到已支付
      const confirm = await confirmPaymentStatus(appointmentId.value)
      const response = confirm.ok ? { data: confirm.payload } : await queryPaymentStatusByAppointment(appointmentId.value)

      if (response.data) {
        const data = response.data.data || response.data
        paymentInfo.value = {
          outTradeNo: outTradeNo || data.transactionId || '-',
          tradeNo: tradeNo || data.transactionId || '-',
          paymentAmount: data.paymentAmount,
          paymentMethod: data.paymentMethod || 'alipay',
          paymentTime: data.paymentTime || new Date().toLocaleString(),
          appointmentId: appointmentId.value
        }

        // 标记“刚完成支付”，用于订单列表/详情页立刻刷新（避免等轮询）
        try {
          localStorage.setItem('lastPaidAppointmentId', String(appointmentId.value))
          localStorage.setItem('lastPaidAt', String(Date.now()))
        } catch (e) {
          // ignore
        }
      } else {
        // 查询失败，使用基本信息
        paymentInfo.value = {
          outTradeNo: outTradeNo || '-',
          tradeNo: tradeNo || '-',
          paymentAmount: null,
          paymentMethod: 'alipay',
          paymentTime: new Date().toLocaleString(),
          appointmentId: appointmentId.value
        }
      }
    } catch (error) {
      console.error('查询支付状态失败:', error)
      // 即使查询失败，也显示基本信息
      paymentInfo.value = {
        outTradeNo: outTradeNo || '-',
        tradeNo: tradeNo || '-',
        paymentAmount: null,
        paymentMethod: 'alipay',
        paymentTime: new Date().toLocaleString(),
        appointmentId: appointmentId.value
      }
    }
  } else {
    // 如果没有appointmentId，只显示URL参数中的信息
    paymentInfo.value = {
      outTradeNo: outTradeNo || '-',
      tradeNo: tradeNo || '-',
      paymentAmount: null,
      paymentMethod: 'alipay',
      paymentTime: new Date().toLocaleString()
    }
    ElMessage.warning('未找到订单信息，请前往订单列表查看')
  }
}

const goToAppointmentDetail = () => {
  if (appointmentId.value) {
    router.push({
      path: `/appointment/${appointmentId.value}`,
      query: { from: 'payment-success', refresh: 'true' }
    })
  } else {
    // 跳转到个人中心的预约列表，并刷新数据
    router.push({
      path: '/profile',
      query: { 
        tab: 'appointments',
        refresh: 'true',
        from: 'payment-success'
      }
    })
  }
}

const goToHome = () => {
  router.push('/home')
}

onMounted(() => {
  loadPaymentInfo()
})
</script>

<style scoped lang="scss">
.payment-success-view {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.content {
  padding: 40px 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 60px);
}

.success-card {
  max-width: 800px;
  width: 100%;
}

.success-content {
  padding: 20px;
}

.amount {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
}

.action-buttons {
  margin-top: 30px;
  text-align: center;
  
  .el-button {
    margin: 0 10px;
    min-width: 120px;
  }
}

:deep(.el-descriptions) {
  margin-top: 20px;
}
</style>
