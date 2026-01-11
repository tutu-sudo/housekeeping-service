<template>
  <el-card class="filter-card">
    <template #header>
      <h3>筛选条件</h3>
    </template>
    <el-form :model="filters" label-width="100px" size="default">
      <el-form-item label="性别">
        <el-select v-model="filters.gender" placeholder="请选择" clearable style="width: 100%">
          <el-option label="男" :value="1" />
          <el-option label="女" :value="0" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="年龄范围">
        <el-row :gutter="10">
          <el-col :span="11">
            <el-input-number
              v-model="filters.minAge"
              :min="18"
              :max="70"
              placeholder="最小"
              style="width: 100%"
            />
          </el-col>
          <el-col :span="2" style="text-align: center; line-height: 32px;">-</el-col>
          <el-col :span="11">
            <el-input-number
              v-model="filters.maxAge"
              :min="18"
              :max="70"
              placeholder="最大"
              style="width: 100%"
            />
          </el-col>
        </el-row>
      </el-form-item>
      
      <el-form-item label="籍贯">
        <el-input 
          v-model="filters.origin" 
          placeholder="请输入籍贯（支持模糊查询，留空表示不限）" 
          clearable
        />
        <div style="font-size: 12px; color: #999; margin-top: 5px;">
          提示：留空表示不限籍贯
        </div>
      </el-form-item>
      
      <el-form-item label="服务类型">
        <el-select 
          v-model="filters.serviceId" 
          placeholder="请选择服务" 
          clearable
          style="width: 100%"
        >
          <el-option
            v-for="service in availableServices"
            :key="service.id || service.serviceId || service.service_id"
            :label="service.name || service.serviceName || service.service_name"
            :value="service.id || service.serviceId || service.service_id"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="工作经验">
        <el-select 
          v-model="filters.workExperienceOption" 
          placeholder="请选择" 
          clearable
          style="width: 100%"
          @change="handleWorkExperienceChange"
        >
          <el-option label="不限" :value="null" />
          <el-option label="1年以上" :value="1" />
          <el-option label="3年以上" :value="3" />
          <el-option label="5年以上" :value="5" />
          <el-option label="10年以上" :value="10" />
          <el-option label="自定义" :value="'custom'" />
        </el-select>
        <el-input-number
          v-if="filters.workExperienceOption === 'custom'"
          v-model="filters.minWorkExperience"
          :min="0"
          :max="50"
          placeholder="最低年限"
          style="width: 100%; margin-top: 5px;"
        />
        <el-input-number
          v-else-if="filters.workExperienceOption !== null && filters.workExperienceOption !== ''"
          :model-value="filters.workExperienceOption"
          disabled
          style="width: 100%; margin-top: 5px;"
        />
      </el-form-item>
      
      <el-form-item label="综合评分">
        <el-select 
          v-model="filters.ratingOption" 
          placeholder="请选择" 
          clearable
          style="width: 100%"
          @change="handleRatingChange"
        >
          <el-option label="不限" :value="null" />
          <el-option label="4.0分以上" :value="4.0" />
          <el-option label="4.5分以上" :value="4.5" />
          <el-option label="4.8分以上" :value="4.8" />
          <el-option label="自定义" :value="'custom'" />
        </el-select>
        <el-slider 
          v-if="filters.ratingOption === 'custom'"
          v-model="filters.minRating" 
          :min="0" 
          :max="5" 
          :step="0.1"
          show-input
          :format-tooltip="formatRating"
          style="margin-top: 10px;"
        />
        <div v-if="filters.ratingOption === 'custom'" style="font-size: 12px; color: #999; margin-top: 5px;">
          最低评分：{{ filters.minRating.toFixed(1) }} 分
        </div>
        <div v-else-if="filters.ratingOption !== null && filters.ratingOption !== ''" style="font-size: 12px; color: #999; margin-top: 5px;">
          最低评分：{{ filters.ratingOption.toFixed(1) }} 分
        </div>
      </el-form-item>
      
      <el-form-item label="姓名搜索">
        <el-input 
          v-model="filters.nameKeyword" 
          placeholder="请输入姓名（支持模糊查询）" 
          clearable
        />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="applyFilters" style="width: 100%">
          应用筛选
        </el-button>
        <el-button @click="resetFilters" style="width: 100%; margin-top: 10px;">
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { getAvailableServices } from '@/api/services'

