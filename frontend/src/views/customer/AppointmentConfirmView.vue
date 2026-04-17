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
              <el-descriptions-item label="客户姓名">
                {{ appointmentData.customerName }}
              </el-descriptions-item>
              <el-descriptions-item label="联系方式">
                {{ appointmentData.customerPhone }}
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
import { ref, reactive, computed, onMounted } from 'vue'
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
  customerName: '',
  customerPhone: '',
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

// 当左侧列表选择服务人员时，同步到右侧表单（服务项目 + 服务人员）
const handleStaffSelected = async (staff) => {
  if (!staff) return

  // 1）永远以当前点击的家政人员为准，覆盖右侧信息栏
  selectedStaff.value = staff
  appointmentData.staffId = staff.staffId || staff.staff_id || staff.id
    appointmentData.staffName = staff.name

  // 2）如果右侧预约表单已经加载，强制把“服务项目 + 服务人员”联动为该家政人员的配置
  if (appointmentFormRef.value) {
    // 确保服务列表已加载（如果还没加载会先拉一次接口）
    if (appointmentFormRef.value.loadServices) {
      await appointmentFormRef.value.loadServices()
    }

    const form = appointmentFormRef.value.form
    if (!form) return

    // 优先使用 staff 对象中明确的 serviceId，其次根据服务名称反查
    const services = appointmentFormRef.value.services || []

    const serviceIdFromStaff =
      staff.serviceId ||
      staff.mainServiceId ||
      (staff.service && (staff.service.serviceId || staff.service.id))

    const serviceNameFromStaff =
      staff.serviceName ||
      (staff.service && (staff.service.serviceName || staff.service.name))

    let finalServiceId = null

    if (serviceIdFromStaff) {
      finalServiceId = Number(serviceIdFromStaff)
    } else if (serviceNameFromStaff && services.length) {
      const matched = services.find(
        (s) =>
          s.serviceName === serviceNameFromStaff ||
          s.name === serviceNameFromStaff
      )
      if (matched) {
        finalServiceId = matched.id || matched.serviceId
      }
    }

    if (finalServiceId) {
      // 同步服务项目到右侧下拉框和本地预约数据
      form.serviceId = finalServiceId
      appointmentData.serviceId = finalServiceId

      // 触发表单内部的服务变更逻辑（计费提示、时间规则等），并等待可选服务人员列表刷新完成
      if (appointmentFormRef.value.handleServiceChange) {
        await appointmentFormRef.value.handleServiceChange()
      }

      // 在服务项目确定并刷新候选人员列表后，再同步当前点击的服务人员到右侧表单
      if (appointmentFormRef.value.syncSelectedStaffFromParent) {
        appointmentFormRef.value.syncSelectedStaffFromParent(staff)
      }
    }
  }
}

// 在离开预约页面前，把当前填写内容临时保存到 sessionStorage
const saveAppointmentDraft = () => {
  try {
    const formSnapshot = appointmentFormRef.value?.form
      ? { ...appointmentFormRef.value.form }
      : null
    const draft = {
      currentStep: currentStep.value,
      appointmentData: { ...appointmentData },
      filterConditions: { ...filterConditions.value },
      selectedStaff: selectedStaff.value,
      form: formSnapshot
    }
    sessionStorage.setItem('appointmentDraft', JSON.stringify(draft))
  } catch (e) {
    // 存草稿失败不影响主流程
    console.warn('保存预约草稿失败:', e)
  }
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
      specialRequirements: appointmentData.specialRequirements || null,
      customerName: appointmentData.customerName,
      customerPhone: appointmentData.customerPhone
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
  // 先保存当前预约草稿，返回时可以恢复
  saveAppointmentDraft()
  router.push({
    path: `/housekeeper/${staffId}`,
    query: {
      staff: encodeURIComponent(JSON.stringify(staff)),
      from: 'appointment'
    }
  })
}

