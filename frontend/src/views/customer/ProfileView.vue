<template>
  <div class="profile-view">
    <Navigation />
    <div class="content">
      <el-container>
        <el-main>
          <el-card>
            <template #header>
              <div class="card-header">
              <h2>个人中心</h2>
                <el-button 
                  type="primary" 
                  size="small" 
                  :icon="Refresh"
                  @click="refreshData"
                  :loading="refreshing"
                >
                  刷新
                </el-button>
              </div>
            </template>
            
            <el-tabs v-model="activeTab" @tab-change="handleTabChange">
              <el-tab-pane label="我的预约" name="appointments">
                <div class="tab-header">
                  <div class="tab-actions">
                  <el-button type="primary" @click="$router.push('/appointment')">
                    <el-icon><Plus /></el-icon>
                    新建预约
                  </el-button>
                    <el-select 
                      v-model="statusFilter" 
                      placeholder="筛选状态" 
                      clearable
                      style="width: 150px; margin-left: 10px;"
                      @change="loadAppointments"
                    >
                      <el-option label="全部" :value="undefined" />
                      <el-option label="待确认" :value="0" />
                      <el-option label="已确认" :value="1" />
                      <el-option label="进行中" :value="2" />
                      <el-option label="已完成" :value="3" />
                      <el-option label="已取消" :value="4" />
                    </el-select>
                  </div>
                </div>
                
                <div v-if="loading" class="loading-container">
                  <el-skeleton :rows="5" animated />
                </div>
                
                <div v-else-if="appointments.length === 0" class="empty-state">
                  <el-empty description="暂无预约记录">
                    <el-button type="primary" @click="$router.push('/appointment')">
                      立即预约
                    </el-button>
                  </el-empty>
                </div>
                
                <el-table :data="appointments" v-else stripe border style="width: 100%">
                  <el-table-column prop="appointmentId" label="预约ID" width="90" align="center">
                    <template #default="scope">
                      {{ scope.row.appointmentId ?? scope.row.id ?? '-' }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="serviceId" label="服务ID" width="90" align="center">
                    <template #default="scope">
                      {{ scope.row.serviceId ?? '-' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="服务项目" width="150" show-overflow-tooltip>
                    <template #default="scope">
                      {{ scope.row.serviceName || scope.row.service?.serviceName || scope.row.service?.name || (scope.row.serviceId ? `服务#${scope.row.serviceId}` : '-') }}
                    </template>
                  </el-table-column>
                  <el-table-column label="服务人员" width="120" show-overflow-tooltip>
                    <template #default="scope">
                      {{ scope.row.staffName || scope.row.staff?.name || scope.row.staff?.staffName || (scope.row.staffId ? `员工#${scope.row.staffId}` : '-') }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="appointmentDate" label="预约日期" width="110" align="center">
                    <template #default="scope">
                      {{ formatDate(scope.row.appointmentDate) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="预约时间" width="180" show-overflow-tooltip>
                    <template #default="scope">
                      {{ formatAppointmentTime(scope.row) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="totalAmount" label="订单金额" width="110" align="right">
                    <template #default="scope">
                      ¥{{ (scope.row.totalAmount || 0).toFixed(2) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="预约状态" width="110" align="center">
                    <template #default="scope">
                      <el-tag :type="getStatusType(scope.row.status)" size="small">
                        {{ getStatusText(scope.row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="paymentStatus" label="支付状态" width="110" align="center">
                    <template #default="scope">
                      <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)" size="small">
                        {{ getPaymentStatusText(scope.row.paymentStatus) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="280" fixed="right" align="center">
                    <template #default="scope">
                      <el-button
                        size="small"
                        type="primary"
                        link
                        @click="viewDetail(scope.row)"
                      >
                        查看详情
                      </el-button>
                      
                      <el-button
                        size="small"
                        type="warning"
                        link
                        v-if="canPay(scope.row)"
                        @click="goToPayment(scope.row)"
                      >
                        去支付
                      </el-button>
                      
                      <el-button
                        size="small"
                        type="success"
                        link
                        v-if="canReview(scope.row)"
                        @click="goToReview(scope.row)"
                      >
                        评价
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
              
              <el-tab-pane label="个人信息" name="info">
                <div class="avatar-section">
                  <el-upload
                    :action="uploadUrl"
                    :headers="uploadHeaders"
                    :on-success="handleAvatarSuccess"
                    :before-upload="beforeAvatarUpload"
                    :show-file-list="false"
                  >
                    <img v-if="userInfo.avatar" :src="userInfo.avatar" class="avatar" />
                    <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                  </el-upload>
                  <p class="avatar-tip">点击上传头像</p>
                </div>
                <el-form :model="userInfo" label-width="100px">
                  <el-form-item label="用户名">
                    <el-input v-model="userInfo.username" disabled />
                  </el-form-item>
                  <el-form-item label="手机号">
                    <el-input v-model="userInfo.phone" />
                  </el-form-item>
                  <el-form-item label="邮箱">
                    <el-input v-model="userInfo.email" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="updateInfo">
                      保存
                    </el-button>
                  </el-form-item>
                </el-form>
              </el-tab-pane>
              
              <el-tab-pane label="我的评价" name="reviews">
                <div v-if="loadingReviews" class="loading-container">
                  <el-skeleton :rows="3" animated />
                </div>
                <div v-else-if="reviews.length === 0" class="empty-state">
                  <el-empty description="暂无评价记录">
                    <el-button type="primary" @click="$router.push('/profile')">
                      查看我的预约
                  </el-button>
                  </el-empty>
                </div>
                <div v-else>
                  <div v-for="review in reviews" :key="review.reviewId || review.id" class="review-item">
                    <el-card shadow="hover" style="margin-bottom: 20px;">
                      <div class="review-header">
                        <div class="review-meta">
                          <span class="service-name">{{ review.serviceName || '服务评价' }}</span>
                          <span class="review-date">{{ formatDate(review.createTime) }}</span>
                        </div>
                        <el-rate 
                          v-model="review.overallRating" 
                          disabled 
                          show-score 
                          text-color="#ff9900"
                        />
                      </div>
                      <div class="review-ratings" v-if="review.serviceAttitudeRating || review.professionalAbilityRating || review.serviceQualityRating">
                        <div class="rating-item">
                          <span>服务态度：</span>
                          <el-rate v-model="review.serviceAttitudeRating" disabled size="small" />
                          <span class="rating-value">{{ review.serviceAttitudeRating }}分</span>
                        </div>
                        <div class="rating-item">
                          <span>专业能力：</span>
                          <el-rate v-model="review.professionalAbilityRating" disabled size="small" />
                          <span class="rating-value">{{ review.professionalAbilityRating }}分</span>
                        </div>
                        <div class="rating-item">
                          <span>服务质量：</span>
                          <el-rate v-model="review.serviceQualityRating" disabled size="small" />
                          <span class="rating-value">{{ review.serviceQualityRating }}分</span>
                        </div>
                      </div>
                      <div class="review-content" v-if="review.reviewContent">
                        <p>{{ review.reviewContent }}</p>
                      </div>
                      <div class="review-footer">
                        <el-tag v-if="review.isAnonymous" size="small" type="info">匿名评价</el-tag>
                        <el-button 
                          size="small" 
                          type="primary" 
                          link
                          @click="viewAppointmentDetail(review.appointmentId)"
                        >
                          查看预约详情
                        </el-button>
                      </div>
                    </el-card>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'
import { getMyAppointments, getAppointmentDetail } from '@/api/appointments'
import { updateUserInfo } from '@/api/user'
import { getMyReviews } from '@/api/reviews'
import { getServiceDetail } from '@/api/services'
import { getStaffDetail } from '@/api/staff'
import { APPOINTMENT_STATUS_CONFIG, APPOINTMENT_STATUS, PAYMENT_STATUS_CONFIG } from '@/utils/constants'
import Navigation from '@/components/common/Navigation.vue'

const router = useRouter()
const route = useRoute()
const store = useStore()

const appointments = ref([])
const reviews = ref([])
const loading = ref(false)
const loadingReviews = ref(false)
const refreshing = ref(false)
const activeTab = ref('appointments')
const statusFilter = ref(undefined)
let statusPollingTimer = null // 快速状态轮询（针对待支付订单）
let autoRefreshTimer = null // 定期自动刷新（所有数据）

const userInfo = ref({
  avatar: '',
  username: '',
  phone: '',
  email: ''
})

const userId = computed(() => store.getters['user/userId'])

const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL || '/api'}/files/upload`)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

// 格式化预约时间
const formatAppointmentTime = (appointment) => {
  if (appointment.startTime && appointment.endTime) {
    return `${appointment.startTime} - ${appointment.endTime}`
  } else if (appointment.appointmentTime) {
    return appointment.appointmentTime
  }
  return '-'
}

// 格式化日期（仅日期，不含时间）
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  // 如果是日期字符串（如 "2026-01-15"），直接返回
  if (typeof dateStr === 'string' && /^\d{4}-\d{2}-\d{2}/.test(dateStr)) {
    return dateStr.split(' ')[0].split('T')[0]
  }
  try {
    const date = new Date(dateStr)
    if (isNaN(date.getTime())) return dateStr
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }).replace(/\//g, '-')
  } catch (e) {
    return dateStr
  }
}

const loadAppointments = async () => {
  loading.value = true
  try {
    // 使用新的API，自动从token获取userId
    const params = {}
    if (statusFilter.value !== undefined && statusFilter.value !== null) {
      params.status = statusFilter.value
    }
    
    const response = await getMyAppointments(params)
    let data = response.data?.data || response.data || []
    
    // 确保是数组
    if (!Array.isArray(data)) {
      data = []
    }
    
    // 调试：打印原始数据（仅开发环境）
    if (import.meta.env.DEV && data.length > 0) {
      console.debug('📥 预约列表原始数据（第一条）:', data[0])
    }
    
    // 数据清洗：确保每个预约都有有效的appointmentId，并补充缺失的信息
    const cleanedData = data.map(item => {
      const appointmentId = item.appointmentId !== undefined && item.appointmentId !== null 
        ? item.appointmentId 
        : (item.id !== undefined && item.id !== null ? item.id : null)
      
      if (appointmentId === null || appointmentId === undefined) {
        return null
      }
      
      return {
        ...item,
        // 确保appointmentId存在且有效
        appointmentId,
        // 确保id字段也存在
        id: appointmentId,
        // 处理服务名称：优先使用已有的serviceName，如果没有则尝试从service对象中获取
        serviceName: item.serviceName || item.service?.serviceName || item.service?.name || null,
        serviceId: item.serviceId || item.service?.serviceId || item.service?.id || null,
        // 处理服务人员名称：优先使用已有的staffName，如果没有则尝试从staff对象中获取
        staffName: item.staffName || item.staff?.name || item.staff?.staffName || null,
        staffId: item.staffId || item.staff?.staffId || item.staff?.id || null,
        // 确保其他关键字段存在
        appointmentDate: item.appointmentDate || item.appointment_date || null,
        appointmentTime: item.appointmentTime || item.appointment_time || null,
        startTime: item.startTime || item.start_time || null,
        endTime: item.endTime || item.end_time || null,
        totalAmount: item.totalAmount || item.total_amount || 0,
        status: item.status !== undefined && item.status !== null ? item.status : 0,
        paymentStatus: item.paymentStatus !== undefined && item.paymentStatus !== null 
          ? item.paymentStatus 
          : (item.payment_status !== undefined && item.payment_status !== null ? item.payment_status : 0)
      }
    }).filter(item => item !== null) // 过滤掉无效的预约
    
    // 调试：打印清洗后的数据（仅开发环境）
    if (import.meta.env.DEV && cleanedData.length > 0) {
      console.debug('✅ 支付状态映射结果（第一条）:', {
        appointmentId: cleanedData[0].appointmentId,
        paymentStatus: cleanedData[0].paymentStatus,
        paymentStatusType: typeof cleanedData[0].paymentStatus,
        displayText: getPaymentStatusText(cleanedData[0].paymentStatus)
      })
    }
    
    // 先设置数据，不阻塞显示
    appointments.value = cleanedData
    
    // 如果有缺失的服务名称或服务人员姓名，尝试批量补充（异步执行，不阻塞显示，不抛出错误）
    enhanceAppointmentData().catch(error => {
      // 静默处理错误，不影响主流程
      console.warn('数据增强失败，但不影响显示:', error)
    })
    
    // 启动快速状态轮询（针对待支付订单，每30秒）
    startStatusPolling()
  } catch (error) {
    console.error('获取预约列表失败:', error)
    const errorMsg = error.response?.data?.message || '获取预约列表失败'
    ElMessage.error(errorMsg)
    appointments.value = []
  } finally {
    loading.value = false
  }
}

// 增强预约数据：补充缺失的服务名称和服务人员姓名（异步，不阻塞显示，静默处理错误）
const enhanceAppointmentData = async () => {
  try {
    // 直接使用 appointments.value，确保响应式更新
    const currentAppointments = appointments.value
    if (!currentAppointments || currentAppointments.length === 0) {
      return
    }
    
    // 收集需要查询的数据（最多查询前20条，确保所有可见数据都能被增强）
    const appointmentsToEnhance = currentAppointments.slice(0, 20)
    const serviceIdsToFetch = new Set()
    const staffIdsToFetch = new Set()
    
    appointmentsToEnhance.forEach(apt => {
      // 检查是否需要查询服务名称（如果当前显示的是"服务#ID"，则需要查询）
      if ((!apt.serviceName || apt.serviceName.startsWith('服务#')) && apt.serviceId) {
        serviceIdsToFetch.add(apt.serviceId)
      }
      // 检查是否需要查询服务人员姓名（如果当前显示的是"员工#ID"，则需要查询）
      if ((!apt.staffName || apt.staffName.startsWith('员工#')) && apt.staffId) {
        staffIdsToFetch.add(apt.staffId)
      }
    })
    
    // 如果没有需要补充的数据，直接返回
    if (serviceIdsToFetch.size === 0 && staffIdsToFetch.size === 0) {
      return
    }
    
    // 缓存查询结果
    const serviceCache = new Map()
    const staffCache = new Map()
    
    // 批量查询服务详情（限制并发数，静默处理所有错误）
    if (serviceIdsToFetch.size > 0) {
      const servicePromises = Array.from(serviceIdsToFetch).map(async (serviceId) => {
        try {
          const response = await getServiceDetail(serviceId)
          const service = response.data?.data || response.data
          if (service && (service.serviceName || service.name || service.service_name)) {
            const name = service.serviceName || service.name || service.service_name
            serviceCache.set(serviceId, name)
            console.log(`✅ 成功获取服务名称: serviceId=${serviceId}, name=${name}`)
          } else {
            console.warn(`⚠️ 服务详情中未找到名称字段: serviceId=${serviceId}`, service)
          }
        } catch (error) {
          // 记录详细错误信息，帮助调试
          console.warn(`❌ 获取服务详情失败 (serviceId: ${serviceId}):`, {
            message: error.message,
            response: error.response?.data,
            status: error.response?.status
          })
        }
      })
      
      await Promise.allSettled(servicePromises)
    }
    
    // 批量查询服务人员详情（限制并发数，静默处理所有错误）
    if (staffIdsToFetch.size > 0) {
      const staffPromises = Array.from(staffIdsToFetch).map(async (staffId) => {
        try {
          const response = await getStaffDetail(staffId)
          const staff = response.data?.data || response.data
          if (staff) {
            // 尝试多种字段名格式获取姓名
            const name = staff.name || staff.staffName || staff.realName || staff.username
            if (name) {
              staffCache.set(staffId, name)
              console.log(`✅ 成功获取服务人员姓名: staffId=${staffId}, name=${name}`)
            } else {
              console.warn(`⚠️ 服务人员详情中未找到姓名字段: staffId=${staffId}`, staff)
            }
          }
        } catch (error) {
          // 记录详细错误信息，帮助调试
          console.warn(`❌ 获取服务人员详情失败 (staffId: ${staffId}):`, {
            message: error.message,
            response: error.response?.data,
            status: error.response?.status,
            url: `/staff/${staffId}`
          })
        }
      })
      
      await Promise.allSettled(staffPromises)
    }
    
    // 更新预约数据（直接修改 appointments.value 中的对象，触发响应式更新）
    let updatedCount = 0
    appointmentsToEnhance.forEach(apt => {
      if (apt.serviceId && serviceCache.has(apt.serviceId)) {
        apt.serviceName = serviceCache.get(apt.serviceId)
        updatedCount++
      }
      if (apt.staffId && staffCache.has(apt.staffId)) {
        apt.staffName = staffCache.get(apt.staffId)
        updatedCount++
      }
    })
    
    if (updatedCount > 0) {
      console.log(`✅ 成功更新 ${updatedCount} 条数据的名称信息`)
    } else {
      console.warn(`⚠️ 未更新任何数据，可能API调用失败或数据已存在`)
    }
  } catch (error) {
    // 捕获所有未预期的错误，静默处理
    console.debug('数据增强过程出错:', error.message)
  }
}

const loadReviews = async () => {
  loadingReviews.value = true
  try {
    // 使用新的API，自动从token获取userId
    const response = await getMyReviews()
    let data = response.data?.data || response.data || []
    
    // 确保是数组
    if (!Array.isArray(data)) {
      data = []
    }
    
    reviews.value = data
  } catch (error) {
    console.error('获取评价列表失败:', error)
    const errorMsg = error.response?.data?.message || '获取评价列表失败'
    // 如果接口不存在或出错，显示空列表而不是错误提示（避免影响用户体验）
  reviews.value = []
    // 只在开发环境显示错误
    if (import.meta.env.DEV) {
      ElMessage.warning(errorMsg)
    }
  } finally {
    loadingReviews.value = false
  }
}

// 快速状态轮询（针对待支付订单，频率较高）
// 注意：后端已有定时任务每5分钟自动更新状态，前端轮询主要用于支付状态等需要及时反馈的场景
const startStatusPolling = () => {
  // 清除之前的定时器
  stopStatusPolling()
  
  // 检查是否有待支付的订单（支付状态需要及时反馈）
  const needPolling = appointments.value.some(apt => 
    (apt.paymentStatus === 0 || apt.paymentStatus === 'unpaid') && 
    apt.status === APPOINTMENT_STATUS.PENDING
  )
  
  if (!needPolling) return
  
  statusPollingTimer = setInterval(async () => {
    // 只轮询待支付的订单（支付状态变化需要及时反馈）
    const unpaidAppointments = appointments.value.filter(apt => 
      (apt.paymentStatus === 0 || apt.paymentStatus === 'unpaid') && 
      apt.status === APPOINTMENT_STATUS.PENDING
    )
    
    if (unpaidAppointments.length === 0) {
      stopStatusPolling()
      return
    }
    
    // 重新加载整个列表（更可靠，确保数据一致）
    try {
      await loadAppointments()
    } catch (error) {
      console.error('轮询更新预约列表失败:', error)
    }
    
    // 检查是否还有待支付的订单
    const stillNeedPolling = appointments.value.some(apt => 
      (apt.paymentStatus === 0 || apt.paymentStatus === 'unpaid') && 
      apt.status === APPOINTMENT_STATUS.PENDING
    )
    
    if (!stillNeedPolling) {
      stopStatusPolling()
    }
  }, 30000) // 每30秒轮询一次（支付状态需要及时反馈，但不能太频繁）
}

const stopStatusPolling = () => {
  if (statusPollingTimer) {
    clearInterval(statusPollingTimer)
    statusPollingTimer = null
  }
}

// 定期自动刷新（所有数据，频率较低）
// 后端定时任务每5分钟更新状态，前端每1分钟刷新一次即可
const startAutoRefresh = () => {
  // 清除之前的定时器
  stopAutoRefresh()
  
  autoRefreshTimer = setInterval(async () => {
    // 根据当前激活的tab刷新对应数据
    if (activeTab.value === 'appointments') {
      try {
        await loadAppointments()
      } catch (error) {
        console.error('自动刷新预约列表失败:', error)
      }
    } else if (activeTab.value === 'reviews') {
      try {
        await loadReviews()
      } catch (error) {
        console.error('自动刷新评价列表失败:', error)
      }
    }
  }, 60000) // 每1分钟自动刷新一次
}

const stopAutoRefresh = () => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
}

const refreshData = async () => {
  refreshing.value = true
  try {
    if (activeTab.value === 'appointments') {
      await loadAppointments()
      ElMessage.success('预约列表已刷新')
    } else if (activeTab.value === 'reviews') {
      await loadReviews()
      ElMessage.success('评价列表已刷新')
    }
  } finally {
    refreshing.value = false
  }
}

const handleTabChange = (tabName) => {
  // 切换tab时加载对应数据
  if (tabName === 'appointments') {
    if (appointments.value.length === 0) {
      loadAppointments()
    }
  } else if (tabName === 'reviews') {
    if (reviews.value.length === 0) {
      loadReviews()
    }
  }
  
  // 重新启动自动刷新（确保正确的tab被刷新）
  startAutoRefresh()
}

// 获取有效的预约ID（处理appointmentId可能为0的情况）
const getValidAppointmentId = (row) => {
  // appointmentId 可能是 0，所以不能直接用 || 判断
  const id = row.appointmentId !== undefined && row.appointmentId !== null 
    ? row.appointmentId 
    : (row.id !== undefined && row.id !== null ? row.id : null)
  
  // 转换为数字，确保是有效的Long类型
  if (id === null || id === undefined || id === '') {
    return null
  }
  
  const numId = typeof id === 'string' ? parseInt(id, 10) : Number(id)
  return isNaN(numId) ? null : numId
}

const viewDetail = (row) => {
  const id = getValidAppointmentId(typeof row === 'object' ? row : { appointmentId: row, id: row })
  if (id !== null && id !== undefined) {
    router.push(`/appointment/${id}`)
  } else {
    ElMessage.warning('预约ID无效，无法查看详情')
  }
}

const viewAppointmentDetail = (appointmentId) => {
  const id = getValidAppointmentId({ appointmentId, id: appointmentId })
  if (id !== null && id !== undefined) {
  router.push(`/appointment/${id}`)
  } else {
    ElMessage.warning('预约ID无效，无法查看详情')
  }
}

const goToReview = (row) => {
  const appointmentId = getValidAppointmentId(typeof row === 'object' ? row : { appointmentId: row, id: row })
  if (appointmentId !== null && appointmentId !== undefined) {
  router.push({
    path: '/review',
      query: { appointmentId: String(appointmentId) }
  })
  } else {
    ElMessage.warning('预约ID无效，无法进行评价')
  }
}

const goToPayment = (row) => {
  const appointmentId = getValidAppointmentId(typeof row === 'object' ? row : { appointmentId: row, id: row })
  if (appointmentId !== null && appointmentId !== undefined) {
  router.push({
    path: '/payment',
      query: { appointmentId: String(appointmentId) }
  })
  } else {
    ElMessage.warning('预约ID无效，无法进行支付')
  }
}

// 判断是否可以支付
const canPay = (appointment) => {
  // 状态为待确认(0)且支付状态为待支付(0)
  return appointment.status === APPOINTMENT_STATUS.PENDING && 
         (appointment.paymentStatus === 0 || appointment.paymentStatus === 'unpaid' || !appointment.paymentStatus)
}

// 判断是否可以评价
const canReview = (appointment) => {
  // 处理状态：可能是数字或字符串
  const status = appointment.status !== undefined && appointment.status !== null ? appointment.status : 0
  const statusNum = typeof status === 'string' ? parseInt(status) : status
  
  // 状态为已完成(3)且还没有评价（检查多个可能的字段）
  return statusNum === APPOINTMENT_STATUS.COMPLETED && 
         !appointment.hasReview && 
         !appointment.reviewed &&
         statusNum === 3
}

const getStatusType = (status) => {
  // 处理数字状态和字符串状态
  const statusNum = typeof status === 'string' ? parseInt(status) : status
  return APPOINTMENT_STATUS_CONFIG[statusNum]?.type || 'info'
}

const getStatusText = (status) => {
  const statusNum = typeof status === 'string' ? parseInt(status) : status
  return APPOINTMENT_STATUS_CONFIG[statusNum]?.text || '未知'
}

// 支付状态类型
const getPaymentStatusType = (paymentStatus) => {
  // 处理 null、undefined 或数字字符串的情况
  if (paymentStatus === null || paymentStatus === undefined) {
    return 'info'
  }
  // 确保是数字类型
  const status = typeof paymentStatus === 'string' ? parseInt(paymentStatus, 10) : paymentStatus
  return PAYMENT_STATUS_CONFIG[status]?.type || 'info'
}

// 支付状态文本
const getPaymentStatusText = (paymentStatus) => {
  // 处理 null、undefined 或数字字符串的情况
  if (paymentStatus === null || paymentStatus === undefined) {
    return '未知'
  }
  // 确保是数字类型
  const status = typeof paymentStatus === 'string' ? parseInt(paymentStatus, 10) : paymentStatus
  return PAYMENT_STATUS_CONFIG[status]?.text || '未知'
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleAvatarSuccess = async (response) => {
  if (response.data?.url) {
    userInfo.value.avatar = response.data.url
    await updateInfo() // 自动保存
    ElMessage.success('头像上传成功')
  }
}

const updateInfo = async () => {
  if (!userId.value) return
  
  try {
    await updateUserInfo(userId.value, userInfo.value)
    // 更新store中的用户信息
    store.dispatch('user/updateUserInfo', userInfo.value)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  }
}

// 监听路由变化，如果是从预约成功/支付成功页面跳转过来，刷新数据
watch(() => route.query, (newQuery) => {
  if (newQuery.refresh === 'true' || newQuery.from === 'appointment-success' || newQuery.from === 'payment-success') {
    // 根据来源决定刷新哪个tab
    if (newQuery.tab) {
      activeTab.value = newQuery.tab
    }
    
    // 立即刷新数据
    if (activeTab.value === 'appointments') {
      loadAppointments()
    } else if (activeTab.value === 'reviews') {
      loadReviews()
    }
    
    // 清除query参数，避免重复刷新
    router.replace({ query: {} })
  }
}, { immediate: true })

// 监听页面可见性，当页面重新可见时刷新数据
const handleVisibilityChange = () => {
  if (!document.hidden) {
    // 页面重新可见时刷新当前tab的数据
    if (activeTab.value === 'appointments') {
      loadAppointments()
    } else if (activeTab.value === 'reviews') {
      loadReviews()
    }
  }
}

onMounted(async () => {
  const info = store.getters['user/userInfo']
  if (info) {
    userInfo.value = { 
      avatar: info.avatar || '',
      username: info.username || '',
      phone: info.phone || '',
      email: info.email || ''
    }
  }
  
  // 根据当前tab加载数据
  if (route.query.tab) {
    activeTab.value = route.query.tab
  }
  
  // 如果是从预约成功/支付成功页面跳转，刷新预约列表
  if (route.query.refresh === 'true' || route.query.from === 'appointment-success' || route.query.from === 'payment-success') {
    if (route.query.tab) {
      activeTab.value = route.query.tab
    }
    if (activeTab.value === 'appointments') {
      await loadAppointments()
    } else if (activeTab.value === 'reviews') {
      await loadReviews()
    }
  } else {
    // 否则根据activeTab加载
    if (activeTab.value === 'appointments') {
      await loadAppointments()
    } else if (activeTab.value === 'reviews') {
      await loadReviews()
    }
  }
  
  // 启动定期自动刷新
  startAutoRefresh()
  
  // 监听页面可见性变化
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  // 清理所有定时器
  stopStatusPolling()
  stopAutoRefresh()
  // 移除事件监听
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style scoped lang="scss">
.profile-view {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.content {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  h2 {
    margin: 0;
    color: #333;
    font-size: 20px;
    font-weight: 500;
  }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.loading-container {
  padding: 20px;
}

.tab-header {
  margin-bottom: 20px;
  
  .tab-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.avatar-section {
  text-align: center;
  margin-bottom: 30px;
  
  .avatar {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    object-fit: cover;
    cursor: pointer;
  }
  
  .avatar-uploader-icon {
    font-size: 40px;
    color: #8c939d;
    width: 120px;
    height: 120px;
    line-height: 120px;
    text-align: center;
    border: 1px dashed #d9d9d9;
    border-radius: 50%;
    cursor: pointer;
    display: inline-block;
    
    &:hover {
      border-color: #4CAF50;
    }
  }
  
  .avatar-tip {
    margin-top: 10px;
    color: #999;
    font-size: 14px;
  }
}

.review-item {
  .review-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    
    .review-meta {
      display: flex;
      flex-direction: column;
      gap: 5px;
      
      .service-name {
        font-size: 16px;
        font-weight: 500;
        color: #333;
      }
      
      .review-date {
        font-size: 12px;
        color: #999;
      }
    }
  }
  
  .review-ratings {
    margin: 15px 0;
    padding: 15px;
    background-color: #f5f7fa;
    border-radius: 4px;
    
    .rating-item {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 10px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      span:first-child {
        width: 80px;
        color: #666;
      }
      
      .rating-value {
        color: #ff9900;
        font-weight: bold;
      }
    }
  }
  
  .review-content {
    margin: 15px 0;
    padding: 15px;
    background-color: #fafafa;
    border-radius: 4px;
    
    p {
      margin: 0;
      line-height: 1.6;
      color: #333;
    }
  }
  
  .review-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 15px;
    padding-top: 15px;
    border-top: 1px solid #eee;
  }
}
</style>

