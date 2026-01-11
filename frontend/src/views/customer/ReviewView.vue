<template>
  <div class="review-view">
    <Navigation />
    <div class="content">
      <el-container>
        <el-main>
          <el-card>
            <template #header>
              <h2>服务评价</h2>
            </template>
            
            <div v-if="appointment" class="review-content">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="服务项目">
                  {{ appointment.serviceName || (appointment.serviceId ? `服务#${appointment.serviceId}` : '-') }}
                </el-descriptions-item>
                <el-descriptions-item label="服务人员">
                  {{ appointment.staffName || (appointment.staffId ? `员工#${appointment.staffId}` : '-') }}
                </el-descriptions-item>
                <el-descriptions-item label="服务日期">
                  {{ appointment.appointmentDate || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="服务时间">
                  {{ appointment.appointmentTime || (appointment.startTime && appointment.endTime ? `${appointment.startTime} - ${appointment.endTime}` : '-') }}
                </el-descriptions-item>
              </el-descriptions>
              
              <el-divider />
              
              <el-form :model="reviewForm" :rules="rules" ref="reviewFormRef" label-width="120px">
                <el-form-item label="总体评分" prop="overallRating" required>
                  <el-rate v-model="reviewForm.overallRating" :max="5" />
                  <span class="rating-text">{{ reviewForm.overallRating }}分</span>
                </el-form-item>
                
                <el-form-item label="服务态度" prop="serviceAttitudeRating">
                  <el-rate v-model="reviewForm.serviceAttitudeRating" :max="5" />
                  <span class="rating-text">{{ reviewForm.serviceAttitudeRating }}分</span>
                </el-form-item>
                
                <el-form-item label="专业能力" prop="professionalAbilityRating">
                  <el-rate v-model="reviewForm.professionalAbilityRating" :max="5" />
                  <span class="rating-text">{{ reviewForm.professionalAbilityRating }}分</span>
                </el-form-item>
                
                <el-form-item label="服务质量" prop="serviceQualityRating">
                  <el-rate v-model="reviewForm.serviceQualityRating" :max="5" />
                  <span class="rating-text">{{ reviewForm.serviceQualityRating }}分</span>
                </el-form-item>
                
                <el-form-item label="评价内容" prop="reviewContent">
                  <el-input
                    v-model="reviewForm.reviewContent"
                    type="textarea"
                    :rows="6"
                    placeholder="请分享您的服务体验（可选）..."
                    maxlength="1000"
                    show-word-limit
                  />
                </el-form-item>
                
                <el-form-item label="是否匿名">
                  <el-switch v-model="reviewForm.isAnonymous" />
                  <span style="margin-left: 10px; color: #999; font-size: 12px;">
                    开启后将匿名显示您的评价
                  </span>
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="submitReview" :loading="submitting" size="large">
                    提交评价
                  </el-button>
                  <el-button @click="$router.back()" size="large">取消</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-card>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAppointmentDetail } from '@/api/appointments'
import { createReview } from '@/api/reviews'
import { getStaffDetail } from '@/api/staff'
import Navigation from '@/components/common/Navigation.vue'

const router = useRouter()
const route = useRoute()

const appointment = ref(null)
const reviewFormRef = ref(null)
const submitting = ref(false)

const reviewForm = reactive({
  appointmentId: '',
  overallRating: 5, // 总体评分，对应数据库字段
  serviceAttitudeRating: 5, // 服务态度分
  professionalAbilityRating: 5, // 专业能力分
  serviceQualityRating: 5, // 服务质量分
  reviewContent: '', // 评价内容
  isAnonymous: false // 是否匿名
})

const rules = {
  overallRating: [{ required: true, message: '请选择总体评分', trigger: 'change' }],
  reviewContent: [{ required: false, message: '请输入评价内容', trigger: 'blur' }] // 评价内容改为非必填
}

const loadAppointment = async () => {
  let appointmentId = route.query.appointmentId
  if (!appointmentId) {
    ElMessage.error('缺少预约信息')
    router.push('/profile')
    return
  }
  
  // 确保appointmentId是有效的数字
  const id = typeof appointmentId === 'string' ? parseInt(appointmentId, 10) : Number(appointmentId)
  if (isNaN(id) || id <= 0) {
    ElMessage.error('无效的预约ID')
    router.push('/profile')
    return
  }
  appointmentId = id
  
  try {
    const response = await getAppointmentDetail(appointmentId)
    // 处理响应数据：可能是 response.data.data 或 response.data
    let data = response.data?.data || response.data
    
    if (!data) {
      ElMessage.error('预约详情不存在')
      router.push('/profile')
      return
    }
    
    // 检查是否已经评价过
    if (data.hasReview || data.reviewed) {
      ElMessage.warning('该预约已评价，请勿重复评价')
      router.push('/profile')
      return
    }
    
    // 检查预约状态是否为已完成
    const status = data.status !== undefined && data.status !== null ? data.status : 0
    if (status !== 3 && status !== 'completed') {
      ElMessage.warning('只有已完成的预约才能进行评价')
      router.push('/profile')
      return
    }
    
    // 数据清洗和增强：确保所有字段都正确映射
    appointment.value = {
      ...data,
      // 确保ID字段存在
      id: data.appointmentId || data.id,
      appointmentId: data.appointmentId || data.id,
      // 处理服务名称和服务ID
      serviceName: data.serviceName || data.service?.serviceName || data.service?.name || null,
      serviceId: data.serviceId || data.service?.serviceId || data.service?.id || null,
      // 处理服务人员名称和ID
      staffName: data.staffName || data.staff?.name || data.staff?.staffName || null,
      staffId: data.staffId || data.staff?.staffId || data.staff?.id || null,
      // 处理日期和时间
      appointmentDate: data.appointmentDate || data.appointment_date || null,
      appointmentTime: data.appointmentTime || data.appointment_time || 
        (data.startTime && data.endTime ? `${data.startTime} - ${data.endTime}` : null) ||
        (data.start_time && data.end_time ? `${data.start_time} - ${data.end_time}` : null) ||
        null,
      startTime: data.startTime || data.start_time || null,
      endTime: data.endTime || data.end_time || null,
      // 处理状态
      status: status,
      // 其他字段
      serviceAddress: data.serviceAddress || data.service_address || null,
      specialRequirements: data.specialRequirements || data.special_requirements || data.notes || null,
      notes: data.notes || data.specialRequirements || data.special_requirements || null,
      reviewed: data.reviewed || data.hasReview || false
    }
    
    reviewForm.appointmentId = appointmentId
    
    // 如果缺少服务名称或服务人员名称，尝试补充
    if (!appointment.value.serviceName && appointment.value.serviceId) {
      appointment.value.serviceName = `服务#${appointment.value.serviceId}`
    }
    if (!appointment.value.staffName && appointment.value.staffId) {
      try {
        const staffResponse = await getStaffDetail(appointment.value.staffId)
        const staff = staffResponse.data?.data || staffResponse.data
        if (staff) {
          // 尝试多种字段名格式获取姓名
          const name = staff.name || staff.staffName || staff.realName || staff.username
          if (name) {
            appointment.value.staffName = name
          }
        }
      } catch (error) {
        // 静默处理，不抛出错误（API可能不存在或权限不足）
        console.debug(`获取服务人员详情失败 (staffId: ${appointment.value.staffId}):`, error.message)
        appointment.value.staffName = appointment.value.staffName || `员工#${appointment.value.staffId}`
      }
    }
  } catch (error) {
    console.error('获取预约信息失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '获取预约信息失败'
    ElMessage.error(errorMsg)
    router.push('/profile')
  }
}

const submitReview = async () => {
  if (!reviewFormRef.value) return
  
  await reviewFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 构造符合后端接口的评价数据
        const reviewData = {
          appointmentId: reviewForm.appointmentId,
          overallRating: reviewForm.overallRating,
          serviceAttitudeRating: reviewForm.serviceAttitudeRating,
          professionalAbilityRating: reviewForm.professionalAbilityRating,
          serviceQualityRating: reviewForm.serviceQualityRating,
          reviewContent: reviewForm.reviewContent || null,
          isAnonymous: reviewForm.isAnonymous ? 1 : 0
        }
        
        await createReview(reviewData)
        ElMessage.success('评价提交成功，感谢您的反馈！')
        
        // 跳转回个人中心，并刷新评价列表
        router.push({
          path: '/profile',
          query: { tab: 'reviews', refresh: 'true' }
        })
      } catch (error) {
        console.error('评价提交失败:', error)
        const errorMsg = error.response?.data?.message || error.message || '评价提交失败，请重试'
        ElMessage.error(errorMsg)
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  loadAppointment()
})
</script>

<style scoped lang="scss">
.review-view {
  min-height: 100vh;
}

.content {
  padding: 20px;
}

.review-content {
  .rating-text {
    margin-left: 10px;
    color: #ff6600;
    font-weight: bold;
  }
}
</style>

