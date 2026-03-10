<template>
  <div class="statistics-view">
    <Navigation />
    <div class="content">
      <el-container>
        <AdminSidebar />
        <el-main>
          <div class="page-header">
            <h2>数据统计分析</h2>
            <div class="header-actions">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                @change="loadStatistics"
              />
              <el-button type="primary" @click="loadStatistics" :loading="loading">
                <el-icon><Refresh /></el-icon>
                刷新数据
              </el-button>
            </div>
          </div>
          
          <!-- 收入统计 -->
          <el-card style="margin-bottom: 20px">
            <template #header>
              <h3>收入统计</h3>
            </template>
            <el-row :gutter="20">
              <el-col :span="6">
                <el-statistic title="总收入(元)" :value="statistics.totalRevenue" :precision="2">
                  <template #prefix>
                    <el-icon><Money /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="6">
                <el-statistic title="支付宝收入(元)" :value="statistics.alipayRevenue" :precision="2">
                  <template #prefix>
                    <el-icon><CreditCard /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="6">
                <el-statistic title="微信支付收入(元)" :value="statistics.wechatRevenue" :precision="2">
                  <template #prefix>
                    <el-icon><Money /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="6">
                <el-statistic title="支付成功订单数" :value="statistics.paidOrdersCount">
                  <template #prefix>
                    <el-icon><Document /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
            </el-row>
            
            <!-- 收入趋势图表 -->
            <div class="chart-container">
              <h4>收入趋势（按日期）</h4>
              <div ref="revenueChartRef" class="chart" style="height: 300px;"></div>
            </div>
          </el-card>
          
          <!-- 订单统计 -->
          <el-card style="margin-bottom: 20px">
            <template #header>
              <h3>订单统计</h3>
            </template>
            <el-row :gutter="20">
              <el-col :span="6">
                <el-statistic title="总订单数" :value="statistics.totalOrders" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="已完成订单" :value="statistics.completedOrders" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="进行中订单" :value="statistics.inProgressOrders" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="待确认订单" :value="statistics.pendingOrders" />
              </el-col>
            </el-row>
          </el-card>
          
          <!-- 服务类型占比统计 -->
          <el-card style="margin-bottom: 20px">
            <template #header>
              <h3>服务类型订单占比</h3>
            </template>
            <div class="service-type-stats">
              <el-table :data="serviceTypeStats" stripe>
                <el-table-column prop="serviceTypeName" label="服务类型" width="200" />
                <el-table-column prop="orderCount" label="订单数量" width="120" />
                <el-table-column prop="totalRevenue" label="总收入(元)" width="150">
                  <template #default="scope">
                    ¥{{ (scope.row.totalRevenue || 0).toFixed(2) }}
                  </template>
                </el-table-column>
                <el-table-column prop="percentage" label="占比" width="150">
                  <template #default="scope">
                    <el-progress :percentage="scope.row.percentage" :color="getProgressColor(scope.row.percentage)" />
                  </template>
                </el-table-column>
                <el-table-column prop="paymentRate" label="支付率" width="150">
                  <template #default="scope">
                    <el-progress 
                      :percentage="scope.row.paymentRate" 
                      type="success"
                      :color="getPaymentRateColor(scope.row.paymentRate)"
                    />
                  </template>
                </el-table-column>
              </el-table>
              
              <!-- 服务类型占比饼图 -->
              <div class="chart-container">
                <h4>服务类型订单占比图表</h4>
                <div ref="serviceTypeChartRef" class="chart" style="height: 400px;"></div>
              </div>
            </div>
          </el-card>
          
          <!-- 服务质量分析 -->
          <el-card style="margin-bottom: 20px">
            <template #header>
              <h3>服务质量分析</h3>
            </template>
            <el-row :gutter="20">
              <el-col :span="6">
                <el-statistic title="平均评分" :value="statistics.avgRating" :precision="2">
                  <template #prefix>
                    <el-icon><Star /></el-icon>
                  </template>
                </el-statistic>
              </el-col>
              <el-col :span="6">
                <el-statistic title="总评价数" :value="statistics.totalReviews" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="平均服务态度分" :value="statistics.avgServiceAttitude" :precision="2" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="平均专业能力分" :value="statistics.avgProfessionalAbility" :precision="2" />
              </el-col>
            </el-row>
            
            <div class="rating-breakdown" style="margin-top: 20px">
              <h4>评分分布</h4>
              <el-table :data="ratingDistribution" stripe>
                <el-table-column prop="rating" label="评分" width="100">
                  <template #default="scope">
                    <el-rate v-model="scope.row.rating" disabled />
                  </template>
                </el-table-column>
                <el-table-column prop="count" label="数量" width="150" />
                <el-table-column prop="percentage" label="占比">
                  <template #default="scope">
                    <el-progress :percentage="scope.row.percentage" />
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
          
          <!-- 客户行为分析 -->
          <el-card>
            <template #header>
              <h3>客户行为分析</h3>
            </template>
            <el-row :gutter="20">
              <el-col :span="6">
                <el-statistic title="总客户数" :value="statistics.totalCustomers" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="活跃客户数" :value="statistics.activeCustomers" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="平均订单金额(元)" :value="statistics.avgOrderAmount" :precision="2" />
              </el-col>
              <el-col :span="6">
                <el-statistic title="客户复购率" :value="statistics.repeatPurchaseRate" :precision="2">
                  <template #suffix>%</template>
                </el-statistic>
              </el-col>
            </el-row>
            
            <!-- 客户活跃度图表 -->
            <div class="chart-container">
              <h4>客户活跃度趋势</h4>
              <div ref="customerActivityChartRef" class="chart" style="height: 300px;"></div>
            </div>
          </el-card>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { Refresh, Money, CreditCard, Document, Star } from '@element-plus/icons-vue'
