<template>
  <div class="service-detail-view">
    <Navigation />
    <div class="content">
      <!-- 面包屑导航 -->
      <div class="breadcrumb-section">
        <el-breadcrumb separator=">">
          <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/housekeepers' }">产品服务</el-breadcrumb-item>
          <el-breadcrumb-item>{{ serviceInfo?.name || '服务详情' }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 服务详情主体 -->
      <div class="detail-main">
        <el-row :gutter="30">
          <!-- 左侧：服务图片 -->
          <el-col :xs="24" :sm="24" :md="10" :lg="10">
            <div class="service-image-section">
              <el-image
                :src="serviceInfo?.image || serviceImagePlaceholder"
                fit="cover"
                class="service-image"
              >
                <template #placeholder>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                    <span>服务图片</span>
                  </div>
                </template>
              </el-image>
            </div>
          </el-col>

          <!-- 右侧：服务信息 -->
          <el-col :xs="24" :sm="24" :md="14" :lg="14">
            <div class="service-info-section">
              <div class="service-title">
                <el-icon class="title-icon" v-if="serviceInfo?.icon && iconMap[serviceInfo.icon]">
                  <component :is="iconMap[serviceInfo.icon]" />
                </el-icon>
                <span>{{ serviceInfo?.title || serviceInfo?.name }}</span>
              </div>
              
              <div class="service-meta">
                <span class="service-source">自营保洁师</span>
                <el-divider direction="vertical" />
                <span class="service-count">
                  <el-icon><StarFilled /></el-icon>
                  已服务 {{ serviceInfo?.servedCount || '36740' }} 家庭
                </span>
              </div>

              <div class="service-description">
                <h4>适用范围：</h4>
                <p>{{ serviceInfo?.applicableScope || serviceInfo?.description || '专业的家政服务，为您提供优质的服务体验。' }}</p>
              </div>

              <div class="service-actions">
                <el-button 
                  type="warning" 
                  size="large" 
                  :icon="EditPen"
                  @click="handleAppointment"
                  class="appointment-btn"
                >
                  立即预约
                </el-button>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 服务详情标签页 -->
      <div class="service-tabs-section">
        <el-tabs v-model="activeTab" class="service-tabs">
          <el-tab-pane label="服务详情" name="details">
            <div class="tab-content">
              <div class="pricing-section">
                <h3>收费标准</h3>
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="结算方式">
                    {{ getBillingTypeText(serviceInfo?.billingType) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="参考价格">
                    {{ serviceInfo?.price || '面议' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="服务时长" v-if="serviceInfo?.billingType === 'hourly'">
                    {{ serviceInfo?.duration || '4' }} 小时起
                  </el-descriptions-item>
                  <el-descriptions-item label="服务天数" v-if="serviceInfo?.billingType === 'daily'">
                    按天结算，不满一天按一天计算
                  </el-descriptions-item>
                  <el-descriptions-item label="服务次数" v-if="serviceInfo?.billingType === 'times'">
                    按次结算，一次多少钱
                  </el-descriptions-item>
                </el-descriptions>
              </div>

              <div class="service-content" v-if="serviceInfo?.content">
                <h3>服务内容</h3>
                <div v-html="serviceInfo.content"></div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="服务特色" name="features">
            <div class="tab-content">
              <div class="features-list">
                <div 
                  v-for="(feature, index) in serviceInfo?.features || defaultFeatures" 
                  :key="index"
                  class="feature-item"
                >
                  <el-icon class="feature-icon"><Check /></el-icon>
                  <span>{{ feature }}</span>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="服务保障" name="guarantee">
            <div class="tab-content">
              <div class="guarantee-list">
                <div 
                  v-for="(guarantee, index) in serviceInfo?.guarantees || defaultGuarantees" 
                  :key="index"
                  class="guarantee-item"
                >
                  <el-icon class="guarantee-icon"><CircleCheckFilled /></el-icon>
                  <div class="guarantee-content">
                    <h4>{{ guarantee.title }}</h4>
                    <p>{{ guarantee.description }}</p>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  EditPen,
  Picture,
  StarFilled,
  Check,
  CircleCheckFilled
} from '@element-plus/icons-vue'
import Navigation from '@/components/common/Navigation.vue'
import { serviceDetailsData } from '@/data/serviceDetails'
import {
  Brush,
  Tools,
  Link,
  Box,
  House,
  User,
  UserFilled,
  Reading,
  FirstAidKit,
  Setting,
  Monitor,
  Grid,
  Coffee,
  Location
} from '@element-plus/icons-vue'

// 图标映射
const iconMap = {
  Brush,
  Tools,
  Link,
  Box,
  House,
  User,
  UserFilled,
  Reading,
  FirstAidKit,
  Setting,
  Monitor,
  Grid,
  Coffee,
  Location
}

const route = useRoute()
const router = useRouter()

const activeTab = ref('details')
const serviceImagePlaceholder = '/images/service-placeholder.jpg'

// 获取服务名称（从路由参数）
const serviceName = computed(() => {
  return route.params.serviceName || route.query.serviceName || ''
})

// 获取服务详情
const serviceInfo = computed(() => {
  if (!serviceName.value) return null
  return serviceDetailsData[serviceName.value] || null
})

// 获取结算方式文本
const getBillingTypeText = (billingType) => {
  const typeMap = {
    'hourly': '按小时结算',
    'daily': '按天结算',
    'times': '按次数结算'
  }
  return typeMap[billingType] || '按小时结算'
}

// 默认特色
const defaultFeatures = [
  '专业团队，经验丰富',
  '服务标准，流程规范',
  '工具齐全，高效清洁',
  '售后保障，放心无忧'
]

// 默认保障
const defaultGuarantees = [
  {
    title: '专业认证',
    description: '所有服务人员均经过专业培训和认证，持证上岗'
  },
  {
    title: '质量保证',
    description: '服务不满意可申请重做，确保服务质量'
  },
  {
    title: '保险保障',
    description: '提供意外险保障，服务过程中出现意外有保障'
  },
  {
    title: '售后服务',
    description: '服务完成后提供售后支持，及时解决后续问题'
  }
]

// 处理预约
const handleAppointment = () => {
  if (!serviceName.value) {
    ElMessage.warning('服务信息不完整')
    return
  }

  // 跳转到预约页面，传递服务名称
  router.push({
    path: '/appointment',
    query: {
      serviceName: serviceName.value
    }
  })
}

onMounted(() => {
  if (!serviceName.value) {
    ElMessage.error('服务信息不存在')
    router.push('/housekeepers')
    return
  }

  if (!serviceInfo.value) {
    ElMessage.warning('该服务详情暂未完善')
  }
})
</script>

<style scoped lang="scss">
.service-detail-view {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

// 面包屑导航
.breadcrumb-section {
  padding: 20px 0;
  background-color: #fff;
  border-radius: 4px;
  padding-left: 20px;
  margin-bottom: 20px;

  :deep(.el-breadcrumb__item) {
    .el-breadcrumb__inner {
      color: #666;
      font-size: 14px;

      &.is-link {
        color: #4CAF50;
        font-weight: 500;

        &:hover {
          color: #45a049;
        }
      }
    }
  }
}

// 详情主体
.detail-main {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.service-image-section {
  .service-image {
    width: 100%;
    height: 400px;
    border-radius: 8px;
    overflow: hidden;

    :deep(.el-image__inner) {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .image-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background-color: #f5f7fa;
      color: #999;

      .el-icon {
        font-size: 48px;
        margin-bottom: 10px;
      }
    }
  }
}

.service-info-section {
  padding-left: 20px;

  .service-title {
    font-size: 28px;
    font-weight: bold;
    color: #333;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 10px;

    .title-icon {
      color: #4CAF50;
      font-size: 32px;
    }
  }

  .service-meta {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 25px;
    color: #666;
    font-size: 14px;

    .service-source {
      color: #4CAF50;
      font-weight: 500;
    }

    .service-count {
      display: flex;
      align-items: center;
      gap: 5px;
      color: #ff9800;

      .el-icon {
        color: #ff9800;
      }
    }
  }

  .service-description {
    margin-bottom: 30px;

    h4 {
      font-size: 16px;
      color: #333;
      margin-bottom: 10px;
    }

    p {
      font-size: 14px;
      color: #666;
      line-height: 1.8;
    }
  }

  .service-actions {
    .appointment-btn {
      width: 100%;
      height: 50px;
      font-size: 18px;
      font-weight: 500;
      background-color: #ff9800;
      border-color: #ff9800;

      &:hover {
        background-color: #f57c00;
        border-color: #f57c00;
      }
    }
  }
}

// 标签页区域
.service-tabs-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

  .service-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 30px;

      .el-tabs__nav-wrap::after {
        background-color: #e4e7ed;
      }

      .el-tabs__item {
        font-size: 16px;
        font-weight: 500;
        color: #666;

        &.is-active {
          color: #4CAF50;
        }

        &:hover {
          color: #4CAF50;
        }
      }

      .el-tabs__active-bar {
        background-color: #4CAF50;
      }
    }
  }
}

.tab-content {
  .pricing-section {
    margin-bottom: 30px;

    h3 {
      font-size: 20px;
      color: #333;
      margin-bottom: 20px;
      padding-bottom: 10px;
      border-bottom: 2px solid #4CAF50;
    }

    :deep(.el-descriptions) {
      .el-descriptions__label {
        font-weight: 500;
        color: #333;
        width: 150px;
      }

      .el-descriptions__content {
        color: #666;
      }
    }
  }

  .service-content {
    h3 {
      font-size: 20px;
      color: #333;
      margin-bottom: 20px;
      padding-bottom: 10px;
      border-bottom: 2px solid #4CAF50;
    }

    :deep(p) {
      font-size: 14px;
      color: #666;
      line-height: 1.8;
      margin-bottom: 15px;
    }

    :deep(ul) {
      padding-left: 20px;
      margin-bottom: 15px;

      li {
        font-size: 14px;
        color: #666;
        line-height: 1.8;
        margin-bottom: 10px;
      }
    }
  }
}

.features-list {
  .feature-item {
    display: flex;
    align-items: center;
    gap: 15px;
    padding: 15px;
    margin-bottom: 15px;
    background-color: #f9f9f9;
    border-radius: 8px;
    border-left: 4px solid #4CAF50;

    .feature-icon {
      color: #4CAF50;
      font-size: 20px;
    }

    span {
      font-size: 15px;
      color: #333;
    }
  }
}

.guarantee-list {
  .guarantee-item {
    display: flex;
    align-items: flex-start;
    gap: 15px;
    padding: 20px;
    margin-bottom: 20px;
    background-color: #f9f9f9;
    border-radius: 8px;

    .guarantee-icon {
      color: #4CAF50;
      font-size: 24px;
      margin-top: 5px;
    }

    .guarantee-content {
      flex: 1;

      h4 {
        font-size: 18px;
        color: #333;
        margin-bottom: 10px;
      }

      p {
        font-size: 14px;
        color: #666;
        line-height: 1.8;
        margin: 0;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .service-info-section {
    padding-left: 0;
    margin-top: 20px;
  }

  .service-image-section .service-image {
    height: 300px;
  }

  .detail-main {
    padding: 15px;
  }

  .service-tabs-section {
    padding: 15px;
  }
}
</style>
