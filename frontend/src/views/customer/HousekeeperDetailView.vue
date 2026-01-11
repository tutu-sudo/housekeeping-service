<template>
  <div class="housekeeper-detail-view">
    <Navigation />
    <div class="content" v-if="housekeeper">
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
            
            <!-- 个人简介 -->
            <div class="detail-section">
              <h3>个人简介</h3>
              <p>
                {{ housekeeper.resume || '该家政服务人员暂未填写个人简介。' }}
              </p>
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
            
            <el-button
              type="primary"
              size="large"
              @click="goToAppointment"
              style="width: 100%; margin-top: 20px;"
            >
              立即预约该服务人员
            </el-button>
          </el-card>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getReviewsByStaff } from '@/api/reviews'
import Navigation from '@/components/common/Navigation.vue'
import ReviewComponent from '@/components/business/ReviewComponent.vue'

const router = useRouter()
const route = useRoute()

const housekeeper = ref(null)
const reviews = ref([])
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

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
  router.push({
    path: '/appointment',
    query: { staffId }
  })
}

onMounted(async () => {
  const staffId = route.params.id

  // 优先从路由 query 中读取传过来的完整 staff 信息，减少一次请求
  const staffStr = route.query.staff
  if (staffStr) {
    try {
      const parsed = JSON.parse(decodeURIComponent(staffStr))
      housekeeper.value = parsed
    } catch (e) {
      console.error('解析家政人员数据失败:', e)
    }
  } else {
    // 如果没有携带完整数据，这里预留调用后端“根据 staffId 获取详细资料”的接口
    // TODO: 调用后端接口，例如 getStaffDetail(staffId)
  housekeeper.value = {
      id: staffId
    }
  }

  // 获取评价列表
  try {
    const response = await getReviewsByStaff(staffId)
    const data = response.data?.data || response.data || []
    reviews.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('获取评价失败:', error)
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
</style>

