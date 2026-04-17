<template>
  <div class="staff-status-monitor-view">
    <Navigation />
    <div class="content">
      <el-container>
        <AdminSidebar />
        <el-main>
          <div class="page-header">
            <h2>人员状态监控</h2>
            <p class="page-desc">监控所有登记在册家政服务人员的服务状态，并可进行标记预警或拉入黑名单</p>
          </div>

          <el-card>
            <el-form :inline="true" class="filter-form">
              <el-form-item label="工作状态">
                <el-select
                  v-model="filters.workStatus"
                  placeholder="请选择状态"
                  clearable
                  @change="loadList"
                  style="width: 180px"
                >
                  <el-option value="all" label="全部" />
                  <el-option :value="1" label="正常" />
                  <el-option :value="0" label="不可服务" />
                  <el-option :value="2" label="警告" />
                  <el-option :value="3" label="黑名单" />
                </el-select>
              </el-form-item>
              <el-form-item label="姓名/手机号">
                <el-input
                  v-model="filters.keyword"
                  placeholder="请输入姓名或手机号"
                  clearable
                  style="width: 220px"
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
            <el-table
              :data="paginatedList"
              v-loading="loading"
              stripe
              :row-class-name="getRowClass"
            >
              <el-table-column prop="staffId" label="ID" width="90" />
              <el-table-column prop="name" label="姓名" width="120" />
              <el-table-column prop="phone" label="手机号" width="130" />
              <el-table-column prop="email" label="邮箱" width="200" />
              <el-table-column label="工作状态" width="110">
                <template #default="scope">
                  <el-tag :type="getWorkStatusType(scope.row.workStatus)">
                    {{ getWorkStatusText(scope.row.workStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="warningReason" label="预警/黑名单原因" min-width="220" show-overflow-tooltip />
              <el-table-column label="操作" width="320" fixed="right">
                <template #default="scope">
                  <el-dropdown @command="(cmd) => handleChangeStatus(scope.row, cmd)">
                    <el-button size="small" type="primary">
                      设置状态
                      <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="1">正常</el-dropdown-item>
                        <el-dropdown-item command="0">不可服务</el-dropdown-item>
                        <el-dropdown-item command="2">警告</el-dropdown-item>
                        <el-dropdown-item command="3" divided>黑名单</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </template>
              </el-table-column>
              <template #empty>
                <el-empty description="暂无服务人员数据" />
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
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, ArrowDown } from '@element-plus/icons-vue'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
import { getStaffList, updateStaffWorkStatus } from '@/api/admin'

const route = useRoute()

const loading = ref(false)
const list = ref([])
const filters = ref({
  workStatus: 'all',
  keyword: ''
})

const getWorkStatusText = (status) => {
  const map = {
    0: '不可服务',
    1: '正常',
    2: '警告',
    3: '黑名单'
  }
  return map[status] || '未知'
}

const getWorkStatusType = (status) => {
  const map = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'danger'
  }
  return map[status] || ''
}

// 分页（前端分页），默认每页 10 条
const currentPage = ref(1)
const pageSize = ref(10)
const total = computed(() => list.value.length)

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return list.value.slice(start, end)
})

// 来自消息的高亮员工 ID（例如服务人员请假申请）
const highlightStaffId = ref(null)

const loadList = async () => {
  loading.value = true
  try {
    const params = {}
    // 只监控已审核通过的服务人员
    params.verificationStatus = 1
    // “全部”不传参；只有选择了具体状态(0/1/2/3)才传
    if (
      filters.value.workStatus !== 'all' &&
      filters.value.workStatus !== '' &&
      filters.value.workStatus !== undefined &&
      filters.value.workStatus !== null
    ) {
      params.workStatus = filters.value.workStatus
    }
    if (filters.value.keyword) {
      params.keyword = filters.value.keyword
    }
    const res = await getStaffList(params)
    const data = res.data?.data || res.data || []
    // 按照 ID 升序排序，确保列表从小到大
    list.value = Array.isArray(data)
      ? data.slice().sort((a, b) => (a.staffId || 0) - (b.staffId || 0))
      : []

    // 如果有需要高亮的员工，自动定位到所在页
    if (highlightStaffId.value) {
      const index = list.value.findIndex(
        (item) => item.staffId === Number(highlightStaffId.value)
      )
      if (index !== -1) {
        currentPage.value = Math.floor(index / pageSize.value) + 1
      }
    }
  } catch (e) {
    console.error('获取服务人员列表失败:', e)
    ElMessage.error('获取服务人员列表失败')
    list.value = []
  } finally {
    loading.value = false
  }
}

// 表格行样式：高亮待处理的员工
const getRowClass = ({ row }) => {
  if (highlightStaffId.value && row.staffId === Number(highlightStaffId.value)) {
    return 'highlight-row'
  }
  return ''
}

const resetFilters = () => {
  filters.value = {
    workStatus: 'all',
    keyword: ''
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

const handleChangeStatus = async (row, status) => {
  const text = getWorkStatusText(Number(status))
  try {
    const { value: reason } = await ElMessageBox.prompt(
      `确定将【${row.name}】设置为「${text}」状态吗？可填写原因，便于后续追踪。`,
      '设置人员状态',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '例如：多次迟到、客户投诉严重等（可选）'
      }
    )
    await updateStaffWorkStatus(row.staffId, Number(status), reason)
    ElMessage.success('状态已更新')
    loadList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

onMounted(async () => {
  // 如果是从消息跳转过来的，请求参数里会带 staffId
  const staffIdFromQuery = route.query.staffId
  if (staffIdFromQuery) {
    highlightStaffId.value = Number(staffIdFromQuery)
  }
  await loadList()
  if (highlightStaffId.value) {
    ElMessage.info('已为你高亮需要处理的家政服务人员，请在列表中查看。')
  }
})
</script>

<style scoped lang="scss">
.staff-status-monitor-view {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.filter-form {
  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}

// 高亮待处理员工的表格行
:deep(.highlight-row) {
  background-color: #fff7e6 !important;
}
</style>

