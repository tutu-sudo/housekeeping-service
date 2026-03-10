<template>
  <div class="appointment-form-container">
    <!-- 右侧预约表单 -->
    <el-form v-if="showForm" :model="form" :rules="rules" ref="formRef" label-width="120px">
      <el-form-item label="服务项目" prop="serviceId">
        <el-select 
          v-model="form.serviceId" 
          placeholder="请选择服务项目"
          style="width: 100%"
          @change="handleServiceChange"
        >
          <el-option
            v-for="service in services"
            :key="service.id"
            :label="service.name"
            :value="service.id"
          />
        </el-select>
        <div v-if="currentBillingType" style="margin-top: 5px; font-size: 12px; color: #4CAF50;">
          <span v-if="currentBillingType === 'hourly'">💡 按小时结算：根据服务时长和定价结算</span>
          <span v-else-if="currentBillingType === 'daily'">💡 按天结算：不满一天按一天计算</span>
          <span v-else-if="currentBillingType === 'times'">💡 按次数结算：一次多少钱</span>
        </div>
      </el-form-item>
      
      <el-form-item label="服务人员" prop="staffId">
        <el-input 
          v-model="selectedStaffName" 
          placeholder="请从下方列表中选择服务人员"
          readonly
          style="width: 100%"
        >
          <template #suffix>
            <el-icon><ArrowDown /></el-icon>
          </template>
        </el-input>
        <div style="margin-top: 10px; font-size: 12px; color: #999;">
          提示：请从下方筛选结果中选择服务人员
        </div>
      </el-form-item>
      
      <!-- 已选择家政服务人员信息卡片（点击可查看详细资料） -->
      <el-card
        v-if="form.staffId && selectedStaffName"
        class="selected-staff-card"
        shadow="hover"
        @click="goToStaffProfile"
      >
        <div class="selected-staff-content">
          <div class="selected-staff-name">
            已选择服务人员：<strong>{{ selectedStaffName }}</strong>
          </div>
          <el-link type="primary">点击查看详细资料</el-link>
        </div>
      </el-card>
      
      <!-- 按次数结算的服务：只需要日期和时间 -->
      <template v-if="currentBillingType === 'times'">
      <el-form-item label="预约日期" prop="appointmentDate">
        <el-date-picker
          v-model="form.appointmentDate"
          type="date"
            placeholder="选择服务日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
          <div style="margin-top: 5px; font-size: 12px; color: #999;">
            提示：按次数结算，一次多少钱
          </div>
        </el-form-item>
        
        <el-form-item label="服务时间" prop="startTime">
          <el-time-picker
            v-model="form.startTime"
            placeholder="选择服务时间"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 100%"
            :disabled="!form.appointmentDate"
          />
        </el-form-item>
        
        <el-form-item label="结算说明">
          <el-input 
            value="按次数结算（一次多少钱）" 
            disabled
            style="width: 100%"
          />
        </el-form-item>
      </template>
      
      <!-- 按小时结算的服务：需要开始和结束时间（支持跨天） -->
      <template v-else-if="currentBillingType === 'hourly'">
        <el-form-item label="开始日期" prop="appointmentDate">
          <el-date-picker
            v-model="form.appointmentDate"
            type="date"
            placeholder="选择服务开始日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
          :disabled-date="disabledDate"
        />
          <div style="margin-top: 5px; font-size: 12px; color: #999;">
            提示：按小时结算，根据服务时长和定价结算
          </div>
      </el-form-item>
      
      <el-form-item label="开始时间" prop="startTime">
        <el-time-picker
          v-model="form.startTime"
            placeholder="选择服务开始时间"
          format="HH:mm"
          value-format="HH:mm"
          style="width: 100%"
            :disabled="!form.appointmentDate"
          @change="calculateDuration"
        />
      </el-form-item>
      
        <el-form-item label="结束时间" prop="endDateTime">
          <el-date-picker
            v-model="form.endDateTime"
            type="datetime"
            placeholder="选择服务结束日期和时间（可跨天）"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
            style="width: 100%"
            :disabled="!form.appointmentDate || !form.startTime"
            :disabled-date="disabledEndDate"
            @change="calculateDuration"
          />
          <div style="margin-top: 5px; font-size: 12px; color: #999;">
            提示：结束时间可以选择其他日期，支持跨天服务
          </div>
        </el-form-item>
        
        <el-form-item label="服务时长" v-if="serviceInfo">
          <el-input 
            :value="serviceInfo" 
            disabled
            style="width: 100%"
          />
        </el-form-item>
      </template>
      
      <!-- 按天结算的服务：需要开始和结束时间（支持跨天，不满一天按一天计算） -->
      <template v-else-if="currentBillingType === 'daily'">
        <el-form-item label="开始日期" prop="appointmentDate">
          <el-date-picker
            v-model="form.appointmentDate"
            type="date"
            placeholder="选择服务开始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
          <div style="margin-top: 5px; font-size: 12px; color: #999;">
            提示：按天结算，不满一天按一天计算
          </div>
        </el-form-item>
        
        <el-form-item label="开始时间" prop="startTime">
        <el-time-picker
            v-model="form.startTime"
            placeholder="选择服务开始时间"
          format="HH:mm"
          value-format="HH:mm"
          style="width: 100%"
            :disabled="!form.appointmentDate"
          @change="calculateDuration"
        />
      </el-form-item>
      
        <el-form-item label="结束时间" prop="endDateTime">
          <el-date-picker
            v-model="form.endDateTime"
            type="datetime"
            placeholder="选择服务结束日期和时间（可跨天）"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
            style="width: 100%"
            :disabled="!form.appointmentDate || !form.startTime"
            :disabled-date="disabledEndDate"
            @change="calculateDuration"
          />
          <div style="margin-top: 5px; font-size: 12px; color: #999;">
            提示：结束时间可以选择其他日期，支持跨天服务。不满一天按一天计算
          </div>
        </el-form-item>
        
        <el-form-item label="服务天数" v-if="serviceInfo">
        <el-input 
            :value="serviceInfo" 
          disabled
          style="width: 100%"
        />
      </el-form-item>
      </template>
      
      <!-- 客户基本信息 -->
      <el-form-item label="客户姓名" prop="customerName">
        <el-input
          v-model="form.customerName"
          placeholder="请输入客户姓名"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="手机号" prop="customerPhone">
        <el-input
          v-model="form.customerPhone"
          placeholder="请输入手机号"
          maxlength="11"
        />
      </el-form-item>
      
      <el-form-item label="服务地址" prop="serviceAddress">
        <el-input
          v-model="form.serviceAddress"
          placeholder="请输入服务地址"
          maxlength="255"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="特殊要求" prop="specialRequirements">
        <el-input
          v-model="form.specialRequirements"
          type="textarea"
          :rows="4"
          placeholder="请填写特殊要求，如：需要自带工具、有宠物、楼层信息等"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="submitForm">下一步：确认信息</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
    
     <!-- 左侧或下方服务人员列表区域：仅在 showForm 为 false 时展示列表 -->
     <div v-else class="staff-list-section">
      <h3 class="section-title">符合条件的服务人员</h3>
      <div v-if="filteredStaffList.length > 0" class="staff-list-container">
        <el-row :gutter="20">
          <el-col
            v-for="staff in filteredStaffList"
            :key="staff.staffId || staff.staff_id || staff.id"
            :span="6"
             :xs="12"
             :sm="8"
             :md="6"
             :lg="4"
          >
            <el-card 
              class="staff-card"
              :class="{
                selected: isStaffSelectable(staff) && form.staffId === (staff.staffId || staff.staff_id || staff.id),
                unavailable: !isStaffSelectable(staff)
              }"
              @click="handleStaffCardClick(staff)"
              shadow="hover"
            >
               <div class="staff-info-simple">
                <div class="staff-avatar-wrapper">
                <el-avatar 
                  :size="80" 
                   :src="staff.avatar || staff.avatar_url || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'"
                  class="staff-avatar"
                />
                  <div
                    v-if="!isStaffSelectable(staff)"
                    class="unavailable-badge"
                    title="当前不可服务"
                  >
                    <el-icon><WarningFilled /></el-icon>
                  </div>
                </div>
                <div class="staff-name">
                  {{ staff.name }}
                  <span
                    v-if="!isStaffSelectable(staff)"
                    class="status-text"
                  >（当前不可服务）</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      <el-empty v-else-if="hasSearched" description="暂无符合条件的服务人员，请调整筛选条件" />
      <div v-else class="empty-hint">
         <p>请设置筛选条件并点击&quot;应用筛选&quot;查看符合条件的服务人员</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, WarningFilled } from '@element-plus/icons-vue'
