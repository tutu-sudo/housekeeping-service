<template>
  <div class="housekeeper-detail-view">
    <Navigation />
    <div class="content" v-if="housekeeper || loading">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="housekeeper">
      <el-container>
        <el-main>
          <el-card>
            <!-- 头部基本信息 -->
            <div class="detail-header">
              <el-avatar
                :size="100"
                :src="housekeeper.avatar || housekeeper.avatar_url || defaultAvatar"
              />
              <div class="info">
                <h2>{{ housekeeper.name }}</h2>
                <p v-if="housekeeper.gender !== undefined">
                  性别：{{ housekeeper.gender === 1 ? '男' : '女' }}
                </p>
                <p v-if="age !== null">年龄：{{ age }} 岁</p>
                <p v-if="housekeeper.work_experience || housekeeper.workExperience">
                  工作经验：{{ housekeeper.work_experience || housekeeper.workExperience }} 年
                </p>
                <p v-if="housekeeper.origin">籍贯：{{ housekeeper.origin }}</p>
                <p v-if="housekeeper.rating">
                  综合评分：{{ Number(housekeeper.rating).toFixed(1) }} 分
                </p>
              </div>
            </div>
            
            <el-divider />
            
            <!-- 个人简介 + 工作经历 + 专业技能（来自服务人员端“电子简历”） -->
            <div class="detail-section intro-section">
              <h3>个人简介</h3>
              <p v-if="mainIntro">
                {{ mainIntro }}
              </p>
              <p v-else>
                该家政服务人员暂未填写个人简介。
              </p>

              <template v-if="workExperienceText">
                <h4>工作经历</h4>
                <p class="sub-text">
                  {{ workExperienceText }}
                </p>
              </template>

              <template v-if="professionalSkillsText">
                <h4>专业技能</h4>
                <p class="sub-text">
                  {{ professionalSkillsText }}
              </p>
              </template>
            </div>

            <el-divider />

            <!-- 服务技能 -->
            <div class="detail-section">
              <h3>服务技能</h3>
              <div v-if="skills.length">
                <el-timeline>
                  <el-timeline-item
                    v-for="skill in skills"
                    :key="skill.skillId || skill.skill_id || `${skill.staffId || skill.staff_id}-${skill.serviceId || skill.service_id}`"
                    :timestamp="formatProficiency(skill.proficiencyLevel || skill.proficiency_level)"
                  >
                    <div class="skill-item">
                      <div class="skill-main">
                        <span class="skill-name">
                          {{ skill.serviceName || skill.service_name || skill.service?.serviceName || '服务技能' }}
                        </span>
                        <span class="skill-price" v-if="skill.service?.price">
                          金额参考：¥{{ skill.service.price }} / 次或小时
                        </span>
                      </div>
                      <div class="skill-meta">
                        <span v-if="skill.experienceYears || skill.experience_years">
                          相关经验：{{ skill.experienceYears || skill.experience_years }} 年
                        </span>
                        <span v-if="skill.certificateUrl || skill.certificate_url">
                          ｜ 证书：
                          <a
                            :href="skill.certificateUrl || skill.certificate_url"
                            target="_blank"
                            rel="noopener"
                          >
                            查看证书
                          </a>
                        </span>
                      </div>
                    </div>
                  </el-timeline-item>
                </el-timeline>
              </div>
              <el-empty v-else description="暂未配置服务技能" />
            </div>
            
            <el-divider />
            
            <!-- 评价列表 -->
            <div class="reviews-section">
              <h3>服务评价</h3>
              <ReviewComponent
                v-for="review in reviews"
                :key="review.reviewId || review.id"
                :review="review"
              />
              <el-empty
                v-if="reviews.length === 0"
                description="暂时还没有评价"
                style="margin-top: 10px;"
              />
            </div>
            
            <div class="action-buttons">
              <el-button
                v-if="fromAppointment"
                type="default"
                size="large"
                @click="backToAppointment"
                style="width: 48%;"
              >
                返回预约
              </el-button>
            <el-button
              type="primary"
              size="large"
              @click="goToAppointment"
                :style="fromAppointment ? { width: '48%', marginLeft: '4%' } : { width: '100%' }"
            >
              立即预约该服务人员
            </el-button>
            </div>
          </el-card>
        </el-main>
      </el-container>
      </div>
      <div v-else class="error-container">
        <el-result
          icon="error"
          title="加载失败"
          sub-title="无法加载服务人员信息，请稍后重试"
        >
          <template #extra>
            <el-button type="primary" @click="$router.back()">返回</el-button>
          </template>
        </el-result>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getReviewsByStaff } from '@/api/reviews'
