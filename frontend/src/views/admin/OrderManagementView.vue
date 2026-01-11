<template>
  <div class="order-management-view">
    <Navigation />
    <div class="content">
      <el-container>
        <AdminSidebar />
        <el-main>
          <div class="page-header">
            <h2>预约/订单管理</h2>
          </div>
          
          <el-card>
            <el-form :inline="true" class="filter-form">
              <el-form-item label="客户ID">
                <el-input v-model="filters.customerId" placeholder="请输入客户ID" clearable />
              </el-form-item>
              <el-form-item label="服务人员ID">
                <el-input v-model="filters.staffId" placeholder="请输入服务人员ID" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="filters.status" placeholder="请选择状态" clearable>
                  <el-option label="全部" :value="undefined" />
                  <el-option label="待确认" :value="0" />
                  <el-option label="已确认" :value="1" />
                  <el-option label="进行中" :value="2" />
                  <el-option label="已完成" :value="3" />
                  <el-option label="已取消" :value="4" />
                  <el-option label="已拒绝" :value="5" />
                </el-select>
              </el-form-item>
              <el-form-item label="开始日期">
                <el-date-picker
                  v-model="filters.startDate"
                  type="date"
                  placeholder="选择开始日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  clearable
                />
              </el-form-item>
              <el-form-item label="结束日期">
                <el-date-picker
                  v-model="filters.endDate"
                  type="date"
                  placeholder="选择结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  clearable
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadOrders" :loading="loading">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button @click="resetFilters">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
          
          <el-card style="margin-top: 20px">
            <el-table :data="orders" v-loading="loading" stripe>
              <el-table-column prop="appointmentId" label="预约ID" width="100" />
              <el-table-column prop="customerName" label="客户姓名" width="120" />
              <el-table-column prop="staffName" label="服务人员" width="120" />
              <el-table-column prop="serviceName" label="服务项目" width="150" />
              <el-table-column prop="appointmentDate" label="预约日期" width="120" />
              <el-table-column prop="startTime" label="开始时间" width="100" />
              <el-table-column prop="endTime" label="结束时间" width="100" />
              <el-table-column prop="totalAmount" label="金额(元)" width="100">
                <template #default="scope">
                  ¥{{ scope.row.totalAmount?.toFixed(2) || '0.00' }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button
                    size="small"
                    type="primary"
                    link
                    @click="viewDetail(scope.row.appointmentId)"
                  >
                    详情
                  </el-button>
                  <el-dropdown @command="(cmd) => handleStatusChange(scope.row.appointmentId, cmd)">
                    <el-button size="small" type="primary" link>
                      修改状态 <el-icon><ArrowDown /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item :command="1" :disabled="scope.row.status === 1">设为已确认</el-dropdown-item>
                        <el-dropdown-item :command="2" :disabled="scope.row.status === 2">设为进行中</el-dropdown-item>
                        <el-dropdown-item :command="3" :disabled="scope.row.status === 3">设为已完成</el-dropdown-item>
                        <el-dropdown-item :command="4" :disabled="scope.row.status === 4">设为已取消</el-dropdown-item>
                        <el-dropdown-item :command="5" :disabled="scope.row.status === 5">设为已拒绝</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </template>
              </el-table-column>
            </el-table>
            
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :total="total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="loadOrders"
                @current-change="loadOrders"
              />
            </div>
          </el-card>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ArrowDown } from '@element-plus/icons-vue'
import { getAdminAppointments, updateAdminAppointmentStatus } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

const router = useRouter()
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const filters = ref({
  customerId: undefined,
  staffId: undefined,
  status: undefined,
  startDate: undefined,
  endDate: undefined
})

const getStatusText = (status) => {
  const statusMap = {
    0: '待确认',
    1: '已确认',
    2: '进行中',
    3: '已完成',
    4: '已取消',
    5: '已拒绝'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'primary',
    3: 'success',
    4: 'info',
    5: 'danger'
  }
  return typeMap[status] || ''
}

const loadOrders = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.value.customerId) params.customerId = filters.value.customerId
    if (filters.value.staffId) params.staffId = filters.value.staffId
    if (filters.value.status !== undefined && filters.value.status !== null) {
      params.status = filters.value.status
    }
    if (filters.value.startDate) params.startDate = filters.value.startDate
    if (filters.value.endDate) params.endDate = filters.value.endDate

    const response = await getAdminAppointments(params)
    if (response.data) {
      orders.value = Array.isArray(response.data) ? response.data : response.data.list || []
      total.value = response.data.total || orders.value.length
    } else {
      orders.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = {
    customerId: undefined,
    staffId: undefined,
    status: undefined,
    startDate: undefined,
    endDate: undefined
  }
  currentPage.value = 1
  loadOrders()
}

const handleStatusChange = async (appointmentId, status) => {
  try {
    await ElMessageBox.confirm(
      `确定要将状态修改为"${getStatusText(status)}"吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updateAdminAppointmentStatus(appointmentId, status)
    ElMessage.success('状态修改成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const viewDetail = (appointmentId) => {
  router.push(`/admin/orders/${appointmentId}`)
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped lang="scss">
.order-management-view {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

