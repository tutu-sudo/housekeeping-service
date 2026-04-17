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
            <el-form :inline="true" class="filter-form" @submit.prevent>
              <el-form-item label="审核状态">
                <el-select 
                  v-model="filters.verificationStatus" 
                  placeholder="请选择状态" 
                  clearable 
                  @change="loadStaffList"
                  style="width: 180px;"
                >
                  <el-option label="全部" value="all" />
                  <el-option label="待审核" :value="0" />
                  <el-option label="审核通过" :value="1" />
                  <el-option label="审核驳回" :value="2" />
                </el-select>
              </el-form-item>
              <el-form-item label="模糊查询">
                <el-input
                  v-model="filters.keyword"
                  placeholder="姓名/手机号/邮箱/ID"
                  clearable
                  style="width: 260px;"
                  @keyup.enter="loadStaffList"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadStaffList" :loading="loading">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button @click="resetFilters">重置</el-button>
                <el-button type="success" plain @click="openAddStaffDialog">
                  新增家政服务人员
                </el-button>
                <el-button v-if="false" type="warning" plain @click="openAddAdminDialog">
                  新增管理员
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
          
          <el-card style="margin-top: 20px">
            <el-table :data="paginatedStaffList" v-loading="loading" stripe>
              <el-table-column prop="staffId" label="ID" width="80" />
              <el-table-column prop="name" label="姓名" width="120" />
              <el-table-column prop="phone" label="手机号" width="130">
                <template #default="scope">
                  {{ scope.row.phone || '—' }}
                </template>
              </el-table-column>
              <el-table-column prop="email" label="邮箱" width="200">
                <template #default="scope">
                  {{ scope.row.email || '—' }}
                </template>
              </el-table-column>
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
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="scope">
                  <el-button
                    size="small"
                    type="primary"
                    link
                    @click="viewDetail(scope.row)"
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
            @close="resetDetailDialog"
          >
            <div v-loading="detailLoading || savingDetail">
              <el-form v-if="staffForm" :model="staffForm" label-width="100px">
                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="ID">
                      <el-input v-model="staffForm.staffId" disabled />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="用户ID">
                      <el-input v-model="staffForm.userId" disabled />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="姓名">
                      <el-input v-model="staffForm.name" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="性别">
                      <el-select v-model="staffForm.gender" placeholder="请选择">
                        <el-option :value="0" label="女" />
                        <el-option :value="1" label="男" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="出生日期">
                      <el-date-picker
                        v-model="staffForm.birthDate"
                        type="date"
                        value-format="YYYY-MM-DD"
                        placeholder="请选择日期"
                        style="width: 100%;"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="籍贯">
                      <el-input v-model="staffForm.origin" />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="身份证号">
                      <el-input v-model="staffForm.idCard" />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="服务类型">
                      <el-select
                        v-model="staffForm.mainServiceCategory"
                        placeholder="请选择服务类型"
                        style="width: 100%;"
                      >
                        <el-option
                          v-for="item in mainServiceCategories"
                          :key="item"
                          :label="item"
                          :value="item"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="具体服务">
                      <el-select
                        v-model="staffForm.serviceId"
                        placeholder="请选择具体服务"
                        style="width: 100%;"
                      >
                        <el-option
                          v-for="item in filteredServiceOptions"
                          :key="item.id"
                          :label="item.name"
                          :value="item.id"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </div>

            <template #footer>
              <div style="display:flex;justify-content:flex-end;gap:12px;align-items:center;">
                <el-button @click="detailDialogVisible = false">关闭</el-button>
                <el-button type="primary" :loading="savingDetail" @click="saveStaffDetail">
                  保存
                </el-button>
                <el-button type="success" @click="handleApproveInDetail">
                  审核通过
                </el-button>
                <el-button type="danger" @click="handleRejectInDetail">
                  审核驳回
                </el-button>
                <el-button
                  type="warning"
                  :loading="demoteLoading"
                  :disabled="!staffForm?.userId"
                  @click="handleDemoteInDetail"
                >
                  降级为普通用户
                </el-button>
              </div> 
            </template>
          </el-dialog>

          <!-- 新增家政服务人员（搜索用户 -> 升级为服务人员） -->
          <el-dialog
            v-model="addStaffDialogVisible"
            title="新增家政服务人员"
            width="900px"
            @close="resetAddStaffDialog"
          >
              <el-alert
                type="info"
                show-icon
                :closable="false"
                title="流程说明：先搜索普通用户（手机号/用户名）→ 选择用户 → 升级为服务人员（升级后生成待审核的服务人员档案，将出现在“人员审核”列表中，填写资料并审核通过后才能接单）"
                style="margin-bottom: 12px;"
              />

            <el-form :inline="true" class="filter-form" @submit.prevent>
              <el-form-item label="手机号/用户名">
                <el-input
                  v-model="userSearchKeyword"
                  placeholder="请输入手机号或用户名"
                  clearable
                  style="width: 260px;"
                  @keyup.enter="handleUserSearch"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleUserSearch" :loading="userSearchLoading">
                  <el-icon><Search /></el-icon>
                  搜索
                </el-button>
                <el-button @click="resetUserSearch" :disabled="userSearchLoading">清空</el-button>
              </el-form-item>
            </el-form>

            <el-table
              :data="userSearchResults"
              v-loading="userSearchLoading"
              highlight-current-row
              @current-change="onUserCurrentChange"
              style="width: 100%;"
            >
              <el-table-column prop="userId" label="用户ID" width="100" />
              <el-table-column prop="username" label="用户名" min-width="160" />
              <el-table-column prop="phone" label="手机号" width="140">
                <template #default="scope">
                  {{ scope.row.phone || '—' }}
                </template>
              </el-table-column>
              <el-table-column prop="userType" label="当前角色" width="120">
                <template #default="scope">
                  <el-tag :type="getUserTypeTagType(scope.row.userType)">
                    {{ getUserTypeText(scope.row.userType) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="160" fixed="right">
                <template #default="scope">
                  <el-button
                    type="success"
                    size="small"
                    :loading="promoteLoading && promotingUserId === scope.row.userId"
                    :disabled="scope.row.userType === 3"
                    @click="handlePromoteToStaff(scope.row)"
                  >
                    升级为服务人员
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <template #footer>
              <div style="display:flex;justify-content:flex-end;gap:12px;">
                <el-button @click="addStaffDialogVisible = false">关闭</el-button>
                  </div>
            </template>
          </el-dialog>

          <!-- 新增管理员：后端未提供接口时先给出明确提示，避免点了报错 -->
          <el-dialog
            v-model="addAdminDialogVisible"
            title="新增管理员"
            width="680px"
          >
            <el-alert
              type="warning"
              show-icon
              :closable="false"
              title="当前后端仅提供：用户搜索 / 升级为服务人员 / 降级回普通用户。若要“新增管理员”，需要后端新增对应接口（例如 promote-to-admin 或创建管理员账号的接口），前端才能联调。"
            />
            <template #footer>
              <div style="display:flex;justify-content:flex-end;gap:12px;">
                <el-button type="primary" @click="addAdminDialogVisible = false">我知道了</el-button>
            </div>
            </template>
          </el-dialog>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import {
  getStaffApplications,
  updateStaffVerificationStatus,
  getStaffDetail,
  searchAdminUsers,
  promoteUserToStaff,
  updateStaffProfileByAdmin,
  demoteUserToUser
} from '@/api/admin'
import { getAvailableServices } from '@/api/services'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

const staffList = ref([])
const loading = ref(false)
const detailDialogVisible = ref(false)
const staffDetail = ref(null)
const detailLoading = ref(false)
const staffForm = ref(null)
const savingDetail = ref(false)
const demoteLoading = ref(false)

// 服务类型/具体服务下拉选项（从后端 service 表动态加载）
const serviceOptions = ref([]) // { id, category, name }

const mainServiceCategories = computed(() =>
  Array.from(new Set(serviceOptions.value.map((i) => i.category))).filter(Boolean)
)

const filteredServiceOptions = computed(() => {
  if (!staffForm.value?.mainServiceCategory) {
    return serviceOptions.value
  }
  return serviceOptions.value.filter((i) => i.category === staffForm.value.mainServiceCategory)
})

// 新增家政服务人员：搜索用户 -> 升级为服务人员
const addStaffDialogVisible = ref(false)
const addAdminDialogVisible = ref(false)
const userSearchKeyword = ref('')
const userSearchLoading = ref(false)
const userSearchResults = ref([])
const currentSelectedUser = ref(null)
const promoteLoading = ref(false)
const promotingUserId = ref(null)

const filters = ref({
  verificationStatus: 'all', // 默认显示全部（避免空字符串导致下拉框显示placeholder）
  keyword: '' // 模糊查询关键字（姓名/手机号/邮箱/ID）
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

const filteredStaffList = computed(() => {
  const kw = (filters.value.keyword || '').trim().toLowerCase()
  if (!kw) return staffList.value
  return staffList.value.filter((r) => {
    const staffId = `${r.staffId ?? ''}`.toLowerCase()
    const name = `${r.name ?? ''}`.toLowerCase()
    const phone = `${r.phone ?? ''}`.toLowerCase()
    const email = `${r.email ?? ''}`.toLowerCase()
    const origin = `${r.origin ?? ''}`.toLowerCase()
    return (
      staffId.includes(kw) ||
      name.includes(kw) ||
      phone.includes(kw) ||
      email.includes(kw) ||
      origin.includes(kw)
    )
  })
})

const total = computed(() => filteredStaffList.value.length)

const paginatedStaffList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredStaffList.value.slice(start, end)
})

// 关键字变化时回到第一页，避免出现“当前页无数据”
watch(
  () => filters.value.keyword,
  () => {
    currentPage.value = 1
  }
)

// 统一字段，兼容后端不同版本字段名（同时满足表格 phone/email 的展示需求）
const normalizeStaffRow = (r) => {
  if (!r) return r
  return {
    ...r,
    staffId: r.staffId ?? r.id ?? r.staff_id ?? null,
    name: r.name ?? r.staffName ?? r.realName ?? r.username ?? '',
    phone: r.phone ?? r.mobile ?? r.phoneNumber ?? r.userPhone ?? r.user_phone ?? '',
    email: r.email ?? r.userEmail ?? r.user_email ?? ''
  }
}

const getUserTypeText = (userType) => {
  const map = {
    1: '普通用户',
    2: '服务人员',
    3: '管理员'
  }
  return map[userType] || `未知(${userType ?? '-'})`
}

const getUserTypeTagType = (userType) => {
  const map = {
    1: 'info',
    2: 'success',
    3: 'warning'
  }
  return map[userType] || ''
}

const openAddStaffDialog = () => {
  addStaffDialogVisible.value = true
  // 默认不自动搜索，避免一打开就打接口；由管理员输入关键字后搜索
}

const openAddAdminDialog = () => {
  addAdminDialogVisible.value = true
}

const resetUserSearch = () => {
  userSearchKeyword.value = ''
  userSearchResults.value = []
  currentSelectedUser.value = null
}

const resetAddStaffDialog = () => {
  resetUserSearch()
  userSearchLoading.value = false
  promoteLoading.value = false
  promotingUserId.value = null
}

const onUserCurrentChange = (row) => {
  currentSelectedUser.value = row || null
}

const normalizeUserSearchRow = (u) => {
  if (!u) return u
  return {
    ...u,
    userId: u.userId ?? u.id ?? u.user_id ?? null,
    username: u.username ?? u.userName ?? u.name ?? '',
    phone: u.phone ?? u.mobile ?? u.phoneNumber ?? '',
    userType: u.userType ?? u.user_type ?? null
  }
}

const handleUserSearch = async () => {
  const keyword = (userSearchKeyword.value || '').trim()
  if (!keyword) {
    ElMessage.warning('请输入手机号或用户名进行搜索')
    return
  }
  userSearchLoading.value = true
  try {
    const res = await searchAdminUsers(keyword, 20)
    const data = res.data?.data || res.data || []
    const list = Array.isArray(data) ? data : (data.list || data.records || [])
    userSearchResults.value = list.map(normalizeUserSearchRow)
    if (userSearchResults.value.length === 0) {
      ElMessage.info('未搜索到匹配用户')
    }
  } catch (e) {
    const status = e?.response?.status
    const url = e?.config?.baseURL ? `${e.config.baseURL}${e.config.url}` : e?.config?.url
    console.error('搜索用户失败:', { status, url, error: e })
    ElMessage.error(status === 404 ? '搜索接口不存在（404），请核对后端路径并确认已重启' : '搜索用户失败')
    userSearchResults.value = []
  } finally {
    userSearchLoading.value = false
  }
}

const handlePromoteToStaff = async (userRow) => {
  const u = normalizeUserSearchRow(userRow)
  if (!u?.userId) {
    ElMessage.error('userId 缺失，无法升级')
    return
  }
  if (u.userType === 3) {
    ElMessage.warning('管理员账号不允许升级为服务人员')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定将用户「${u.username || u.phone || u.userId}」升级为家政服务人员吗？升级后默认进入“待审核”，审核通过前不能接单。`,
      '确认操作',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch (e) {
    return
  }

  promoteLoading.value = true
  promotingUserId.value = u.userId
  try {
    const res = await promoteUserToStaff(u.userId)
    const payload = res.data?.data || res.data || {}
    const staffId = payload.staffId ?? payload.staff_id
    ElMessage.success(
      `升级成功${staffId ? `（staffId=${staffId}）` : ''}，已生成待审核的服务人员档案，请在“人员审核”页面填写资料并审核通过后再开始接单。`
    )
    // 刷新当前审核列表：让新员工出现在“待审核”里
    if (filters.value.verificationStatus === 'all' || filters.value.verificationStatus === 0) {
      loadStaffList()
    }
    // 更新搜索结果里该用户的角色展示（不必强依赖后端再次搜索）
    userSearchResults.value = userSearchResults.value.map((item) =>
      item.userId === u.userId ? { ...item, userType: 2 } : item
    )
  } catch (e) {
    const status = e?.response?.status
    const resBody = e?.response?.data
    const backendCode = resBody?.code
    const backendMsg = resBody?.message || resBody?.msg
    const url = e?.config?.baseURL ? `${e.config.baseURL}${e.config.url}` : e?.config?.url
    console.error('升级为服务人员失败:', { status, backendCode, backendMsg, url, error: e })

    // 优先展示后端 message，便于定位（例如“不能升级管理员/用户不存在/数据库异常”等）
    const msg =
      backendMsg ||
      (status === 500 ? '后端内部错误（500），请查看后端控制台异常栈' : '') ||
      e?.message ||
      '升级为服务人员失败'
    ElMessage.error(msg)
  } finally {
    promoteLoading.value = false
    promotingUserId.value = null
  }
}

const loadStaffList = async () => {
  loading.value = true
  try {
    const params = {}
    // “全部”不传参；只有选择了具体状态(0/1/2)才传
    if (
      filters.value.verificationStatus !== 'all' &&
      filters.value.verificationStatus !== '' &&
      filters.value.verificationStatus !== undefined &&
      filters.value.verificationStatus !== null
    ) {
      params.verificationStatus = filters.value.verificationStatus
    }
    // 透传 keyword（后端支持则可服务端过滤；即使不支持，前端也会本地过滤）
    if (filters.value.keyword && `${filters.value.keyword}`.trim()) {
      params.keyword = `${filters.value.keyword}`.trim()
    }

    const response = await getStaffApplications(params)
    const data = response.data?.data || response.data || []
    // 不依赖后端分页，统一按数组来处理，供前端分页
    const rawList = Array.isArray(data) ? data : (data.list || data.records || [])
    staffList.value = rawList.map(normalizeStaffRow)
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
    verificationStatus: 'all', // 重置为全部
    keyword: ''
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
      const status = error?.response?.status
      const backendMsg = error?.response?.data?.message || error?.response?.data?.msg
      ElMessage.error(backendMsg || (status === 404 ? '审核接口不存在（404），请核对后端路径/方法' : '') || error.message || '操作失败')
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
      const status = error?.response?.status
      const backendMsg = error?.response?.data?.message || error?.response?.data?.msg
      ElMessage.error(backendMsg || (status === 404 ? '审核接口不存在（404），请核对后端路径/方法' : '') || error.message || '操作失败')
    }
  }
}

const viewDetail = async (row) => {
  detailLoading.value = true
  detailDialogVisible.value = true
  try {
    // 先用列表行数据兜底展示（避免详情请求失败时对话框空白）
    const base = normalizeStaffRow(row)
    staffDetail.value = base

    const staffId = base?.staffId
    if (!staffId) {
      throw new Error('staffId 缺失')
    }
    const response = await getStaffDetail(staffId)
    const detail = normalizeStaffRow(response.data?.data || response.data)
    // 合并：详情优先，但 phone/email 如果详情缺失则沿用列表行
    staffDetail.value = {
      ...base,
      ...detail,
      phone: detail?.phone || base?.phone || '',
      email: detail?.email || base?.email || ''
    }

    // 初始化可编辑表单；从 professionalSkills 中尝试解析服务类型/具体服务（格式：类型-服务名）
    let mainServiceCategory = ''
    let serviceId = null

    const skillsText = staffDetail.value.professionalSkills || staffDetail.value.skills
    if (skillsText && typeof skillsText === 'string') {
      const byDash = skillsText.split('-')
      if (byDash.length >= 1) {
        mainServiceCategory = byDash[0].trim()
      }
      if (byDash.length >= 2) {
        const serviceName = byDash[1].trim()
        const matched = serviceOptions.value.find((s) => s.name === serviceName)
        if (matched) {
          serviceId = matched.id
        }
      }
    }

    staffForm.value = {
      ...staffDetail.value,
      mainServiceCategory,
      serviceId
    }
  } catch (error) {
    console.error('获取服务人员详情失败:', error)
    ElMessage.error('获取服务人员详情失败')
    detailDialogVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

const resetDetailDialog = () => {
  staffDetail.value = null
  staffForm.value = null
  savingDetail.value = false
}

// 校验详情表单是否填写完整（用于审核前检查）
const validateStaffForm = () => {
  const form = staffForm.value || {}
  const requiredFields = [
    { key: 'name', label: '姓名' },
    { key: 'gender', label: '性别' },
    { key: 'birthDate', label: '出生日期' },
    { key: 'origin', label: '籍贯' },
    { key: 'idCard', label: '身份证号' },
    { key: 'mainServiceCategory', label: '服务类型' },
    { key: 'serviceId', label: '具体服务' }
  ]

  const missing = requiredFields.filter((f) => {
    const v = form[f.key]
    return v === undefined || v === null || `${v}`.trim() === ''
  })

  if (missing.length > 0) {
    const labels = missing.map((m) => m.label).join('、')
    ElMessage.error(`请先将以下信息填写完整后再审核通过：${labels}`)
    return false
  }
  return true
}

const saveStaffDetail = async () => {
  if (!staffDetail.value?.staffId) {
    ElMessage.error('缺少 staffId，无法保存')
    return
  }

  const payload = {
    name: staffForm.value.name,
    gender: staffForm.value.gender,
    birthDate: staffForm.value.birthDate,
    origin: staffForm.value.origin,
    idCard: staffForm.value.idCard
  }

  // 如果选择了具体服务，则同时传 serviceId 以及一个可读的专业技能描述，便于后端同步 staff_service_skill
  if (staffForm.value.mainServiceCategory && staffForm.value.serviceId) {
    const svc = serviceOptions.value.find((s) => s.id === staffForm.value.serviceId)
    const serviceName = svc?.name || ''
    payload.serviceId = staffForm.value.serviceId
    payload.professionalSkills = `${staffForm.value.mainServiceCategory}${serviceName ? '-' + serviceName : ''}`
  }

  savingDetail.value = true
  try {
    const res = await updateStaffProfileByAdmin(staffDetail.value.staffId, payload)
    const updated = normalizeStaffRow(res.data?.data || res.data || {})

    // 更新详情和表单
    staffDetail.value = { ...staffDetail.value, ...updated }
    staffForm.value = { ...staffForm.value, ...staffDetail.value }

    // 同步更新列表中的该条记录
    const idx = staffList.value.findIndex((item) => item.staffId === staffDetail.value.staffId)
    if (idx !== -1) {
      staffList.value[idx] = {
        ...staffList.value[idx],
        ...staffDetail.value
      }
    }

    ElMessage.success('保存成功')
  } catch (e) {
    const status = e?.response?.status
    const backendMsg = e?.response?.data?.message || e?.response?.data?.msg
    ElMessage.error(backendMsg || (status === 404 ? '编辑接口不存在（404），请核对后端路径' : '') || e.message || '保存失败')
  } finally {
    savingDetail.value = false
  }
}

// 在详情弹窗中触发审核通过/驳回，复用原有审核逻辑
const handleApproveInDetail = async () => {
  if (!staffDetail.value) return
  // 审核前先检查必填项
  if (!validateStaffForm()) return
  // 为避免遗漏，先保存一次资料，再发起审核
  await saveStaffDetail()
  handleApprove(staffDetail.value)
}

const handleRejectInDetail = () => {
  if (!staffDetail.value) return
  handleReject(staffDetail.value)
}

// 在详情弹窗中触发“降级为普通用户”
const handleDemoteInDetail = async () => {
  if (!staffForm.value?.userId) {
    ElMessage.error('缺少 userId，无法降级')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定将用户「${staffForm.value.name || staffForm.value.userId}」降级为普通用户吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch (e) {
    // 取消确认不做任何处理
    return
  }

  demoteLoading.value = true
  try {
    await demoteUserToUser(staffForm.value.userId)
    ElMessage.success('降级成功')
    detailDialogVisible.value = false
    resetDetailDialog()
    loadStaffList()
  } catch (e) {
    const status = e?.response?.status
    const backendMsg = e?.response?.data?.message || e?.response?.data?.msg
    ElMessage.error(
      backendMsg ||
        (status === 404 ? '降级接口不存在（404），请核对后端路径/方法' : '') ||
        e.message ||
        '降级失败'
    )
  } finally {
    demoteLoading.value = false
  }
}

onMounted(async () => {
  // 先加载服务类型/服务项目列表，供下拉选项使用
  try {
    const res = await getAvailableServices()
    const data = res.data?.data || res.data || []
    const list = Array.isArray(data) ? data : (data.list || data.records || [])
    serviceOptions.value = list.map((item) => {
      const category =
        item.mainCategory ??
        item.main_category ??
        item.serviceTypeName ??
        item.service_type_name ??
        ''
      const name =
        item.serviceName ??
        item.service_name ??
        item.name ??
        ''
      return {
        id: item.serviceId ?? item.id ?? item.service_id,
        category,
        name
      }
    }).filter((s) => s.id && s.category && s.name)
  } catch (e) {
    console.error('加载服务类型/服务列表失败:', e)
    serviceOptions.value = []
  }

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

