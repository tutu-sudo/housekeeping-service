<template>
  <div class="admin-dashboard-view">
    <Navigation />
    <div class="content">
      <el-container>
        <AdminSidebar />
        <el-main>
          <div class="page-header">
            <h2>数据概览</h2>
          </div>
          
          <div v-loading="loading">
            <el-row :gutter="20" class="statistics-row">
              <el-col :span="6">
                <el-card class="stat-card">
                  <el-statistic title="今日预约" :value="statistics.todayAppointments">
                    <template #prefix>
                      <el-icon><Calendar /></el-icon>
                    </template>
                  </el-statistic>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <el-statistic title="今日收入(元)" :value="statistics.todayRevenue" :precision="2">
                    <template #prefix>
                      <el-icon><Money /></el-icon>
                    </template>
                  </el-statistic>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <el-statistic title="本月预约" :value="statistics.monthAppointments">
                    <template #prefix>
                      <el-icon><Document /></el-icon>
                    </template>
                  </el-statistic>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <el-statistic title="本月收入(元)" :value="statistics.monthRevenue" :precision="2">
                    <template #prefix>
                      <el-icon><Money /></el-icon>
                    </template>
                  </el-statistic>
                </el-card>
              </el-col>
            </el-row>
            
            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :span="6">
                <el-card class="stat-card">
                  <el-statistic title="待确认预约" :value="statistics.pendingAppointments">
                    <template #prefix>
                      <el-icon><Warning /></el-icon>
                    </template>
                  </el-statistic>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <el-statistic title="进行中预约" :value="statistics.inProgressAppointments">
                    <template #prefix>
                      <el-icon><Clock /></el-icon>
                    </template>
                  </el-statistic>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <el-statistic title="服务人员数" :value="statistics.totalStaff">
                    <template #prefix>
                      <el-icon><UserFilled /></el-icon>
                    </template>
                  </el-statistic>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <el-statistic title="总顾客数" :value="statistics.totalCustomers">
                    <template #prefix>
                      <el-icon><UserFilled /></el-icon>
                    </template>
                  </el-statistic>
                </el-card>
              </el-col>
            </el-row>
            
            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :span="6">
                <el-card class="stat-card">
                  <el-statistic title="平均评分" :value="statistics.averageRating" :precision="2">
                    <template #prefix>
                      <el-icon><Star /></el-icon>
                    </template>
                  </el-statistic>
                </el-card>
              </el-col>
            </el-row>
          </div>
          
          <el-divider />
          
          <h3>数据图表</h3>
          <Charts />
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  Document,
  Calendar,
  UserFilled,
  Money,
  Clock,
  Setting,
  List,
  Warning
} from '@element-plus/icons-vue'
import {
  getDashboardStatistics,
  getAdminAppointments,
  getStaffList,
  getAdminServiceTypes,
  getAdminServices
} from '@/api/admin'
import { ElMessage } from 'element-plus'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
import Charts from '@/components/admin/Charts.vue'

const statistics = ref({
  todayAppointments: 0,
  todayRevenue: 0,
  monthAppointments: 0,
  monthRevenue: 0,
  pendingAppointments: 0,
  inProgressAppointments: 0,
  totalStaff: 0,
  totalCustomers: 0,
  averageRating: 0,
  last7DaysTrend: [],
  last7DaysRevenue: [],
  topServiceTypes: [],
  topStaff: []
})

const loading = ref(false)

const loadStatistics = async () => {
  loading.value = true
  try {
    // 优先使用新的dashboard接口
    const response = await getDashboardStatistics()
    const data = response.data?.data || response.data || {}
    
    statistics.value = {
      todayAppointments: data.todayAppointments || 0,
      todayRevenue: data.todayRevenue || 0,
      monthAppointments: data.monthAppointments || 0,
      monthRevenue: data.monthRevenue || 0,
      pendingAppointments: data.pendingAppointments || 0,
      inProgressAppointments: data.inProgressAppointments || 0,
      totalStaff: data.totalStaff || 0,
      totalCustomers: data.totalCustomers || 0,
      averageRating: data.averageRating || 0,
      last7DaysTrend: data.last7DaysTrend || [],
      last7DaysRevenue: data.last7DaysRevenue || [],
      topServiceTypes: data.topServiceTypes || [],
      topStaff: data.topStaff || []
    }
  } catch (error) {
    console.warn('Dashboard接口不可用，使用降级方案:', error)
    // 如果新接口不可用，使用旧的统计方法作为降级方案
    // 不显示错误消息，因为request.js已经显示了，这里静默降级
    await loadStatisticsFallback()
  } finally {
    loading.value = false
  }
}

// 降级方案：使用旧的接口计算统计数据
const loadStatisticsFallback = async () => {
  try {
    // 加载订单统计
    const ordersRes = await getAdminAppointments({})
    const orders = ordersRes.data?.data || ordersRes.data || []
    
    // 今日订单和本月订单
    const today = new Date().toISOString().split('T')[0]
    const todayOrders = orders.filter(order => order.appointmentDate === today)
    const todayDate = new Date()
    const monthStart = new Date(todayDate.getFullYear(), todayDate.getMonth(), 1).toISOString().split('T')[0]
    const monthOrders = orders.filter(order => order.appointmentDate >= monthStart)
    
    // 计算今日收入（从已完成的订单）
    const todayCompletedOrders = todayOrders.filter(order => order.status === 3)
    const todayRevenue = todayCompletedOrders.reduce((sum, order) => sum + (order.totalAmount || 0), 0)
    
    // 计算本月收入
    const monthCompletedOrders = monthOrders.filter(order => order.status === 3)
    const monthRevenue = monthCompletedOrders.reduce((sum, order) => sum + (order.totalAmount || 0), 0)
    
    // 待确认和进行中订单
    const pendingAppointments = orders.filter(order => order.status === 0).length
    const inProgressAppointments = orders.filter(order => order.status === 2).length
    
    // 加载服务人员统计
    const staffRes = await getStaffList({})
    const staffList = staffRes.data?.data || staffRes.data || []
    
    // 加载服务类型统计
    const typesRes = await getAdminServiceTypes()
    const types = typesRes.data?.data || typesRes.data || []
    
    statistics.value = {
      todayAppointments: todayOrders.length,
      todayRevenue: todayRevenue,
      monthAppointments: monthOrders.length,
      monthRevenue: monthRevenue,
      pendingAppointments: pendingAppointments,
      inProgressAppointments: inProgressAppointments,
      totalStaff: staffList.length,
      totalCustomers: 0, // 旧接口无法获取顾客数，设为0
      averageRating: 0, // 旧接口无法获取平均评分，设为0
      last7DaysTrend: [],
      last7DaysRevenue: [],
      topServiceTypes: [],
      topStaff: []
    }
  } catch (error) {
    console.error('降级方案也失败了:', error)
    ElMessage.error('加载统计数据失败，请检查后端接口')
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped lang="scss">
.admin-dashboard-view {
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

.statistics-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  
  :deep(.el-statistic) {
    .el-statistic__head {
      font-size: 14px;
      color: #909399;
      margin-bottom: 10px;
    }
    
    .el-statistic__content {
      .el-statistic__number {
        font-size: 28px;
        font-weight: bold;
        color: #303133;
      }
    }
  }
}

h3 {
  margin: 20px 0;
  color: #333;
  font-size: 18px;
  font-weight: 500;
}
</style>

