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
                <el-select
                  v-model="filters.status"
                  placeholder="请选择状态"
                  clearable
                  @change="loadList"
                  style="width: 180px"
                >
                  <el-option value="all" label="全部" />
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
              <el-table-column label="服务人员姓名" width="130">
                <template #default="scope">
                  {{ getStaffName(scope.row.staffId) }}
                </template>
              </el-table-column>
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
                    size="small"
                    type="primary"
                    link
                    @click="openDetail(scope.row)"
                  >
                    查看详情
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

          <!-- 证书详情对话框：在这里进行通过/驳回 -->
          <el-dialog
            v-model="detailDialogVisible"
            title="证书详情"
            width="820px"
            @close="selectedDetail = null"
          >
            <div v-loading="detailSubmitting">
              <el-descriptions v-if="selectedDetail" :column="2" border>
                <el-descriptions-item label="技能ID">
                  {{ selectedDetail.skillId }}
                </el-descriptions-item>
                <el-descriptions-item label="服务人员ID">
                  {{ selectedDetail.staffId }}
                </el-descriptions-item>
                <el-descriptions-item label="服务人员姓名">
                  {{ getStaffName(selectedDetail.staffId) }}
                </el-descriptions-item>
                <el-descriptions-item label="服务项目ID">
                  {{ selectedDetail.serviceId }}
                </el-descriptions-item>
                <el-descriptions-item label="服务类型">
                  {{ getServiceName(selectedDetail.serviceId) }}
                </el-descriptions-item>
                <el-descriptions-item label="熟练程度">
                  <el-tag :type="getProficiencyType(selectedDetail.proficiencyLevel)">
                    {{ getProficiencyText(selectedDetail.proficiencyLevel) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="经验(年)">
                  {{ selectedDetail.experienceYears ?? '—' }}
                </el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="getStatusType(selectedDetail.certificateStatus)">
                    {{ getStatusText(selectedDetail.certificateStatus) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="上传时间">
                  {{ selectedDetail.createTime || '—' }}
                </el-descriptions-item>
                <el-descriptions-item label="证书" :span="2">
                  <div class="certificate-detail">
                    <el-image
                      v-if="selectedDetail.certificateUrl"
                      :src="selectedDetail.certificateUrl"
                      :preview-src-list="[selectedDetail.certificateUrl]"
                      fit="contain"
                      style="width: 260px; height: 180px; border-radius: 6px; border: 1px solid #ebeef5"
                    />
                    <div v-else class="no-certificate">无证书</div>
                  </div>
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <template #footer>
              <el-button @click="detailDialogVisible = false">关闭</el-button>
              <template v-if="selectedDetail">
                <el-button
                  type="danger"
                  :disabled="selectedDetail.certificateStatus === 2 || detailSubmitting"
                  @click="approveOrRejectInDialog('reject')"
                >
                  驳回
                </el-button>
                <el-button
                  type="success"
                  :disabled="selectedDetail.certificateStatus === 1 || detailSubmitting"
                  @click="approveOrRejectInDialog('approve')"
                >
                  审核通过
                </el-button>
              </template>
            </template>
          </el-dialog>
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
  getAdminServices,
  getStaffList,
  getStaffDetail
} from '@/api/admin'

const loading = ref(false)
const list = ref([])
const filters = ref({
  status: 'all',
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
const staffNameMap = ref({})

// 详情弹窗状态
const detailDialogVisible = ref(false)
const selectedDetail = ref(null)
const detailSubmitting = ref(false)

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

const getStaffName = (staffId) => {
  if (!staffId && staffId !== 0) return '—'
  return staffNameMap.value[staffId] || `员工ID: ${staffId}`
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

const loadStaffNameMap = async () => {
  try {
    // 尝试一次性获取服务人员列表，建立 staffId -> name 的映射
    const res = await getStaffList({})
    const items = res.data?.data || res.data || []
    const arr = Array.isArray(items) ? items : (items.list || items.records || [])
    const map = {}
    arr.forEach((s) => {
      const id = s.staffId ?? s.id ?? s.staff_id
      const name = s.name ?? s.staffName ?? s.realName ?? s.username
      if (id !== undefined && id !== null) {
        map[id] = name || map[id] || ''
      }
    })
    staffNameMap.value = { ...staffNameMap.value, ...map }
  } catch (e) {
    // 忽略：后续会按需用详情接口兜底补齐
  }
}

const enrichStaffNames = async (rows) => {
  if (!Array.isArray(rows) || rows.length === 0) return
  const ids = Array.from(
    new Set(
      rows
        .map((r) => r?.staffId ?? r?.staff_id ?? null)
        .filter((id) => id !== null && id !== undefined && id !== '')
        .map((id) => (typeof id === 'string' ? Number(id) : id))
    )
  )
  const missing = ids.filter((id) => !staffNameMap.value[id])
  if (missing.length === 0) return

  // 用详情接口逐个兜底补齐（列表一般不大，且有缓存）
  await Promise.allSettled(
    missing.map(async (id) => {
      try {
        const res = await getStaffDetail(id)
        const d = res.data?.data || res.data || {}
        const name = d.name ?? d.staffName ?? d.realName ?? d.username
        if (name) {
          staffNameMap.value[id] = name
        }
      } catch (e) {
        // ignore single failure
      }
    })
  )
}

const loadList = async () => {
  loading.value = true
  try {
    const params = {}
    // “全部”不传参；只有选择了具体状态(0/1/2)才传
    if (
      filters.value.status !== 'all' &&
      filters.value.status !== '' &&
      filters.value.status !== undefined &&
      filters.value.status !== null
    ) {
      params.status = filters.value.status
    }
    if (filters.value.staffId) {
      params.staffId = filters.value.staffId
    }
    const res = await getStaffCertificates(params)
    const data = res.data?.data || res.data || []
    // 不依赖后端分页，统一当作数组处理
    list.value = Array.isArray(data) ? data : (data.list || data.records || [])
    await enrichStaffNames(list.value)
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
    status: 'all',
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
      '驳回后前台将立即下线该技能展示，需重新上传并审核。请输入驳回原因（可选）',
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

const openDetail = (row) => {
  selectedDetail.value = row
  detailDialogVisible.value = true
}

const approveOrRejectInDialog = async (action) => {
  if (!selectedDetail.value?.skillId) return
  const currentStatus = Number(selectedDetail.value.certificateStatus)
  if (action === 'approve' && currentStatus === 1) {
    ElMessage.info('当前已是审核通过状态')
    return
  }
  if (action === 'reject' && currentStatus === 2) {
    ElMessage.info('当前已是驳回状态')
    return
  }

  detailSubmitting.value = true
  try {
    if (action === 'approve') {
      const { value: remark } = await ElMessageBox.prompt(
        '请输入审核通过备注（可选）',
        `通过技能证书(ID: ${selectedDetail.value.skillId})`,
        {
          confirmButtonText: '通过',
          cancelButtonText: '取消',
          inputPlaceholder: '例如：证书在有效期内'
        }
      )
      await approveStaffCertificate(selectedDetail.value.skillId, remark)
      ElMessage.success('证书审核通过')
      selectedDetail.value = {
        ...selectedDetail.value,
        certificateStatus: 1
      }
    } else {
      const { value: reason } = await ElMessageBox.prompt(
        '驳回后前台将立即下线该技能展示，需重新上传并审核。请输入驳回原因（可选）',
        `驳回技能证书(ID: ${selectedDetail.value.skillId})`,
        {
          confirmButtonText: '驳回',
          cancelButtonText: '取消',
          inputPlaceholder: '例如：证书模糊或过期'
        }
      )
      await rejectStaffCertificate(selectedDetail.value.skillId, reason)
      ElMessage.success('证书已驳回')
      selectedDetail.value = {
        ...selectedDetail.value,
        certificateStatus: 2
      }
    }
    await loadList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  } finally {
    detailSubmitting.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadServices(), loadStaffNameMap()])
  await loadList()
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

.certificate-detail {
  display: flex;
  align-items: center;
  gap: 12px;
}

.no-certificate {
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 6px;
  color: #909399;
}
</style>

