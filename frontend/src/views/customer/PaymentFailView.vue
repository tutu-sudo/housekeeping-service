<template>
  <div class="payment-fail-view">
    <Navigation />
    <div class="content">
      <el-container>
        <el-main>
          <el-card class="fail-card">
            <div class="fail-content">
              <el-result
                icon="error"
                title="支付失败"
                :sub-title="failReason || '支付过程中出现问题，请重试或联系客服'"
              >
                <template #extra>
                  <div class="fail-info" v-if="outTradeNo || tradeNo">
                    <el-descriptions title="订单信息" :column="1" border>
                      <el-descriptions-item label="订单号" v-if="outTradeNo">
                        {{ outTradeNo }}
                      </el-descriptions-item>
                      <el-descriptions-item label="交易号" v-if="tradeNo">
                        {{ tradeNo }}
                      </el-descriptions-item>
                    </el-descriptions>
                  </div>
                  
                  <div class="action-buttons">
                    <el-button type="primary" @click="retryPayment" :disabled="!appointmentId">
                      重新支付
                    </el-button>
                    <el-button @click="goToAppointmentDetail" v-if="appointmentId">
                      查看订单
                    </el-button>
                    <el-button @click="goToHome">
                      返回首页
                    </el-button>
                  </div>
                  
                  <div class="help-info">
                    <el-alert
                      title="支付提示"
                      type="info"
                      :closable="false"
                      show-icon
                    >
                      <template #default>
                        <p>如果支付过程中遇到问题，您可以：</p>
                        <ul>
                          <li>检查您的账户余额是否充足</li>
                          <li>确认网络连接是否正常</li>
                          <li>稍后重试支付</li>
                          <li>联系客服：400-123-4567</li>
                        </ul>
                      </template>
                    </el-alert>
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
import Navigation from '@/components/common/Navigation.vue'

const router = useRouter()
const route = useRoute()

const failReason = ref('')
const outTradeNo = ref('')
const tradeNo = ref('')
const appointmentId = ref(null)

const loadFailInfo = () => {
  // 从URL参数获取失败原因和订单信息
  failReason.value = route.query.reason || '支付过程中出现问题，请重试或联系客服'
  outTradeNo.value = route.query.outTradeNo || ''
  tradeNo.value = route.query.tradeNo || ''
  appointmentId.value = route.query.appointmentId || null
  
  // 常见失败原因的中文提示
  const reasonMap = {
    'USER_CANCEL': '您取消了支付',
    'INSUFFICIENT_BALANCE': '账户余额不足',
    'NETWORK_ERROR': '网络连接异常',
    'TIMEOUT': '支付超时，请重试',
    'SYSTEM_ERROR': '系统错误，请稍后重试'
  }
  
  if (reasonMap[failReason.value]) {
    failReason.value = reasonMap[failReason.value]
  }
}

const retryPayment = () => {
  if (appointmentId.value) {
    router.push({
      path: '/payment',
      query: { appointmentId: appointmentId.value }
    })
  } else {
    router.push('/profile')
  }
}

const goToAppointmentDetail = () => {
  if (appointmentId.value) {
    router.push(`/appointment/${appointmentId.value}`)
  } else {
    router.push('/profile')
  }
}

const goToHome = () => {
  router.push('/home')
}

onMounted(() => {
  loadFailInfo()
})
</script>

<style scoped lang="scss">
.payment-fail-view {
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

.fail-card {
  max-width: 800px;
  width: 100%;
}

.fail-content {
  padding: 20px;
}

.fail-info {
  margin-top: 20px;
  margin-bottom: 30px;
}

.action-buttons {
  margin-top: 30px;
  text-align: center;
  
  .el-button {
    margin: 0 10px;
    min-width: 120px;
  }
}

.help-info {
  margin-top: 30px;
  
  ul {
    margin: 10px 0 0 20px;
    padding: 0;
    
    li {
      margin: 8px 0;
      line-height: 1.6;
    }
  }
}

:deep(.el-descriptions) {
  margin-top: 20px;
}
</style>
