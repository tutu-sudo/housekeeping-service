<template>
  <el-aside width="220px" class="admin-sidebar">
    <el-menu
      :default-active="activeMenu"
      router
      class="admin-menu"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
    >
      <el-menu-item index="/admin/dashboard">
        <el-icon><DataBoard /></el-icon>
        <span>仪表盘</span>
      </el-menu-item>
      
      <el-sub-menu index="order">
        <template #title>
          <el-icon><Document /></el-icon>
          <span>订单管理</span>
        </template>
        <el-menu-item index="/admin/orders">预约/订单</el-menu-item>
        <el-menu-item index="/admin/payments">支付明细</el-menu-item>
      </el-sub-menu>
      
      <el-sub-menu index="service">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>服务管理</span>
        </template>
        <el-menu-item index="/admin/services/types">服务类型</el-menu-item>
        <el-menu-item index="/admin/services/items">服务项目</el-menu-item>
      </el-sub-menu>
      
      <el-sub-menu index="staff">
        <template #title>
          <el-icon><UserFilled /></el-icon>
          <span>人员管理</span>
        </template>
        <el-menu-item index="/admin/verification">人员审核</el-menu-item>
        <el-menu-item index="/admin/schedules">排班管理</el-menu-item>
      </el-sub-menu>
      
      <el-menu-item index="/admin/reviews">
        <el-icon><ChatLineRound /></el-icon>
        <span>评价管理</span>
      </el-menu-item>
      
      <el-menu-item index="/admin/companies">
        <el-icon><OfficeBuilding /></el-icon>
        <span>公司信息</span>
      </el-menu-item>
      
      <el-menu-item index="/admin/statistics">
        <el-icon><DataAnalysis /></el-icon>
        <span>数据统计</span>
      </el-menu-item>
    </el-menu>
  </el-aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  DataBoard,
  Document,
  Setting,
  UserFilled,
  ChatLineRound,
  OfficeBuilding,
  DataAnalysis
} from '@element-plus/icons-vue'

const route = useRoute()

const activeMenu = computed(() => {
  const path = route.path
  // 根据路径返回对应的菜单项
  if (path.startsWith('/admin/dashboard')) return '/admin/dashboard'
  if (path.startsWith('/admin/orders')) return '/admin/orders'
  if (path.startsWith('/admin/payments')) return '/admin/payments'
  if (path.startsWith('/admin/services/types')) return '/admin/services/types'
  if (path.startsWith('/admin/services/items')) return '/admin/services/items'
  if (path.startsWith('/admin/verification')) return '/admin/verification'
  if (path.startsWith('/admin/schedules')) return '/admin/schedules'
  if (path.startsWith('/admin/reviews')) return '/admin/reviews'
  if (path.startsWith('/admin/companies')) return '/admin/companies'
  if (path.startsWith('/admin/statistics')) return '/admin/statistics'
  return path
})
</script>

<style scoped lang="scss">
.admin-sidebar {
  min-height: calc(100vh - 60px);
  background-color: #304156;
}

.admin-menu {
  border-right: none;
}
</style>
