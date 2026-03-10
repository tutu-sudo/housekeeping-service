<template>
  <div class="staff-certificate-audit-view">
    <Navigation />
    <div class="content">
      <el-container>
        <AdminSidebar />
        <el-main>
          <div class="page-header">
            <h2>资质信息管理</h2>
            <p class="page-desc">审核家政服务人员上传的技能证书，控制证书状态</p>
          </div>

          <el-card>
            <el-form :inline="true" class="filter-form">
              <el-form-item label="审核状态">
                <el-select v-model="filters.status" placeholder="请选择状态" clearable @change="loadList">
                  <el-option :value="''" label="全部" />
                  <el-option :value="0" label="待审核" />
                  <el-option :value="1" label="通过" />
                  <el-option :value="2" label="驳回" />
                </el-select>
              </el-form-item>
              <el-form-item label="服务人员ID">
                <el-input
                  v-model="filters.staffId"
                  placeholder="请输入服务人员ID"
                  clearable
                  style="width: 200px"
                  @keyup.enter="loadList"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadList" :loading="loading">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button @click="resetFilters">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <el-card style="margin-top: 20px">
            <el-table :data="paginatedList" v-loading="loading" stripe>
              <el-table-column prop="skillId" label="技能ID" width="90" />
              <el-table-column prop="staffId" label="服务人员ID" width="110" />
              <el-table-column prop="serviceId" label="服务项目ID" width="110" />
              <el-table-column label="服务类型" min-width="140">
                <template #default="scope">
                  {{ getServiceName(scope.row.serviceId) }}
                </template>
              </el-table-column>
              <el-table-column label="熟练程度" width="100">
                <template #default="scope">
                  <el-tag :type="getProficiencyType(scope.row.proficiencyLevel)">
                    {{ getProficiencyText(scope.row.proficiencyLevel) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="experienceYears" label="经验(年)" width="100" />
              <el-table-column label="证书" width="140">
                <template #default="scope">
                  <el-image
                    v-if="scope.row.certificateUrl"
                    :src="scope.row.certificateUrl"
                    :preview-src-list="[scope.row.certificateUrl]"
                    fit="cover"
                    style="width: 80px; height: 60px; border-radius: 4px"
                  />
                  <el-link
                    v-else
                    type="primary"
                    :underline="false"
                  >
                    无证书
                  </el-link>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="110">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.certificateStatus)">
                    {{ getStatusText(scope.row.certificateStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="上传时间" min-width="160" />
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button
                    v-if="scope.row.certificateStatus === 0"
                    size="small"
                    type="success"
                    @click="handleApprove(scope.row)"
                  >
                    通过
                  </el-button>
                  <el-button
                    v-if="scope.row.certificateStatus === 0"
                    size="small"
                    type="danger"
                    @click="handleReject(scope.row)"
                  >
                    驳回
                  </el-button>
                </template>
              </el-table-column>
              <template #empty>
                <el-empty description="暂无证书数据" />
              </template>
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
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
import {
  getStaffCertificates,
  approveStaffCertificate,
  rejectStaffCertificate,
  getAdminServices
} from '@/api/admin'

const loading = ref(false)
const list = ref([])
const filters = ref({
  status: 0,
  staffId: ''
})

// 分页（前端分页），默认每页 10 条
const currentPage = ref(1)
const pageSize = ref(10)
const total = computed(() => list.value.length)

// 当前页数据
const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return list.value.slice(start, end)
})

const serviceMap = ref({})

const getStatusText = (status) => {
  const map = {
    0: '待审核',
    1: '通过',
    2: '驳回'
  }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return map[status] || ''
}

const getProficiencyText = (level) => {
  const map = {
    1: '初级',
    2: '中级',
    3: '高级'
  }
  return map[level] || '初级'
}

const getProficiencyType = (level) => {
  const text = getProficiencyText(level)
  if (text === '高级') return 'success'
  if (text === '中级') return 'warning'
  return 'info'
}

const getServiceName = (serviceId) => {
  return serviceMap.value[serviceId] || `服务ID: ${serviceId}`
}

const loadServices = async () => {
  try {
    const res = await getAdminServices()
    const items = res.data?.data || res.data || []
    const map = {}
    items.forEach(item => {
      const id = item.serviceId || item.id
      const name = item.serviceName || item.name
      if (id) {
        map[id] = name
      }
    })
    serviceMap.value = map
  } catch (e) {
    serviceMap.value = {}
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.value.status !== '' && filters.value.status !== undefined) {
      params.status = filters.value.status
    }
    if (filters.value.staffId) {
      params.staffId = filters.value.staffId
    }
    const res = await getStaffCertificates(params)
    const data = res.data?.data || res.data || []
    // 不依赖后端分页，统一当作数组处理
    list.value = Array.isArray(data) ? data : (data.list || data.records || [])
  } catch (e) {
    console.error('获取证书列表失败:', e)
    ElMessage.error('获取证书列表失败')
    list.value = []
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = {
    status: 0,
    staffId: ''
  }
  currentPage.value = 1
  loadList()
}

const handleSizeChange = () => {
  currentPage.value = 1
  loadList()
}

const handleCurrentChange = () => {
  loadList()
}

const handleApprove = async (row) => {
  try {
    const { value: remark } = await ElMessageBox.prompt(
      '请输入审核通过备注（可选）',
      `通过技能证书(ID: ${row.skillId})`,
      {
        confirmButtonText: '通过',
        cancelButtonText: '取消',
        inputPlaceholder: '例如：证书在有效期内'
      }
    )
    await approveStaffCertificate(row.skillId, remark)
    ElMessage.success('证书审核通过')
    loadList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

const handleReject = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入驳回原因（可选）',
      `驳回技能证书(ID: ${row.skillId})`,
      {
        confirmButtonText: '驳回',
        cancelButtonText: '取消',
        inputPlaceholder: '例如：证书模糊或过期'
      }
    )
    await rejectStaffCertificate(row.skillId, reason)
    ElMessage.success('证书已驳回')
    loadList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

onMounted(async () => {
  await Promise.all([loadServices(), loadList()])
})
</script>

<style scoped lang="scss">
.staff-certificate-audit-view {
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

  .page-desc {
    margin-top: 8px;
    color: #666;
    font-size: 14px;
  }
}

.filter-form {
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

