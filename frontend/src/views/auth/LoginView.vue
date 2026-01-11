<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>登录</h2>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
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
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading">
            登录
          </el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
        </el-form-item>
        <el-form-item>
          <el-link type="primary" @click="$router.push('/forgot-password')" underline="never">
            忘记密码？
          </el-link>
        </el-form-item>
      </el-form>
    </el-card>
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
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  
  h2 {
    text-align: center;
    margin: 0;
  }
}
</style>