import { getStaffDetail, getStaffSkillsByStaffId } from '@/api/staff'
import Navigation from '@/components/common/Navigation.vue'
import ReviewComponent from '@/components/business/ReviewComponent.vue'

const router = useRouter()
const route = useRoute()

const housekeeper = ref(null)
const reviews = ref([])
const loading = ref(true)
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 是否是从预约页面跳转过来的，用于显示“返回预约”按钮
const fromAppointment = computed(() => route.query.from === 'appointment')

// 年龄根据出生日期计算
const age = computed(() => {
  const birth = housekeeper.value?.birth_date || housekeeper.value?.birthDate
  if (!birth) return null
  const birthDate = new Date(birth)
  if (Number.isNaN(birthDate.getTime())) return null
  const now = new Date()
  let years = now.getFullYear() - birthDate.getFullYear()
  const m = now.getMonth() - birthDate.getMonth()
  if (m < 0 || (m === 0 && now.getDate() < birthDate.getDate())) {
    years--
  }
  return years
})

// 统一的技能列表
const skills = computed(() => {
  if (!housekeeper.value) return []
  return housekeeper.value.serviceSkills || housekeeper.value.skills || []
})

// 个人简介/工作经历/专业技能文本（与服务人员端“电子简历”字段保持一致）
const mainIntro = computed(() => {
  const h = housekeeper.value || {}
  return h.bio || h.personalIntroduction || h.resume || ''
})

const workExperienceText = computed(() => {
  const h = housekeeper.value || {}
  return h.workExperienceText || h.work_experience_text || ''
})

const professionalSkillsText = computed(() => {
  const h = housekeeper.value || {}
  // 注意：skills 可能是数组（技能列表），需要检查类型
  // professionalSkills 才是专业技能文本
  if (typeof h.skills === 'string') {
    return h.skills
  }
  return h.professionalSkills || ''
})

const formatProficiency = (level) => {
  switch (level) {
    case 1:
      return '熟练程度：初级'
    case 2:
      return '熟练程度：中级'
    case 3:
      return '熟练程度：高级'
    default:
      return '熟练程度：未知'
  }
}

const goToAppointment = () => {
  if (!housekeeper.value) return
  const staffId = housekeeper.value.staffId || housekeeper.value.staff_id || housekeeper.value.id
  // 选择一个默认服务类型（优先取通过审核的技能，对应 serviceId）
  const firstSkill = skills.value[0] || {}
  const serviceId = firstSkill.serviceId || firstSkill.service_id

  const query = {
    staffId,
    from: 'housekeeper-detail' // 标记来源，用于区分是否自动填充
  }
  if (serviceId) {
    query.serviceId = serviceId
  }
  // 将当前家政人员信息一并传给预约页，便于预填充
  query.staff = encodeURIComponent(JSON.stringify(housekeeper.value))

  router.push({
    path: '/appointment',
    query
  })
}

// 从详情页返回预约页，恢复之前填写的内容
const backToAppointment = () => {
  router.push({
    path: '/appointment',
    query: {
      restore: '1'
    }
  })
}

