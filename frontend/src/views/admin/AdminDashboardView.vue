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
          
          <el-row :gutter="20" class="statistics-row">
            <el-col :span="6">
              <el-card class="stat-card">
                <el-statistic title="总订单数" :value="statistics.totalOrders">
                  <template #prefix>
                    <el-icon><Document /></el-icon>
                  </template>
                </el-statistic>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">
                <el-statistic title="今日订单" :value="statistics.todayOrders">
                  <template #prefix>
                    <el-icon><Calendar /></el-icon>
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
                <el-statistic title="总收入(元)" :value="statistics.totalRevenue" :precision="2">
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
                <el-statistic title="待审核人员" :value="statistics.pendingStaff">
                  <template #prefix>
                    <el-icon><Clock /></el-icon>
                  </template>
                </el-statistic>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">
                <el-statistic title="服务类型数" :value="statistics.serviceTypes">
                  <template #prefix>
                    <el-icon><Setting /></el-icon>
                  </template>
                </el-statistic>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">
                <el-statistic title="服务项目数" :value="statistics.services">
                  <template #prefix>
                    <el-icon><List /></el-icon>
                  </template>
                </el-statistic>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">
                <el-statistic title="待确认订单" :value="statistics.pendingOrders">
                  <template #prefix>
                    <el-icon><Warning /></el-icon>
                  </template>
                </el-statistic>
              </el-card>
            </el-col>
          </el-row>
          
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
  getAdminAppointments,
  getStaffList,
  getAdminServiceTypes,
  getAdminServices
} from '@/api/admin'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
import Charts from '@/components/admin/Charts.vue'

const statistics = ref({
  totalOrders: 0,
  todayOrders: 0,
  totalStaff: 0,
  totalRevenue: 0,
  pendingStaff: 0,
  serviceTypes: 0,
  services: 0,
  pendingOrders: 0
})

const loadStatistics = async () => {
  try {
    // 加载订单统计
    const ordersRes = await getAdminAppointments({})
    const orders = ordersRes.data?.data || ordersRes.data || []
    statistics.value.totalOrders = orders.length
    
    // 今日订单
    const today = new Date().toISOString().split('T')[0]
    statistics.value.todayOrders = orders.filter(
      order => order.appointmentDate === today
    ).length
    
    // 待确认订单
    statistics.value.pendingOrders = orders.filter(
      order => order.status === 0
    ).length
    
    // 加载服务人员统计
    const staffRes = await getStaffList({})
    const staffList = staffRes.data?.data || staffRes.data || []
    statistics.value.totalStaff = staffList.length
    statistics.value.pendingStaff = staffList.filter(
      staff => staff.verificationStatus === 0
    ).length
    
    // 加载服务类型和项目统计
    const typesRes = await getAdminServiceTypes()
    const types = typesRes.data?.data || typesRes.data || []
    statistics.value.serviceTypes = types.length
    
    const servicesRes = await getAdminServices({})
    const services = servicesRes.data?.data || servicesRes.data || []
    statistics.value.services = services.length
    
    // 计算总收入（从已完成的订单）
    const completedOrders = orders.filter(order => order.status === 3)
    statistics.value.totalRevenue = completedOrders.reduce(
      (sum, order) => sum + (order.totalAmount || 0),
      0
    )
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped lang="scss">
.admin-dashboard-view {
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

