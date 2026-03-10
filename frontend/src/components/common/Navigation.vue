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
        <el-menu-item index="/staff/profile" v-if="isLoggedIn && userRole === 'staff'">
          个人资料
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
          <!-- 管理员消息中心：铃铛 + 下拉消息列表 -->
          <template v-if="userRole === 'admin'">
            <el-dropdown
              trigger="click"
              @command="handleMessageCommand"
            >
              <span class="message-trigger">
                <el-badge
                  :value="unreadCount"
                  :hidden="unreadCount === 0"
                  class="message-badge"
                >
                  <el-button text circle>
                    <el-icon><Bell /></el-icon>
                  </el-button>
                </el-badge>
              </span>
              <template #dropdown>
                <el-dropdown-menu class="message-dropdown">
                  <el-dropdown-item
                    v-if="messages.length === 0"
                    disabled
                  >
                    暂无消息
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-for="msg in messages"
                    :key="msg.id || msg.messageId"
                    :command="{ type: 'open', message: msg }"
                    class="message-item"
                  >
                    <div class="message-item-title">
                      {{ msg.title || msg.subject || '系统通知' }}
                    </div>
                    <div class="message-item-content">
                      {{ msg.content || msg.message || '' }}
                    </div>
                    <div class="message-item-time">
                      {{ msg.createTime || msg.createdAt || '' }}
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="messages.length > 0"
                    divided
                    :command="{ type: 'markAllRead' }"
                  >
                    全部标为已读
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
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
import { computed, watch, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { 
  House, 
  User, 
  Document, 
  Calendar, 
  DataBoard, 
  SwitchButton,
  Bell 
} from '@element-plus/icons-vue'
import { getMessages, markMessageAsRead } from '@/api/messages'

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

// 管理员未读消息列表 & 数量（用于显示请假等审核提醒）
const messages = ref([])
const unreadCount = ref(0)
let messageTimer = null

const loadUnreadMessages = async () => {
  // 仅管理员需要轮询站内消息
  if (!store.getters['user/isLoggedIn'] || userRole.value !== 'admin') return
  try {
    const res = await getMessages({ isRead: 0 })
    const data = res.data?.data || res.data || []
    messages.value = Array.isArray(data) ? data : []
    unreadCount.value = messages.value.length
  } catch (e) {
    // 静默失败，避免打扰正常操作
    console.warn('加载未读消息失败：', e?.message || e)
  }
}

const startMessagePolling = () => {
  if (messageTimer) return
  // 立即拉一次
  loadUnreadMessages()
  // 每 30 秒轮询一次
  messageTimer = setInterval(loadUnreadMessages, 30000)
}

const stopMessagePolling = () => {
  if (messageTimer) {
    clearInterval(messageTimer)
    messageTimer = null
  }
}

const goToMessageTarget = (message) => {
  // 通用：如果后端有提供 targetUrl，则直接跳转
  if (message.targetUrl) {
    router.push(message.targetUrl)
    return
  }

  // 特殊：服务人员请假/不可服务申请 → 人员状态监控，并高亮对应员工
  const title = message.title || message.subject || ''
  const content = message.content || message.message || ''
  const type = message.type || message.category || ''

  if (
    type === 'STAFF_LEAVE_REQUEST' ||
    title.includes('请假') ||
    content.includes('请假') ||
    content.includes('不可服务')
  ) {
    const staffId =
      message.staffId ||
      message.relatedStaffId ||
      message.relatedId ||
      message.bizId

    if (staffId) {
      router.push({
        path: '/admin/staff/status',
        query: { staffId }
      })
    } else {
      router.push('/admin/staff/status')
    }
    return
  }

  // 其他消息暂时跳到后台首页，可按需要扩展
  router.push('/admin/dashboard')
}

const handleMessageCommand = async (cmd) => {
  if (!cmd || !cmd.type) return

  if (cmd.type === 'open' && cmd.message) {
    const msg = cmd.message
    const id = msg.id || msg.messageId
    if (id) {
      try {
        await markMessageAsRead(id)
      } catch (e) {
        console.warn('标记消息已读失败：', e?.message || e)
      }
    }
    // 跳转到对应页面
    goToMessageTarget(msg)
    // 重新拉取未读消息，更新红点
    loadUnreadMessages()
  } else if (cmd.type === 'markAllRead') {
    // 简单做法：逐条标记已读
    const ids = messages.value
      .map((m) => m.id || m.messageId)
      .filter(Boolean)
    if (ids.length === 0) return
    try {
      await Promise.all(ids.map((id) => markMessageAsRead(id)))
    } catch (e) {
      console.warn('批量标记消息已读失败：', e?.message || e)
    }
    messages.value = []
    unreadCount.value = 0
  }
}

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
  if (!newVal) {
    stopMessagePolling()
    unreadCount.value = 0
  }
})

// 监听角色变化，只有管理员才轮询消息
watch(userRole, (role) => {
  if (role === 'admin' && store.getters['user/isLoggedIn']) {
    startMessagePolling()
  } else {
    stopMessagePolling()
    unreadCount.value = 0
  }
})

onMounted(() => {
  if (store.getters['user/isLoggedIn'] && userRole.value === 'admin') {
    startMessagePolling()
  }
})

onBeforeUnmount(() => {
  stopMessagePolling()
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

// 管理员消息铃铛样式优化
.message-badge {
  cursor: pointer;

  :deep(.el-badge__content) {
    transform: translate(4px, -6px);
    padding: 0 4px;
    min-width: 16px;
    height: 16px;
    line-height: 16px;
    font-size: 12px;
  }

  :deep(.el-button) {
    padding: 0;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  :deep(.el-icon) {
    font-size: 18px;
  }
}
</style>