onMounted(async () => {
  loading.value = true
  const staffId = route.params.id

  // 如果没有 staffId，显示错误
  if (!staffId) {
    console.error('缺少服务人员ID')
    loading.value = false
    // 确保 housekeeper.value 有值，这样模板才能渲染错误信息
    housekeeper.value = { id: null, staffId: null }
    return
  }

  // 初始化 housekeeper.value，确保始终是一个对象
  housekeeper.value = {
    id: staffId,
    staffId: staffId
  }

  // 优先从路由 query 中读取传过来的完整 staff 信息，减少一次请求
  const staffStr = route.query.staff
  if (staffStr) {
    try {
      const parsed = JSON.parse(decodeURIComponent(staffStr))
      if (parsed && typeof parsed === 'object') {
        housekeeper.value = {
          ...housekeeper.value,
          ...parsed,
          // 确保 id 和 staffId 都存在
          id: parsed.id || parsed.staffId || staffId,
          staffId: parsed.staffId || parsed.id || staffId
        }
        if (import.meta.env.DEV) {
          console.log('HousekeeperDetailView - 从路由 query 解析的数据:', housekeeper.value)
        }
      }
    } catch (e) {
      console.error('解析家政人员数据失败:', e)
    }
  }

  // 无论是否有 query 数据，都调用后端接口获取完整信息（包括简历、技能证书等）
  try {
    // 使用 Promise.all 并分别处理错误，确保即使一个失败也能继续
    const detailPromise = getStaffDetail(staffId).catch((error) => {
      console.warn('获取服务人员详情失败:', error)
      return null
    })
    
    const skillsPromise = getStaffSkillsByStaffId(staffId).catch((error) => {
      console.warn('获取服务人员技能列表失败:', error)
      return null
    })

    const [detailRes, skillsRes] = await Promise.all([detailPromise, skillsPromise])

    // 处理详情数据
    if (detailRes) {
      const detailData = detailRes.data?.data || detailRes.data
      
      // 调试日志（开发环境）- 先打印原始数据
      if (import.meta.env.DEV) {
        console.log('HousekeeperDetailView - 原始接口响应:', detailRes)
        console.log('HousekeeperDetailView - 提取的详情数据:', detailData)
        console.log('HousekeeperDetailView - detailData 类型:', typeof detailData)
        console.log('HousekeeperDetailView - detailData 是否为对象:', detailData && typeof detailData === 'object')
      }
      
      if (detailData && typeof detailData === 'object') {
        // 先保存专业技能文本（避免被技能数组覆盖）
        const professionalSkillsText = detailData.professionalSkills || 
          (typeof detailData.skills === 'string' ? detailData.skills : '') ||
          housekeeper.value?.professionalSkills || ''
        
        // 确保 id 和 staffId 都存在
        const finalId = detailData.id || detailData.staffId || housekeeper.value?.id || housekeeper.value?.staffId || staffId
        const finalStaffId = detailData.staffId || detailData.id || housekeeper.value?.staffId || housekeeper.value?.id || staffId
        
        housekeeper.value = {
          ...housekeeper.value,
          ...detailData,
          // 确保 id 和 staffId 都存在
          id: finalId,
          staffId: finalStaffId,
          // 确保简历相关字段被正确设置（后端返回 bio, workExperienceText, professionalSkills）
          // 同时支持兼容字段：personalIntroduction, resume, work_experience_text
          bio: detailData.bio || detailData.personalIntroduction || detailData.resume || housekeeper.value?.bio || '',
          workExperienceText: detailData.workExperienceText || detailData.work_experience_text || housekeeper.value?.workExperienceText || '',
          // 专业技能文本（注意：不要被技能数组覆盖）
          professionalSkills: professionalSkillsText,
          // 确保技能列表被正确设置（如果详情接口也返回了技能数组）
          // 注意：detailData.skills 可能是数组（技能列表）或字符串（专业技能文本），需要区分
          serviceSkills: Array.isArray(detailData.serviceSkills) ? detailData.serviceSkills : 
            (Array.isArray(detailData.skills) ? detailData.skills : housekeeper.value?.serviceSkills || [])
        }
        
        // 调试日志（开发环境）
        if (import.meta.env.DEV) {
          console.log('HousekeeperDetailView - 合并后的完整数据:', housekeeper.value)
          console.log('HousekeeperDetailView - 合并后的简历数据:', {
            bio: housekeeper.value.bio,
            workExperienceText: housekeeper.value.workExperienceText,
            professionalSkills: housekeeper.value.professionalSkills,
            hasBio: !!housekeeper.value.bio,
            hasWorkExperience: !!housekeeper.value.workExperienceText,
            hasProfessionalSkills: !!housekeeper.value.professionalSkills
          })
        }
      } else {
        // 如果 detailData 不是对象，记录警告
        if (import.meta.env.DEV) {
          console.warn('HousekeeperDetailView - detailData 不是有效对象:', detailData)
        }
      }
    } else {
      // 如果 detailRes 为空，记录警告
      if (import.meta.env.DEV) {
        console.warn('HousekeeperDetailView - detailRes 为空，接口可能失败')
      }
    }

    // 处理技能列表数据
    if (skillsRes) {
      const skillsData = Array.isArray(skillsRes.data?.data) 
        ? skillsRes.data.data 
        : (Array.isArray(skillsRes.data) ? skillsRes.data : [])
      
      // 后端返回的字段：skillId, staffId, serviceId, serviceName, proficiencyLevel, 
      // experienceYears, certificateUrl, status, service
      // status 字段对应数据库的 certificateStatus，值为1表示已审核通过
      // 后端已经过滤，这里不需要再次过滤
      if (skillsData.length > 0) {
        housekeeper.value = {
          ...housekeeper.value,
          serviceSkills: skillsData,
          // 注意：不要覆盖 professionalSkills 文本字段
          // skills 字段用于存储技能数组，但 professionalSkills 用于存储专业技能文本
        }
        
        // 调试日志（开发环境）
        if (import.meta.env.DEV) {
          console.log('HousekeeperDetailView - 获取到的技能数据:', skillsData)
          console.log('HousekeeperDetailView - 合并后的技能数据:', housekeeper.value.serviceSkills)
        }
      } else {
        // 确保即使没有技能，也设置空数组
        if (!housekeeper.value.serviceSkills) {
          housekeeper.value = {
            ...housekeeper.value,
            serviceSkills: [],
          }
        }
        if (import.meta.env.DEV) {
          console.log('HousekeeperDetailView - 技能数据为空或长度为0')
        }
      }
    }

    // 确保 housekeeper.value 始终有值，即使接口失败
    if (!housekeeper.value) {
      housekeeper.value = {
        id: staffId,
        staffId: staffId
    }
  } else {
      // 确保 id 和 staffId 都存在
      if (!housekeeper.value.id && !housekeeper.value.staffId) {
        housekeeper.value.id = staffId
        housekeeper.value.staffId = staffId
      } else if (!housekeeper.value.id) {
        housekeeper.value.id = housekeeper.value.staffId || staffId
      } else if (!housekeeper.value.staffId) {
        housekeeper.value.staffId = housekeeper.value.id || staffId
      }
    }
    
    // 调试日志（开发环境）
    if (import.meta.env.DEV) {
      console.log('HousekeeperDetailView - 最终 housekeeper.value:', housekeeper.value)
      console.log('HousekeeperDetailView - 简历字段检查:', {
        bio: housekeeper.value.bio,
        workExperienceText: housekeeper.value.workExperienceText,
        professionalSkills: housekeeper.value.professionalSkills,
        hasBio: !!housekeeper.value.bio,
        hasWorkExperience: !!housekeeper.value.workExperienceText,
        hasProfessionalSkills: !!housekeeper.value.professionalSkills
      })
    }
  } catch (error) {
    console.error('获取服务人员详细信息失败:', error)
    // 如果后端接口不存在，至少保证有基本数据
    if (!housekeeper.value) {
  housekeeper.value = {
        id: staffId,
        staffId: staffId
      }
    } else {
      // 确保 id 和 staffId 都存在
      if (!housekeeper.value.id && !housekeeper.value.staffId) {
        housekeeper.value.id = staffId
        housekeeper.value.staffId = staffId
      } else if (!housekeeper.value.id) {
        housekeeper.value.id = housekeeper.value.staffId || staffId
      } else if (!housekeeper.value.staffId) {
        housekeeper.value.staffId = housekeeper.value.id || staffId
      }
    }
  } finally {
    loading.value = false
  }

  // 获取评价列表（不阻塞页面显示）
  try {
    const response = await getReviewsByStaff(staffId)
    const data = response.data?.data || response.data || []
    reviews.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('获取评价失败:', error)
    reviews.value = []
  }
})
</script>