import { getStatisticsData } from '@/api/admin'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import * as echarts from 'echarts'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

use([
  CanvasRenderer,
  LineChart,
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const loading = ref(false)
const dateRange = ref([])

const revenueChartRef = ref(null)
const serviceTypeChartRef = ref(null)
const customerActivityChartRef = ref(null)

let revenueChart = null
let serviceTypeChart = null
let customerActivityChart = null

const statistics = ref({
  totalRevenue: 0,
  alipayRevenue: 0,
  wechatRevenue: 0,
  paidOrdersCount: 0,
  totalOrders: 0,
  completedOrders: 0,
  inProgressOrders: 0,
  pendingOrders: 0,
  avgRating: 0,
  totalReviews: 0,
  avgServiceAttitude: 0,
  avgProfessionalAbility: 0,
  totalCustomers: 0,
  activeCustomers: 0,
  avgOrderAmount: 0,
  repeatPurchaseRate: 0
})

const serviceTypeStats = ref([])
const ratingDistribution = ref([])

// 初始化图表
const initCharts = async () => {
  await nextTick()
  
  // 初始化收入趋势图
  if (revenueChartRef.value && !revenueChart) {
    revenueChart = echarts.init(revenueChartRef.value)
  }
  
  // 初始化服务类型占比图
  if (serviceTypeChartRef.value && !serviceTypeChart) {
    serviceTypeChart = echarts.init(serviceTypeChartRef.value)
  }
  
  // 初始化客户活跃度图
  if (customerActivityChartRef.value && !customerActivityChart) {
    customerActivityChart = echarts.init(customerActivityChartRef.value)
  }
  
  updateCharts()
}

// 更新图表数据
const updateCharts = () => {
  // 更新收入趋势图
  if (revenueChart) {
    const option = {
      title: {
        text: '收入趋势',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis',
        formatter: '{b}<br/>{a}: ¥{c}'
      },
      xAxis: {
        type: 'category',
        data: statistics.value.revenueTrend?.map(item => item.date) || []
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '¥{value}'
        }
      },
      series: [{
        name: '收入',
        type: 'line',
        data: statistics.value.revenueTrend?.map(item => item.amount) || [],
        smooth: true,
        itemStyle: {
          color: '#409EFF'
        }
      }]
    }
    revenueChart.setOption(option)
  }
  
  // 更新服务类型占比图
  if (serviceTypeChart && serviceTypeStats.value.length > 0) {
    const option = {
      title: {
        text: '服务类型订单占比',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [{
        type: 'pie',
        radius: '60%',
        data: serviceTypeStats.value.map(item => ({
          value: item.orderCount,
          name: item.serviceTypeName
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    }
    serviceTypeChart.setOption(option)
  }
  
  // 更新客户活跃度图
  if (customerActivityChart) {
    const option = {
      title: {
        text: '客户活跃度趋势',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: statistics.value.customerActivityTrend?.map(item => item.date) || []
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        name: '活跃客户数',
        type: 'bar',
        data: statistics.value.customerActivityTrend?.map(item => item.count) || [],
        itemStyle: {
          color: '#67C23A'
        }
      }]
    }
    customerActivityChart.setOption(option)
  }
}

const getProgressColor = (percentage) => {
  if (percentage >= 70) return '#67C23A'
  if (percentage >= 40) return '#E6A23C'
  return '#F56C6C'
}

const getPaymentRateColor = (percentage) => {
  if (percentage >= 80) return '#67C23A'
  if (percentage >= 60) return '#E6A23C'
  return '#F56C6C'
}

const loadStatistics = async () => {
  loading.value = true
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const response = await getStatisticsData(params)
    const data = response.data?.data || response.data || {}
    
    // 更新统计数据
    statistics.value = {
      totalRevenue: data.totalRevenue || 0,
      alipayRevenue: data.alipayRevenue || 0,
      wechatRevenue: data.wechatRevenue || 0,
      paidOrdersCount: data.paidOrdersCount || 0,
      totalOrders: data.totalOrders || 0,
      completedOrders: data.completedOrders || 0,
      inProgressOrders: data.inProgressOrders || 0,
      pendingOrders: data.pendingOrders || 0,
      avgRating: data.avgRating || 0,
      totalReviews: data.totalReviews || 0,
      avgServiceAttitude: data.avgServiceAttitude || 0,
      avgProfessionalAbility: data.avgProfessionalAbility || 0,
      totalCustomers: data.totalCustomers || 0,
      activeCustomers: data.activeCustomers || 0,
      avgOrderAmount: data.avgOrderAmount || 0,
      repeatPurchaseRate: data.repeatPurchaseRate || 0,
      revenueTrend: data.revenueTrend || [],
      customerActivityTrend: data.customerActivityTrend || []
    }
    
    // 更新服务类型统计
    const totalOrders = data.totalOrders || 1 // 避免除以0
    serviceTypeStats.value = (data.serviceTypeStats || []).map(item => {
      const orderCount = item.orderCount || 0
      const paidCount = item.paidCount || 0
      return {
        ...item,
        orderCount,
        totalRevenue: item.totalRevenue || 0,
        percentage: totalOrders > 0 ? parseFloat(((orderCount / totalOrders) * 100).toFixed(2)) : 0,
        paymentRate: orderCount > 0 ? parseFloat(((paidCount / orderCount) * 100).toFixed(2)) : 0
      }
    }).sort((a, b) => b.orderCount - a.orderCount) // 按订单数降序排列
    
    // 更新评分分布
    const totalReviewCount = data.totalReviews || 1
    ratingDistribution.value = (data.ratingDistribution || []).map(item => ({
      ...item,
      percentage: totalReviewCount > 0 ? parseFloat(((item.count / totalReviewCount) * 100).toFixed(2)) : 0
    }))
    
    // 更新图表
    updateCharts()
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 如果接口不存在，使用模拟数据
    ElMessage.warning('统计数据接口暂未实现，显示模拟数据')
    loadMockData()
  } finally {
    loading.value = false
  }
}

// 加载模拟数据（用于开发测试）
const loadMockData = () => {
  statistics.value = {
    totalRevenue: 125000.50,
    alipayRevenue: 80000.30,
    wechatRevenue: 45000.20,
    paidOrdersCount: 150,
    totalOrders: 180,
    completedOrders: 120,
    inProgressOrders: 20,
    pendingOrders: 40,
    avgRating: 4.6,
    totalReviews: 95,
    avgServiceAttitude: 4.7,
    avgProfessionalAbility: 4.5,
    totalCustomers: 85,
    activeCustomers: 65,
    avgOrderAmount: 694.45,
    repeatPurchaseRate: 52.3,
    revenueTrend: [
      { date: '2026-01-01', amount: 5000 },
      { date: '2026-01-02', amount: 6200 },
      { date: '2026-01-03', amount: 5800 },
      { date: '2026-01-04', amount: 7500 },
      { date: '2026-01-05', amount: 6900 }
    ],
    customerActivityTrend: [
      { date: '2026-01-01', count: 20 },
      { date: '2026-01-02', count: 25 },
      { date: '2026-01-03', count: 22 },
      { date: '2026-01-04', count: 28 },
      { date: '2026-01-05', count: 30 }
    ]
  }
  
  serviceTypeStats.value = [
    { serviceTypeName: '日常保洁', orderCount: 45, totalRevenue: 2475, paidCount: 40, totalOrders: 180 },
    { serviceTypeName: '月嫂服务', orderCount: 35, totalRevenue: 7350, paidCount: 32, totalOrders: 180 },
    { serviceTypeName: '育儿嫂服务', orderCount: 30, totalRevenue: 4500, paidCount: 28, totalOrders: 180 },
    { serviceTypeName: '家电清洗', orderCount: 25, totalRevenue: 2000, paidCount: 22, totalOrders: 180 },
    { serviceTypeName: '其他服务', orderCount: 15, totalRevenue: 750, paidCount: 12, totalOrders: 180 }
  ].map(item => ({
    ...item,
    percentage: parseFloat(((item.orderCount / 180) * 100).toFixed(2)),
    paymentRate: parseFloat(((item.paidCount / item.orderCount) * 100).toFixed(2))
  }))
  
  ratingDistribution.value = [
    { rating: 5, count: 45, percentage: 47.4 },
    { rating: 4, count: 35, percentage: 36.8 },
    { rating: 3, count: 10, percentage: 10.5 },
    { rating: 2, count: 3, percentage: 3.2 },
    { rating: 1, count: 2, percentage: 2.1 }
  ]
  
  updateCharts()
}

// 监听窗口大小变化，调整图表大小
watch(() => serviceTypeStats.value, () => {
  updateCharts()
}, { deep: true })

onMounted(async () => {
  await initCharts()
  loadStatistics()
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    revenueChart?.resize()
    serviceTypeChart?.resize()
    customerActivityChart?.resize()
  })
})
</script>

<style scoped lang="scss">
.statistics-view {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.content {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h2 {
    margin: 0;
    color: #333;
    font-size: 24px;
    font-weight: 500;
  }
  
  .header-actions {
    display: flex;
    gap: 10px;
    align-items: center;
  }
}

.chart-container {
  margin-top: 30px;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 4px;
  
  h4 {
    margin: 0 0 20px 0;
    color: #333;
    font-size: 16px;
  }
}

.service-type-stats {
  .chart-container {
    margin-top: 30px;
  }
}

.rating-breakdown {
  h4 {
    margin-bottom: 20px;
    color: #333;
    font-size: 16px;
  }
}
</style>