import { getAvailableServices, getStaffByService, searchStaff } from '@/api/services'
import dayjs from 'dayjs'

const props = defineProps({
  filterConditions: {
    type: Object,
    default: () => ({})
  },
  // 父组件传入选中的服务人员（仅在 showForm = true 的表单中使用）
  selectedStaff: {
    type: Object,
    default: null
  },
  showForm: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['submit', 'staff-selected'])

const route = useRoute()
const router = useRouter()
const formRef = ref(null)

const form = reactive({
  serviceId: route.query.serviceId ? Number(route.query.serviceId) : '',
  staffId: route.query.staffId ? Number(route.query.staffId) : '',
  appointmentDate: '',
  startTime: '',
  endTime: '', // 保留用于向后兼容
  endDateTime: '', // 新的结束日期时间字段（支持跨天）
  totalDuration: 0,
  totalDays: 0, // 按天结算的天数
  customerName: '',
  customerPhone: '',
  serviceAddress: '',
  specialRequirements: ''
})

const services = ref([])
const staffList = ref([])
const hasSearched = ref(false)
// 当前选中的服务人员姓名（用于表单显示和提示）
const selectedStaffName = ref('')

// 服务类型和结算方式的映射
// 按小时结算的服务
const hourlyServices = [
  '日常保洁',
  '餐具处理',
  '管道疏通',
  '衣物护理',
  '家居养护',
  '膳食服务',
  '宠物照料'
]

// 按天结算的服务
const dailyServices = [
  '月嫂服务',
  '育儿嫂服务',
  '生活照料',
  '健康辅助'
]

// 按次数结算的服务
const timesServices = [
  '家电维修',
  '家电保养',
  '小型安装'
]

// 获取当前选中服务的结算方式
const currentBillingType = computed(() => {
  if (!form.serviceId) return null
  
  const selectedService = services.value.find(s => s.id === form.serviceId || s.serviceId === form.serviceId)
  if (!selectedService) {
    return null
  }
  
  const serviceName = selectedService.serviceName || selectedService.name || selectedService.service_name || ''
  
  if (hourlyServices.includes(serviceName)) {
    return 'hourly'
  } else if (dailyServices.includes(serviceName)) {
    return 'daily'
  } else if (timesServices.includes(serviceName)) {
    return 'times'
  }
  
  return 'hourly'
})

// 服务信息显示
const serviceInfo = computed(() => {
  if (currentBillingType.value === 'hourly') {
    if (form.totalDuration > 0) {
      return `服务时长：${form.totalDuration} 小时（按小时结算）`
    }
    return '按小时结算（根据服务时长和定价结算）'
  } else if (currentBillingType.value === 'daily') {
    if (form.totalDays > 0) {
      return `服务天数：${form.totalDays} 天（按天结算，不满一天按一天计算）`
    }
    return '按天结算（不满一天按一天计算）'
  }
  return ''
})

// 动态验证规则
const rules = computed(() => {
  const baseRules = {
  serviceId: [{ required: true, message: '请选择服务项目', trigger: 'change' }],
  appointmentDate: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
    customerName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
    customerPhone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
    ],
  serviceAddress: [{ required: true, message: '请输入服务地址', trigger: 'blur' }],
  specialRequirements: [{ required: false, max: 500, message: '特殊要求不能超过500字', trigger: 'blur' }]
}
  
  // 根据结算方式添加不同的验证规则
  if (currentBillingType.value === 'times') {
    // 按次数结算：只需要日期和时间
    baseRules.startTime = [{ required: true, message: '请选择服务时间', trigger: 'change' }]
  } else {
    // 按小时或按天结算：需要开始时间和结束时间
    baseRules.startTime = [{ required: true, message: '请选择开始时间', trigger: 'change' }]
    baseRules.endDateTime = [{ required: true, message: '请选择结束日期和时间', trigger: 'change' }]
  }
  
  return baseRules
})