<style scoped lang="scss">
.housekeeper-detail-view {
  min-height: 100vh;
}

.content {
  padding: 20px;
}

.detail-header {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  
  .info {
    flex: 1;
    
    h2 {
      margin-bottom: 10px;
      color: #333;
    }
    
    p {
      margin: 5px 0;
      color: #666;
    }
  }
}

.detail-section {
  margin: 20px 0;
  
  h3 {
    margin: 20px 0 10px 0;
    color: #333;
  }
  
  p {
    color: #666;
    line-height: 1.6;
  }
}

.intro-section {
  padding: 10px 0 20px;

  h3 {
    font-size: 18px;
  }

  h4 {
    margin: 16px 0 6px;
    font-size: 15px;
    color: #333;
  }

  .sub-text {
    white-space: pre-wrap;
  }
}

.skill-item {
  .skill-main {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 4px;

    .skill-name {
      font-weight: 600;
      color: #333;
    }

    .skill-price {
      font-size: 13px;
      color: #ff6600;
    }
  }

  .skill-meta {
    font-size: 13px;
    color: #666;
  }
}

.reviews-section {
  margin-top: 20px;
  
  h3 {
    margin-bottom: 15px;
    color: #333;
  }
}

.action-buttons {
  margin-top: 24px;
  display: flex;
  justify-content: space-between;
}

.loading-container {
  padding: 40px 20px;
}

.error-container {
  padding: 40px 20px;
}
</style>

