<template>
  <div class="housekeeper-list-view">
    <Navigation />
    <div class="content">
      <h2>家政服务资料</h2>
      <p class="subtitle">这里展示平台上所有家政服务人员的基本信息和专业技能，您可以点击查看详细资料。</p>

      <el-row :gutter="20">
        <el-col
          v-for="staff in staffList"
          :key="staff.staff_id || staff.id"
          :span="6"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card class="staff-card" shadow="hover" @click="goToDetail(staff)">
            <div class="staff-card-content">
              <el-avatar
                :size="80"
                :src="staff.avatar || staff.avatar_url || defaultAvatar"
              />
              <div class="staff-name">{{ staff.name }}</div>
              <div class="staff-meta">
                <span v-if="staff.gender !== undefined">
                  性别：{{ staff.gender === 1 ? '男' : '女' }}
                </span>
                <span v-if="staff.work_experience || staff.workExperience">
                  ｜ 工作经验：{{ staff.work_experience || staff.workExperience }} 年
                </span>
              </div>
              <div class="staff-meta" v-if="staff.origin">
                籍贯：{{ staff.origin }}
              </div>
              <!-- 评分行固定占位：没有评分时也保留一行高度，避免卡片高度不一致 -->
              <div class="staff-meta staff-meta--rating" :class="{ 'is-placeholder': !hasRating(staff) }">
                <span v-if="hasRating(staff)">评分：{{ parseFloat(staff.rating).toFixed(1) }} 分</span>
                <span v-else>&nbsp;</span>
              </div>
              <!-- 列表卡片只展示一段“个人简介”，来源与详情页保持一致（bio / personalIntroduction / resume） -->
              <div class="staff-resume" :class="{ 'is-placeholder': !getMainIntro(staff) }">
                <span v-if="getMainIntro(staff)">{{ getMainIntro(staff) }}</span>
                <span v-else>&nbsp;</span>
              </div>
              <div class="card-actions">
                <el-button type="primary" plain size="small" @click.stop="goToDetail(staff)">
                  查看资料
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty
        v-if="!loading && staffList.length === 0"
        description="暂时没有家政服务人员数据，请稍后再试"
        style="margin-top: 40px;"
      />
    </div>
  </div>
</template>

<script setup>
import Navigation from '@/components/common/Navigation.vue'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { searchStaff } from '@/api/services'

const router = useRouter()
const staffList = ref([])
const loading = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const formatProficiency = (level) => {
  switch (level) {
    case 1:
      return '初级'
    case 2:
      return '中级'
    case 3:
      return '高级'
    default:
      return '未知'
  }
}

// 与详情页保持同一规则：优先使用 bio，其次 personalIntroduction，最后兼容旧字段 resume
const getMainIntro = (staff) => {
  if (!staff) return ''
  return staff.bio || staff.personalIntroduction || staff.resume || ''
}

const hasRating = (staff) => {
  const v = staff?.rating
  if (v === null || v === undefined || v === '') return false
  const n = Number(v)
  return Number.isFinite(n) && n > 0
}

const loadStaff = async () => {
  loading.value = true
  try {
    const response = await searchStaff({})
    let data = response.data?.list || response.data?.data || response.data || []
    if (!Array.isArray(data)) {
      data = []
    }
    staffList.value = data
  } catch (error) {
    console.error('获取家政服务人员列表失败:', error)
    ElMessage.error('获取家政服务人员列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goToDetail = (staff) => {
  const staffId = staff.staffId || staff.staff_id || staff.id
  if (!staffId) return
  router.push({
    path: `/housekeeper/${staffId}`,
    query: {
      // 将当前家政人员的基础信息一并传过去，减少二次请求
      staff: encodeURIComponent(JSON.stringify(staff))
    }
  })
}

onMounted(() => {
  loadStaff()
})
</script>

<style scoped lang="scss">
.housekeeper-list-view {
  min-height: 100vh;
}

.content {
  padding: 20px;
  
  h2 {
    margin-bottom: 10px;
    color: #333;
  }
  
  .subtitle {
    color: #666;
    margin-bottom: 20px;
    font-size: 14px;
  }
}

.staff-card {
  margin-bottom: 20px;
  cursor: pointer;

  .staff-card-content {
    text-align: center;
  }
}

.staff-name {
  font-size: 16px;
  font-weight: bold;
  margin-top: 10px;
  margin-bottom: 5px;
  color: #333;
}

.staff-meta {
  font-size: 13px;
  color: #666;
  margin-top: 2px;
}

.staff-resume {
  margin-top: 10px;
  font-size: 13px;
  color: #555;
  text-align: left;
  /* 限制为多行省略，保证每个卡片高度一致，更加美观 */
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.6;
  /* 固定占位高度：3 行文本 */
  min-height: calc(1.6em * 3);
}

.staff-meta--rating {
  /* 固定占位高度：1 行文本 */
  line-height: 1.6;
  min-height: calc(1.6em * 1);
}

.card-actions {
  margin-top: 12px;
  text-align: center;
}
</style>
