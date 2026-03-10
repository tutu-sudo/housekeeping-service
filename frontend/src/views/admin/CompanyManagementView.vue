<template>
  <div class="company-management-view">
    <Navigation />
    <div class="content">
      <el-container>
        <AdminSidebar />
        <el-main>
          <div class="page-header">
            <h2>公司信息管理</h2>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增公司
            </el-button>
          </div>
          
          <el-card>
            <el-table :data="companies" v-loading="loading" stripe>
              <el-table-column prop="companyId" label="ID" width="80" />
              <el-table-column prop="companyName" label="公司名称" />
              <el-table-column prop="contactPhone" label="联系电话" width="150" />
              <el-table-column prop="email" label="邮箱" width="200" />
              <el-table-column prop="address" label="地址" show-overflow-tooltip />
              <el-table-column prop="businessHours" label="营业时间" width="150" />
              <el-table-column prop="serviceCities" label="服务城市" width="200" show-overflow-tooltip />
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
            <div v-loading="loadingDetail">
              <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
              <el-form-item label="公司名称" prop="companyName">
                <el-input v-model="form.companyName" placeholder="请输入公司名称" />
              </el-form-item>
              <el-form-item label="联系电话" prop="contactPhone">
                <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item label="地址" prop="address">
                <el-input
                  v-model="form.address"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入公司地址"
                />
              </el-form-item>
              <el-form-item label="营业时间" prop="businessHours">
                <el-input v-model="form.businessHours" placeholder="如：08:00-20:00" />
              </el-form-item>
              <el-form-item label="服务城市" prop="serviceCities">
                <el-input
                  v-model="form.serviceCities"
                  type="textarea"
                  :rows="2"
                  placeholder="多个城市用逗号分隔，如：北京,上海,广州"
                />
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
import { Plus } from '@element-plus/icons-vue'
import {
  getCompanies,
  createCompany,
  updateCompany,
  deleteCompany,
  getCompanyDetail
} from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navigation from '@/components/common/Navigation.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

const companies = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const currentId = ref(null)
const loadingDetail = ref(false)

const form = ref({
  companyName: '',
  contactPhone: '',
  email: '',
  address: '',
  businessHours: '',
  serviceCities: ''
})

const rules = {
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑公司信息' : '新增公司'))

const loadCompanies = async () => {
  loading.value = true
  try {
    const response = await getCompanies()
    companies.value = response.data?.data || response.data || []
  } catch (error) {
    console.error('获取公司列表失败:', error)
    ElMessage.error('获取公司列表失败')
    companies.value = []
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

const handleEdit = async (row) => {
  isEdit.value = true
  currentId.value = row.companyId
  loadingDetail.value = true
  dialogVisible.value = true
  
  try {
    // 使用详情接口获取最新数据
    const response = await getCompanyDetail(row.companyId)
    const detail = response.data?.data || response.data
    form.value = {
      companyName: detail.companyName || '',
      contactPhone: detail.contactPhone || '',
      email: detail.email || '',
      address: detail.address || '',
      businessHours: detail.businessHours || '',
      serviceCities: detail.serviceCities || ''
    }
  } catch (error) {
    console.error('获取公司详情失败，使用列表数据:', error)
    // 如果获取详情失败，使用列表数据作为备用
    form.value = {
      companyName: row.companyName || '',
      contactPhone: row.contactPhone || '',
      email: row.email || '',
      address: row.address || '',
      businessHours: row.businessHours || '',
      serviceCities: row.serviceCities || ''
    }
  } finally {
    loadingDetail.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除公司"${row.companyName}"吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteCompany(row.companyId)
    ElMessage.success('删除成功')
    loadCompanies()
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
      await updateCompany(currentId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createCompany(form.value)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadCompanies()
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
    companyName: '',
    contactPhone: '',
    email: '',
    address: '',
    businessHours: '',
    serviceCities: ''
  }
  formRef.value?.clearValidate()
}

onMounted(() => {
  loadCompanies()
})
</script>

<style scoped lang="scss">
.company-management-view {
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
</style>
