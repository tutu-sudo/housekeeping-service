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
                  <el-option label="全部" :value="undefined" />
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
            <el-table :data="staffList" v-loading="loading" stripe>
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
          </el-card>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getStaffList, updateStaffVerificationStatus } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

const staffList = ref([])
const loading = ref(false)

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

const loadStaffList = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.value.verificationStatus !== undefined) {
      params.verificationStatus = filters.value.verificationStatus
    }

    const response = await getStaffList(params)
    staffList.value = response.data?.data || response.data || []
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

const viewDetail = (staffId) => {
  // TODO: 跳转到详情页面
  ElMessage.info('详情功能待实现')
}

onMounted(() => {
  loadStaffList()
})
</script>

<style scoped lang="scss">
.verification-view {
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

