<template>
  <div class="forgot-password-container">
    <el-card class="forgot-password-card">
      <template #header>
        <h2>找回密码</h2>
      </template>
      <el-steps :active="currentStep" align-center>
        <el-step title="验证身份" />
        <el-step title="重置密码" />
        <el-step title="完成" />
      </el-steps>
      
      <div class="step-content">
        <!-- 步骤1: 验证身份 -->
        <div v-if="currentStep === 0" class="step-panel">
          <el-form :model="verifyForm" :rules="verifyRules" ref="verifyFormRef">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="verifyForm.phone" placeholder="请输入注册手机号" />
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <div class="code-input">
                <el-input v-model="verifyForm.code" placeholder="请输入验证码" />
                <el-button
                  :disabled="countdown > 0"
                  @click="sendCode"
                  :loading="sendingCode"
                >
                  {{ countdown > 0 ? `${countdown}秒后重试` : '发送验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleVerify" :loading="verifying">
                下一步
              </el-button>
              <el-button @click="$router.push('/login')">返回登录</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 步骤2: 重置密码 -->
        <div v-if="currentStep === 1" class="step-panel">
          <el-form :model="resetForm" :rules="resetRules" ref="resetFormRef">
            <el-form-item label="新密码" prop="password">
              <el-input
                v-model="resetForm.password"
                type="password"
                placeholder="请输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="resetForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleReset" :loading="resetting">
                确认重置
              </el-button>
              <el-button @click="currentStep = 0">上一步</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 步骤3: 完成 -->
        <div v-if="currentStep === 2" class="step-panel success-panel">
          <el-result
            icon="success"
            title="密码重置成功"
            sub-title="您的密码已成功重置，请使用新密码登录"
          >
            <template #extra>
              <el-button type="primary" @click="$router.push('/login')">
                前往登录
              </el-button>
            </template>
          </el-result>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendVerificationCode, verifyCode, resetPassword } from '@/api/auth'

const router = useRouter()

const currentStep = ref(0)
const verifyFormRef = ref(null)
const resetFormRef = ref(null)
const sendingCode = ref(false)
const verifying = ref(false)
const resetting = ref(false)
const countdown = ref(0)

const verifyForm = reactive({
  phone: '',
  code: ''
})

const resetForm = reactive({
  password: '',
  confirmPassword: ''
})

const verifyRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== resetForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const resetRules = {
  password: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 发送验证码
const sendCode = async () => {
  if (!verifyForm.phone || !/^1[3-9]\d{9}$/.test(verifyForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  sendingCode.value = true
  try {
    await sendVerificationCode(verifyForm.phone)
    ElMessage.success('验证码已发送')
    // 开始倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  } finally {
    sendingCode.value = false
  }
}

// 验证验证码
const handleVerify = async () => {
  if (!verifyFormRef.value) return
  
  await verifyFormRef.value.validate(async (valid) => {
    if (valid) {
      verifying.value = true
      try {
        await verifyCode(verifyForm.phone, verifyForm.code)
        currentStep.value = 1
      } catch (error) {
        ElMessage.error(error.message || '验证码错误')
      } finally {
        verifying.value = false
      }
    }
  })
}

// 重置密码
const handleReset = async () => {
  if (!resetFormRef.value) return
  
  await resetFormRef.value.validate(async (valid) => {
    if (valid) {
      resetting.value = true
      try {
        await resetPassword({
          phone: verifyForm.phone,
          code: verifyForm.code,
          password: resetForm.password
        })
        currentStep.value = 2
      } catch (error) {
        ElMessage.error(error.message || '重置密码失败')
      } finally {
        resetting.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.forgot-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.forgot-password-card {
  width: 500px;
  
  h2 {
    text-align: center;
    margin: 0;
  }
}

.step-content {
  margin-top: 30px;
  min-height: 300px;
}

.step-panel {
  padding: 20px 0;
}

.code-input {
  display: flex;
  gap: 10px;
  
  .el-input {
    flex: 1;
  }
}

.success-panel {
  text-align: center;
  padding: 40px 0;
}
</style>

