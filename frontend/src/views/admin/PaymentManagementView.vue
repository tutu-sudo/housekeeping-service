<template>
  <div class="payment-management-view">
    <Navigation />
    <div class="content">
      <el-container>
        <AdminSidebar />
        <el-main>
          <div class="page-header">
            <h2>支付明细管理</h2>
          </div>
          
          <el-card>
            <el-form :inline="true" class="filter-form">
              <el-form-item label="预约ID">
                <el-input v-model="filters.appointmentId" placeholder="请输入预约ID" clearable />
              </el-form-item>
              <el-form-item label="支付状态">
                <el-select v-model="filters.paymentStatus" placeholder="请选择状态" clearable>
                  <el-option label="全部" :value="''" />
                  <el-option label="待支付" :value="0" />
                  <el-option label="支付成功" :value="1" />
                  <el-option label="支付失败" :value="2" />
                  <el-option label="已退款" :value="3" />
                </el-select>
              </el-form-item>
              <el-form-item label="支付方式">
                <el-select v-model="filters.paymentMethod" placeholder="请选择支付方式" clearable>
                  <el-option label="全部" :value="''" />
                  <el-option label="支付宝" value="alipay" />
                  <el-option label="微信支付" value="wechat" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadPayments" :loading="loading">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button @click="resetFilters">重置</el-button>
                <el-button type="success" @click="queryByAppointmentDialogVisible = true">
                  根据预约查询支付
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
          
          <el-card style="margin-top: 20px">
            <el-table :data="payments" v-loading="loading" stripe>
              <el-table-column prop="paymentId" label="ID" width="80" />
              <el-table-column prop="appointmentId" label="预约ID" width="100" />
              <el-table-column prop="customerName" label="客户姓名" width="120" />
              <el-table-column prop="paymentAmount" label="支付金额(元)" width="120">
                <template #default="scope">
                  ¥{{ scope.row.paymentAmount?.toFixed(2) || '0.00' }}
                </template>
              </el-table-column>
              <el-table-column prop="paymentMethod" label="支付方式" width="120">
                <template #default="scope">
                  <el-tag :type="scope.row.paymentMethod === 'alipay' ? 'success' : 'primary'">
                    {{ scope.row.paymentMethod === 'alipay' ? '支付宝' : '微信支付' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="paymentStatus" label="支付状态" width="120">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.paymentStatus)">
                    {{ getStatusText(scope.row.paymentStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="transactionId" label="交易号" width="200" show-overflow-tooltip />
              <el-table-column prop="paymentTime" label="支付时间" width="180" />
              <el-table-column prop="createTime" label="创建时间" width="180" />
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="primary" link @click="viewDetail(scope.row.paymentId)">
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
          
          <!-- 支付详情对话框 -->
          <el-dialog
            v-model="detailDialogVisible"
            title="支付详情"
            width="800px"
            @close="paymentDetail = null"
          >
            <div v-loading="detailLoading">
              <el-descriptions :column="2" border v-if="paymentDetail">
                <el-descriptions-item label="支付ID">
                  {{ paymentDetail.paymentId }}
                </el-descriptions-item>
                <el-descriptions-item label="预约ID">
                  {{ paymentDetail.appointmentId }}
                </el-descriptions-item>
                <el-descriptions-item label="客户ID">
                  {{ paymentDetail.customerId }}
                </el-descriptions-item>
                <el-descriptions-item label="客户姓名">
                  {{ paymentDetail.customerName || '未知' }}
                </el-descriptions-item>
                <el-descriptions-item label="支付金额">
                  ¥{{ paymentDetail.paymentAmount?.toFixed(2) || '0.00' }}
                </el-descriptions-item>
                <el-descriptions-item label="支付方式">
                  <el-tag :type="paymentDetail.paymentMethod === 'alipay' ? 'success' : 'primary'">
                    {{ paymentDetail.paymentMethod === 'alipay' ? '支付宝' : '微信支付' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="支付状态">
                  <el-tag :type="getStatusType(paymentDetail.paymentStatus)">
                    {{ getStatusText(paymentDetail.paymentStatus) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="交易号">
                  {{ paymentDetail.transactionId || '无' }}
                </el-descriptions-item>
                <el-descriptions-item label="支付时间">
                  {{ paymentDetail.paymentTime || '未支付' }}
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">
                  {{ paymentDetail.createTime }}
                </el-descriptions-item>
                <el-descriptions-item label="更新时间">
                  {{ paymentDetail.updateTime || '未知' }}
                </el-descriptions-item>
                <el-descriptions-item label="备注" :span="2">
                  {{ paymentDetail.remarks || '无' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-dialog>
          
          <!-- 根据预约查询支付对话框 -->
          <el-dialog
            v-model="queryByAppointmentDialogVisible"
            title="根据预约ID查询支付"
            width="500px"
            @close="queryAppointmentId = ''"
          >
            <el-form>
              <el-form-item label="预约ID">
                <el-input
                  v-model="queryAppointmentId"
                  placeholder="请输入预约ID"
                  clearable
                  @keyup.enter="handleQueryByAppointment"
                />
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="queryByAppointmentDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="handleQueryByAppointment" :loading="queryLoading">
                查询
              </el-button>
            </template>
          </el-dialog>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getPayments, getPaymentDetail, getPaymentByAppointment } from '@/api/admin'
import { queryPaymentStatusByAppointment } from '@/api/payment'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

const payments = ref([])
const loading = ref(false)
const detailDialogVisible = ref(false)
const paymentDetail = ref(null)
const detailLoading = ref(false)
const queryByAppointmentDialogVisible = ref(false)
const queryAppointmentId = ref('')
const queryLoading = ref(false)

const filters = ref({
  appointmentId: undefined,
  paymentStatus: undefined,
  paymentMethod: undefined
})

const getStatusText = (status) => {
  const statusMap = {
    0: '待支付',
    1: '支付成功',
    2: '支付失败',
    3: '已退款'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'info'
  }
  return typeMap[status] || ''
}

const loadPayments = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.value.appointmentId) params.appointmentId = filters.value.appointmentId
    if (filters.value.paymentStatus !== undefined) {
      params.paymentStatus = filters.value.paymentStatus
    }
    if (filters.value.paymentMethod) params.paymentMethod = filters.value.paymentMethod

    const response = await getPayments(params)
    payments.value = response.data?.data || response.data || []
  } catch (error) {
    console.error('获取支付列表失败:', error)
    ElMessage.error('获取支付列表失败')
    payments.value = []
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = {
    appointmentId: undefined,
    paymentStatus: undefined,
    paymentMethod: undefined
  }
  loadPayments()
}

const viewDetail = async (paymentId) => {
  detailLoading.value = true
  detailDialogVisible.value = true
  try {
    const response = await getPaymentDetail(paymentId)
    paymentDetail.value = response.data?.data || response.data
    
    // 如果支付状态为"待支付"且有预约ID，主动查询支付状态以触发后端自动同步
    const paymentStatus = paymentDetail.value?.paymentStatus
    const appointmentId = paymentDetail.value?.appointmentId
    const isUnpaid = paymentStatus === 0 || paymentStatus === null || paymentStatus === undefined
    if (isUnpaid && appointmentId) {
      try {
        // 调用支付状态查询接口，触发后端自动同步支付宝状态
        await queryPaymentStatusByAppointment(appointmentId)
        // 同步后重新查询支付详情以获取最新状态
        const refreshResponse = await getPaymentDetail(paymentId)
        const refreshData = refreshResponse.data?.data || refreshResponse.data
        if (refreshData) {
          paymentDetail.value = refreshData
          // 如果支付状态已更新为已支付，提示用户
          if (paymentDetail.value.paymentStatus === 1) {
            ElMessage.success('支付状态已更新：订单已支付')
          }
        }
      } catch (error) {
        // 静默处理错误，不影响页面正常显示
        console.debug('自动同步支付状态失败:', error)
      }
    }
  } catch (error) {
    console.error('获取支付详情失败:', error)
    ElMessage.error('获取支付详情失败')
    detailDialogVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

const handleQueryByAppointment = async () => {
  if (!queryAppointmentId.value) {
    ElMessage.warning('请输入预约ID')
    return
  }
  
  queryLoading.value = true
  try {
    // 先查询支付状态，触发后端自动同步（如果状态为待支付）
    try {
      await queryPaymentStatusByAppointment(queryAppointmentId.value)
    } catch (error) {
      // 静默处理，继续查询支付记录
      console.debug('自动同步支付状态失败:', error)
    }
    
    // 然后查询支付记录
    const response = await getPaymentByAppointment(queryAppointmentId.value)
    paymentDetail.value = response.data?.data || response.data
    
    // 如果查询到支付记录且状态为待支付，再次同步以确保状态最新
    const paymentStatus = paymentDetail.value?.paymentStatus
    const isUnpaid = paymentStatus === 0 || paymentStatus === null || paymentStatus === undefined
    if (isUnpaid && paymentDetail.value?.paymentId) {
      try {
        await queryPaymentStatusByAppointment(queryAppointmentId.value)
        // 重新查询支付详情
        const refreshResponse = await getPaymentByAppointment(queryAppointmentId.value)
        const refreshData = refreshResponse.data?.data || refreshResponse.data
        if (refreshData) {
          paymentDetail.value = refreshData
          if (paymentDetail.value.paymentStatus === 1) {
            ElMessage.success('支付状态已更新：订单已支付')
          }
        }
      } catch (error) {
        console.debug('重新同步支付状态失败:', error)
      }
    }
    
    queryByAppointmentDialogVisible.value = false
    detailDialogVisible.value = true
    queryAppointmentId.value = ''
  } catch (error) {
    console.error('查询支付记录失败:', error)
    ElMessage.error(error.message || '查询支付记录失败')
  } finally {
    queryLoading.value = false
  }
}

onMounted(() => {
  loadPayments()
})
</script>

<style scoped lang="scss">
.payment-management-view {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.content {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
  
  h2 {
    margin: 0;
    color: #333;
    font-size: 24px;
    font-weight: 500;
  }
}

.filter-form {
  margin-bottom: 0;
  
  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}
</style>
