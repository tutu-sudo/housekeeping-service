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
                  v-model="filters.mainCategory"
                  placeholder="请选择服务类型"
                  clearable
                  @change="loadServices"
                  style="width: 180px;"
                >
                  <el-option label="全部" value="all" />
                  <el-option
                    v-for="type in serviceTypes"
                    :key="type.value"
                    :label="type.name"
                    :value="type.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="可用状态">
                <el-select
                  v-model="filters.availableStatus"
                  placeholder="请选择状态"
                  clearable
                  @change="loadServices"
                  style="width: 180px;"
                >
                  <el-option label="全部" value="all" />
                  <el-option label="可用" :value="1" />
                  <el-option label="不可用" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item label="关键词">
                <el-input
                  v-model="filters.keyword"
                  placeholder="请输入服务名称或描述"
                  clearable
                  style="width: 200px"
                  @keyup.enter="loadServices"
                />
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
            <el-table :data="paginatedServices" v-loading="loading" stripe>
              <el-table-column prop="serviceId" label="ID" width="80" />
              <el-table-column prop="mainCategory" label="服务类型" width="150" />
              <el-table-column prop="serviceName" label="服务名称" min-width="150" />
              <el-table-column prop="description" label="描述" show-overflow-tooltip min-width="200" />
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
              <template #empty>
                <el-empty description="暂无服务项目数据" />
              </template>
            </el-table>
            
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :total="total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </el-card>
          
          <!-- 新增/编辑对话框 -->
          <el-dialog
            v-model="dialogVisible"
            :title="dialogTitle"
            width="600px"
            @close="resetForm"
          >
            <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
              <!-- 新增时显示服务类型选择，编辑时不显示（后端不允许修改服务类型） -->
              <el-form-item v-if="!isEdit" label="服务类型" prop="mainCategory">
                <el-select v-model="form.mainCategory" placeholder="请选择服务类型" style="width: 100%">
                  <el-option
                    v-for="type in serviceTypes"
                    :key="type.value"
                    :label="type.name"
                    :value="type.value"
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
              <el-form-item label="服务图片" prop="imageUrl">
                <el-upload
                  :action="uploadUrl"
                  :headers="uploadHeaders"
                  :on-success="handleImageSuccess"
                  :before-upload="beforeImageUpload"
                  :show-file-list="false"
                  name="file"
                  :data="{ category: 'service' }"
                  class="image-uploader"
                >
                  <img v-if="form.imageUrl" :src="form.imageUrl" class="service-image" />
                  <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
                </el-upload>
                <div style="font-size: 12px; color: #999; margin-top: 5px;">
                  点击上传服务图片（支持JPG、PNG格式，大小不超过5MB）
                </div>
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
import { Plus, Search, Upload } from '@element-plus/icons-vue'
import {
  getAdminServiceTypes,
  getMainCategories,
  getAdminServices,
  createService,
  updateService,
  updateServiceStatus,
  deleteService
} from '@/api/admin'
import { uploadFile } from '@/api/files'
import { getToken } from '@/utils/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

const services = ref([])
const serviceTypes = ref([]) // 大类型列表（用于过滤下拉框和表单选择）
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const currentId = ref(null)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = computed(() => services.value.length)

// 分页后的服务列表
const paginatedServices = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return services.value.slice(start, end)
})

const filters = ref({
  mainCategory: 'all', // 默认全部（避免空值导致下拉框显示placeholder）
  availableStatus: 'all',
  keyword: ''
})

const form = ref({
  mainCategory: null, // 大类名称（字符串），用于前端展示，创建时可选传给后端
  serviceName: '',
  description: '',
  price: 0,
  estimatedDuration: 1,
  availableStatus: 1,
  imageUrl: ''
})

