<template>
  <div class="service-type-management-view">
    <Navigation />
    <div class="content">
      <el-container>
        <AdminSidebar />
        <el-main>
          <div class="page-header">
            <h2>服务类型管理</h2>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增服务类型
            </el-button>
          </div>
          
          <el-card>
            <el-table :data="serviceTypes" v-loading="loading" stripe>
              <el-table-column prop="serviceTypeId" label="ID" width="80" />
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
          
          <!-- 新增/编辑对话框 -->
          <el-dialog
            v-model="dialogVisible"
            :title="dialogTitle"
            width="600px"
            @close="resetForm"
          >
            <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
              <el-form-item label="类型名称" prop="typeName">
                <el-input v-model="form.typeName" placeholder="请输入服务类型名称" />
              </el-form-item>
              <el-form-item label="描述" prop="description">
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入类型描述"
                />
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
  getAdminServiceTypes,
  createServiceType,
  updateServiceType,
  deleteServiceType
} from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

const serviceTypes = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const currentId = ref(null)

const form = ref({
  typeName: '',
  description: '',
  basePrice: 0,
  minDuration: 1
})

const rules = {
  typeName: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
  basePrice: [{ required: true, message: '请输入基础价格', trigger: 'blur' }],
  minDuration: [{ required: true, message: '请输入最小时长', trigger: 'blur' }]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑服务类型' : '新增服务类型'))

const loadServiceTypes = async () => {
  loading.value = true
  try {
    const response = await getAdminServiceTypes()
    serviceTypes.value = response.data?.data || response.data || []
  } catch (error) {
    console.error('获取服务类型列表失败:', error)
    ElMessage.error('获取服务类型列表失败')
    serviceTypes.value = []
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.serviceTypeId
  form.value = {
    typeName: row.typeName || '',
    description: row.description || '',
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
      await updateServiceType(currentId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createServiceType(form.value)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadServiceTypes()
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
    typeName: '',
    description: '',
    basePrice: 0,
    minDuration: 1
  }
  formRef.value?.clearValidate()
}

onMounted(() => {
  loadServiceTypes()
})
</script>

<style scoped lang="scss">
.service-type-management-view {
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
</style>
