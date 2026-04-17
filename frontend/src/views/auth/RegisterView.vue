<template>
  <div class="auth-page">
    <div class="auth-right">
      <div class="auth-card">
        <div class="auth-title">
          <div class="auth-title__main">创建账号</div>
          <div class="auth-title__sub">填写信息完成注册</div>
        </div>

        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="rules"
          label-position="top"
          class="auth-form"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="registerForm.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
          </el-form-item>

          <div class="auth-buttons">
            <el-button type="primary" @click="handleRegister" :loading="loading" class="auth-btn-primary">
              注册
            </el-button>
            <el-button @click="$router.push('/login')" class="auth-btn-secondary">返回登录</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

const router = useRouter()
const store = useStore()

const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const { confirmPassword, ...formData } = registerForm
        await store.dispatch('user/register', formData)
        ElMessage.success('注册成功')
        // 注册接口可能不返回 token，此时不应直接进入需要登录的页面
        router.push('/login')
      } catch (error) {
        ElMessage.error(error.message || '注册失败')
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
  background-image: url('@/assets/auth/register.png');
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

.auth-buttons {
  display: flex;
  gap: 12px;
  margin-top: 6px;
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

