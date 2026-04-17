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
              <el-form-item label="客户姓名">
                <el-input v-model="filters.customerName" placeholder="请输入客户姓名" clearable />
              </el-form-item>
              <el-form-item label="服务人员姓名">
                <el-input v-model="filters.staffName" placeholder="请输入服务人员姓名" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <el-select
                  v-model="filters.status"
                  placeholder="请选择状态"
                  clearable
                  style="min-width: 180px"
                >
                  <el-option label="全部" :value="''" />
                  <el-option label="待确认" :value="0" />
                  <el-option label="已接单" :value="1" />
                  <el-option label="进行中" :value="2" />
                  <el-option label="已完成" :value="3" />
                  <el-option label="已取消" :value="4" />
                  <el-option label="已拒单/已关闭" :value="5" />
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
                <el-button @click="setLast30Days">最近30天</el-button>
                <el-button @click="resetFilters">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
          
          <el-card style="margin-top: 20px">
            <el-table :data="pagedOrders" v-loading="loading" stripe>
              <el-table-column prop="appointmentId" label="预约ID" width="100" />
              <el-table-column prop="customerName" label="客户姓名" width="120" />
              <el-table-column prop="staffName" label="服务人员" width="120" />
              <el-table-column prop="serviceName" label="服务项目" width="150" />
              <el-table-column prop="appointmentDate" label="预约日期" width="120" />
              <el-table-column prop="startTime" label="开始时间" width="100" />
              <el-table-column prop="endTime" label="结束时间" width="100" />
              <el-table-column prop="billingType" label="结算方式" width="110" align="center">
                <template #default="scope">
                  <el-tag size="small" type="info">
                    {{ getBillingTypeText(scope.row.billingType) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="paymentStatus" label="支付状态" width="110" align="center">
                <template #default="scope">
                  <el-tag size="small" :type="getPaymentStatusType(scope.row.paymentStatus)">
                    {{ getPaymentStatusText(scope.row.paymentStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
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
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="scope">
                  <el-button
                    size="small"
                    type="primary"
                    link
                    @click="viewDetail(scope.row.appointmentId)"
                  >
                    详情
                  </el-button>
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
                @size-change="handlePageSizeChange"
                @current-change="handlePageChange"
              />
            </div>
          </el-card>
          
          <!-- 预约详情对话框 -->
          <el-dialog
            v-model="detailDialogVisible"
            title="预约详情"
            width="800px"
            @close="appointmentDetail = null"
          >
            <div v-loading="detailLoading">
              <el-descriptions :column="2" border v-if="appointmentDetail">
                <el-descriptions-item label="预约ID">
                  {{ appointmentDetail.appointmentId }}
                </el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="getStatusType(appointmentDetail.status)">
                    {{ getStatusText(appointmentDetail.status) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="客户ID">
                  {{ appointmentDetail.customerId }}
                </el-descriptions-item>
                <el-descriptions-item label="客户姓名">
                  {{ appointmentDetail.customerName || '未知' }}
                </el-descriptions-item>
              <el-descriptions-item label="联系方式">
                {{ appointmentDetail.customerPhone || appointmentDetail.contactPhone || '未知' }}
              </el-descriptions-item>
                <el-descriptions-item label="服务人员ID">
                  {{ appointmentDetail.staffId }}
                </el-descriptions-item>
                <el-descriptions-item label="服务人员">
                  {{ appointmentDetail.staffName || '未知' }}
                </el-descriptions-item>
                <el-descriptions-item label="服务ID">
                  {{ appointmentDetail.serviceId }}
                </el-descriptions-item>
                <el-descriptions-item label="服务项目">
                  {{ appointmentDetail.serviceName || '未知' }}
                </el-descriptions-item>
                <el-descriptions-item label="预约日期">
                  {{ appointmentDetail.appointmentDate }}
                </el-descriptions-item>
                <el-descriptions-item label="开始时间">
                  {{ appointmentDetail.startTime }}
                </el-descriptions-item>
                <el-descriptions-item label="结束时间">
                  {{ appointmentDetail.endTime }}
                </el-descriptions-item>
                <el-descriptions-item label="总金额">
                  ¥{{ appointmentDetail.totalAmount?.toFixed(2) || '0.00' }}
                </el-descriptions-item>
                <el-descriptions-item label="服务地址" :span="2">
                  {{ appointmentDetail.serviceAddress || '未填写' }}
                </el-descriptions-item>
                <el-descriptions-item label="备注" :span="2">
                  {{ appointmentDetail.remarks || '无' }}
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">
                  {{ appointmentDetail.createTime }}
                </el-descriptions-item>
                <el-descriptions-item label="更新时间">
                  {{ appointmentDetail.updateTime || '未知' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-dialog>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getAdminAppointments, getAdminAppointmentDetail } from '@/api/admin'
import { getUserInfo } from '@/api/user'
import { getStaffDetail } from '@/api/staff'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
import {
  normalizePaymentStatus,
  getPaymentStatusType as getUnifiedPaymentStatusType,
  getPaymentStatusText as getUnifiedPaymentStatusText
} from '@/utils/payment'

const router = useRouter()
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const detailDialogVisible = ref(false)
const appointmentDetail = ref(null)
const detailLoading = ref(false)

const filters = ref({
  customerName: '',
  staffName: '',
  status: null,
  startDate: '',
  endDate: ''
})

// 缓存：避免列表里同一用户/员工重复请求
const customerNameCache = new Map()
const staffNameCache = new Map()

const isBlank = (v) => v === null || v === undefined || String(v).trim() === ''

const pickId = (row, keys) => {
  for (const k of keys) {
    const val = row?.[k]
    if (!isBlank(val)) return val
  }
  return null
}

const fetchCustomerUsername = async (customerId) => {
  const id = Number(customerId)
  if (!Number.isFinite(id)) return ''
  if (customerNameCache.has(id)) return customerNameCache.get(id)
  try {
    const res = await getUserInfo(id)
    const data = res.data?.data || res.data || {}
    const username = data.username || data.userName || data.name || ''
    customerNameCache.set(id, username)
    return username
  } catch (e) {
    customerNameCache.set(id, '')
    return ''
  }
}

const fetchStaffName = async (staffId) => {
  const id = Number(staffId)
  if (!Number.isFinite(id)) return ''
  if (staffNameCache.has(id)) return staffNameCache.get(id)
  try {
    const res = await getStaffDetail(id)
    const data = res.data?.data || res.data || {}
    const name = data.name || data.staffName || ''
    staffNameCache.set(id, name)
    return name
  } catch (e) {
    staffNameCache.set(id, '')
    return ''
  }
}

const enrichOrderNames = async (list) => {
  if (!Array.isArray(list) || list.length === 0) return

  const needCustomerIds = new Set()
  const needStaffIds = new Set()

  for (const row of list) {
    const cid = pickId(row, ['customerId', 'customer_id', 'userId', 'user_id'])
    const sid = pickId(row, ['staffId', 'staff_id'])
    if (isBlank(row.customerName) && !isBlank(cid)) needCustomerIds.add(Number(cid))
    if (isBlank(row.staffName) && !isBlank(sid)) needStaffIds.add(Number(sid))
  }

  const tasks = []
  for (const id of needCustomerIds) tasks.push(fetchCustomerUsername(id))
  for (const id of needStaffIds) tasks.push(fetchStaffName(id))
  await Promise.allSettled(tasks)

  for (const row of list) {
    const cid = pickId(row, ['customerId', 'customer_id', 'userId', 'user_id'])
    const sid = pickId(row, ['staffId', 'staff_id'])
    if (isBlank(row.customerName) && !isBlank(cid)) {
      row.customerName = customerNameCache.get(Number(cid)) || row.customerName || ''
    }
    if (isBlank(row.staffName) && !isBlank(sid)) {
      row.staffName = staffNameCache.get(Number(sid)) || row.staffName || ''
    }
  }
}

// 使用详情接口补全一条预约记录的关键信息（服务项目、结算方式、支付状态等）
const enrichOrderDetails = async (list) => {
  if (!Array.isArray(list) || list.length === 0) return

  const tasks = list.map(async (row) => {
    const id = row.appointmentId || row.id
    if (!id) return
    try {
      const res = await getAdminAppointmentDetail(id)
      const detail = normalizeAppointmentRow(res.data?.data || res.data || {})
      if (!detail) return

      // 只在列表缺少时才用详情补齐，避免覆盖后端列表里已有值
      row.serviceName = row.serviceName || detail.serviceName
      row.billingType = row.billingType || detail.billingType
      if (row.paymentStatus === undefined || row.paymentStatus === null || row.paymentStatus === '') {
        row.paymentStatus = detail.paymentStatus
      }
      if (row.totalAmount === undefined || row.totalAmount === null) {
        row.totalAmount = detail.totalAmount
      }
    } catch (e) {
      // 单条失败不影响整体列表
      console.warn('补全预约详情失败 appointmentId=', id, e)
    }
  })

  await Promise.allSettled(tasks)
}

const getStatusText = (status) => {
  // 订单状态文案以后端文档为准：
  // 0: 待确认, 1: 已接单, 2: 进行中, 3: 已完成, 4: 已取消, 5: 已拒单/已关闭
  const statusMap = {
    0: '待确认',
    1: '已接单',
    2: '进行中',
    3: '已完成',
    4: '已取消',
    5: '已拒单/已关闭'
  }
  return statusMap[status] || '未知状态'
}

const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'primary',
    2: 'primary',
    3: 'success',
    4: 'info',
    5: 'danger'
  }
  return typeMap[status] || 'info'
}

const getBillingTypeText = (billingType) => {
  const map = {
    hourly: '按小时',
    daily: '按天',
    times: '按次数'
  }
  return map[billingType] || (billingType ? String(billingType) : '未知')
}

const getPaymentStatusText = (paymentStatus) => {
  return getUnifiedPaymentStatusText(paymentStatus)
}

const getPaymentStatusType = (paymentStatus) => {
  return getUnifiedPaymentStatusType(paymentStatus)
}

// 统一归一化一条预约记录的字段，保证列表与详情展示一致
const normalizeAppointmentRow = (row) => {
  if (!row || typeof row !== 'object') return row

  // 订单状态字段兼容：status / orderStatus / order_status
  const rawStatus = row.status ?? row.orderStatus ?? row.order_status
  const status =
    typeof rawStatus === 'string' && /^\d+$/.test(rawStatus) ? parseInt(rawStatus, 10) : rawStatus

  // 结算方式字段兼容：billingType / billing_type / settlementType
  const billingType =
    row.billingType ?? row.billing_type ?? row.settlementType ?? row.settlement_type

  const paymentStatus = normalizePaymentStatus(row)

  return {
    ...row,
    status,
    billingType,
    paymentStatus
  }
}

const toYmd = (date) => {
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

const setLast30Days = () => {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 29)
  filters.value.startDate = toYmd(start)
  filters.value.endDate = toYmd(end)
  currentPage.value = 1
  loadOrders()
}

const loadOrders = async () => {
  loading.value = true
  try {
    const params = {}
    // 只传有值的筛选参数（按姓名模糊查询）
    if (filters.value.customerName && String(filters.value.customerName).trim()) {
      // 按姓名模糊查询，后端可按 LIKE 处理；若后端暂不支持，则由前端在本地再做一次过滤
      params.customerName = String(filters.value.customerName).trim()
    }
    if (filters.value.staffName && String(filters.value.staffName).trim()) {
      params.staffName = String(filters.value.staffName).trim()
    }

    // 状态为 null/undefined/空字符串 时都视为“不筛选状态”
    if (!isBlank(filters.value.status)) {
      params.status = filters.value.status
    }
    if (filters.value.startDate) params.startDate = filters.value.startDate
    if (filters.value.endDate) params.endDate = filters.value.endDate

    const response = await getAdminAppointments(params)
    // 统一Result格式：{ code, message, data, timestamp }
    const payload = response.data?.data ?? response.data
    const list = Array.isArray(payload) ? payload : (payload?.list || [])
    orders.value = (Array.isArray(list) ? list : []).map(normalizeAppointmentRow)
    // 先补全姓名，再用详情接口补全服务项目/支付状态等关键信息
    await enrichOrderNames(orders.value)
    await enrichOrderDetails(orders.value)
    total.value = orders.value.length
    // 如果当前页超过总页数，回到最后一页
    const maxPage = Math.max(1, Math.ceil(total.value / pageSize.value))
    if (currentPage.value > maxPage) currentPage.value = maxPage
  } catch (error) {
    console.error('获取订单列表失败:', error)
    const msg = error.response?.data?.message || error.message || '获取订单列表失败'
    ElMessage.error(msg)
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = {
    customerName: '',
    staffName: '',
    status: null,
    startDate: '',
    endDate: ''
  }
  currentPage.value = 1
  // 重置后也给一个合理默认（最近30天），避免拉取全量历史
  setLast30Days()
}

// 在前端再做一层模糊筛选，确保在后端暂不支持姓名模糊时，也能正常按姓名/状态过滤
const filteredOrders = computed(() => {
  let list = [...orders.value]

  const nameKeyword = String(filters.value.customerName || '').trim().toLowerCase()
  const staffKeyword = String(filters.value.staffName || '').trim().toLowerCase()
  const statusFilter = isBlank(filters.value.status) ? null : Number(filters.value.status)

  if (nameKeyword) {
    list = list.filter((row) => {
      const name = String(row.customerName || '').toLowerCase()
      return name.includes(nameKeyword)
    })
  }

  if (staffKeyword) {
    list = list.filter((row) => {
      const name = String(row.staffName || '').toLowerCase()
      return name.includes(staffKeyword)
    })
  }

  if (statusFilter !== null && !Number.isNaN(statusFilter)) {
    list = list.filter((row) => {
      const val = row.status
      return Number(val) === statusFilter
    })
  }

  return list
})

const pagedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredOrders.value.slice(start, start + pageSize.value)
})

const handlePageChange = (page) => {
  currentPage.value = page
}

const handlePageSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const viewDetail = async (appointmentId) => {
  detailLoading.value = true
  detailDialogVisible.value = true
  try {
    const response = await getAdminAppointmentDetail(appointmentId)
    appointmentDetail.value = normalizeAppointmentRow(response.data?.data || response.data)
    // 详情接口如果没带名字，也做一次补全
    if (appointmentDetail.value) {
      const list = [appointmentDetail.value]
      await enrichOrderNames(list)
      appointmentDetail.value = list[0]
    }
  } catch (error) {
    console.error('获取预约详情失败:', error)
    ElMessage.error('获取预约详情失败')
    detailDialogVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  // 默认展示最近30天，避免后台页面打开就全量拉历史数据
  setLast30Days()
})
</script>

<style scoped lang="scss">
.order-management-view {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