// 判断服务人员是否可被预约选择：工作状态=1 且 账号状态正常
const isStaffSelectable = (staff) => {
  if (!staff) return false
  const workStatus = staff.workStatus ?? staff.work_status
  const userStatus = staff.userStatus ?? staff.status
  const isWorkOk = Number(workStatus) === 1 || workStatus === '正常'
  const isUserOk = userStatus === undefined || Number(userStatus) === 1
  return isWorkOk && isUserOk
}

// 根据筛选条件过滤服务人员（暂时保留所有状态，在卡片上区分是否可选）
const filteredStaffList = computed(() => {
  return [...staffList.value]
})

// 监听父组件传入的选中服务人员（用于右侧预约表单同步显示）
watch(
  () => props.selectedStaff,
  (staff) => {
    if (!props.showForm) return
    if (staff) {
      form.staffId = staff.staffId || staff.staff_id || staff.id || ''
      selectedStaffName.value = staff.name || ''
      // 清除之前对 staffId 的校验错误状态
      if (formRef.value) {
        formRef.value.clearValidate('staffId')
      }
    } else {
      form.staffId = ''
      selectedStaffName.value = ''
    }
  },
  { immediate: true }
)

// 内部选择服务人员（只在可选时调用）
const selectStaff = (staff) => {
  const staffId = staff.staffId || staff.staff_id || staff.id
  form.staffId = staffId
  selectedStaffName.value = staff.name
  ElMessage.success(`已选择服务人员：${staff.name}`)
  emit('staff-selected', staff)
}

