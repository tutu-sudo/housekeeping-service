<template>
  <div class="service-type-management-view">
    <Navigation />
    <div class="content">
      <el-container>
        <AdminSidebar />
        <el-main>
          <div class="page-header">
            <h2>服务类型管理</h2>
            <el-button type="primary" @click="handleAdd" :disabled="!selectedMainCategoryId">
              <el-icon><Plus /></el-icon>
              新增小类型
            </el-button>
          </div>
          
          <el-card style="margin-bottom: 20px">
            <el-form :inline="true" class="filter-form">
              <el-form-item label="选择大类型">
                <el-select
                  v-model="selectedMainCategoryId"
                  placeholder="请先选择大类型"
                  style="width: 300px"
                  @change="handleMainCategoryChange"
                >
                  <el-option
                    v-for="category in mainCategories"
                    :key="category.serviceTypeId"
                    :label="category.typeName"
                    :value="category.serviceTypeId"
                  />
                </el-select>
              </el-form-item>
            </el-form>
          </el-card>
          
          <el-card v-if="selectedMainCategoryId">
            <template #header>
              <div class="card-header">
                <span>{{ selectedMainCategoryName }} - 小类型列表</span>
              </div>
            </template>
            <el-table :data="serviceTypes" v-loading="loading" stripe>
              <el-table-column prop="serviceTypeId" label="ID" width="80" />
              <el-table-column label="类型图片" width="120">
                <template #default="scope">
                  <el-image
                    v-if="scope.row.imageUrl"
                    :src="scope.row.imageUrl"
                    style="width: 80px; height: 80px"
                    fit="cover"
                    :preview-src-list="[scope.row.imageUrl]"
                  />
                  <span v-else style="color: #999">无图片</span>
                </template>
              </el-table-column>
              <el-table-column prop="typeName" label="类型名称" />
              <el-table-column prop="description" label="描述" show-overflow-tooltip />
              <el-table-column prop="basePrice" label="基础价格(元)" width="120">
                <template #default="scope">
                  ¥{{ scope.row.basePrice?.toFixed(2) || '0.00' }}
                </template>
              </el-table-column>
              <el-table-column prop="minDuration" label="最小时长(小时)" width="140" />
              <el-table-column prop="createTime" label="创建时间" width="180" />
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
          
          <el-empty v-else description="请先选择一个大类型" />
          
          <!-- 新增/编辑对话框 -->
          <el-dialog
            v-model="dialogVisible"
            :title="dialogTitle"
            width="600px"
            @close="resetForm"
          >
            <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
              <el-form-item label="大类型" v-if="!isEdit">
                <el-input :value="selectedMainCategoryName" disabled />
              </el-form-item>
              <el-form-item label="类型名称" prop="typeName">
                <el-input v-model="form.typeName" placeholder="请输入小类型名称" />
              </el-form-item>
              <el-form-item label="描述" prop="description">
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入类型描述"
                />
              </el-form-item>
              <el-form-item label="类型图片" prop="imageUrl">
                <el-upload
                  :action="uploadUrl"
                  :headers="uploadHeaders"
                  :on-success="handleImageSuccess"
                  :before-upload="beforeImageUpload"
                  :show-file-list="false"
                  name="file"
                  :data="{ category: 'service-type' }"
                  class="image-uploader"
                >
                  <img v-if="form.imageUrl" :src="form.imageUrl" class="service-type-image" />
                  <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
                </el-upload>
                <div style="font-size: 12px; color: #999; margin-top: 5px;">
                  点击上传类型图片（支持JPG、PNG格式，大小不超过5MB）
                </div>
              </el-form-item>
              <el-form-item label="基础价格(元)" prop="basePrice">
                <el-input-number
                  v-model="form.basePrice"
                  :precision="2"
                  :min="0"
                  :step="10"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="最小时长(小时)" prop="minDuration">
                <el-input-number
                  v-model="form.minDuration"
                  :min="1"
                  :step="1"
                  style="width: 100%"
                />
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
import { Plus } from '@element-plus/icons-vue'
import {
  getMainCategories,
  getSubTypes,
  createSubType,
  getAdminServiceTypes,
  updateServiceType,
  deleteServiceType
} from '@/api/admin'
import { uploadFile } from '@/api/files'
import { getToken } from '@/utils/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

const mainCategories = ref([])
const serviceTypes = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const currentId = ref(null)
const selectedMainCategoryId = ref(null)

