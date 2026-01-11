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
                  <el-option label="全部" :value="undefined" />
                  <el-option label="待支付" :value="0" />
                  <el-option label="支付成功" :value="1" />
                  <el-option label="支付失败" :value="2" />
                  <el-option label="已退款" :value="3" />
                </el-select>
              </el-form-item>
              <el-form-item label="支付方式">
                <el-select v-model="filters.paymentMethod" placeholder="请选择支付方式" clearable>
                  <el-option label="全部" :value="undefined" />
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
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getPayments, getPaymentDetail } from '@/api/admin'
import { ElMessage } from 'element-plus'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

const payments = ref([])
const loading = ref(false)

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
  try {
    const response = await getPaymentDetail(paymentId)
    ElMessageBox.alert(
      JSON.stringify(response.data, null, 2),
      '支付详情',
      {
        confirmButtonText: '确定',
        type: 'info'
      }
    )
  } catch (error) {
    ElMessage.error('获取支付详情失败')
  }
}

onMounted(() => {
  loadPayments()
})
</script>

<style scoped lang="scss">
.payment-management-view {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.content {
  padding: 20px;
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
