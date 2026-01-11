<template>
  <div class="appointment-confirm-view">
    <Navigation />
    <div class="content">
      <el-container>
        <el-main>
          <el-card>
            <template #header>
              <h2>预约服务</h2>
            </template>
            
            <el-steps :active="currentStep" align-center style="margin-bottom: 40px;">
              <el-step title="填写信息" />
              <el-step title="确认信息" />
              <el-step title="完成" />
            </el-steps>
            
            <div v-if="currentStep === 0" class="form-step">
              <el-row :gutter="20">
                <!-- 左侧筛选条件 + 符合条件的服务人员 -->
                <el-col :span="10">
                  <FilterComponent @filter-change="handleFilterChange" />
                  <!-- 筛选条件下方展示符合条件的服务人员（列表模式） -->
              <div class="staff-list-section">
                <AppointmentForm 
                  ref="appointmentFormRef2" 
                  :filter-conditions="filterConditions"
                  :show-form="false"
                  @staff-selected="handleStaffSelected"
                />
              </div>
                </el-col>
                
                <!-- 右侧预约信息 -->
                <el-col :span="14">
                  <AppointmentForm 
                    ref="appointmentFormRef" 
                    :filter-conditions="filterConditions"
                    :selected-staff="selectedStaff"
                    :show-form="true"
                    @submit="handleFormSubmit"
                    @staff-selected="handleStaffSelected"
                  />
                </el-col>
              </el-row>
            </div>
            
            <div v-if="currentStep === 1" class="confirm-step">
              <el-descriptions title="预约信息确认" :column="2" border>
                <el-descriptions-item label="服务项目">
                  {{ appointmentData.serviceName }}
                </el-descriptions-item>
                <el-descriptions-item label="服务人员">
                  {{ appointmentData.staffName }}
                </el-descriptions-item>
                <el-descriptions-item label="预约日期">
                  {{ appointmentData.appointmentDate }}
                </el-descriptions-item>
                <el-descriptions-item label="开始时间">
                  {{ appointmentData.startTime }}
                </el-descriptions-item>
                <el-descriptions-item label="结束时间" v-if="appointmentData.billingType !== 'times'">
                  {{ appointmentData.endDateTime || appointmentData.endTime }}
                </el-descriptions-item>
                <el-descriptions-item label="结算方式">
                  <el-tag :type="appointmentData.billingType === 'hourly' ? 'success' : appointmentData.billingType === 'daily' ? 'warning' : 'info'">
                    {{ appointmentData.billingType === 'hourly' ? '按小时结算' : appointmentData.billingType === 'daily' ? '按天结算' : '按次数结算' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="服务时长" v-if="appointmentData.billingType === 'hourly'">
                  {{ appointmentData.totalDuration }} 小时
                </el-descriptions-item>
                <el-descriptions-item label="服务天数" v-if="appointmentData.billingType === 'daily'">
                  {{ appointmentData.totalDays }} 天（不满一天按一天计算）
                </el-descriptions-item>
                <el-descriptions-item label="服务地址" :span="2">
                  {{ appointmentData.serviceAddress }}
                </el-descriptions-item>
                <el-descriptions-item label="特殊要求" :span="2">
                  {{ appointmentData.specialRequirements || '无' }}
                </el-descriptions-item>
                <el-descriptions-item label="预计费用" :span="2">
                  <span class="price">¥ {{ appointmentData.totalAmount || calculatedPrice || '计算中...' }}</span>
                </el-descriptions-item>
              </el-descriptions>

              <!-- 已选服务人员区域：进入确认步骤就能看到 -->
              <div class="selected-staff-summary">
                <div class="section-title">已选择的服务人员</div>
                <template v-if="selectedStaff">
                  <!-- 样式尽量与“符合条件的服务人员”列表一致 -->
                  <el-card
                    class="staff-card simple"
                    shadow="hover"
                    @click="goToSelectedStaffDetail"
                  >
                    <div class="staff-info-simple">
                      <el-avatar 
                        :size="80" 
                        :src="selectedStaff.avatar || selectedStaff.avatar_url || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'"
                        class="staff-avatar"
                      />
                      <div class="staff-name">{{ selectedStaff.name }}</div>
                    </div>
                  </el-card>
                </template>
                <template v-else>
                  <el-card class="staff-card simple" shadow="never">
                    <div class="staff-info-simple">
                      <div class="staff-name">暂未选择服务人员</div>
                    </div>
                  </el-card>
                </template>
              </div>
              
              <div class="confirm-actions">
                <el-button type="primary" size="large" @click="confirmAppointment" :loading="submitting">
                  确认预约
                </el-button>
                <el-button @click="currentStep = 0">返回修改</el-button>
              </div>
            </div>
            
            <div v-if="currentStep === 2" class="success-step">
              <el-result
                icon="success"
                title="预约成功"
                :sub-title="`预约编号：${appointmentId}`"
              >
                <template #extra>
                  <el-button type="primary" @click="goToPayment">
                    立即支付
                  </el-button>
                  <el-button @click="goToProfile">
                    查看我的预约
                  </el-button>
                </template>
              </el-result>
            </div>
          </el-card>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { createAppointment } from '@/api/appointments'
import { getServiceDetail } from '@/api/services'
import Navigation from '@/components/common/Navigation.vue'
import AppointmentForm from '@/components/business/AppointmentForm.vue'
import FilterComponent from '@/components/common/FilterComponent.vue'

const router = useRouter()
const route = useRoute()
const store = useStore()

const currentStep = ref(0)
const submitting = ref(false)
const appointmentId = ref(null)
const calculatedPrice = ref(null)
const filterConditions = ref({})
const selectedStaff = ref(null) // 当前选中的服务人员（用于右侧表单显示）
const appointmentFormRef = ref(null)

const appointmentData = reactive({
  serviceId: '',
  serviceName: '',
  staffId: '',
  staffName: '',
  appointmentDate: '',
  startTime: '',
  endTime: '',
  endDateTime: '',
  totalDuration: 0,
  totalDays: 0,
  billingType: '',
  serviceAddress: '',
  specialRequirements: '',
  totalAmount: 0
})

const customerId = computed(() => {
  const userInfo = store.getters['user/userInfo']
  if (userInfo?.customerId) return userInfo.customerId
  return userInfo?.id || userInfo?.userId || null
})

const handleFilterChange = (conditions) => {
  filterConditions.value = conditions
}

const handleStaffSelected = (staff) => {
  selectedStaff.value = staff
  appointmentData.staffId = staff.staffId || staff.staff_id || staff.id
    appointmentData.staffName = staff.name
}

const handleFormSubmit = async (formData) => {
  // 保存表单数据
  Object.assign(appointmentData, formData)
  
  // 获取服务详情以显示服务名称
  if (formData.serviceId) {
    try {
      const serviceResponse = await getServiceDetail(formData.serviceId)
      const service = serviceResponse.data?.data || serviceResponse.data
      
      appointmentData.serviceName = service?.serviceName || service?.service_name || service?.name || '服务项目'
      
      const basePrice = service?.price || 0
      
      if (formData.billingType === 'hourly') {
      const duration = formData.totalDuration || 0
      calculatedPrice.value = basePrice * duration
      } else if (formData.billingType === 'daily') {
        const days = formData.totalDays || 1
        calculatedPrice.value = basePrice * days
      } else if (formData.billingType === 'times') {
        calculatedPrice.value = basePrice
      } else {
        const duration = formData.totalDuration || 1
        calculatedPrice.value = basePrice * duration
      }
      appointmentData.totalAmount = calculatedPrice.value
    } catch (error) {
      ElMessage.warning('获取服务详情失败，价格可能不准确')
      const selectedService = appointmentFormRef.value?.services?.find(s => 
        (s.id === formData.serviceId || s.serviceId === formData.serviceId)
      )
      if (selectedService) {
        appointmentData.serviceName = selectedService.serviceName || selectedService.name || '服务项目'
      }
    }
  }
  
  // 进入确认步骤
  currentStep.value = 1
}

const confirmAppointment = async () => {
  const userInfo = store.getters['user/userInfo']
  if (!userInfo) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  submitting.value = true
  try {
    const normalizeTime = (timeStr) => {
      if (!timeStr) return null
      return timeStr.length === 5 ? `${timeStr}:00` : timeStr
    }

    const appointmentPayload = {
      staffId: appointmentData.staffId,
      serviceId: appointmentData.serviceId,
      appointmentDate: appointmentData.appointmentDate,
      startTime: normalizeTime(appointmentData.startTime),
      serviceAddress: appointmentData.serviceAddress,
      specialRequirements: appointmentData.specialRequirements || null
    }
    
    if (customerId.value) {
      appointmentPayload.customerId = customerId.value
    }
    
    if (appointmentData.billingType === 'times') {
      appointmentPayload.endTime = normalizeTime(appointmentData.startTime)
    } else if (appointmentData.endDateTime) {
      let endDateTimeStr = appointmentData.endDateTime
      if (endDateTimeStr.includes(' ')) {
        endDateTimeStr = endDateTimeStr.replace(' ', 'T')
      }
      const timePart = endDateTimeStr.split('T')[1] || ''
      if (timePart && timePart.split(':').length === 2) {
        endDateTimeStr += ':00'
      }
      appointmentPayload.endDatetime = endDateTimeStr

      const [startDate] = appointmentData.appointmentDate.split(' ')
      const [endDate] = endDateTimeStr.split('T')
      if (startDate === endDate) {
        const endTimeStr = endDateTimeStr.split('T')[1]?.slice(0, 8)
        appointmentPayload.endTime = endTimeStr
      }
    } else if (appointmentData.endTime) {
      appointmentPayload.endTime = normalizeTime(appointmentData.endTime)
    }

    appointmentPayload.billingType = appointmentData.billingType || 'hourly'
    
    const response = await createAppointment(appointmentPayload)
    
    const result = response.data?.data || response.data
    appointmentId.value = result?.appointmentId || result?.id
    
    ElMessage.success('预约成功')
    currentStep.value = 2
  } catch (error) {
    const errorMessage = error.response?.data?.message || error.message || '预约失败，请稍后重试'
    ElMessage.error(errorMessage)
  } finally {
    submitting.value = false
  }
}

const goToPayment = () => {
  router.push({
    path: '/payment',
    query: { appointmentId: appointmentId.value }
  })
}

const goToProfile = () => {
  // 跳转到个人中心并刷新数据
  router.push({
    path: '/profile',
    query: { 
      tab: 'appointments',
      refresh: 'true',
      from: 'appointment-success'
    }
  })
}

const goToSelectedStaffDetail = () => {
  if (!selectedStaff.value) return
  const staff = selectedStaff.value
  const staffId = staff.staffId || staff.staff_id || staff.id
  if (!staffId) return
  router.push({
    path: `/housekeeper/${staffId}`,
    query: {
      staff: encodeURIComponent(JSON.stringify(staff))
    }
  })
}
</script>

<style scoped lang="scss">
.appointment-confirm-view {
  min-height: 100vh;
}

.content {
  padding: 20px;
}

.form-step,
.confirm-step,
.success-step {
  padding: 20px 0;
}

.selected-staff-summary {
  margin-top: 24px;
}

.selected-staff-summary .section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #303133;
}

.selected-staff-summary .staff-card {
  width: 200px;
  cursor: pointer;
}

.selected-staff-summary .staff-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
}

.selected-staff-summary .staff-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.selected-staff-summary .staff-name {
  font-size: 16px;
  font-weight: 500;
  margin-top: 8px;
  text-align: center;
}

.selected-staff-summary .staff-tip {
  font-size: 13px;
  color: #909399;
}

.staff-list-section {
  margin-top: 30px;
  padding-top: 30px;
  border-top: 1px solid #ebeef5;
}

.confirm-actions {
  margin-top: 30px;
  text-align: center;
  
  .el-button {
    margin: 0 10px;
  }
}

.price {
  font-size: 24px;
  color: #ff6600;
  font-weight: bold;
}
</style>

