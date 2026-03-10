<template>
  <div class="verification-view">
    <Navigation />
    <div class="content">
      <el-container>
        <AdminSidebar />
        <el-main>
          <div class="page-header">
            <h2>人员审核</h2>
          </div>
          
          <el-card>
            <el-form :inline="true" class="filter-form">
              <el-form-item label="审核状态">
                <el-select v-model="filters.verificationStatus" placeholder="请选择状态" clearable @change="loadStaffList">
                  <el-option label="全部" :value="''" />
                  <el-option label="待审核" :value="0" />
                  <el-option label="审核通过" :value="1" />
                  <el-option label="审核驳回" :value="2" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadStaffList" :loading="loading">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button @click="resetFilters">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
          
          <el-card style="margin-top: 20px">
            <el-table :data="paginatedStaffList" v-loading="loading" stripe>
              <el-table-column prop="staffId" label="ID" width="80" />
              <el-table-column prop="name" label="姓名" width="120" />
              <el-table-column prop="phone" label="手机号" width="130" />
              <el-table-column prop="email" label="邮箱" width="200" />
              <el-table-column prop="gender" label="性别" width="80">
                <template #default="scope">
                  {{ scope.row.gender === 1 ? '男' : '女' }}
                </template>
              </el-table-column>
              <el-table-column prop="origin" label="籍贯" width="120" />
              <el-table-column prop="workExperience" label="工作经验(年)" width="120" />
              <el-table-column prop="hourlyRate" label="时薪(元)" width="100">
                <template #default="scope">
                  ¥{{ scope.row.hourlyRate?.toFixed(2) || '0.00' }}
                </template>
              </el-table-column>
              <el-table-column prop="rating" label="评分" width="100" />
              <el-table-column prop="verificationStatus" label="审核状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.verificationStatus)">
                    {{ getStatusText(scope.row.verificationStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button
                    size="small"
                    type="success"
                    @click="handleApprove(scope.row)"
                    v-if="scope.row.verificationStatus === 0"
                  >
                    通过
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    @click="handleReject(scope.row)"
                    v-if="scope.row.verificationStatus === 0"
                  >
                    拒绝
                  </el-button>
                  <el-button
                    size="small"
                    type="primary"
                    link
                    @click="viewDetail(scope.row.staffId)"
                  >
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-container">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :total="total"
                :page-sizes="[10, 20, 50]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </el-card>
          
          <!-- 服务人员详情对话框 -->
          <el-dialog
            v-model="detailDialogVisible"
            title="服务人员详情"
            width="800px"
            @close="staffDetail = null"
          >
            <div v-loading="detailLoading">
              <el-descriptions :column="2" border v-if="staffDetail">
                <el-descriptions-item label="ID">
                  {{ staffDetail.staffId }}
                </el-descriptions-item>
                <el-descriptions-item label="姓名">
                  {{ staffDetail.name }}
                </el-descriptions-item>
                <el-descriptions-item label="手机号">
                  {{ staffDetail.phone }}
                </el-descriptions-item>
                <el-descriptions-item label="邮箱">
                  {{ staffDetail.email || '未填写' }}
                </el-descriptions-item>
                <el-descriptions-item label="性别">
                  {{ staffDetail.gender === 1 ? '男' : '女' }}
                </el-descriptions-item>
                <el-descriptions-item label="籍贯">
                  {{ staffDetail.origin || '未填写' }}
                </el-descriptions-item>
                <el-descriptions-item label="工作经验(年)">
                  {{ staffDetail.workExperience || '0' }}
                </el-descriptions-item>
                <el-descriptions-item label="时薪(元)">
                  ¥{{ staffDetail.hourlyRate?.toFixed(2) || '0.00' }}
                </el-descriptions-item>
                <el-descriptions-item label="评分">
                  {{ staffDetail.rating || '暂无' }}
                </el-descriptions-item>
                <el-descriptions-item label="审核状态">
                  <el-tag :type="getStatusType(staffDetail.verificationStatus)">
                    {{ getStatusText(staffDetail.verificationStatus) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="个人简介" :span="2">
                  <div style="padding: 10px; background-color: #f5f5f5; border-radius: 4px;">
                    {{ staffDetail.bio || '未填写' }}
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="服务技能" :span="2">
                  {{ staffDetail.skills || '未填写' }}
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">
                  {{ staffDetail.createTime || '未知' }}
                </el-descriptions-item>
                <el-descriptions-item label="更新时间">
                  {{ staffDetail.updateTime || '未知' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-dialog>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getStaffApplications, updateStaffVerificationStatus, getStaffDetail } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

const staffList = ref([])
const loading = ref(false)
const detailDialogVisible = ref(false)
const staffDetail = ref(null)
const detailLoading = ref(false)

const filters = ref({
  verificationStatus: 0 // 默认显示待审核
})

const getStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '审核通过',
    2: '审核驳回'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return typeMap[status] || ''
}

// 分页（前端分页），默认每页 10 条
const currentPage = ref(1)
const pageSize = ref(10)
const total = computed(() => staffList.value.length)

const paginatedStaffList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return staffList.value.slice(start, end)
})

const loadStaffList = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.value.verificationStatus !== '' && filters.value.verificationStatus !== undefined) {
      params.verificationStatus = filters.value.verificationStatus
    }

    const response = await getStaffApplications(params)
    const data = response.data?.data || response.data || []
    // 不依赖后端分页，统一按数组来处理，供前端分页
    staffList.value = Array.isArray(data) ? data : (data.list || data.records || [])
  } catch (error) {
    console.error('获取人员列表失败:', error)
    ElMessage.error('获取人员列表失败')
    staffList.value = []
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = {
    verificationStatus: 0
  }
  currentPage.value = 1
  loadStaffList()
}

const handleSizeChange = () => {
  currentPage.value = 1
  loadStaffList()
}

const handleCurrentChange = () => {
  loadStaffList()
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过"${row.name}"的审核吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updateStaffVerificationStatus(row.staffId, 1)
    ElMessage.success('审核通过')
    loadStaffList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleReject = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要驳回"${row.name}"的审核吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updateStaffVerificationStatus(row.staffId, 2)
    ElMessage.success('已驳回')
    loadStaffList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const viewDetail = async (staffId) => {
  detailLoading.value = true
  detailDialogVisible.value = true
  try {
    const response = await getStaffDetail(staffId)
    staffDetail.value = response.data?.data || response.data
  } catch (error) {
    console.error('获取服务人员详情失败:', error)
    ElMessage.error('获取服务人员详情失败')
    detailDialogVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  loadStaffList()
})
</script>

<style scoped lang="scss">
.verification-view {
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
</style>

