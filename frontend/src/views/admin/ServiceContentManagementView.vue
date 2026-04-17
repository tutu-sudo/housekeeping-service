<template>
  <div class="service-content-management-view">
    <Navigation />
    <div class="content">
      <el-container>
        <AdminSidebar />
        <el-main>
          <div class="page-header">
            <h2>服务内容管理</h2>
            <p class="page-desc">管理服务详情页面的展示内容（宣传图片、适用范围、服务内容等）</p>
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
              <el-form-item label="关键词">
                <el-input
                  v-model="filters.keyword"
                  placeholder="请输入服务名称"
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
              <el-table-column label="宣传图片" width="120">
                <template #default="scope">
                  <el-image
                    v-if="scope.row.imageUrl"
                    :src="scope.row.imageUrl"
                    :preview-src-list="[scope.row.imageUrl]"
                    fit="cover"
                    style="width: 80px; height: 60px; border-radius: 4px"
                  >
                    <template #error>
                      <div class="image-placeholder">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div v-else class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                    <span>无图片</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="适用范围" show-overflow-tooltip min-width="200" />
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="handleEdit(scope.row)">
                    <el-icon><Edit /></el-icon>
                    编辑内容
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <!-- 分页 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </el-card>
        </el-main>
      </el-container>
    </div>

    <!-- 编辑服务内容对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="`编辑服务内容 - ${currentService?.serviceName}`"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="服务名称">
          <el-input v-model="form.serviceName" disabled />
        </el-form-item>

        <el-form-item label="服务类型">
          <el-input v-model="form.mainCategory" disabled />
        </el-form-item>

        <el-form-item label="宣传图片" prop="imageUrl">
          <div class="upload-container">
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleImageSuccess"
              :before-upload="beforeImageUpload"
              accept="image/*"
            >
              <div v-if="form.imageUrl" class="image-preview">
                <el-image :src="form.imageUrl" fit="cover" class="preview-image" />
                <div class="image-overlay">
                  <el-icon class="upload-icon"><Upload /></el-icon>
                  <span>重新上传</span>
                </div>
              </div>
              <div v-else class="upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <div class="upload-text">点击上传图片</div>
                <div class="upload-tip">建议尺寸：800x600，支持JPG/PNG格式</div>
              </div>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="适用范围" prop="applicableScope">
          <el-input
            v-model="form.applicableScope"
            type="textarea"
            :rows="4"
            placeholder="请输入服务的适用范围描述，如：包括客厅、卧室、厨房、卫生间等区域的日常清洁服务"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="服务内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请输入详细的服务内容描述"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="收费标准">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="参考价格" label-width="100px" prop="price">
                <el-input-number
                  v-model="form.price"
                  :min="0"
                  :precision="2"
                  :step="10"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="服务时长" label-width="100px" prop="estimatedDuration">
                <el-input-number
                  v-model="form.estimatedDuration"
                  :min="1"
                  :precision="0"
                  :step="1"
                  controls-position="right"
                />
                <span style="margin-left: 8px">小时</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="结算方式" label-width="100px">
                <el-select v-model="form.billingType" style="width: 100%">
                  <el-option label="按小时结算" value="hourly" />
                  <el-option label="按天结算" value="daily" />
                  <el-option label="按次结算" value="times" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item label="服务特色">
          <div class="features-list">
            <div
              v-for="(feature, index) in form.features"
              :key="index"
              class="feature-item"
            >
              <el-input
                v-model="form.features[index]"
                placeholder="请输入服务特色"
                maxlength="50"
              >
                <template #append>
                  <el-button
                    :icon="Delete"
                    @click="removeFeature(index)"
                    v-if="form.features.length > 1"
                  />
                </template>
              </el-input>
            </div>
            <el-button
              type="primary"
              plain
              :icon="Plus"
              @click="addFeature"
              v-if="form.features.length < 6"
            >
              添加特色
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search,
  Edit,
  Picture,
  Plus,
  Upload,
  Delete
} from '@element-plus/icons-vue'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
import { getAdminServices, updateService, getMainCategories } from '@/api/admin'
import { getToken } from '@/utils/auth'

const services = ref([])
const serviceTypes = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const currentService = ref(null)

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
  mainCategory: 'all',
  keyword: ''
})

const form = ref({
  serviceName: '',
  mainCategory: '',
  imageUrl: '',
  applicableScope: '',
  content: '',
  price: 0,
  estimatedDuration: 1,
  billingType: 'hourly',
  features: ['专业团队，经验丰富', '服务标准，流程规范', '工具齐全，高效清洁', '售后保障，放心无忧']
})

