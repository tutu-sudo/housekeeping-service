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

use([
  CanvasRenderer,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const orderTrendOption = ref({
  title: {
    text: '订单趋势'
  },
  tooltip: {
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: ['1月', '2月', '3月', '4月', '5月', '6月']
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      data: [120, 132, 101, 134, 90, 230],
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
      data: [
        { value: 1048, name: '保洁' },
        { value: 735, name: '月嫂' },
        { value: 580, name: '育儿' },
        { value: 484, name: '其他' }
      ]
    }
  ]
})

onMounted(() => {
  // TODO: 加载实际数据
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

