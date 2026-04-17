<template>
  <div class="auth-page">
    <div class="auth-right">
      <div class="auth-card">
        <div class="auth-title">
          <div class="auth-title__main">账号登录</div>
          <div class="auth-title__sub">欢迎回来，请输入账号信息</div>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          label-position="top"
          class="auth-form"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>

          <div class="auth-actions">
            <el-link
              type="primary"
              underline="never"
              class="auth-forgot"
              @click="$router.push('/forgot-password')"
            >
              忘记密码？
            </el-link>
          </div>

          <div class="auth-buttons">
            <el-button type="primary" @click="handleLogin" :loading="loading" class="auth-btn-primary">
              登录
            </el-button>
            <el-button @click="$router.push('/register')" class="auth-btn-secondary">注册</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { getStaffProfile } from '@/api/staff'

const router = useRouter()
const route = useRoute()
const store = useStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await store.dispatch('user/login', loginForm)
        ElMessage.success('登录成功')
        
        // 如果是服务人员，获取个人资料并更新userInfo中的name
        const userRole = store.getters['user/userRole']
        if (userRole === 'staff') {
          try {
            const currentUserInfo = store.getters['user/userInfo']
            const response = await getStaffProfile()
            const data = response.data?.data || response.data
            
            if (data) {
              const staffInfo = data.staff || data
              
              // 调试：打印获取到的数据
              if (import.meta.env.DEV) {
                console.log('获取服务人员个人资料响应:', response)
                console.log('解析后的数据:', data)
                console.log('Staff信息:', staffInfo)
                console.log('Staff姓名:', staffInfo.name)
              }
              
              if (currentUserInfo && staffInfo.name) {
                // 更新userInfo，添加name字段
                store.dispatch('user/updateUserInfo', {
                  ...currentUserInfo,
                  name: staffInfo.name
                })
                
                if (import.meta.env.DEV) {
                  console.log('已更新userInfo.name为:', staffInfo.name)
                }
              }
            }
          } catch (error) {
            // 获取个人资料失败不影响登录，只记录错误
            console.error('获取服务人员个人资料失败:', error)
          }
        }
        
        const redirect = route.query.redirect || '/home'
        router.push(redirect)
      } catch (error) {
        // 如果错误消息已经在响应拦截器中显示过，这里就不再显示
        // 避免重复显示错误消息
        if (!error.messageShown) {
          ElMessage.error(error.message || '登录失败')
        }
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.auth-page {
  min-height: 100vh;
  display: flex;
  background-image: url('@/assets/auth/login.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.auth-right {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 48px 24px;
}

.auth-card {
  width: 100%;
  max-width: 420px;
  margin-right: clamp(40px, 8vw, 140px);
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 14px;
  padding: 28px 26px 24px;
  box-shadow: 0 18px 42px rgba(18, 22, 33, 0.14);
  backdrop-filter: blur(10px);
}

.auth-title {
  margin-bottom: 18px;
}

.auth-title__main {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  letter-spacing: 0.2px;
}

.auth-title__sub {
  margin-top: 6px;
  font-size: 13px;
  color: #6b7280;
}

.auth-form :deep(.el-form-item__label) {
  color: #374151;
  font-weight: 600;
}

.auth-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: -6px;
  margin-bottom: 12px;
}

.auth-buttons {
  display: flex;
  gap: 12px;
}

.auth-btn-primary,
.auth-btn-secondary {
  flex: 1 1 0;
}

@media (max-width: 960px) {
  .auth-right {
    width: 100%;
    justify-content: center;
  }

  .auth-card {
    margin-right: 0;
  }
}
</style>

