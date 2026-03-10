<template>
  <div class="schedule-management-view">
    <Navigation />
    <div class="content">
      <el-container>
        <AdminSidebar />
        <el-main>
          <div class="page-header">
            <h2>排班管理</h2>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增排班
            </el-button>
          </div>
          
          <el-card>
            <el-form :inline="true" class="filter-form">
              <el-form-item label="服务人员ID">
                <el-input v-model="filters.staffId" placeholder="请输入服务人员ID" clearable />
              </el-form-item>
              <el-form-item label="开始日期">
                <el-date-picker
                  v-model="filters.startDate"
                  type="date"
                  placeholder="选择开始日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  clearable
                />
              </el-form-item>
              <el-form-item label="结束日期">
                <el-date-picker
                  v-model="filters.endDate"
                  type="date"
                  placeholder="选择结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  clearable
                />
              </el-form-item>
              <el-form-item label="可用状态">
                <el-select v-model="filters.availableStatus" placeholder="请选择状态" clearable>
                  <el-option label="全部" :value="undefined" />
                  <el-option label="可用" :value="1" />
                  <el-option label="不可用" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadSchedules" :loading="loading">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button @click="resetFilters">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
          
          <el-card style="margin-top: 20px">
            <el-table :data="schedules" v-loading="loading" stripe>
              <el-table-column prop="scheduleId" label="ID" width="80" />
              <el-table-column prop="staffName" label="服务人员" width="120" />
              <el-table-column prop="workDate" label="工作日期" width="120" />
              <el-table-column prop="startTime" label="开始时间" width="100" />
              <el-table-column prop="endTime" label="结束时间" width="100" />
              <el-table-column prop="availableStatus" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.availableStatus === 1 ? 'success' : 'danger'">
                    {{ scope.row.availableStatus === 1 ? '可用' : '不可用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="primary" link @click="handleEdit(scope.row)">
                    编辑
                  </el-button>
                  <el-button size="small" type="danger" link @click="handleDelete(scope.row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
          
          <!-- 新增/编辑对话框 -->
          <el-dialog
            v-model="dialogVisible"
            :title="dialogTitle"
            width="600px"
            @close="resetForm"
          >
            <div v-loading="loadingDetail">
              <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
              <el-form-item label="服务人员ID" prop="staffId">
                <el-input-number
                  v-model="form.staffId"
                  :min="1"
                  style="width: 100%"
                  placeholder="请输入服务人员ID"
                />
              </el-form-item>
              <el-form-item label="工作日期" prop="workDate">
                <el-date-picker
                  v-model="form.workDate"
                  type="date"
                  placeholder="选择工作日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="开始时间" prop="startTime">
                <el-time-picker
                  v-model="form.startTime"
                  format="HH:mm:ss"
                  value-format="HH:mm:ss"
                  placeholder="选择开始时间"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="结束时间" prop="endTime">
                <el-time-picker
                  v-model="form.endTime"
                  format="HH:mm:ss"
                  value-format="HH:mm:ss"
                  placeholder="选择结束时间"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="可用状态" prop="availableStatus">
                <el-radio-group v-model="form.availableStatus">
                  <el-radio :label="1">可用</el-radio>
                  <el-radio :label="0">不可用</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
            </div>
            <template #footer>
              <el-button @click="dialogVisible = false">取消</el-button>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">
                确定
              </el-button>
            </template>
          </el-dialog>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  getSchedules,
  createSchedule,
  updateSchedule,
  deleteSchedule,
  getScheduleDetail
} from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

const schedules = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const currentId = ref(null)
const loadingDetail = ref(false)

const filters = ref({
  staffId: undefined,
  startDate: undefined,
  endDate: undefined,
  availableStatus: undefined
})

const form = ref({
  staffId: undefined,
  workDate: '',
  startTime: '',
  endTime: '',
  availableStatus: 1
})

const rules = {
  staffId: [{ required: true, message: '请输入服务人员ID', trigger: 'blur' }],
  workDate: [{ required: true, message: '请选择工作日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑排班' : '新增排班'))

const loadSchedules = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.value.staffId) params.staffId = filters.value.staffId
    if (filters.value.startDate) params.startDate = filters.value.startDate
    if (filters.value.endDate) params.endDate = filters.value.endDate
    if (filters.value.availableStatus !== undefined) {
      params.availableStatus = filters.value.availableStatus
    }

    const response = await getSchedules(params)
    schedules.value = response.data?.data || response.data || []
  } catch (error) {
    console.error('获取排班列表失败:', error)
    ElMessage.error('获取排班列表失败')
    schedules.value = []
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = {
    staffId: undefined,
    startDate: undefined,
    endDate: undefined,
    availableStatus: undefined
  }
  loadSchedules()
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  currentId.value = row.scheduleId
  loadingDetail.value = true
  dialogVisible.value = true
  
  try {
    // 使用详情接口获取最新数据
    const response = await getScheduleDetail(row.scheduleId)
    const detail = response.data?.data || response.data
    form.value = {
      staffId: detail.staffId,
      workDate: detail.workDate || '',
      startTime: detail.startTime || '',
      endTime: detail.endTime || '',
      availableStatus: detail.availableStatus !== undefined ? detail.availableStatus : 1
    }
  } catch (error) {
    console.error('获取排班详情失败，使用列表数据:', error)
    // 如果获取详情失败，使用列表数据作为备用
    form.value = {
      staffId: row.staffId,
      workDate: row.workDate || '',
      startTime: row.startTime || '',
      endTime: row.endTime || '',
      availableStatus: row.availableStatus !== undefined ? row.availableStatus : 1
    }
  } finally {
    loadingDetail.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除该排班记录吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteSchedule(row.scheduleId)
    ElMessage.success('删除成功')
    loadSchedules()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await updateSchedule(currentId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createSchedule(form.value)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadSchedules()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  form.value = {
    staffId: undefined,
    workDate: '',
    startTime: '',
    endTime: '',
    availableStatus: 1
  }
  formRef.value?.clearValidate()
}

onMounted(() => {
  loadSchedules()
})
</script>

<style scoped lang="scss">
.schedule-management-view {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.content {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