const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL || '/api'}/files/upload`)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

const selectedMainCategoryName = computed(() => {
  const category = mainCategories.value.find(c => c.serviceTypeId === selectedMainCategoryId.value)
  return category?.typeName || ''
})

const form = ref({
  parentTypeId: null,
  typeName: '',
  description: '',
  imageUrl: '',
  basePrice: 0,
  minDuration: 1
})

const rules = {
  typeName: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
  basePrice: [{ required: true, message: '请输入基础价格', trigger: 'blur' }],
  minDuration: [{ required: true, message: '请输入最小时长', trigger: 'blur' }]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑小类型' : '新增小类型'))

const loadMainCategories = async () => {
  try {
    // 优先使用新接口
    const response = await getMainCategories()
    mainCategories.value = response.data?.data || response.data || []
  } catch (error) {
    console.warn('新接口不可用，使用降级方案:', error)
    // 降级方案：使用旧接口获取所有类型，然后过滤出大类型
    try {
      const response = await getAdminServiceTypes()
      const allTypes = response.data?.data || response.data || []
      // 过滤出大类型（isMainCategory === 1 或 parentTypeId === NULL）
      mainCategories.value = allTypes.filter(type => 
        type.isMainCategory === 1 || type.parentTypeId === null
      )
      if (mainCategories.value.length > 0) {
        console.log('降级方案成功，找到大类型:', mainCategories.value.length)
      }
    } catch (fallbackError) {
      console.error('降级方案也失败:', fallbackError)
      ElMessage.error('获取大类型列表失败，请检查后端接口')
      mainCategories.value = []
    }
  }
}

const loadServiceTypes = async () => {
  if (!selectedMainCategoryId.value) {
    serviceTypes.value = []
    return
  }
  
  loading.value = true
  try {
    // 优先使用新接口
    const response = await getSubTypes(selectedMainCategoryId.value)
    serviceTypes.value = response.data?.data || response.data || []
  } catch (error) {
    console.warn('新接口不可用，使用降级方案:', error)
    // 降级方案：使用旧接口获取所有类型，然后过滤出该大类型下的小类型
    try {
      const response = await getAdminServiceTypes()
      const allTypes = response.data?.data || response.data || []
      // 过滤出该大类型下的小类型（parentTypeId === selectedMainCategoryId）
      serviceTypes.value = allTypes.filter(type => 
        type.parentTypeId === selectedMainCategoryId.value
      )
      if (serviceTypes.value.length > 0) {
        console.log('降级方案成功，找到小类型:', serviceTypes.value.length)
      }
    } catch (fallbackError) {
      console.error('降级方案也失败:', fallbackError)
      ElMessage.error('获取小类型列表失败')
    serviceTypes.value = []
    }
  } finally {
    loading.value = false
  }
}

const handleMainCategoryChange = () => {
  loadServiceTypes()
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

const handleAdd = () => {
  if (!selectedMainCategoryId.value) {
    ElMessage.warning('请先选择大类型')
    return
  }
  isEdit.value = false
  currentId.value = null
  resetForm()
  form.value.parentTypeId = selectedMainCategoryId.value
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.serviceTypeId
  // 编辑时不需要 parentTypeId，因为小类型不能修改父类型
  form.value = {
    typeName: row.typeName || '',
    description: row.description || '',
    imageUrl: row.imageUrl || '',
    basePrice: row.basePrice || 0,
    minDuration: row.minDuration || 1
  }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除服务类型"${row.typeName}"吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteServiceType(row.serviceTypeId)
    ElMessage.success('删除成功')
    loadServiceTypes()
  } catch (error) {
    if (error !== 'cancel') {
      // 处理特定的错误码
      const errorCode = error.response?.data?.code
      const errorMessage = error.response?.data?.message || error.message
      
      if (errorCode === 2005) {
        ElMessage.error('该服务类型下存在服务项目，无法删除。请先删除或下架相关的服务项目。')
      } else {
        ElMessage.error(errorMessage || '删除失败')
      }
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (!isEdit.value && !form.value.parentTypeId) {
      ElMessage.warning('请先选择大类型')
      return
    }
    
    submitting.value = true
    
    if (isEdit.value) {
      // 编辑时，不传递 parentTypeId（小类型不能修改父类型）
      const { parentTypeId, ...updateData } = form.value
      await updateServiceType(currentId.value, updateData)
      ElMessage.success('更新成功')
    } else {
      // 创建时，必须包含 parentTypeId
      // 后端会在创建类型时自动创建对应的服务项目（在同一事务中）
      await createSubType(form.value)
      ElMessage.success('创建成功，系统已自动生成对应的默认服务项目，可在"服务项目管理"页面查看并编辑')
    }
    
    dialogVisible.value = false
    loadServiceTypes()
  } catch (error) {
    if (error !== false) {
      // 处理特定的错误码
      const errorCode = error.response?.data?.code
      const errorMessage = error.response?.data?.message || error.message
      
      if (errorCode === 2002) {
        ElMessage.error('父类型不存在或不是大类型，请刷新页面重试')
      } else if (errorCode === 2006) {
        ElMessage.error('服务类型名称已存在，请使用其他名称')
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
    parentTypeId: selectedMainCategoryId.value,
    typeName: '',
    description: '',
    imageUrl: '',
    basePrice: 0,
    minDuration: 1
  }
  formRef.value?.clearValidate()
}

onMounted(async () => {
  await loadMainCategories()
})
</script>

<style scoped lang="scss">
.service-type-management-view {
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
    margin-bottom: 0;
  }
}

.card-header {
  font-weight: 500;
  font-size: 16px;
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
  
  .service-type-image {
    width: 178px;
    height: 178px;
    display: block;
    object-fit: cover;
  }
}
</style>