// 卡片点击处理：不可服务人员禁止选择，仅提示
const handleStaffCardClick = (staff) => {
  if (!isStaffSelectable(staff)) {
    ElMessage.warning('该服务人员当前不可服务，请选择其他人员')
    return
  }
  selectStaff(staff)
}

// 从预约表单跳转到家政服务人员资料页面（直接进入某个人的详细资料）
const goToStaffProfile = () => {
  if (!form.staffId) return
  const staffId = form.staffId
  router.push({
    path: `/housekeeper/${staffId}`,
    query: props.selectedStaff
      ? {
          staff: encodeURIComponent(JSON.stringify(props.selectedStaff))
        }
      : {}
  })
}

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

// 禁用结束日期（不能早于开始日期）
const disabledEndDate = (time) => {
  if (!form.appointmentDate) {
    return time.getTime() < Date.now() - 8.64e7
  }
  const startDate = dayjs(form.appointmentDate)
  const selectedDate = dayjs(time)
  return selectedDate.isBefore(startDate, 'day')
}

// 计算服务时长或天数
const calculateDuration = () => {
  if (currentBillingType.value === 'times') {
    // 按次数结算，不需要计算
    form.totalDuration = 0
    form.totalDays = 0
    return
  }
  
  if (!form.appointmentDate || !form.startTime || !form.endDateTime) {
    form.totalDuration = 0
    form.totalDays = 0
    return
  }
  
  // 解析开始和结束时间
  const startDateTime = dayjs(`${form.appointmentDate} ${form.startTime}`)
  const endDateTime = dayjs(form.endDateTime)
    
  if (endDateTime.isBefore(startDateTime)) {
      form.totalDuration = 0
    form.totalDays = 0
      return
    }
    
  if (currentBillingType.value === 'daily') {
    // 按天结算：计算天数，不满一天按一天计算
    const days = endDateTime.diff(startDateTime, 'day', true)
    // 如果天数小于1，按1天计算；否则向上取整
    form.totalDays = days < 1 ? 1 : Math.ceil(days)
    form.totalDuration = 0
  } else {
    // 按小时结算：计算小时数
    const diff = endDateTime.diff(startDateTime, 'hour', true)
    form.totalDuration = Math.round(diff * 100) / 100 // 保留两位小数
    form.totalDays = 0
  }
}