const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL || '/api'}/files/upload`)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

const rules = {
  imageUrl: [{ required: true, message: '请上传服务宣传图片', trigger: 'change' }],
  applicableScope: [{ required: true, message: '请输入适用范围', trigger: 'blur' }],
  price: [{ required: true, message: '请输入参考价格', trigger: 'blur' }],
  estimatedDuration: [{ required: true, message: '请输入服务时长', trigger: 'blur' }]
}

// 六大类型名称（固定，与后端保持一致）
const MAIN_CATEGORIES = [
  '基础家务服务',
  '专业保洁与养护服务',
  '母婴护理服务',
  '养老护理服务',
  '家电维修与维护服务',
  '特色专项服务'
]

const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
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

const addFeature = () => {
  form.value.features.push('')
}

const removeFeature = (index) => {
  form.value.features.splice(index, 1)
}

const loadServiceTypes = async () => {
  try {
    const mainCategoriesRes = await getMainCategories()
    const mainCategories = mainCategoriesRes.data?.data || mainCategoriesRes.data || []

    if (Array.isArray(mainCategories) && mainCategories.length > 0) {
      serviceTypes.value = mainCategories.map((name) => ({
        name: name,
        value: name
      }))
    } else {
      serviceTypes.value = MAIN_CATEGORIES.map(name => ({
        name: name,
        value: name
      }))
    }
  } catch (error) {
    serviceTypes.value = MAIN_CATEGORIES.map(name => ({
      name: name,
      value: name
    }))
  }
}

const loadServices = async () => {
  // 筛选条件变化后，回到第一页，避免当前页码超过结果页数导致“看起来搜不到”
  currentPage.value = 1
  loading.value = true
  try {
    const params = {}
    // “全部”不传参；只有选择具体服务类型才传
    if (
      filters.value.mainCategory &&
      filters.value.mainCategory !== 'all'
    ) {
      params.mainCategory = filters.value.mainCategory
    }
    // 关键词：为了支持“模糊查询”且避免后端 keyword 字段名/匹配口径不一致导致无法搜索
    // 这里不把 keyword 透传给后端，而是拿到满足其他筛选条件的列表后再做前端模糊过滤。

    const response = await getAdminServices(params)
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
        const content = `${row?.content ?? ''}`.toLowerCase()
        return serviceName.includes(kw) || description.includes(kw) || mainCategory.includes(kw) || content.includes(kw)
      })
    }
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
    keyword: ''
  }
  currentPage.value = 1
  loadServices()
}

const handleSizeChange = () => {
  currentPage.value = 1
}

const handleCurrentChange = () => {
  // 页码改变时不需要额外操作，computed会自动更新
}

const handleEdit = (row) => {
  currentService.value = row
  form.value = {
    serviceName: row.serviceName,
    mainCategory: row.mainCategory,
    imageUrl: row.imageUrl || '',
    applicableScope: row.description || '',
    content: row.content || row.description || '',
    price: row.price || 0,
    estimatedDuration: row.estimatedDuration || 1,
    billingType: row.billingType || 'hourly',
    features: row.features || ['专业团队，经验丰富', '服务标准，流程规范', '工具齐全，高效清洁', '售后保障，放心无忧']
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  submitting.value = true
  try {
    const updateData = {
      imageUrl: form.value.imageUrl,
      description: form.value.applicableScope,
      content: form.value.content,
      price: form.value.price,
      estimatedDuration: form.value.estimatedDuration,
      billingType: form.value.billingType,
      features: form.value.features.filter(f => f.trim() !== '')
    }

    await updateService(currentService.value.serviceId, updateData)
    ElMessage.success('服务内容更新成功')
    dialogVisible.value = false
    loadServices()
  } catch (error) {
    console.error('更新服务内容失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '更新服务内容失败'
    ElMessage.error(errorMessage)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadServiceTypes()
  loadServices()
})
</script>

<style scoped lang="scss">
.service-content-management-view {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.content {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;

  h2 {
    margin: 0 0 10px 0;
    color: #333;
  }

  .page-desc {
    margin: 0;
    color: #666;
    font-size: 14px;
  }
}

.filter-form {
  margin-bottom: 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.image-placeholder {
  width: 80px;
  height: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #999;
  font-size: 12px;

  .el-icon {
    font-size: 24px;
    margin-bottom: 4px;
  }
}

.upload-container {
  width: 100%;

  .image-preview {
    position: relative;
    width: 300px;
    height: 225px;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;

    .preview-image {
      width: 100%;
      height: 100%;
    }

    .image-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: rgba(0, 0, 0, 0.5);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s;
      color: #fff;

      .upload-icon {
        font-size: 32px;
        margin-bottom: 8px;
      }
    }

    &:hover .image-overlay {
      opacity: 1;
    }
  }

  .upload-placeholder {
    width: 300px;
    height: 225px;
    border: 2px dashed #dcdfe6;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: border-color 0.3s;

    &:hover {
      border-color: #4caf50;
    }

    .upload-icon {
      font-size: 48px;
      color: #999;
      margin-bottom: 12px;
    }

    .upload-text {
      font-size: 16px;
      color: #666;
      margin-bottom: 8px;
    }

    .upload-tip {
      font-size: 12px;
      color: #999;
    }
  }
}

.features-list {
  width: 100%;

  .feature-item {
    margin-bottom: 12px;
  }
}
</style>