const emit = defineEmits(['filter-change'])

const filters = ref({
  gender: null,
  minAge: null,
  maxAge: null,
  origin: '', // 籍贯：空字符串表示"不限"
  serviceId: null, // 改为直接使用serviceId
  workExperienceOption: null, // 工作经验选项：null表示"不限"
  minWorkExperience: 0, // 自定义工作经验
  ratingOption: null, // 评分选项：null表示"不限"
  minRating: 0, // 自定义评分
  nameKeyword: ''
})

const availableServices = ref([]) // 可用服务列表

// 格式化评分显示
const formatRating = (val) => {
  return `${val.toFixed(1)} 分`
}

// 处理工作经验选项变化
const handleWorkExperienceChange = (value) => {
  if (value === null || value === '') {
    filters.value.minWorkExperience = 0
  } else if (value !== 'custom') {
    filters.value.minWorkExperience = value
  }
}

// 处理评分选项变化
const handleRatingChange = (value) => {
  if (value === null || value === '') {
    filters.value.minRating = 0
  } else if (value !== 'custom') {
    filters.value.minRating = value
  }
}

// 加载可用服务列表
const loadAvailableServices = async () => {
  try {
    const response = await getAvailableServices()
    const raw = response?.data?.data
    const rawServices = Array.isArray(raw) ? raw : []

    availableServices.value = rawServices.map(service => ({
      id: service.serviceId,
      name: service.serviceName,
      ...service
    }))
  } catch (error) {
    availableServices.value = []
  }
}

const applyFilters = () => {
  // 构建筛选条件对象，根据Swagger文档使用驼峰命名
  const filterParams = {}
  
  // 性别筛选
  if (filters.value.gender !== null && filters.value.gender !== '') {
    filterParams.gender = filters.value.gender
  }
  
  // 年龄范围筛选（如果后端支持，传递；否则前端过滤）
  if (filters.value.minAge) {
    filterParams.minAge = filters.value.minAge
  }
  
  if (filters.value.maxAge) {
    filterParams.maxAge = filters.value.maxAge
  }
  
  // 籍贯筛选（空字符串表示"不限"，不传递该参数）
  if (filters.value.origin && filters.value.origin.trim()) {
    filterParams.origin = filters.value.origin.trim()
  }
  
  // 服务类型筛选（根据Swagger文档，使用serviceId）
  if (filters.value.serviceId) {
    filterParams.serviceId = filters.value.serviceId
  }
  
  // 工作经验筛选（如果选择了"不限"，不传递该参数）
  if (filters.value.workExperienceOption !== null && filters.value.workExperienceOption !== '') {
    if (filters.value.workExperienceOption === 'custom') {
      if (filters.value.minWorkExperience > 0) {
        filterParams.minWorkExperience = filters.value.minWorkExperience
      }
    } else {
      filterParams.minWorkExperience = filters.value.workExperienceOption
    }
  }
  
  // 评分筛选（如果选择了"不限"，不传递该参数）
  if (filters.value.ratingOption !== null && filters.value.ratingOption !== '') {
    if (filters.value.ratingOption === 'custom') {
      if (filters.value.minRating > 0) {
        filterParams.minRating = filters.value.minRating
      }
    } else {
      filterParams.minRating = filters.value.ratingOption
    }
  }
  
  if (filters.value.nameKeyword && filters.value.nameKeyword.trim()) {
    filterParams.nameKeyword = filters.value.nameKeyword.trim()
  }
  
  emit('filter-change', filterParams)
}

const resetFilters = () => {
  filters.value = {
    gender: null,
    minAge: null,
    maxAge: null,
    origin: '',
    serviceId: null,
    workExperienceOption: null,
    minWorkExperience: 0,
    ratingOption: null,
    minRating: 0,
    nameKeyword: ''
  }
  emit('filter-change', {})
}

onMounted(() => {
  loadAvailableServices()
})
</script>

<style scoped lang="scss">
.filter-card {
  h3 {
    margin: 0;
    font-size: 16px;
  }
}
</style>