const loadServices = async () => {
  try {
    const response = await getAvailableServices()
    // 标准响应：{ code, message, data: [...] }
    const raw = response?.data?.data
    const rawServices = Array.isArray(raw) ? raw : []

    if (!rawServices.length) {
      ElMessage.warning('暂无可用服务，请联系管理员')
      services.value = []
      return
    }

    services.value = rawServices.map(service => ({
      id: service.serviceId,
      name: service.serviceName,
      ...service
    }))

    // 如果路由参数中有 serviceId，则优先按 serviceId 预选
    const serviceIdFromRoute = route.query.serviceId
    if (serviceIdFromRoute && services.value.length > 0) {
      const targetId = Number(serviceIdFromRoute)
      const matchedById = services.value.find(service => service.id === targetId)
      if (matchedById) {
        form.serviceId = matchedById.id
        handleServiceChange(matchedById.id)
      }
    } else {
      // 否则，如果有服务名称，则按名称预选
    const serviceNameFromRoute = route.query.serviceName
    if (serviceNameFromRoute && services.value.length > 0) {
      const matchedService = services.value.find(service => 
        service.name === serviceNameFromRoute || 
        service.serviceName === serviceNameFromRoute
      )
      if (matchedService) {
        form.serviceId = matchedService.id
        handleServiceChange(matchedService.id)
        }
      }
    }
  } catch (error) {
    const errorMessage = error.response?.data?.message || error.message || '获取服务列表失败，请稍后重试'
    ElMessage.error(errorMessage)
    services.value = []
  }
}

const loadStaff = async () => {
  try {
    const hasFilters = Object.keys(props.filterConditions).length > 0
    const params = {
      ...(form.serviceId ? { serviceId: form.serviceId } : {}),
      ...props.filterConditions
    }

    if (!form.serviceId && !hasFilters) {
    staffList.value = []
    hasSearched.value = false
    return
  }
  
    hasSearched.value = hasFilters
      const response = await searchStaff(params)
    let staffData = response?.data?.data
    if (!Array.isArray(staffData)) staffData = []

    staffList.value = filterStaffByAgeAndName(staffData, props.filterConditions)
  } catch (error) {
    staffList.value = []
    hasSearched.value = true
  }
}

const handleServiceChange = () => {
  form.staffId = ''
  selectedStaffName.value = ''
  // 重置时间相关字段
  form.startTime = ''
  form.endTime = ''
  form.endDateTime = ''
  form.totalDuration = 0
  form.totalDays = 0
  loadStaff()
}

// 监听筛选条件变化
watch(() => props.filterConditions, () => {
  if (form.serviceId) {
    loadStaff()
  } else {
    // 即使没有选择服务，也可以根据筛选条件搜索服务人员
    if (Object.keys(props.filterConditions).length > 0) {
      loadStaffByFilter()
    }
  }
}, { deep: true })

// 仅根据筛选条件加载服务人员（不依赖服务类型）
const loadStaffByFilter = async () => {
  try {
    const params = { ...props.filterConditions }
    if (!Object.keys(params).length) return
    
      hasSearched.value = true
      const response = await searchStaff(params)
    let staffData = response?.data?.data
    if (!Array.isArray(staffData)) staffData = []

    staffList.value = filterStaffByAgeAndName(staffData, props.filterConditions)
  } catch (error) {
    staffList.value = []
  }
}

