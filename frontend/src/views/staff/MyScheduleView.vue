<template>
  <div class="my-schedule-view">
    <Navigation />
    <div class="content">
      <el-container>
        <el-main>
          <el-card>
            <template #header>
              <h2>工作日程管理</h2>
            </template>
            
            <el-tabs v-model="activeMainTab">
              <!-- 日历视图 -->
              <el-tab-pane label="日历视图" name="calendar">
            <el-calendar v-model="currentDate">
              <template #date-cell="{ data }">
                <div class="calendar-day">
                  <div class="day-number">{{ data.day.split('-').slice(2).join('-') }}</div>
                      <div class="day-events">
                        <!-- 可服务时间 -->
                        <el-tag
                          v-for="schedule in getSchedulesByDate(data.day)"
                          :key="`schedule-${schedule.scheduleId}`"
                          size="small"
                          type="success"
                          style="margin: 2px; display: block;"
                        >
                          {{ formatTime(schedule.startTime) }}-{{ formatTime(schedule.endTime) }}
                        </el-tag>
                        <!-- 预约信息 -->
                    <el-tag
                      v-for="appointment in getAppointmentsByDate(data.day)"
                          :key="`appointment-${appointment.appointmentId}`"
                      size="small"
                          :type="getAppointmentTagType(appointment.status)"
                          style="margin: 2px; display: block;"
                    >
                          {{ formatAppointmentTime(appointment) }}
                    </el-tag>
                  </div>
                </div>
              </template>
            </el-calendar>
              </el-tab-pane>
            
              <!-- 预约列表 -->
              <el-tab-pane label="预约列表" name="appointments">
                <el-tabs v-model="activeAppointmentTab">
              <el-tab-pane label="待接单" name="pending">
                    <el-table :data="pendingAppointments" border>
                      <el-table-column prop="appointmentId" label="预约ID" width="100" />
                      <el-table-column prop="customerName" label="客户姓名" width="120" />
                      <el-table-column prop="serviceName" label="服务项目" width="150" />
                      <el-table-column prop="appointmentDate" label="预约日期" width="120" />
                      <el-table-column label="预约时间" width="200">
                        <template #default="scope">
                          {{ formatAppointmentTime(scope.row) }}
                        </template>
                      </el-table-column>
                      <el-table-column prop="serviceAddress" label="服务地址" show-overflow-tooltip />
                      <el-table-column prop="totalAmount" label="金额" width="100">
                        <template #default="scope">
                          ¥{{ scope.row.totalAmount || 0 }}
                        </template>
                      </el-table-column>
                      <el-table-column label="支付状态" width="100">
                        <template #default="scope">
                          <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
                            {{ getPaymentStatusText(scope.row.paymentStatus) }}
                          </el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column label="操作" width="200" fixed="right">
                    <template #default="scope">
                      <el-button
                        type="success"
                        size="small"
                            @click="confirmAppointment(scope.row.appointmentId)"
                      >
                        接单
                      </el-button>
                      <el-button
                        type="danger"
                        size="small"
                            @click="rejectAppointment(scope.row.appointmentId)"
                      >
                        拒绝
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
              
              <el-tab-pane label="已接单" name="confirmed">
                    <el-table :data="confirmedAppointments" border>
                      <el-table-column prop="appointmentId" label="预约ID" width="100" />
                      <el-table-column prop="customerName" label="客户姓名" width="120" />
                      <el-table-column prop="serviceName" label="服务项目" width="150" />
                      <el-table-column prop="appointmentDate" label="预约日期" width="120" />
                      <el-table-column label="预约时间" width="200">
                        <template #default="scope">
                          {{ formatAppointmentTime(scope.row) }}
                        </template>
                      </el-table-column>
                      <el-table-column prop="serviceAddress" label="服务地址" show-overflow-tooltip />
                      <el-table-column prop="totalAmount" label="金额" width="100">
                        <template #default="scope">
                          ¥{{ scope.row.totalAmount || 0 }}
                        </template>
                      </el-table-column>
                      <el-table-column label="支付状态" width="100">
                        <template #default="scope">
                          <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
                            {{ getPaymentStatusText(scope.row.paymentStatus) }}
                          </el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column label="状态跟踪" width="150">
                        <template #default="scope">
                          <el-tooltip :content="getStatusTrackingText(scope.row)" placement="top">
                            <el-tag :type="getAppointmentTagType(scope.row.status)" size="small">
                              {{ getStatusText(scope.row.status) }}
                            </el-tag>
                          </el-tooltip>
                        </template>
                      </el-table-column>
                      <el-table-column label="操作" width="200" fixed="right">
                    <template #default="scope">
                      <el-button
                        type="primary"
                        size="small"
                        :disabled="scope.row.paymentStatus !== PAYMENT_STATUS.PAID"
                            @click="startService(scope.row.appointmentId)"
                      >
                        开始服务
                      </el-button>
                      <el-tooltip v-if="scope.row.paymentStatus !== PAYMENT_STATUS.PAID" content="订单未支付，无法开始服务" placement="top">
                        <el-icon style="margin-left: 5px; color: #f56c6c;"><Warning /></el-icon>
                      </el-tooltip>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
              
              <el-tab-pane label="服务中" name="in_service">
                    <el-table :data="inServiceAppointments" border>
                      <el-table-column prop="appointmentId" label="预约ID" width="100" />
                      <el-table-column prop="customerName" label="客户姓名" width="120" />
                      <el-table-column prop="serviceName" label="服务项目" width="150" />
                      <el-table-column prop="appointmentDate" label="预约日期" width="120" />
                      <el-table-column label="预约时间" width="200">
                        <template #default="scope">
                          {{ formatAppointmentTime(scope.row) }}
                        </template>
                      </el-table-column>
                      <el-table-column prop="serviceAddress" label="服务地址" show-overflow-tooltip />
                      <el-table-column prop="totalAmount" label="金额" width="100">
                        <template #default="scope">
                          ¥{{ scope.row.totalAmount || 0 }}
                        </template>
                      </el-table-column>
                      <el-table-column label="支付状态" width="100">
                        <template #default="scope">
                          <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
                            {{ getPaymentStatusText(scope.row.paymentStatus) }}
                          </el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column label="状态跟踪" width="180">
                        <template #default="scope">
                          <el-tooltip :content="getStatusTrackingText(scope.row)" placement="top">
                            <div style="font-size: 12px; color: #666;">
                              <div>状态: {{ getStatusText(scope.row.status) }}</div>
                              <div v-if="scope.row.startTime" style="margin-top: 4px;">
                                开始: {{ formatTime(scope.row.startTime) }}
                              </div>
                            </div>
                          </el-tooltip>
                        </template>
                      </el-table-column>
                      <el-table-column label="操作" width="200" fixed="right">
                    <template #default="scope">
                      <el-button
                        type="success"
                        size="small"
                        :disabled="scope.row.paymentStatus !== PAYMENT_STATUS.PAID"
                            @click="completeService(scope.row.appointmentId)"
                      >
                        完成服务
                      </el-button>
                      <el-tooltip v-if="scope.row.paymentStatus !== PAYMENT_STATUS.PAID" content="订单未支付，无法结束服务" placement="top">
                        <el-icon style="margin-left: 5px; color: #f56c6c;"><Warning /></el-icon>
                      </el-tooltip>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
              
              <el-tab-pane label="已完成" name="completed">
                    <el-table :data="completedAppointments" border>
                      <el-table-column prop="appointmentId" label="预约ID" width="100" />
                      <el-table-column prop="customerName" label="客户姓名" width="120" />
                      <el-table-column prop="serviceName" label="服务项目" width="150" />
                      <el-table-column prop="appointmentDate" label="预约日期" width="120" />
                      <el-table-column label="预约时间" width="200">
                        <template #default="scope">
                          {{ formatAppointmentTime(scope.row) }}
                        </template>
                      </el-table-column>
                      <el-table-column prop="serviceAddress" label="服务地址" show-overflow-tooltip />
                      <el-table-column prop="totalAmount" label="金额" width="100">
                        <template #default="scope">
                          ¥{{ scope.row.totalAmount || 0 }}
                        </template>
                      </el-table-column>
                      <el-table-column label="支付状态" width="100">
                        <template #default="scope">
                          <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
                            {{ getPaymentStatusText(scope.row.paymentStatus) }}
                          </el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column label="状态跟踪" width="200">
                        <template #default="scope">
                          <el-tooltip :content="getStatusTrackingText(scope.row)" placement="top">
                            <div style="font-size: 12px; color: #666;">
                              <div>状态: {{ getStatusText(scope.row.status) }}</div>
                              <div v-if="scope.row.startTime" style="margin-top: 4px;">
                                开始: {{ formatTime(scope.row.startTime) }}
                              </div>
                              <div v-if="scope.row.endTime" style="margin-top: 4px;">
                                结束: {{ formatTime(scope.row.endTime) }}
                              </div>
                            </div>
                          </el-tooltip>
                        </template>
                      </el-table-column>
                    </el-table>
                  </el-tab-pane>
                </el-tabs>
              </el-tab-pane>
              
              <!-- 可服务时间 -->
              <el-tab-pane label="可服务时间" name="schedules">
                <div class="schedules-section">
                  <div class="section-header">
                    <h3>可服务时间列表</h3>
                    <el-button type="primary" @click="showAddScheduleDialog = true">
                      <el-icon><Plus /></el-icon>
                      添加可服务时间
                    </el-button>
                  </div>
                  
                  <el-table :data="schedulesList" border style="margin-top: 20px;">
                    <el-table-column prop="workDate" label="日期" width="120" />
                    <el-table-column label="时间" width="200">
                      <template #default="scope">
                        {{ formatTime(scope.row.startTime) }} - {{ formatTime(scope.row.endTime) }}
                      </template>
                    </el-table-column>
                    <el-table-column prop="availableStatus" label="状态" width="100">
                      <template #default="scope">
                        <el-tag :type="scope.row.availableStatus === 1 ? 'success' : 'danger'">
                          {{ scope.row.availableStatus === 1 ? '可服务' : '不可服务' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="150">
                      <template #default="scope">
                        <el-button size="small" @click="editSchedule(scope.row)">编辑</el-button>
                        <el-button size="small" type="danger" @click="deleteScheduleItem(scope.row.scheduleId)">
                          删除
                        </el-button>
                      </template>
                    </el-table-column>
                </el-table>
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-main>
      </el-container>
    </div>
    
    <!-- 添加/编辑可服务时间对话框 -->
    <el-dialog
      v-model="showAddScheduleDialog"
      :title="editingSchedule ? '编辑可服务时间' : '添加可服务时间'"
      width="500px"
    >
      <el-form :model="scheduleForm" :rules="scheduleRules" ref="scheduleFormRef" label-width="120px">
        <el-form-item label="日期" prop="workDate">
          <el-date-picker
            v-model="scheduleForm.workDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled-date="disabledPastDate"
          />
        </el-form-item>
        
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker
            v-model="scheduleForm.startTime"
            placeholder="选择开始时间"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="结束时间" prop="endDateTime">
          <el-date-picker
            v-model="scheduleForm.endDateTime"
            type="datetime"
            placeholder="选择结束日期和时间（支持跨天）"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
            :disabled-date="disabledPastDate"
            :default-time="scheduleForm.startTime ? [new Date(`2000-01-01 ${scheduleForm.startTime}`)] : undefined"
          />
          <div style="font-size: 12px; color: #999; margin-top: 5px;">
            提示：结束时间可以选择其他日期，支持跨天服务
          </div>
        </el-form-item>
        
        <el-form-item label="状态" prop="availableStatus">
          <el-radio-group v-model="scheduleForm.availableStatus">
            <el-radio :label="1">可服务</el-radio>
            <el-radio :label="0">不可服务</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-alert
          v-if="conflictWarning"
          :title="conflictWarning"
          type="warning"
          :closable="false"
          style="margin-bottom: 20px;"
        />
      </el-form>
      
      <template #footer>
        <el-button @click="showAddScheduleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSchedule" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Warning } from '@element-plus/icons-vue'
import { 
  getStaffAppointmentsList,
  acceptAppointment,
  rejectAppointment as rejectAppointmentAPI,
  startAppointment,
  completeAppointment
} from '@/api/appointments'
import { getSchedules, createSchedule, updateSchedule, deleteSchedule } from '@/api/schedules'
import { PAYMENT_STATUS_CONFIG, PAYMENT_STATUS } from '@/utils/constants'
import Navigation from '@/components/common/Navigation.vue'
import dayjs from 'dayjs'

const store = useStore()

const currentDate = ref(new Date())
const activeMainTab = ref('calendar')
const activeAppointmentTab = ref('pending')
const appointments = ref([])
const schedulesList = ref([])
const showAddScheduleDialog = ref(false)
const editingSchedule = ref(null)
const saving = ref(false)
const conflictWarning = ref('')
const scheduleFormRef = ref(null)

const staffId = computed(() => store.getters['user/userId'])

const scheduleForm = reactive({
  workDate: '',
  startTime: '',
  endDateTime: '', // 结束日期时间（支持跨天）
  availableStatus: 1
})

const scheduleRules = {
  workDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endDateTime: [{ required: true, message: '请选择结束日期和时间', trigger: 'change' }],
  availableStatus: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 按状态筛选预约
const pendingAppointments = computed(() => {
  return appointments.value.filter(apt => apt.status === 0)
})

const confirmedAppointments = computed(() => {
  return appointments.value.filter(apt => apt.status === 1)
})

const inServiceAppointments = computed(() => {
  return appointments.value.filter(apt => apt.status === 2)
})

const completedAppointments = computed(() => {
  return appointments.value.filter(apt => apt.status === 3 || apt.status === 4)
})

const getAppointmentsByDate = (date) => {
  return appointments.value.filter(
    apt => apt.appointmentDate === date
  )
}

const getSchedulesByDate = (date) => {
  return schedulesList.value.filter(
    schedule => schedule.workDate === date && schedule.availableStatus === 1
  )
}

const getAppointmentTagType = (status) => {
  const statusMap = {
    0: 'info',      // 待确认 - 蓝色
    1: 'success',   // 已确认 - 绿色
    2: 'warning',   // 进行中 - 橙色
    3: '',          // 已完成 - 灰色
    4: 'info'       // 已取消 - 灰色
  }
  return statusMap[status] || ''
}

const getStatusText = (status) => {
  const statusMap = {
    0: '待确认',
    1: '已确认',
    2: '进行中',
    3: '已完成',
    4: '已取消',
    5: '已拒绝'
  }
  return statusMap[status] || '未知'
}

// 支付状态类型
const getPaymentStatusType = (paymentStatus) => {
  const config = PAYMENT_STATUS_CONFIG[paymentStatus]
  return config?.type || 'info'
}

// 支付状态文本
const getPaymentStatusText = (paymentStatus) => {
  const config = PAYMENT_STATUS_CONFIG[paymentStatus]
  return config?.text || '未知'
}

// 状态跟踪文本（用于tooltip）
const getStatusTrackingText = (appointment) => {
  const parts = []
  parts.push(`预约状态: ${getStatusText(appointment.status)}`)
  parts.push(`支付状态: ${getPaymentStatusText(appointment.paymentStatus)}`)
  if (appointment.createTime) {
    parts.push(`创建时间: ${appointment.createTime}`)
  }
  if (appointment.startTime) {
    parts.push(`开始时间: ${appointment.startTime}`)
  }
  if (appointment.endTime) {
    parts.push(`结束时间: ${appointment.endTime}`)
  }
  return parts.join('\n')
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.length === 8 ? timeStr.slice(0, 5) : timeStr
}

const formatAppointmentTime = (appointment) => {
  if (appointment.endDatetime) {
    const start = `${appointment.appointmentDate} ${formatTime(appointment.startTime)}`
    const end = appointment.endDatetime.replace('T', ' ').slice(0, 16)
    return `${start} - ${end}`
  } else {
    return `${appointment.appointmentDate} ${formatTime(appointment.startTime)} - ${formatTime(appointment.endTime)}`
  }
}

const disabledPastDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

// 检测时间冲突
const checkConflict = () => {
  if (!scheduleForm.workDate || !scheduleForm.startTime || !scheduleForm.endDateTime) {
    conflictWarning.value = ''
    return
  }
  
  const scheduleStart = dayjs(`${scheduleForm.workDate} ${scheduleForm.startTime}`)
  const scheduleEnd = dayjs(scheduleForm.endDateTime)
  
  if (scheduleEnd.isBefore(scheduleStart)) {
    conflictWarning.value = '结束时间不能早于开始时间'
    return
  }
  
  // 检查与现有预约的冲突
  const conflicts = appointments.value.filter(apt => {
    if (apt.status === 4 || apt.status === 5) return false // 已取消或已拒绝的不算冲突
    
    const aptStart = dayjs(`${apt.appointmentDate} ${formatTime(apt.startTime)}`)
    const aptEnd = apt.endDatetime 
      ? dayjs(apt.endDatetime)
      : dayjs(`${apt.appointmentDate} ${formatTime(apt.endTime)}`)
    
    return (scheduleStart.isBefore(aptEnd) && scheduleEnd.isAfter(aptStart))
  })
  
  if (conflicts.length > 0) {
    conflictWarning.value = `警告：该时间段与 ${conflicts.length} 个预约冲突，请确认是否继续`
  } else {
    conflictWarning.value = ''
  }
}

// 监听时间变化，检测冲突
watch([() => scheduleForm.workDate, () => scheduleForm.startTime, () => scheduleForm.endDateTime], () => {
  checkConflict()
})

// 接单
const confirmAppointment = async (appointmentId) => {
  try {
    await ElMessageBox.confirm('确定要接单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await acceptAppointment(appointmentId)
    ElMessage.success('接单成功')
    await loadAppointments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '接单失败')
    }
  }
}

// 拒绝接单
const rejectAppointment = async (appointmentId) => {
  try {
    await ElMessageBox.confirm('确定要拒绝此订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await rejectAppointmentAPI(appointmentId)
    ElMessage.success('已拒绝')
    await loadAppointments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

// 开始服务
const startService = async (appointmentId) => {
  try {
    // 检查支付状态
    const appointment = appointments.value.find(apt => apt.appointmentId === appointmentId)
    if (appointment && appointment.paymentStatus !== PAYMENT_STATUS.PAID) {
      ElMessage.warning('订单未支付，无法开始服务')
      return
    }
    
    await ElMessageBox.confirm('确定要开始服务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await startAppointment(appointmentId)
    ElMessage.success('服务已开始')
    await loadAppointments()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.message || '操作失败'
      ElMessage.error(errorMsg)
    }
  }
}

// 完成服务
const completeService = async (appointmentId) => {
  try {
    // 检查支付状态
    const appointment = appointments.value.find(apt => apt.appointmentId === appointmentId)
    if (appointment && appointment.paymentStatus !== PAYMENT_STATUS.PAID) {
      ElMessage.warning('订单未支付，无法结束服务')
      return
    }
    
    await ElMessageBox.confirm('确定要完成服务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    await completeAppointment(appointmentId)
    ElMessage.success('服务已完成，等待客户评价')
    await loadAppointments()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.message || '操作失败'
      ElMessage.error(errorMsg)
    }
  }
}

const editSchedule = (schedule) => {
  editingSchedule.value = schedule
  scheduleForm.workDate = schedule.workDate
  scheduleForm.startTime = formatTime(schedule.startTime)
  // 如果结束时间在同一天，使用workDate；如果跨天，使用endDate（如果有）
  const endDate = schedule.endDate || schedule.workDate
  const endTime = formatTime(schedule.endTime)
  scheduleForm.endDateTime = `${endDate} ${endTime}`
  scheduleForm.availableStatus = schedule.availableStatus
  showAddScheduleDialog.value = true
  checkConflict()
}

const deleteScheduleItem = async (scheduleId) => {
  try {
    await ElMessageBox.confirm('确定要删除此可服务时间吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteSchedule(scheduleId)
    ElMessage.success('删除成功')
    await loadSchedules()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const saveSchedule = async () => {
  if (!scheduleFormRef.value) return
  
  await scheduleFormRef.value.validate(async (valid) => {
    if (valid) {
      if (conflictWarning.value && conflictWarning.value.includes('冲突')) {
        try {
          await ElMessageBox.confirm(conflictWarning.value, '时间冲突警告', {
            confirmButtonText: '继续保存',
            cancelButtonText: '取消',
            type: 'warning'
          })
        } catch (error) {
          if (error === 'cancel') return
        }
      }
      
      saving.value = true
      try {
        // 解析结束日期时间
        const endDateTime = dayjs(scheduleForm.endDateTime)
        const endDate = endDateTime.format('YYYY-MM-DD')
        const endTime = endDateTime.format('HH:mm:ss')
        
        const data = {
          workDate: scheduleForm.workDate,
          startTime: scheduleForm.startTime.length === 5 ? scheduleForm.startTime + ':00' : scheduleForm.startTime,
          endDate: endDate, // 结束日期（可能跨天）
          endTime: endTime, // 结束时间
          availableStatus: scheduleForm.availableStatus
        }
        
        if (editingSchedule.value) {
          await updateSchedule(editingSchedule.value.scheduleId, data)
          ElMessage.success('更新成功')
        } else {
          await createSchedule(data)
          ElMessage.success('添加成功')
        }
        
        showAddScheduleDialog.value = false
        resetScheduleForm()
        await loadSchedules()
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '保存失败')
      } finally {
        saving.value = false
      }
    }
  })
}

const resetScheduleForm = () => {
  editingSchedule.value = null
  scheduleForm.workDate = ''
  scheduleForm.startTime = ''
  scheduleForm.endDateTime = ''
  scheduleForm.availableStatus = 1
  conflictWarning.value = ''
}

const loadAppointments = async () => {
  if (!staffId.value) {
    console.warn('⚠️ staffId为空，无法加载预约列表')
    return
  }
  
  try {
    // 使用服务人员专用接口，不需要传递 staffId（后端会自动识别当前登录用户）
    console.log('📥 开始加载服务人员预约列表...')
    const response = await getStaffAppointmentsList()
    console.log('📥 后端返回的原始数据:', response.data)
    
    const data = response.data?.data || response.data || []
    const appointmentList = Array.isArray(data) ? data : []
    
    console.log(`✅ 成功加载 ${appointmentList.length} 条预约数据`)
    if (appointmentList.length > 0) {
      console.log('预约数据示例:', appointmentList[0])
    }
    
    appointments.value = appointmentList
  } catch (error) {
    console.error('❌ 加载预约列表失败:', error)
    console.error('错误详情:', {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status
    })
    appointments.value = []
    ElMessage.error(error.response?.data?.message || '加载预约列表失败')
  }
}

const loadSchedules = async () => {
  if (!staffId.value) return
  
  try {
    // 使用服务人员专用接口，不需要传递 staffId（后端会自动识别当前登录用户）
    // 可以传递日期范围等筛选参数
    const response = await getSchedules()
    const data = response.data?.data || response.data || []
    schedulesList.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('加载可服务时间列表失败:', error)
    schedulesList.value = []
    ElMessage.error(error.response?.data?.message || '加载可服务时间列表失败')
  }
}

onMounted(async () => {
  await Promise.all([
    loadAppointments(),
    loadSchedules()
  ])
})
</script>

<style scoped lang="scss">
.my-schedule-view {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.content {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.calendar-day {
  height: 100%;
  padding: 5px;
  
  .day-number {
    font-weight: bold;
    margin-bottom: 5px;
    font-size: 14px;
  }
  
  .day-events {
    display: flex;
    flex-direction: column;
    gap: 2px;
    
    .el-tag {
      font-size: 11px;
      padding: 2px 4px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

.schedules-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
    }
  }
}
</style>
