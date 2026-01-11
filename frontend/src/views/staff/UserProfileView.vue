<template>
  <div class="user-profile-view">
    <Navigation />
    <div class="content">
      <el-container>
        <el-main>
          <el-card>
            <template #header>
              <h2>个人资料</h2>
            </template>
            <el-tabs v-model="activeTab">
              <!-- 基本信息 -->
              <el-tab-pane label="基本信息" name="basic">
                <el-form :model="profileForm" :rules="basicRules" ref="basicFormRef" label-width="120px">
                  <el-form-item label="头像">
                    <el-upload
                      :action="uploadUrl"
                      :headers="uploadHeaders"
                      :on-success="handleAvatarSuccess"
                      :before-upload="beforeAvatarUpload"
                      :show-file-list="false"
                      name="file"
                      :data="{ category: 'avatar' }"
                    >
                      <img v-if="profileForm.avatar" :src="profileForm.avatar" class="avatar" />
                      <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                    </el-upload>
                    <div style="font-size: 12px; color: #999; margin-top: 5px;">
                      点击头像区域上传新头像（支持JPG、PNG格式，大小不超过2MB）
                    </div>
                  </el-form-item>
                  
                  <el-form-item label="姓名" prop="name">
                    <el-input v-model="profileForm.name" placeholder="请输入姓名" />
                  </el-form-item>
                  
                  <el-form-item label="性别" prop="gender">
                    <el-radio-group v-model="profileForm.gender">
                      <el-radio :label="1">男</el-radio>
                      <el-radio :label="0">女</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  
                  <el-form-item label="出生日期" prop="birthDate">
                    <el-date-picker
                      v-model="profileForm.birthDate"
                      type="date"
                      placeholder="选择出生日期"
                      format="YYYY-MM-DD"
                      value-format="YYYY-MM-DD"
                      style="width: 100%"
                    />
                  </el-form-item>
                  
                  <el-form-item label="手机号" prop="phone">
                    <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
                  </el-form-item>
                  
                  <el-form-item label="邮箱" prop="email">
                    <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
                  </el-form-item>
                  
                  <el-form-item label="籍贯" prop="origin">
                    <el-input v-model="profileForm.origin" placeholder="请输入籍贯" />
                  </el-form-item>
                  
                  <el-form-item label="身份证号" prop="idCard">
                    <el-input v-model="profileForm.idCard" placeholder="请输入身份证号" maxlength="18" />
                  </el-form-item>
                  
                  <el-form-item label="工作经验" prop="workExperience">
                    <el-input-number 
                      v-model="profileForm.workExperience" 
                      :min="0" 
                      :max="50"
                      placeholder="年"
                    />
                    <span style="margin-left: 10px; color: #999;">年</span>
                  </el-form-item>
                  
                  <el-form-item>
                    <el-button type="primary" @click="saveBasicInfo" :loading="saving">
                      保存基本信息
                    </el-button>
                    <el-button @click="resetBasicForm">重置</el-button>
                  </el-form-item>
                </el-form>
              </el-tab-pane>
              
              <!-- 电子简历 -->
              <el-tab-pane label="电子简历" name="resume">
                <el-form :model="resumeForm" ref="resumeFormRef" label-width="120px">
                  <el-form-item label="个人简介">
                    <el-input
                      v-model="resumeForm.bio"
                      type="textarea"
                      :rows="4"
                      placeholder="请填写个人简介"
                      maxlength="500"
                      show-word-limit
                    />
                  </el-form-item>
                  
                  <el-form-item label="工作经历">
                    <el-input
                      v-model="resumeForm.workExperience"
                      type="textarea"
                      :rows="6"
                      placeholder="请填写工作经历，包括工作单位、职位、工作时间等"
                      maxlength="1000"
                      show-word-limit
                    />
                  </el-form-item>
                  
                  <el-form-item label="专业技能">
                    <el-input
                      v-model="resumeForm.skills"
                      type="textarea"
                      :rows="6"
                      placeholder="请填写专业技能，如：熟练掌握各类清洁工具的使用、具备良好的沟通能力等"
                      maxlength="1000"
                      show-word-limit
                    />
                  </el-form-item>
                  
                  <el-form-item>
                    <el-button type="primary" @click="saveResume" :loading="saving">
                      保存简历
                    </el-button>
                    <el-button @click="resetResumeForm">重置</el-button>
                  </el-form-item>
                </el-form>
              </el-tab-pane>
              
              <!-- 技能证书 -->
              <el-tab-pane label="技能证书" name="certificates">
                <div class="skills-section">
                  <div class="section-header">
                    <h3>服务技能</h3>
                    <el-button type="primary" @click="showAddSkillDialog = true">
                      <el-icon><Plus /></el-icon>
                      添加服务技能
                    </el-button>
                  </div>
                  
                  <el-table :data="skillsList" border style="margin-top: 20px;">
                    <el-table-column label="服务类型" width="150">
                      <template #default="scope">
                        {{ getServiceName(scope.row.serviceId) }}
                      </template>
                    </el-table-column>
                    <el-table-column label="熟练程度" width="120">
                      <template #default="scope">
                        <el-tag :type="getProficiencyType(scope.row.proficiencyLevel || scope.row.proficiency)">
                          {{ getProficiencyText(scope.row.proficiencyLevel || scope.row.proficiency) }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="experienceYears" label="经验年限" width="120">
                      <template #default="scope">
                        {{ scope.row.experienceYears || 0 }} 年
                      </template>
                    </el-table-column>
                    <el-table-column prop="certificateUrl" label="证书" width="200">
                      <template #default="scope">
                        <el-link 
                          v-if="scope.row.certificateUrl" 
                          :href="scope.row.certificateUrl" 
                          target="_blank"
                          type="primary"
                        >
                          查看证书
                        </el-link>
                        <span v-else style="color: #999;">未上传</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="150">
                      <template #default="scope">
                        <el-button size="small" @click="editSkill(scope.row)">编辑</el-button>
                        <el-button size="small" type="danger" @click="deleteSkill(scope.row)">
                          删除
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-main>
      </el-container>
    </div>
    
    <!-- 添加/编辑技能对话框 -->
    <el-dialog
      v-model="showAddSkillDialog"
      :title="editingSkill ? '编辑服务技能' : '添加服务技能'"
      width="600px"
    >
      <el-form :model="skillForm" :rules="skillRules" ref="skillFormRef" label-width="120px">
        <el-form-item label="服务类型" prop="serviceId">
          <el-select 
            v-model="skillForm.serviceId" 
            placeholder="请选择服务类型"
            style="width: 100%"
          >
            <el-option
              v-for="service in availableServices"
              :key="service.id"
              :label="service.name"
              :value="service.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="熟练程度" prop="proficiency">
          <el-select v-model="skillForm.proficiency" placeholder="请选择熟练程度" style="width: 100%">
            <el-option label="初级" value="初级" />
            <el-option label="中级" value="中级" />
            <el-option label="高级" value="高级" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="经验年限" prop="experienceYears">
          <el-input-number 
            v-model="skillForm.experienceYears" 
            :min="0" 
            :max="50"
            placeholder="年"
            style="width: 100%"
          />
          <span style="margin-left: 10px; color: #999;">年</span>
        </el-form-item>
        
        <el-form-item label="技能证书">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleCertificateSuccess"
            :before-upload="beforeCertificateUpload"
            :show-file-list="false"
            name="file"
            :data="{ category: 'certificate' }"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>
              上传证书（支持图片和PDF）
            </el-button>
          </el-upload>
          <div v-if="skillForm.certificateUrl" style="margin-top: 10px;">
            <el-link :href="skillForm.certificateUrl" target="_blank" type="primary">
              查看已上传证书
            </el-link>
            <el-button 
              type="danger" 
              size="small" 
              style="margin-left: 10px;"
              @click="skillForm.certificateUrl = ''"
            >
              删除
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddSkillDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSkill" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'
import { uploadFile } from '@/api/files'
import { getAvailableServices } from '@/api/services'
import { 
  getStaffProfile,
  updateStaffBasicInfo,
  updateStaffResume,
  updateStaffAvatar,
  getStaffSkills, 
  addStaffSkill, 
  updateStaffSkill, 
  deleteStaffSkill
} from '@/api/staff'
import Navigation from '@/components/common/Navigation.vue'

const store = useStore()

const activeTab = ref('basic')
const saving = ref(false)
const showAddSkillDialog = ref(false)
const editingSkill = ref(null)
const basicFormRef = ref(null)
const resumeFormRef = ref(null)
const skillFormRef = ref(null)

const profileForm = reactive({
  avatar: '',
  name: '',
  gender: 1,
  birthDate: '',
  phone: '',
  email: '',
  origin: '',
  idCard: '',
  workExperience: 0
})

const resumeForm = reactive({
  bio: '',
  workExperience: '',
  skills: ''
})

const skillForm = reactive({
  serviceId: '',
  proficiency: '初级',
  experienceYears: 0,
  certificateUrl: ''
})

const skillsList = ref([])
const availableServices = ref([])

const basicRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const skillRules = {
  serviceId: [{ required: true, message: '请选择服务类型', trigger: 'change' }],
  proficiency: [{ required: true, message: '请选择熟练程度', trigger: 'change' }],
  experienceYears: [{ required: true, message: '请输入经验年限', trigger: 'blur' }]
}

const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL || '/api'}/files/upload`)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

const staffId = computed(() => store.getters['user/userId'])

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const beforeCertificateUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isPdf = file.type === 'application/pdf'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage && !isPdf) {
    ElMessage.error('只能上传图片或PDF文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('文件大小不能超过 5MB!')
    return false
  }
  return true
}

const handleAvatarSuccess = async (response) => {
  if (response.code === 200 && response.data?.fileUrl) {
    const avatarUrl = response.data.fileUrl
    try {
      // 更新头像到后端
      await updateStaffAvatar(avatarUrl)
      profileForm.avatar = avatarUrl
    ElMessage.success('头像上传成功')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '头像更新失败')
    }
  } else {
    ElMessage.error('头像上传失败')
  }
}

const handleCertificateSuccess = (response) => {
  if (response.code === 200 && response.data?.fileUrl) {
    skillForm.certificateUrl = response.data.fileUrl
    ElMessage.success('证书上传成功')
  } else {
    ElMessage.error('证书上传失败')
  }
}

const saveBasicInfo = async () => {
  if (!basicFormRef.value) return
  
  await basicFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        await updateStaffBasicInfo(profileForm)
        ElMessage.success('基本信息保存成功')
        await loadStaffInfo()
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '保存失败')
      } finally {
        saving.value = false
      }
    }
  })
}

const saveResume = async () => {
  saving.value = true
  try {
    await updateStaffResume(staffId.value, resumeForm)
    ElMessage.success('简历保存成功')
    await loadStaffInfo()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const saveSkill = async () => {
  if (!skillFormRef.value) return
  
  await skillFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        // 转换熟练程度为数字：初级=1, 中级=2, 高级=3
        const proficiencyMap = { '初级': 1, '中级': 2, '高级': 3 }
        const skillData = {
          serviceId: skillForm.serviceId,
          proficiencyLevel: proficiencyMap[skillForm.proficiency] || 1,
          experienceYears: skillForm.experienceYears,
          certificateUrl: skillForm.certificateUrl || null
        }
        
        if (editingSkill.value) {
          await updateStaffSkill(editingSkill.value.skillId || editingSkill.value.id, skillData)
          ElMessage.success('技能更新成功')
        } else {
          await addStaffSkill(skillData)
          ElMessage.success('技能添加成功')
        }
        showAddSkillDialog.value = false
        resetSkillForm()
        await loadSkills()
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '保存失败')
      } finally {
        saving.value = false
      }
    }
  })
}

const editSkill = (skill) => {
  editingSkill.value = skill
  skillForm.serviceId = skill.serviceId
  // 转换熟练程度：1=初级, 2=中级, 3=高级
  const proficiencyMap = { 1: '初级', 2: '中级', 3: '高级' }
  skillForm.proficiency = proficiencyMap[skill.proficiencyLevel] || skill.proficiency || '初级'
  skillForm.experienceYears = skill.experienceYears || 0
  skillForm.certificateUrl = skill.certificateUrl || ''
  showAddSkillDialog.value = true
}

const deleteSkill = async (skill) => {
  try {
    await ElMessageBox.confirm('确定要删除此技能吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const skillId = skill.skillId || skill.id
    await deleteStaffSkill(skillId)
    ElMessage.success('删除成功')
    await loadSkills()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const resetBasicForm = () => {
  loadStaffInfo()
}

const resetResumeForm = () => {
  loadStaffInfo()
}

const resetSkillForm = () => {
  editingSkill.value = null
  skillForm.serviceId = ''
  skillForm.proficiency = '初级'
  skillForm.experienceYears = 0
  skillForm.certificateUrl = ''
}

const loadStaffInfo = async () => {
  if (!staffId.value) return
  
  try {
    const response = await getStaffProfile()
    const data = response.data?.data || response.data
    
    // 调试：打印获取到的数据
    if (import.meta.env.DEV) {
      console.log('UserProfileView - 获取服务人员个人资料响应:', response)
      console.log('UserProfileView - 解析后的数据:', data)
    }
    
    if (data) {
      // 处理基本信息
      const staffInfo = data.staff || data
      const userInfo = data.user || {}
      
      // 调试：打印staff信息
      if (import.meta.env.DEV) {
        console.log('UserProfileView - Staff信息:', staffInfo)
        console.log('UserProfileView - Staff姓名:', staffInfo.name)
        console.log('UserProfileView - Staff ID:', staffInfo.staffId || staffInfo.staff_id || staffInfo.id)
        console.log('UserProfileView - User ID:', userInfo.id || userInfo.userId)
        console.log('UserProfileView - 当前登录用户ID:', staffId.value)
      }
      
      // 调试：打印验证信息
      if (import.meta.env.DEV) {
        const staffUserId = staffInfo.userId || staffInfo.user_id
        const currentUserInfo = store.getters['user/userInfo']
        const currentUserId = currentUserInfo?.id || staffId.value
        console.log('UserProfileView - 验证信息:', {
          currentUserId: currentUserId,
          staffUserId: staffUserId,
          staffName: staffInfo.name
        })
      }
      
      Object.assign(profileForm, {
        avatar: userInfo.avatar || staffInfo.avatar || data.avatar || '',
        name: staffInfo.name || userInfo.name || userInfo.username || '',
        gender: staffInfo.gender !== undefined ? staffInfo.gender : (userInfo.gender !== undefined ? userInfo.gender : 1),
        birthDate: staffInfo.birthDate || staffInfo.birth_date || userInfo.birthDate || '',
        phone: userInfo.phone || staffInfo.phone || userInfo.phoneNumber || '',
        email: userInfo.email || staffInfo.email || '',
        origin: staffInfo.origin || '',
        idCard: staffInfo.idCard || staffInfo.id_card || '',
        workExperience: staffInfo.workExperience || staffInfo.work_experience || 0
      })
      
      // 处理简历信息
      Object.assign(resumeForm, {
        bio: staffInfo.bio || staffInfo.personalIntroduction || staffInfo.resume || '',
        workExperience: staffInfo.workExperienceText || staffInfo.work_experience_text || '',
        skills: staffInfo.skills || staffInfo.professionalSkills || ''
      })
      
      // 更新store中的userInfo（如果name不同）
      const currentUserInfo = store.getters['user/userInfo']
      if (currentUserInfo && staffInfo.name && currentUserInfo.name !== staffInfo.name) {
        store.dispatch('user/updateUserInfo', {
          ...currentUserInfo,
          name: staffInfo.name
        })
        if (import.meta.env.DEV) {
          console.log('UserProfileView - 已更新userInfo.name为:', staffInfo.name)
        }
      }
    }
  } catch (error) {
    console.error('加载个人资料失败:', error)
    // 如果接口不存在，使用用户信息
  const userInfo = store.getters['user/userInfo']
  if (userInfo) {
      Object.assign(profileForm, {
        avatar: userInfo.avatar || '',
        name: userInfo.name || userInfo.username || '',
        phone: userInfo.phone || '',
        email: userInfo.email || ''
      })
    }
    ElMessage.error(error.response?.data?.message || '加载个人资料失败')
  }
}

const loadSkills = async () => {
  if (!staffId.value) return
  
  try {
    const response = await getStaffSkills()
    skillsList.value = response.data?.data || response.data || []
  } catch (error) {
    console.error('加载技能列表失败:', error)
    skillsList.value = []
    // 不显示错误消息，因为可能是首次使用
  }
}

const loadServices = async () => {
  try {
    const response = await getAvailableServices()
    const raw = response?.data?.data || []
    availableServices.value = raw.map(service => ({
      id: service.serviceId || service.id,
      name: service.serviceName || service.name
    }))
  } catch (error) {
    availableServices.value = []
  }
}

// 获取服务名称
const getServiceName = (serviceId) => {
  const service = availableServices.value.find(s => s.id === serviceId)
  return service ? service.name : `服务ID: ${serviceId}`
}

// 获取熟练程度文本
const getProficiencyText = (proficiency) => {
  if (typeof proficiency === 'string') return proficiency
  const proficiencyMap = { 1: '初级', 2: '中级', 3: '高级' }
  return proficiencyMap[proficiency] || '初级'
}

// 获取熟练程度标签类型
const getProficiencyType = (proficiency) => {
  const text = getProficiencyText(proficiency)
  if (text === '高级') return 'success'
  if (text === '中级') return 'warning'
  return 'info'
}

// 更新store中的userInfo（包含staff.name）
const updateUserInfoInStore = async () => {
  try {
    const response = await getStaffProfile()
    const data = response.data?.data || response.data
    
    if (import.meta.env.DEV) {
      console.log('updateUserInfoInStore - 获取到的数据:', data)
    }
    
    if (data) {
      const staffInfo = data.staff || data
      const userInfo = data.user || {}
      
      // 调试：打印验证信息
      if (import.meta.env.DEV) {
        const staffUserId = staffInfo.userId || staffInfo.user_id
        const currentUserInfo = store.getters['user/userInfo']
        const currentUserId = currentUserInfo?.id || staffId.value
        console.log('updateUserInfoInStore - Staff信息:', staffInfo)
        console.log('updateUserInfoInStore - Staff姓名:', staffInfo.name)
        console.log('updateUserInfoInStore - Staff User ID:', staffUserId)
        console.log('updateUserInfoInStore - 当前用户ID:', currentUserId)
      }
      
      // 更新store中的userInfo，添加name字段
      if (currentUserInfo && staffInfo.name) {
        store.dispatch('user/updateUserInfo', {
          ...currentUserInfo,
          name: staffInfo.name
        })
        
        if (import.meta.env.DEV) {
          console.log('updateUserInfoInStore - 已更新userInfo.name为:', staffInfo.name)
        }
      }
    }
  } catch (error) {
    console.error('更新用户信息失败:', error)
  }
}

onMounted(async () => {
  await Promise.all([
    loadStaffInfo(),
    loadSkills(),
    loadServices()
  ])
  // 加载完个人资料后，更新store中的userInfo
  await updateUserInfoInStore()
})
</script>

<style scoped lang="scss">
.user-profile-view {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.content {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
  border: 2px solid #ddd;
  
  &:hover {
    border-color: #4CAF50;
  }
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  
  &:hover {
    border-color: #4CAF50;
  }
}

.skills-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
    }
  }
}
</style>
