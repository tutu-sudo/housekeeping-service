<template>
  <el-header class="navigation-header">
    <div class="nav-container">
      <div class="logo" @click="$router.push('/home')">
        <div class="logo-icon">
          <el-icon><House /></el-icon>
        </div>
        <h1>家政服务平台</h1>
      </div>
      <el-menu
        mode="horizontal"
        :default-active="activeIndex"
        router
        class="nav-menu"
      >
        <el-menu-item index="/home">首页</el-menu-item>
        <el-menu-item index="/housekeepers">产品服务</el-menu-item>
        <el-menu-item index="/appointment" v-if="isLoggedIn && userRole === 'customer'">
          预约服务
        </el-menu-item>
        <el-menu-item index="/housekeeper-info" v-if="isLoggedIn && userRole === 'customer'">
          家政服务资料
        </el-menu-item>
        <el-menu-item index="/profile" v-if="isLoggedIn && userRole === 'customer'">
          个人中心
        </el-menu-item>
        <el-menu-item index="/staff/schedule" v-if="isLoggedIn && userRole === 'staff'">
          我的日程
        </el-menu-item>
        <el-menu-item index="/admin/dashboard" v-if="isLoggedIn && userRole === 'admin'">
          管理后台
        </el-menu-item>
      </el-menu>
      <div class="user-actions">
        <template v-if="!isLoggedIn">
          <el-button @click="$router.push('/login')" plain>登录</el-button>
          <el-button type="primary" @click="$router.push('/register')" style="background-color: #4CAF50; border-color: #4CAF50;">
            注册
          </el-button>
        </template>
        <template v-else>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userInfo?.avatar" />
              <span style="margin-left: 8px;">{{ userInfo?.name || userInfo?.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <!-- 顾客端菜单 -->
                <template v-if="userRole === 'customer'">
                  <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                  <el-dropdown-item command="appointments">
                  <el-icon><Document /></el-icon>
                  我的预约
                </el-dropdown-item>
                </template>
                
                <!-- 服务人员端菜单 -->
                <template v-if="userRole === 'staff'">
                  <el-dropdown-item command="staff-profile">
                  <el-icon><User /></el-icon>
                  个人资料
                </el-dropdown-item>
                  <el-dropdown-item command="staff-schedule">
                  <el-icon><Calendar /></el-icon>
                  我的日程
                </el-dropdown-item>
                </template>
                
                <!-- 管理员端菜单 -->
                <template v-if="userRole === 'admin'">
                  <el-dropdown-item command="admin-dashboard">
                  <el-icon><DataBoard /></el-icon>
                  管理后台
                </el-dropdown-item>
                </template>
                
                <!-- 退出登录（所有角色通用） -->
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { 
  House, 
  User, 
  Document, 
  Calendar, 
  DataBoard, 
  SwitchButton 
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const store = useStore()

const activeIndex = computed(() => route.path)
const isLoggedIn = computed(() => store.getters['user/isLoggedIn'])
const userInfo = computed(() => store.getters['user/userInfo'])
const userRole = computed(() => {
  const role = store.getters['user/userRole']
  // 开发环境调试：打印角色信息
  if (import.meta.env.DEV && role) {
    console.log('当前用户角色:', role, '用户信息:', userInfo.value)
  }
  return role
})

const handleCommand = (command) => {
  switch (command) {
    case 'logout':
      store.dispatch('user/logout')
      ElMessage.success('已退出登录')
      router.push('/home')
      break
    case 'profile':
      router.push('/profile')
      break
    case 'appointments':
      router.push('/profile')
      break
    case 'staff-profile':
      router.push('/staff/profile')
      break
    case 'staff-schedule':
      router.push('/staff/schedule')
      break
    case 'admin-dashboard':
      router.push('/admin/dashboard')
      break
  }
}

// 监听登录状态变化，强制更新
watch(() => store.getters['user/isLoggedIn'], (newVal) => {
  // 登录状态变化时，触发组件更新
  if (newVal) {
    // 可以在这里添加其他逻辑
  }
})
</script>

<style scoped lang="scss">
.navigation-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
  height: 70px;
  line-height: 70px;
  position: sticky;
  top: 0;
  z-index: 999;
}

.nav-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  width: 100%;
}

.logo {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  
  .logo-icon {
    width: 40px;
    height: 40px;
    background-color: #4CAF50;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: bold;
    font-size: 20px;
  }
  
  h1 {
    margin: 0;
    font-size: 20px;
    color: #4CAF50;
    font-weight: bold;
  }
}

.nav-menu {
  flex: 1;
  border-bottom: none;
  
  :deep(.el-menu-item) {
    font-size: 16px;
    color: #333;
    
    &:hover {
      color: #4CAF50;
    }
    
    &.is-active {
      color: #4CAF50;
      border-bottom-color: #4CAF50;
    }
  }
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}
</style>