// 前端过滤：根据年龄和姓名进行二次筛选（如果后端不支持）
const filterStaffByAgeAndName = (staffList, filterConditions) => {
  if (!staffList || !Array.isArray(staffList)) {
    return []
  }
  
  let filtered = [...staffList]
  
  // 年龄筛选（如果后端不支持）
  if (filterConditions.minAge || filterConditions.maxAge) {
    const currentYear = new Date().getFullYear()
    filtered = filtered.filter(staff => {
      if (!staff.birthDate && !staff.birth_date) return true
      
      const birthDate = staff.birthDate || staff.birth_date
      const birthYear = new Date(birthDate).getFullYear()
      const age = currentYear - birthYear
      
      if (filterConditions.minAge && age < filterConditions.minAge) {
        return false
      }
      if (filterConditions.maxAge && age > filterConditions.maxAge) {
        return false
      }
      return true
    })
  }
  
  // 姓名搜索（如果后端不支持）
  if (filterConditions.nameKeyword) {
    const keyword = filterConditions.nameKeyword.toLowerCase().trim()
    filtered = filtered.filter(staff => {
      const name = (staff.name || '').toLowerCase()
      return name.includes(keyword)
    })
  }
  
  return filtered
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate((valid) => {
    if (!form.staffId) {
      ElMessage.warning('请选择服务人员')
      return
    }

    if (valid) {
      // 根据结算方式验证
      if (currentBillingType.value === 'times') {
        // 按次数结算，只需要日期和时间
        if (!form.appointmentDate || !form.startTime) {
          ElMessage.warning('请选择预约日期和服务时间')
          return
        }
      } else {
        // 按小时或按天结算，需要验证时间范围
        if (!form.appointmentDate || !form.startTime || !form.endDateTime) {
          ElMessage.warning('请选择完整的服务时间范围')
          return
        }
        
        if (currentBillingType.value === 'hourly' && form.totalDuration <= 0) {
        ElMessage.warning('请选择有效的服务时间')
        return
        }
        
        if (currentBillingType.value === 'daily' && form.totalDays <= 0) {
          ElMessage.warning('请选择有效的服务时间')
          return
        }
      }
      
      // 获取选中的服务人员名称
      const selectedStaff = filteredStaffList.value.find(s => 
        (s.staffId || s.staff_id || s.id) === form.staffId
      )
      const staffName = selectedStaff ? selectedStaff.name : '服务人员'
      
      // 构建提交数据
      const submitData = {
        ...form,
        staffName,
        billingType: currentBillingType.value,
        // 为了向后兼容，如果使用新的 endDateTime，也设置 endTime
        endTime: form.endDateTime ? form.endDateTime.split(' ')[1] : form.endTime
      }
      
      emit('submit', submitData)
    }
  })
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
    form.totalDuration = 0
    form.totalDays = 0
    form.endDateTime = ''
    selectedStaffName.value = ''
  }
}

onMounted(() => {
  loadServices()
  if (form.serviceId) {
    loadStaff()
  }
})

// 向父组件暴露需要访问的属性和方法
defineExpose({
  form,
  selectedStaffName,
  loadServices,
  handleServiceChange
})
</script>

<style scoped lang="scss">
.appointment-form-container {
  padding: 20px;
}

.staff-list-container {
  margin-top: 10px;
  max-height: 600px;
  overflow-y: auto;
}

.staff-list-section {
  width: 100%;
  margin-top: 30px;
  
  .section-title {
    font-size: 18px;
    font-weight: bold;
    color: #333;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 2px solid #4CAF50;
  }
  
  .empty-hint {
    text-align: center;
    padding: 40px 20px;
    color: #999;
    font-size: 14px;
  }
}

.staff-card {
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
  
  &.selected {
    border: 2px solid #4CAF50;
    background-color: #f0f9ff;
  }
}

.staff-info-simple {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 10px;
}

.staff-avatar-wrapper {
  position: relative;
}

.staff-avatar {
  flex-shrink: 0;
}

.staff-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  text-align: center;
  word-break: break-all;
}

.staff-name .status-text {
  font-size: 12px;
  color: #f56c6c;
  font-weight: normal;
}

.staff-card.unavailable {
  cursor: not-allowed;
  opacity: 0.7;
  position: relative;
}

.unavailable-badge {
  position: absolute;
  right: -4px;
  top: -4px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background-color: #f56c6c;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 0 0 2px #fff;

  :deep(.el-icon) {
    font-size: 14px;
  }
}

.selected-staff-card {
  margin-bottom: 15px;
  cursor: pointer;
}

.selected-staff-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  }
  
.selected-staff-name {
  font-size: 14px;
  color: #333;
}
</style>

