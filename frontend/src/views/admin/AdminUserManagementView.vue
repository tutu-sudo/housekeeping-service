<template>
  <div class="admin-user-management-view">
    <Navigation />
    <div class="content">
      <el-container>
        <AdminSidebar />
        <el-main>
          <div class="page-header">
            <h2>用户管理监控</h2>
            <p class="page-desc">管理员可查看普通用户信息并启用/禁用其登录状态</p>
          </div>

          <el-card>
            <el-form :inline="true" class="filter-form" @submit.prevent>
              <el-form-item label="关键词">
                <el-input
                  v-model="filters.keyword"
                  placeholder="姓名/手机号/邮箱"
                  clearable
                  style="width: 260px"
                  @keyup.enter="handleSearch"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="loading" @click="handleSearch">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <el-card style="margin-top: 20px">
            <el-table :data="userList" v-loading="loading" stripe>
              <el-table-column prop="userId" label="用户ID" width="100" />
              <el-table-column prop="username" label="姓名" min-width="180">
                <template #default="scope">
                  {{ scope.row.username || '—' }}
                </template>
              </el-table-column>
              <el-table-column prop="phone" label="手机号" width="160">
                <template #default="scope">
                  {{ scope.row.phone || '—' }}
                </template>
              </el-table-column>
              <el-table-column prop="email" label="邮箱" min-width="220">
                <template #default="scope">
                  {{ scope.row.email || '—' }}
                </template>
              </el-table-column>
              <el-table-column label="状态" width="110">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                    {{ scope.row.status === 1 ? '启用' : '已禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="140" fixed="right">
                <template #default="scope">
                  <el-button
                    size="small"
                    :type="scope.row.status === 1 ? 'danger' : 'success'"
                    :loading="actionLoading && actionUserId === scope.row.userId"
                    @click="handleToggleStatus(scope.row)"
                  >
                    {{ scope.row.status === 1 ? '禁用登录' : '启用登录' }}
                  </el-button>
                </template>
              </el-table-column>
              <template #empty>
                <el-empty description="暂无普通用户数据" />
              </template>
            </el-table>

            <div class="pagination-container">
              <el-pagination
                v-model:current-page="pagination.page"
                v-model:page-size="pagination.size"
                :total="pagination.total"
                :page-sizes="[10, 20, 50, 100]"
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
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
import {
  getNormalUsers,
  disableNormalUserLogin,
  enableNormalUserLogin
} from '@/api/admin'

const loading = ref(false)
const actionLoading = ref(false)
const actionUserId = ref(null)
const userList = ref([])

const filters = ref({
  keyword: ''
})

const pagination = ref({
  page: 1,
  size: 20,
  total: 0
})

const normalizeUserRow = (row) => {
  if (!row) return row
  return {
    ...row,
    userId: row.userId ?? row.id ?? row.user_id,
    username: row.username ?? row.name ?? row.userName ?? '',
    phone: row.phone ?? row.mobile ?? row.phoneNumber ?? '',
    email: row.email ?? '',
    status: Number(row.status ?? 1)
  }
}

const resolvePagedData = (raw) => {
  if (Array.isArray(raw)) {
    return { list: raw, total: raw.length }
  }
  const list = raw?.list || raw?.records || raw?.rows || raw?.content || raw?.data || []
  const total = Number(
    raw?.total ??
    raw?.totalCount ??
    raw?.count ??
    (Array.isArray(list) ? list.length : 0)
  )
  return { list: Array.isArray(list) ? list : [], total }
}

const loadUserList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size
    }
    if ((filters.value.keyword || '').trim()) {
      params.keyword = filters.value.keyword.trim()
    }
    const res = await getNormalUsers(params)
    const payload = res.data?.data ?? res.data ?? {}
    const { list, total } = resolvePagedData(payload)
    userList.value = list.map(normalizeUserRow)
    pagination.value.total = total
  } catch (e) {
    console.error('加载普通用户列表失败:', e)
    ElMessage.error(e?.response?.data?.message || '加载普通用户列表失败')
    userList.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  loadUserList()
}

const handleReset = () => {
  filters.value.keyword = ''
  pagination.value.page = 1
  pagination.value.size = 20
  loadUserList()
}

const handleSizeChange = () => {
  pagination.value.page = 1
  loadUserList()
}

const handleCurrentChange = () => {
  loadUserList()
}

const handleToggleStatus = async (row) => {
  const user = normalizeUserRow(row)
  if (!user?.userId) {
    ElMessage.error('用户ID缺失，无法操作')
    return
  }
  const willDisable = user.status === 1
  const actionText = willDisable ? '禁用登录' : '启用登录'
  try {
    await ElMessageBox.confirm(
      `确定要对用户「${user.username || user.phone || user.userId}」执行“${actionText}”吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  actionLoading.value = true
  actionUserId.value = user.userId
  try {
    if (willDisable) {
      await disableNormalUserLogin(user.userId)
    } else {
      await enableNormalUserLogin(user.userId)
    }
    ElMessage.success(`${actionText}成功`)
    await loadUserList()
  } catch (e) {
    const backendMsg = e?.response?.data?.message || e?.response?.data?.msg
    ElMessage.error(backendMsg || `${actionText}失败`)
  } finally {
    actionLoading.value = false
    actionUserId.value = null
  }
}

onMounted(() => {
  loadUserList()
})
</script>

<style scoped lang="scss">
.admin-user-management-view {
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
