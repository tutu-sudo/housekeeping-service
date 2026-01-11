<template>
  <div class="service-item-management-view">
    <Navigation />
    <div class="content">
      <el-container>
        <AdminSidebar />
        <el-main>
          <div class="page-header">
            <h2>服务项目管理</h2>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增服务项目
            </el-button>
          </div>
          
          <el-card>
            <el-form :inline="true" class="filter-form">
              <el-form-item label="服务类型">
                <el-select
                  v-model="filters.serviceTypeId"
                  placeholder="请选择服务类型"
                  clearable
                  @change="loadServices"
                >
                  <el-option
                    v-for="type in serviceTypes"
                    :key="type.serviceTypeId"
                    :label="type.typeName"
                    :value="type.serviceTypeId"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="可用状态">
                <el-select
                  v-model="filters.availableStatus"
                  placeholder="请选择状态"
                  clearable
                  @change="loadServices"
                >
                  <el-option label="全部" :value="undefined" />
                  <el-option label="可用" :value="1" />
                  <el-option label="不可用" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadServices" :loading="loading">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button @click="resetFilters">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
          
          <el-card style="margin-top: 20px">
            <el-table :data="services" v-loading="loading" stripe>
              <el-table-column prop="serviceId" label="ID" width="80" />
              <el-table-column prop="serviceTypeName" label="服务类型" width="150" />
              <el-table-column prop="serviceName" label="服务名称" />
              <el-table-column prop="description" label="描述" show-overflow-tooltip />
              <el-table-column prop="price" label="价格(元)" width="120">
                <template #default="scope">
                  ¥{{ scope.row.price?.toFixed(2) || '0.00' }}
                </template>
              </el-table-column>
              <el-table-column prop="estimatedDuration" label="预估时长(小时)" width="140" />
              <el-table-column prop="availableStatus" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.availableStatus === 1 ? 'success' : 'danger'">
                    {{ scope.row.availableStatus === 1 ? '可用' : '不可用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="250" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="primary" link @click="handleEdit(scope.row)">
                    编辑
                  </el-button>
                  <el-button
                    size="small"
                    :type="scope.row.availableStatus === 1 ? 'warning' : 'success'"
                    link
                    @click="handleToggleStatus(scope.row)"
                  >
                    {{ scope.row.availableStatus === 1 ? '下架' : '上架' }}
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
            <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
              <el-form-item label="服务类型" prop="serviceTypeId">
                <el-select v-model="form.serviceTypeId" placeholder="请选择服务类型" style="width: 100%">
                  <el-option
                    v-for="type in serviceTypes"
                    :key="type.serviceTypeId"
                    :label="type.typeName"
                    :value="type.serviceTypeId"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="服务名称" prop="serviceName">
                <el-input v-model="form.serviceName" placeholder="请输入服务名称" />
              </el-form-item>
              <el-form-item label="描述" prop="description">
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入服务描述"
                />
              </el-form-item>
              <el-form-item label="价格(元)" prop="price">
                <el-input-number
                  v-model="form.price"
                  :precision="2"
                  :min="0"
                  :step="10"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="预估时长(小时)" prop="estimatedDuration">
                <el-input-number
                  v-model="form.estimatedDuration"
                  :min="1"
                  :step="1"
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
  getAdminServiceTypes,
  getAdminServices,
  createService,
  updateService,
  updateServiceStatus,
  deleteService
} from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

const services = ref([])
const serviceTypes = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const currentId = ref(null)

const filters = ref({
  serviceTypeId: undefined,
  availableStatus: undefined
})

const form = ref({
  serviceTypeId: undefined,
  serviceName: '',
  description: '',
  price: 0,
  estimatedDuration: 1,
  availableStatus: 1
})

const rules = {
  serviceTypeId: [{ required: true, message: '请选择服务类型', trigger: 'change' }],
  serviceName: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  estimatedDuration: [{ required: true, message: '请输入预估时长', trigger: 'blur' }]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑服务项目' : '新增服务项目'))

const loadServiceTypes = async () => {
  try {
    const response = await getAdminServiceTypes()
    serviceTypes.value = response.data?.data || response.data || []
  } catch (error) {
    console.error('获取服务类型列表失败:', error)
  }
}

const loadServices = async () => {
  loading.value = true
  try {
    const params = {}
    if (filters.value.serviceTypeId) params.serviceTypeId = filters.value.serviceTypeId
    if (filters.value.availableStatus !== undefined) {
      params.availableStatus = filters.value.availableStatus
    }

    const response = await getAdminServices(params)
    if (response.data) {
      services.value = Array.isArray(response.data) ? response.data : response.data.list || []
      // 添加服务类型名称
      services.value.forEach(service => {
        const type = serviceTypes.value.find(t => t.serviceTypeId === service.serviceTypeId)
        service.serviceTypeName = type?.typeName || ''
      })
    } else {
      services.value = []
    }
  } catch (error) {
    console.error('获取服务列表失败:', error)
    ElMessage.error('获取服务列表失败')
    services.value = []
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = {
    serviceTypeId: undefined,
    availableStatus: undefined
  }
  loadServices()
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.serviceId
  form.value = {
    serviceTypeId: row.serviceTypeId,
    serviceName: row.serviceName || '',
    description: row.description || '',
    price: row.price || 0,
    estimatedDuration: row.estimatedDuration || 1,
    availableStatus: row.availableStatus !== undefined ? row.availableStatus : 1
  }
  dialogVisible.value = true
}

const handleToggleStatus = async (row) => {
  try {
    const newStatus = row.availableStatus === 1 ? 0 : 1
    const action = newStatus === 1 ? '上架' : '下架'
    
    await ElMessageBox.confirm(
      `确定要${action}服务"${row.serviceName}"吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updateServiceStatus(row.serviceId, newStatus)
    ElMessage.success(`${action}成功`)
    loadServices()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除服务"${row.serviceName}"吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteService(row.serviceId)
    ElMessage.success('删除成功')
    loadServices()
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
      await updateService(currentId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createService(form.value)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadServices()
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
    serviceTypeId: undefined,
    serviceName: '',
    description: '',
    price: 0,
    estimatedDuration: 1,
    availableStatus: 1
  }
  formRef.value?.clearValidate()
}

onMounted(async () => {
  await loadServiceTypes()
  loadServices()
})
</script>

<style scoped lang="scss">
.service-item-management-view {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.content {
  padding: 20px;
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
