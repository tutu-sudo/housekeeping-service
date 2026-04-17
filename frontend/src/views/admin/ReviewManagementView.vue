<template>
  <div class="review-management-view">
    <Navigation />
    <div class="content">
      <el-container>
        <AdminSidebar />
        <el-main>
          <div class="page-header">
            <h2>评价管理</h2>
          </div>
          
          <el-card>
            <el-form :inline="true" class="filter-form">
              <el-form-item label="服务人员ID">
                <el-input v-model="filters.staffId" placeholder="请输入服务人员ID" clearable />
              </el-form-item>
              <el-form-item label="预约ID">
                <el-input v-model="filters.appointmentId" placeholder="请输入预约ID" clearable />
              </el-form-item>
              <!-- 新增：评价来源（顾客评价 / 员工自评 / 管理员评价） -->
              <el-form-item label="评价来源">
                <el-select
                  v-model="filters.reviewerRole"
                  placeholder="全部"
                  clearable
                  style="width: 150px"
                >
                  <el-option label="顾客评价" :value="1" />
                  <el-option label="员工自评" :value="2" />
                  <el-option label="管理员评价" :value="3" />
                </el-select>
              </el-form-item>
              <!-- 新增：评价对象（顾客 / 服务人员），便于后端区分平台对顾客和对员工的评价 -->
              <el-form-item label="评价对象">
                <el-select
                  v-model="filters.reviewTarget"
                  placeholder="全部"
                  clearable
                  style="width: 150px"
                >
                  <el-option label="顾客" :value="1" />
                  <el-option label="服务人员" :value="2" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadReviews" :loading="loading">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button @click="resetFilters">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
          
          <el-card style="margin-top: 20px">
            <el-table :data="paginatedReviews" v-loading="loading" stripe>
              <el-table-column prop="reviewId" label="ID" width="80" />
              <el-table-column prop="appointmentId" label="预约ID" width="100" />
              <el-table-column prop="staffName" label="服务人员" width="120" />
              <el-table-column prop="customerName" label="客户姓名" width="120" />
              <el-table-column prop="overallRating" label="总体评分" width="100">
                <template #default="scope">
                  <el-rate v-model="scope.row.overallRating" disabled show-score />
                </template>
              </el-table-column>
              <!-- 员工自评列表中不需要拆分的细分评分字段，只在顾客评价等场景展示 -->
              <el-table-column
                v-if="!isStaffSelfMode"
                prop="serviceAttitudeRating"
                label="服务态度"
                width="100"
              />
              <el-table-column
                v-if="!isStaffSelfMode"
                prop="professionalAbilityRating"
                label="专业能力"
                width="100"
              />
              <el-table-column
                v-if="!isStaffSelfMode"
                prop="serviceQualityRating"
                label="服务质量"
                width="100"
              />
              <el-table-column prop="reviewContent" label="评价内容" show-overflow-tooltip />
              <el-table-column prop="isAnonymous" label="是否匿名" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.isAnonymous ? 'info' : 'success'">
                    {{ scope.row.isAnonymous ? '匿名' : '实名' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="评价时间" width="180" />
              <el-table-column label="操作" width="250" fixed="right">
                <template #default="scope">
                  <el-button
                    size="small"
                    type="primary"
                    link
                    @click="viewDetail(scope.row)"
                  >
                    详情
                  </el-button>
                  <el-button
                    size="small"
                    type="success"
                    link
                    :class="['state-switch-btn', 'approve-btn', { 'is-active': getReviewDisplayState(scope.row) === 'approved' }]"
                    @click="handleApprove(scope.row)"
                  >
                    审核通过
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    link
                    :class="['state-switch-btn', 'mask-btn', { 'is-active': getReviewDisplayState(scope.row) === 'masked' }]"
                    @click="handleMask(scope.row)"
                  >
                    屏蔽
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <!-- 分页 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :total="total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </el-card>
          
          <!-- 评价详情对话框（含三方互评信息 & 管理员评价入口） -->
          <el-dialog
            v-model="detailDialogVisible"
            title="评价详情"
            width="800px"
            @close="reviewDetail = null"
          >
            <div v-loading="detailLoading">
              <el-descriptions :column="2" border v-if="reviewDetail">
                <el-descriptions-item label="评价ID">
                  {{ reviewDetail.reviewId }}
                </el-descriptions-item>
                <el-descriptions-item label="预约ID">
                  {{ reviewDetail.appointmentId }}
                </el-descriptions-item>
                <el-descriptions-item label="客户ID">
                  {{ reviewDetail.customerId }}
                </el-descriptions-item>
                <el-descriptions-item label="客户姓名">
                  {{ reviewDetail.customerName || '未知' }}
                </el-descriptions-item>
                <el-descriptions-item label="服务人员ID">
                  {{ reviewDetail.staffId }}
                </el-descriptions-item>
                <el-descriptions-item label="服务人员">
                  {{ reviewDetail.staffName || '未知' }}
                </el-descriptions-item>
                <el-descriptions-item label="总体评分">
                  <el-rate v-model="reviewDetail.overallRating" disabled show-score />
                </el-descriptions-item>
                <!-- 员工自评详情中不再展示服务态度 / 专业能力 / 服务质量三项细分评分 -->
                <el-descriptions-item v-if="!isStaffSelfMode" label="服务态度">
                  <el-rate v-model="reviewDetail.serviceAttitudeRating" disabled show-score />
                </el-descriptions-item>
                <el-descriptions-item v-if="!isStaffSelfMode" label="专业能力">
                  <el-rate v-model="reviewDetail.professionalAbilityRating" disabled show-score />
                </el-descriptions-item>
                <el-descriptions-item v-if="!isStaffSelfMode" label="服务质量">
                  <el-rate v-model="reviewDetail.serviceQualityRating" disabled show-score />
                </el-descriptions-item>
                <el-descriptions-item label="是否匿名">
                  <el-tag :type="reviewDetail.isAnonymous ? 'info' : 'success'">
                    {{ reviewDetail.isAnonymous ? '匿名' : '实名' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="评价时间">
                  {{ reviewDetail.createTime }}
                </el-descriptions-item>
                <el-descriptions-item label="评价内容" :span="2">
                  <div style="padding: 10px; background-color: #f5f5f5; border-radius: 4px;">
                    {{ reviewDetail.reviewContent || '无' }}
                  </div>
                </el-descriptions-item>

                <el-descriptions-item label="互评概览" :span="2">
                  <div style="font-size: 13px; color: #666;">
                    <div>顾客评价：{{ customerReviewBlock ? '已评价' : '未评价' }}</div>
                    <div>服务人员自评：{{ staffSelfReviewBlock ? '已自评' : '未自评' }}</div>
                    <div>管理员评价顾客：{{ adminReviewForCustomerBlock ? '已评价' : '未评价' }}</div>
                    <div>管理员评价服务人员：{{ adminReviewForStaffBlock ? '已评价' : '未评价' }}</div>
                  </div>
                </el-descriptions-item>
              </el-descriptions>

              <el-divider />

              <!-- 管理员评价顾客 -->
              <h4 style="margin: 10px 0;">管理员对顾客的评价</h4>
              <div v-if="adminReviewForCustomerBlock">
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="总体评分">
                    <el-rate v-model="adminReviewForCustomerBlock.overallRating" disabled show-score />
                  </el-descriptions-item>
                  <el-descriptions-item label="评价时间">
                    {{ adminReviewForCustomerBlock.reviewTime || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="评价内容" :span="2">
                    <div style="padding: 10px; background-color: #f5f5f5; border-radius: 4px;">
                      {{ adminReviewForCustomerBlock.reviewContent || '无' }}
                    </div>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
              <div v-else>
                <el-alert
                  title="当前顾客尚未被管理员评价，可以在下方进行评价。"
                  type="info"
                  :closable="false"
                  style="margin-bottom: 10px;"
                />
                <el-form :model="adminReviewCustomerForm" label-width="90px" style="max-width: 600px;">
                  <el-form-item label="总体评分">
                    <el-rate v-model="adminReviewCustomerForm.overallRating" show-score />
                  </el-form-item>
                  <el-form-item label="评价内容">
                    <el-input
                      v-model="adminReviewCustomerForm.reviewContent"
                      type="textarea"
                      :rows="3"
                      placeholder="请简要评价顾客的配合度、沟通情况等"
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="submitAdminReview(1)" :loading="submittingAdminReviewCustomer">
                      提交对顾客的评价
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>

              <el-divider />

              <!-- 管理员评价服务人员 -->
              <h4 style="margin: 10px 0;">管理员对服务人员的评价</h4>
              <div v-if="adminReviewForStaffBlock">
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="总体评分">
                    <el-rate v-model="adminReviewForStaffBlock.overallRating" disabled show-score />
                  </el-descriptions-item>
                  <el-descriptions-item label="评价时间">
                    {{ adminReviewForStaffBlock.reviewTime || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="评价内容" :span="2">
                    <div style="padding: 10px; background-color: #f5f5f5; border-radius: 4px;">
                      {{ adminReviewForStaffBlock.reviewContent || '无' }}
                    </div>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
              <div v-else>
                <el-alert
                  title="当前服务人员尚未被管理员评价，可以在下方进行评价。"
                  type="info"
                  :closable="false"
                  style="margin-bottom: 10px;"
                />
                <el-form :model="adminReviewStaffForm" label-width="90px" style="max-width: 600px;">
                  <el-form-item label="总体评分">
                    <el-rate v-model="adminReviewStaffForm.overallRating" show-score />
                  </el-form-item>
                  <el-form-item label="评价内容">
                    <el-input
                      v-model="adminReviewStaffForm.reviewContent"
                      type="textarea"
                      :rows="3"
                      placeholder="请评价服务人员的服务质量、态度等"
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="submitAdminReview(2)" :loading="submittingAdminReviewStaff">
                      提交对服务人员的评价
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>
            </div>
          </el-dialog>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { isReviewMasked } from '@/utils/review'
import {
  getAdminReviews,
  approveReview,
  maskReview,
  getAdminReviewDetail,
  createAdminReviewExtra,
  getAdminAppointmentReviewBundle
} from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

const route = useRoute()

const reviews = ref([])
const loading = ref(false)
// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = computed(() => reviews.value.length)

const paginatedReviews = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return reviews.value.slice(start, end)
})
const detailDialogVisible = ref(false)
const reviewDetail = ref(null)
const detailLoading = ref(false)
 const appointmentBundle = ref(null)
 const customerReviewBlock = ref(null)
 const staffSelfReviewBlock = ref(null)
 const adminReviewForCustomerBlock = ref(null)
 const adminReviewForStaffBlock = ref(null)
 const adminReviewCustomerForm = reactive({
   overallRating: 0,
   reviewContent: ''
 })
 const adminReviewStaffForm = reactive({
   overallRating: 0,
   reviewContent: ''
 })
 const submittingAdminReviewCustomer = ref(false)
 const submittingAdminReviewStaff = ref(false)

const filters = ref({
  staffId: undefined,
  appointmentId: undefined,
  // reviewerRole: 1=顾客评价，2=服务人员自评，3=管理员评价
  reviewerRole: undefined,
  // reviewTarget: 1=评价顾客，2=评价服务人员
  reviewTarget: undefined
})

// 是否处于“员工自评”视图：用于控制列表和详情中部分字段的展示
const isStaffSelfMode = computed(() => filters.value.reviewerRole === 2)
const getReviewDisplayState = (row) => (isReviewMasked(row) ? 'masked' : 'approved')

const applyReviewerRoleFromRoute = () => {
  const q = route.query?.reviewerRole
  if (q === undefined || q === null || q === '') return
  const num = typeof q === 'string' ? parseInt(q, 10) : Number(q)
  if (num === 1 || num === 2 || num === 3) {
    filters.value.reviewerRole = num
  }
}

const loadReviews = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.value.staffId) params.staffId = filters.value.staffId
    if (filters.value.appointmentId) params.appointmentId = filters.value.appointmentId
    if (filters.value.reviewerRole) params.reviewerRole = filters.value.reviewerRole
    if (filters.value.reviewTarget) params.reviewTarget = filters.value.reviewTarget

    const response = await getAdminReviews(params)
    const raw = response.data?.data || response.data || []

    const list = Array.isArray(raw)
      ? raw
      : (raw.records || raw.list || raw.items || raw.rows || raw.content || [])

    // 统一字段命名，保证表格里的“服务人员 / 客户姓名”都有值
    reviews.value = list.map((r) => ({
      ...r,
      staffName: r.staffName ?? r.staff_name ?? r.staff?.name ?? r.staffRealName ?? r.staff_real_name ?? '',
      customerName:
        r.customerName ?? r.customer_name ?? r.customer?.name ?? r.customerRealName ?? r.customer_real_name ?? ''
    }))
  } catch (error) {
    console.error('获取评价列表失败:', error)
    ElMessage.error('获取评价列表失败')
    reviews.value = []
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = {
    staffId: undefined,
    appointmentId: undefined,
    reviewerRole: undefined,
    reviewTarget: undefined
  }
  currentPage.value = 1
  loadReviews()
}

const handleSizeChange = () => {
  currentPage.value = 1
}

const handleCurrentChange = () => {
  // 交给 computed 自动切片
}

const handleApprove = async (row) => {
  if (getReviewDisplayState(row) === 'approved') return
  try {
    await ElMessageBox.confirm(
      '确定要审核通过该评价吗？',
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await approveReview(row.reviewId)
    ElMessage.success('审核通过')
    loadReviews()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleMask = async (row) => {
  if (getReviewDisplayState(row) === 'masked') return
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入屏蔽原因（可选）',
      '屏蔽评价',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入屏蔽原因'
      }
    )
    
    await maskReview(row.reviewId, reason || '')
    ElMessage.success('屏蔽成功')
    loadReviews()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const viewDetail = async (row) => {
  const reviewId = row?.reviewId || row?.id
  detailLoading.value = true
  detailDialogVisible.value = true
  appointmentBundle.value = null
  customerReviewBlock.value = null
  staffSelfReviewBlock.value = null
  adminReviewForCustomerBlock.value = null
  adminReviewForStaffBlock.value = null
  adminReviewCustomerForm.overallRating = 0
  adminReviewCustomerForm.reviewContent = ''
  adminReviewStaffForm.overallRating = 0
  adminReviewStaffForm.reviewContent = ''
  try {
    const response = await getAdminReviewDetail(reviewId)
    const data = response.data?.data || response.data || {}

    // 统一并补充客户 / 服务人员信息：优先使用详情接口返回，其次回退到列表行里的数据
    reviewDetail.value = {
      ...data,
      customerId:
        data.customerId ??
        data.customer_id ??
        data.customer?.id ??
        row?.customerId ??
        row?.customer_id ??
        null,
      staffId:
        data.staffId ??
        data.staff_id ??
        data.staff?.id ??
        row?.staffId ??
        row?.staff_id ??
        null,
      customerName:
        data.customerName ??
        data.customer_name ??
        data.customer?.name ??
        data.customerRealName ??
        data.customer_real_name ??
        row?.customerName ??
        row?.customer_name ??
        '',
      staffName:
        data.staffName ??
        data.staff_name ??
        data.staff?.name ??
        data.staffRealName ??
        data.staff_real_name ??
        row?.staffName ??
        row?.staff_name ??
        ''
    }

   // 加载该预约的三方评价包
   const appointmentId = reviewDetail.value?.appointmentId
   if (appointmentId) {
     try {
       const bundleRes = await getAdminAppointmentReviewBundle(appointmentId)
       const payload = bundleRes.data?.data || bundleRes.data || {}
       const normalize = (r) => {
         if (!r) return null
         return {
           overallRating: r.overallRating ?? r.rating ?? r.overall_rating ?? null,
           reviewContent: r.reviewContent ?? r.content ?? r.review_content ?? '',
           reviewTime: r.createTime ?? r.createdAt ?? r.created_at ?? r.reviewTime ?? ''
         }
       }
       appointmentBundle.value = payload
       customerReviewBlock.value = normalize(payload.customerReview)
       staffSelfReviewBlock.value = normalize(payload.staffSelfReview)
       adminReviewForCustomerBlock.value = normalize(payload.adminReviewForCustomer)
       adminReviewForStaffBlock.value = normalize(payload.adminReviewForStaff)
     } catch (e) {
       // 不影响主详情展示
       console.error('加载三方评价包失败:', e)
     }
   }
  } catch (error) {
    console.error('获取评价详情失败:', error)
    ElMessage.error('获取评价详情失败')
    detailDialogVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  // 支持从侧边栏下拉（顾客评价/员工自评）进入时自动设置筛选条件
  applyReviewerRoleFromRoute()
  loadReviews()
})

// 侧边栏切换“顾客评价/员工自评”会改变 query，这里自动同步筛选并刷新列表
watch(
  () => route.query?.reviewerRole,
  () => {
    applyReviewerRoleFromRoute()
    loadReviews()
  }
)

 const submitAdminReview = async (reviewTarget) => {
   if (!reviewDetail.value?.appointmentId) {
     ElMessage.error('预约ID缺失，无法提交管理员评价')
     return
   }

   const isCustomer = reviewTarget === 1
   const form = isCustomer ? adminReviewCustomerForm : adminReviewStaffForm
   const loadingRef = isCustomer ? submittingAdminReviewCustomer : submittingAdminReviewStaff

   if (!form.overallRating) {
     ElMessage.warning('请先给出总体评分')
     return
   }
   if (loadingRef.value) return

   loadingRef.value = true
   try {
     await createAdminReviewExtra({
       appointmentId: reviewDetail.value.appointmentId,
       reviewTarget,
       overallRating: form.overallRating,
       reviewContent: form.reviewContent
     })
     ElMessage.success('管理员评价提交成功')

     // 重新加载三方评价包，刷新展示
     try {
       const bundleRes = await getAdminAppointmentReviewBundle(reviewDetail.value.appointmentId)
       const payload = bundleRes.data?.data || bundleRes.data || {}
       const normalize = (r) => {
         if (!r) return null
         return {
           overallRating: r.overallRating ?? r.rating ?? r.overall_rating ?? null,
           reviewContent: r.reviewContent ?? r.content ?? r.review_content ?? '',
           reviewTime: r.createTime ?? r.createdAt ?? r.created_at ?? r.reviewTime ?? ''
         }
       }
       appointmentBundle.value = payload
       customerReviewBlock.value = normalize(payload.customerReview)
       staffSelfReviewBlock.value = normalize(payload.staffSelfReview)
       adminReviewForCustomerBlock.value = normalize(payload.adminReviewForCustomer)
       adminReviewForStaffBlock.value = normalize(payload.adminReviewForStaff)
     } catch (e) {
       console.error('重新加载三方评价包失败:', e)
     }
   } catch (error) {
     const msg = error.response?.data?.message || error.message || '提交管理员评价失败'
     ElMessage.error(msg)
   } finally {
     loadingRef.value = false
   }
 }
</script>

<style scoped lang="scss">
.review-management-view {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.content {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
  
  h2 {
    margin: 0;
    color: #333;
    font-size: 24px;
    font-weight: 500;
  }
}

.filter-form {
  margin-bottom: 0;
  
  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.state-switch-btn {
  transition: all 0.2s ease;
  font-weight: 400;
}

.approve-btn {
  color: #3a8b1d !important;
}

.approve-btn.is-active {
  color: rgba(103, 194, 58, 0.45) !important;
  font-weight: 400;
}

.mask-btn {
  color: #c45656 !important;
}

.mask-btn.is-active {
  color: rgba(245, 108, 108, 0.45) !important;
  font-weight: 400;
}
</style>
