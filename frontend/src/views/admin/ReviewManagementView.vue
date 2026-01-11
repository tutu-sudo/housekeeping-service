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
            <el-table :data="reviews" v-loading="loading" stripe>
              <el-table-column prop="reviewId" label="ID" width="80" />
              <el-table-column prop="appointmentId" label="预约ID" width="100" />
              <el-table-column prop="staffName" label="服务人员" width="120" />
              <el-table-column prop="customerName" label="客户姓名" width="120" />
              <el-table-column prop="overallRating" label="总体评分" width="100">
                <template #default="scope">
                  <el-rate v-model="scope.row.overallRating" disabled show-score />
                </template>
              </el-table-column>
              <el-table-column prop="serviceAttitudeRating" label="服务态度" width="100" />
              <el-table-column prop="professionalAbilityRating" label="专业能力" width="100" />
              <el-table-column prop="serviceQualityRating" label="服务质量" width="100" />
              <el-table-column prop="reviewContent" label="评价内容" show-overflow-tooltip />
              <el-table-column prop="isAnonymous" label="是否匿名" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.isAnonymous ? 'info' : 'success'">
                    {{ scope.row.isAnonymous ? '匿名' : '实名' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="评价时间" width="180" />
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button
                    size="small"
                    type="success"
                    link
                    @click="handleApprove(scope.row)"
                    v-if="!scope.row.approved"
                  >
                    审核通过
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    link
                    @click="handleMask(scope.row)"
                  >
                    屏蔽
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import {
  getAdminReviews,
  approveReview,
  maskReview
} from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

const reviews = ref([])
const loading = ref(false)

const filters = ref({
  staffId: undefined,
  appointmentId: undefined
})

const loadReviews = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.value.staffId) params.staffId = filters.value.staffId
    if (filters.value.appointmentId) params.appointmentId = filters.value.appointmentId

    const response = await getAdminReviews(params)
    reviews.value = response.data?.data || response.data || []
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
    appointmentId: undefined
  }
  loadReviews()
}

const handleApprove = async (row) => {
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

onMounted(() => {
  loadReviews()
})
</script>

<style scoped lang="scss">
.review-management-view {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.content {
  padding: 20px;
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
</style>
