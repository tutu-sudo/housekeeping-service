<template>
  <div class="charts-container">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <h3>订单趋势</h3>
          </template>
          <v-chart class="chart" :option="orderTrendOption" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <h3>服务类型分布</h3>
          </template>
          <v-chart class="chart" :option="serviceTypeOption" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { ElMessage } from 'element-plus'
import {
  getAppointmentStatisticsByDate,
  getAppointmentStatisticsByServiceType,
  getStatisticsData
} from '@/api/admin'

use([
  CanvasRenderer,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const fallbackTrend = {
  xAxis: ['1月', '2月', '3月', '4月', '5月', '6月'],
  series: [120, 132, 101, 134, 90, 230]
}

const fallbackServiceType = [
  { value: 1048, name: '保洁' },
  { value: 735, name: '月嫂' },
  { value: 580, name: '育儿' },
  { value: 484, name: '其他' }
]

const orderTrendOption = ref({
  title: {
    text: '订单趋势'
  },
  tooltip: {
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: [...fallbackTrend.xAxis]
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      data: [...fallbackTrend.series],
      type: 'line'
    }
  ]
})

const serviceTypeOption = ref({
  title: {
    text: '服务类型分布'
  },
  tooltip: {
    trigger: 'item'
  },
  series: [
    {
      type: 'pie',
      data: [...fallbackServiceType]
    }
  ]
})

const extractData = (response) => {
  const body = response?.data
  if (body && typeof body === 'object' && 'code' in body) {
    if (body.code !== 200) {
      throw new Error(body.message || '接口返回异常')
    }
    return body.data
  }
  return body
}

const toDateLabel = (item) => {
  const rawDate = item?.date || item?.statDate || item?.day || item?.label || item?.name
  if (!rawDate) return ''
  return String(rawDate)
}

const toCount = (item) => {
  const value =
    item?.count ??
    item?.orderCount ??
    item?.appointmentCount ??
    item?.totalCount ??
    item?.value
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : 0
}

const toTypeName = (item) =>
  item?.serviceTypeName || item?.serviceName || item?.name || item?.typeName || '未分类'

const buildServiceTypeDataFromStatistics = (statisticsData) => {
  const stats = statisticsData?.serviceTypeStats
  if (!Array.isArray(stats)) return []
  return stats.map(item => ({
    name: toTypeName(item),
    value: toCount(item)
  })).filter(item => item.value > 0)
}

const applyFallbackCharts = () => {
  orderTrendOption.value = {
    ...orderTrendOption.value,
    xAxis: { ...orderTrendOption.value.xAxis, data: [...fallbackTrend.xAxis] },
    series: [{ ...orderTrendOption.value.series[0], data: [...fallbackTrend.series] }]
  }

  serviceTypeOption.value = {
    ...serviceTypeOption.value,
    series: [{ ...serviceTypeOption.value.series[0], data: [...fallbackServiceType] }]
  }
}

const loadChartsData = async () => {
  try {
    const [trendRes, statisticsRes, typeRes] = await Promise.all([
      getAppointmentStatisticsByDate({}),
      getStatisticsData({}),
      getAppointmentStatisticsByServiceType({})
    ])

    const trendDataRaw = extractData(trendRes)
    const statisticsDataRaw = extractData(statisticsRes)
    const typeDataRaw = extractData(typeRes)

    const trendList = Array.isArray(trendDataRaw) ? trendDataRaw : []
    const typeList = Array.isArray(typeDataRaw) ? typeDataRaw : []

    const trendXAxis = trendList.map(toDateLabel).filter(Boolean)
    const trendSeries = trendList.map(toCount)
    // 先与“服务统计分析”页统一口径（statisticsData.serviceTypeStats），
    // 若该数据不可用再回退到 service-type 接口，减少口径不一致。
    const serviceTypeDataFromStatistics = buildServiceTypeDataFromStatistics(statisticsDataRaw)
    const serviceTypeDataFromTypeApi = typeList.map(item => ({
      name: toTypeName(item),
      value: toCount(item)
    })).filter(item => item.value > 0)
    const serviceTypeData = serviceTypeDataFromStatistics.length > 0
      ? serviceTypeDataFromStatistics
      : serviceTypeDataFromTypeApi

    orderTrendOption.value = {
      ...orderTrendOption.value,
      xAxis: {
        ...orderTrendOption.value.xAxis,
        data: trendXAxis.length > 0 ? trendXAxis : [...fallbackTrend.xAxis]
      },
      series: [{
        ...orderTrendOption.value.series[0],
        data: trendSeries.length > 0 ? trendSeries : [...fallbackTrend.series]
      }]
    }

    serviceTypeOption.value = {
      ...serviceTypeOption.value,
      series: [{
        ...serviceTypeOption.value.series[0],
        data: serviceTypeData.length > 0 ? serviceTypeData : [...fallbackServiceType]
      }]
    }
  } catch (error) {
    console.error('加载仪表盘图表数据失败，使用兜底数据:', error)
    applyFallbackCharts()
    ElMessage.warning('图表接口异常，已展示默认数据')
  }
}

onMounted(() => {
  loadChartsData()
})
</script>

<style scoped lang="scss">
.charts-container {
  margin-top: 20px;
  
  h3 {
    margin: 0;
    font-size: 16px;
  }
  
  .chart {
    height: 300px;
    width: 100%;
  }
}
</style>