// 进入预约页时，如果有草稿并且带有 restore 标记，则恢复上一次填写内容；
// 否则，如果路由中带有 staff / service 信息，则进行预填充。
onMounted(async () => {
  const restore = route.query.restore === '1'
  const draftStr = sessionStorage.getItem('appointmentDraft')

  if (restore && draftStr) {
    try {
      const draft = JSON.parse(draftStr)
      if (draft) {
        currentStep.value = draft.currentStep ?? 0
        Object.assign(appointmentData, draft.appointmentData || {})
        filterConditions.value = draft.filterConditions || {}
        selectedStaff.value = draft.selectedStaff || null

        // 等待子组件加载完成后再恢复表单数据
        await new Promise(resolve => {
          const checkInterval = setInterval(() => {
            if (appointmentFormRef.value) {
              clearInterval(checkInterval)
              resolve()
            }
          }, 50)
          // 最多等待2秒
          setTimeout(() => {
            clearInterval(checkInterval)
            resolve()
          }, 2000)
        })

        if (draft.form && appointmentFormRef.value) {
          Object.assign(appointmentFormRef.value.form, draft.form)
          // 如果恢复了服务项目，需要触发服务变更逻辑
          if (draft.form.serviceId) {
            appointmentFormRef.value.form.serviceId = draft.form.serviceId
            // 等待服务列表加载完成
            if (appointmentFormRef.value.loadServices) {
              await appointmentFormRef.value.loadServices()
            }
            // 触发服务变更，加载对应的服务人员
            if (appointmentFormRef.value.handleServiceChange) {
              appointmentFormRef.value.handleServiceChange()
            }
          }
          // 恢复选中的服务人员名称
          if (selectedStaff.value) {
            appointmentFormRef.value.selectedStaffName = selectedStaff.value.name || ''
          }
        }
        ElMessage.success('已恢复上次预约信息')
      }
    } catch (e) {
      console.warn('恢复预约草稿失败:', e)
      ElMessage.error('恢复预约信息失败')
    } finally {
      // 草稿只用一次，避免以后新预约误用旧数据
      sessionStorage.removeItem('appointmentDraft')
    }
    return
  }

  // 没有草稿时，根据路由参数进行预填充（从"家政服务资料"立即预约跳转过来）
  const staffStr = route.query.staff
  if (staffStr) {
    try {
      const parsed = JSON.parse(decodeURIComponent(staffStr))
      selectedStaff.value = parsed
      appointmentData.staffId =
        parsed.staffId || parsed.staff_id || parsed.id || appointmentData.staffId
      appointmentData.staffName = parsed.name || appointmentData.staffName
    } catch (e) {
      console.warn('解析路由中的家政人员信息失败:', e)
    }
  }

  // 等待子组件加载完成后再设置服务项目和服务人员
  await new Promise(resolve => {
    const checkInterval = setInterval(() => {
      if (appointmentFormRef.value) {
        clearInterval(checkInterval)
        resolve()
      }
    }, 50)
    setTimeout(() => {
      clearInterval(checkInterval)
      resolve()
    }, 2000)
  })

  if (appointmentFormRef.value) {
    // 即使没有 serviceId，也先加载服务列表，确保后续可按服务名称/人员信息反查服务项目
      if (appointmentFormRef.value.loadServices) {
        await appointmentFormRef.value.loadServices()
      }
      
    // 有预选家政人员时，复用统一联动逻辑同步“服务项目 + 服务人员”
    if (selectedStaff.value) {
      await handleStaffSelected(selectedStaff.value)
    }

    // 如果路由明确传了 serviceId，优先覆盖为该 serviceId
    if (route.query.serviceId && appointmentFormRef.value.form) {
      const serviceId = Number(route.query.serviceId)
      if (Number.isFinite(serviceId) && serviceId > 0) {
        appointmentData.serviceId = serviceId
        appointmentFormRef.value.form.serviceId = serviceId
        if (appointmentFormRef.value.handleServiceChange) {
          await appointmentFormRef.value.handleServiceChange()
        }
        if (selectedStaff.value && appointmentFormRef.value.syncSelectedStaffFromParent) {
          appointmentFormRef.value.syncSelectedStaffFromParent(selectedStaff.value)
        }
      }
    }

    // 兜底：确保服务人员名称一定同步显示
    if (selectedStaff.value && appointmentFormRef.value.form) {
      const staffId = selectedStaff.value.staffId || selectedStaff.value.staff_id || selectedStaff.value.id
      if (staffId) {
        appointmentFormRef.value.form.staffId = staffId
        appointmentFormRef.value.selectedStaffName = selectedStaff.value.name || ''
      }
    }
  }
})
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