const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL || '/api'}/files/upload`)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))


const rules = {
  mainCategory: [{ required: true, message: '请选择服务类型', trigger: 'change' }],
  serviceName: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  estimatedDuration: [{ required: true, message: '请输入预估时长', trigger: 'blur' }]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑服务项目' : '新增服务项目'))

// 六大类型名称（固定，与后端保持一致）
const MAIN_CATEGORIES = [
  '基础家务服务',
  '专业保洁与养护服务',
  '母婴护理服务',
  '养老护理服务',
  '家电维修与维护服务',
  '特色专项服务'
]

// 加载大类型（用于过滤下拉框和表单选择）
const loadServiceTypes = async () => {
  try {
    // 后端返回的是字符串数组：["基础家务服务", "专业保洁与养护服务", ...]
    const mainCategoriesRes = await getMainCategories()
    const mainCategories = mainCategoriesRes.data?.data || mainCategoriesRes.data || []
    
    if (Array.isArray(mainCategories) && mainCategories.length > 0) {
      // 将字符串数组转换为对象数组，用于下拉框显示
      serviceTypes.value = mainCategories.map((name, index) => ({
        name: name,
        value: name // 使用名称作为值
      }))
      
    } else {
      // 如果后端返回的数据格式不符合预期，使用前端固定的六大类型
      console.warn('后端返回的大类型数据格式不符合预期，使用前端固定数据')
      serviceTypes.value = MAIN_CATEGORIES.map(name => ({
        name: name,
        value: name
      }))
    }
  } catch (error) {
    console.warn('获取大类型列表失败，使用前端固定数据:', error)
    // 如果接口失败，使用前端固定的六大类型
    serviceTypes.value = MAIN_CATEGORIES.map(name => ({
      name: name,
      value: name
    }))
  }
}


const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleImageSuccess = (response) => {
  if (response.code === 200 && response.data?.fileUrl) {
    form.value.imageUrl = response.data.fileUrl
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

const loadServices = async () => {
  // 筛选条件变化后，回到第一页，避免当前页码超过结果页数导致“看起来搜不到”
  currentPage.value = 1
  loading.value = true
  try {
    const params = {}
    // “全部”不传参；只有选择具体服务类型才传
    if (filters.value.mainCategory && filters.value.mainCategory !== 'all') {
      params.mainCategory = filters.value.mainCategory
    }
    // “全部”不传参；只有选择具体状态(0/1)才传
    if (
      filters.value.availableStatus !== undefined &&
      filters.value.availableStatus !== null &&
      filters.value.availableStatus !== 'all' &&
      filters.value.availableStatus !== ''
    ) {
      params.availableStatus = filters.value.availableStatus
    }

    // 关键词：为了支持“模糊查询”且避免后端 keyword 字段名/匹配口径不一致导致无法搜索
    // 这里不把 keyword 透传给后端，而是拿到满足其他筛选条件的列表后再做前端模糊过滤。

    const response = await getAdminServices(params)
    // 统一处理响应数据格式
    const data = response.data?.data || response.data
    if (Array.isArray(data)) {
      services.value = data
    } else if (data && Array.isArray(data.list)) {
      services.value = data.list
    } else {
      services.value = []
    }

    const kw = (filters.value.keyword || '').trim().toLowerCase()
    if (kw) {
      services.value = services.value.filter((row) => {
        const serviceName = `${row?.serviceName ?? ''}`.toLowerCase()
        const description = `${row?.description ?? ''}`.toLowerCase()
        const mainCategory = `${row?.mainCategory ?? ''}`.toLowerCase()
        return serviceName.includes(kw) || description.includes(kw) || mainCategory.includes(kw)
      })
    }
    
    // 确保服务类型已加载（仅在首次加载时尝试）
    if (serviceTypes.value.length === 0) {
      console.warn('服务类型列表为空，尝试重新加载')
      await loadServiceTypes()
    }
    
    // 后端返回的服务数据中已经包含 mainCategory 字段（六大类之一）
    // 不需要额外处理，直接使用 service.mainCategory 显示服务类型
  } catch (error) {
    console.error('获取服务列表失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '获取服务列表失败'
    ElMessage.error(errorMessage)
    services.value = []
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.value = {
    mainCategory: 'all',
    availableStatus: 'all',
    keyword: ''
  }
  currentPage.value = 1 // 重置分页
  loadServices()
}

const handleSizeChange = () => {
  currentPage.value = 1 // 改变每页数量时，重置到第一页
}

const handleCurrentChange = () => {
  // 当前页改变时，表格数据会自动更新（通过 computed）
}

const handleAdd = async () => {
  isEdit.value = false
  currentId.value = null
  resetForm()
  // 确保大类型列表已加载
  if (serviceTypes.value.length === 0) {
    await loadServiceTypes()
  }
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  currentId.value = row.serviceId
  // 确保大类型列表已加载
  if (serviceTypes.value.length === 0) {
    await loadServiceTypes()
  }
  form.value = {
    mainCategory: null, // 编辑时不显示大类（后端不允许修改服务类型）
    serviceName: row.serviceName || '',
    description: row.description || '',
    price: row.price || 0,
    estimatedDuration: row.estimatedDuration || 1,
    availableStatus: row.availableStatus !== undefined ? row.availableStatus : 1,
    imageUrl: row.imageUrl || ''
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
    currentPage.value = 1 // 操作后重置到第一页
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
    currentPage.value = 1 // 操作后重置到第一页
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
      // 编辑时：传递所有表单数据（后端会同步更新对应的 ServiceType）
      // 编辑时不允许修改大类，所以不传 mainCategory
      const { mainCategory, ...updateData } = form.value
      await updateService(currentId.value, updateData)
      ElMessage.success('更新成功！服务项目和对应的服务类型详情已同步更新')
    } else {
      // 新建服务项目时：
      // 1. 用户从下拉框选择6个大类型之一（mainCategory，必填）
      // 2. 用户自定义输入服务名称（如"日常保洁"、"餐具处理"等）
      // 3. 后端会根据服务名称自动创建对应的 ServiceType（小类型）和详情介绍
      // 4. 创建成功后，服务项目会自动出现在服务项目管理列表中
      const createData = {
        mainCategory: form.value.mainCategory, // 必填，六大类之一
        serviceName: form.value.serviceName,
        description: form.value.description,
        price: form.value.price,
        estimatedDuration: form.value.estimatedDuration,
        availableStatus: form.value.availableStatus,
        imageUrl: form.value.imageUrl
      }
      await createService(createData)
      ElMessage.success('创建成功！服务项目已添加到列表中，对应的服务类型和详情介绍已自动生成')
    }
    
    dialogVisible.value = false
    currentPage.value = 1 // 操作后重置到第一页
    loadServices()
  } catch (error) {
    if (error !== false) {
      // 处理特定的错误码
      const errorCode = error.response?.data?.code
      const errorMessage = error.response?.data?.message || error.message
      
      if (errorCode === 2002) {
        ElMessage.error('服务类型不存在，请刷新页面重试')
      } else if (errorMessage && errorMessage.includes('服务大类必须是六大类之一')) {
        ElMessage.error('请选择有效的服务大类')
      } else {
        ElMessage.error(errorMessage || '操作失败')
      }
    }
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  form.value = {
    mainCategory: null,
    serviceName: '',
    description: '',
    price: 0,
    estimatedDuration: 1,
    availableStatus: 1,
    imageUrl: ''
  }
  formRef.value?.clearValidate()
}

onMounted(async () => {
  // 先加载大类型（用于过滤下拉框和表单选择）
  await loadServiceTypes()
  // 然后加载服务列表
  await loadServices()
})
</script>

<style scoped lang="scss">
.service-item-management-view {
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

.image-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;
    
    &:hover {
      border-color: #409eff;
    }
  }
  
  .image-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    line-height: 178px;
    text-align: center;
  }
  
  .service-image {
    width: 178px;
    height: 178px;
    display: block;
    object-fit: cover;
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
