<template>
  <div class="annual-package-view">
    <Navigation />
    <div class="content">
      <!-- 面包屑导航 -->
      <div class="breadcrumb-section">
        <el-breadcrumb separator=">">
          <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/housekeepers' }">产品服务</el-breadcrumb-item>
          <el-breadcrumb-item>专业版星级包年服务</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 服务详情主体 -->
      <div class="detail-main">
        <el-row :gutter="30">
          <!-- 左侧：服务图片 -->
          <el-col :xs="24" :sm="24" :md="10" :lg="10">
            <div class="service-image-section">
              <el-image
                :src="serviceInfo.image"
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
                <el-icon class="title-icon"><Star /></el-icon>
                <span>{{ serviceInfo.title }}</span>
              </div>
              
              <div class="service-meta">
                <span class="service-source">自营保洁师</span>
                <el-divider direction="vertical" />
                <span class="service-count">
                  <el-icon><StarFilled /></el-icon>
                  已服务 {{ serviceInfo.servedCount }} 家庭
                </span>
              </div>

              <div class="service-description">
                <h4>适用范围：</h4>
                <p>{{ serviceInfo.applicableScope }}</p>
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
                  <el-descriptions-item label="服务类型">
                    专业版星级包年服务
                  </el-descriptions-item>
                  <el-descriptions-item label="参考价格">
                    {{ serviceInfo.price }}
                  </el-descriptions-item>
                  <el-descriptions-item label="服务频率">
                    两周一次，全年定制管家式服务
                  </el-descriptions-item>
                  <el-descriptions-item label="服务时长">
                    每次服务 4 小时
                  </el-descriptions-item>
                  <el-descriptions-item label="服务次数">
                    全年 28 次服务
                  </el-descriptions-item>
                </el-descriptions>
              </div>

              <div class="service-content">
                <h3>服务内容</h3>
                <div v-html="serviceInfo.content"></div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="会员优势" name="advantages">
            <div class="tab-content">
              <div class="advantages-list">
                <div 
                  v-for="(advantage, index) in serviceInfo.advantages" 
                  :key="index"
                  class="advantage-item"
                >
                  <div class="advantage-icon-wrapper">
                    <el-icon class="advantage-icon"><component :is="advantage.icon" /></el-icon>
                  </div>
                  <div class="advantage-content">
                    <h4>{{ advantage.title }}</h4>
                    <p>{{ advantage.description }}</p>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="服务保障" name="guarantee">
            <div class="tab-content">
              <div class="guarantee-list">
                <div 
                  v-for="(guarantee, index) in serviceInfo.guarantees" 
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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  EditPen,
  Picture,
  StarFilled,
  Star,
  CircleCheckFilled,
  Money,
  Clock,
  User,
  Lock,
  Box,
  Setting
} from '@element-plus/icons-vue'
import Navigation from '@/components/common/Navigation.vue'

const router = useRouter()

const activeTab = ref('details')

// 专业版星级包年服务信息
const serviceInfo = computed(() => ({
  title: '专业版星级包年·乐享生活4H·28次',
  servedCount: '32600',
  image: '/images/包年保洁.png',
  price: '¥7288起',
  applicableScope: '更适合上班族、新婚家庭、中小户型的星级包年套餐。建议服务频率为全年两周1次或半年一周1次。专业版星级包年服务为您提供全年定制管家式服务，让您的生活更轻松、更舒适。',
  content: `
    <h4>服务内容包含：</h4>
    <ul>
      <li><strong>全年服务：</strong>28次专业保洁服务，每次4小时，覆盖全年52周</li>
      <li><strong>服务频率：</strong>两周一次，全年定制管家式服务，让您的生活更轻松</li>
      <li><strong>服务范围：</strong>包含8大区域保洁，40多项服务内容</li>
      <li><strong>专属服务：</strong>固定服务人员，熟悉您的家庭环境，提供个性化服务</li>
      <li><strong>灵活调整：</strong>可根据您的需求调整服务时间和内容</li>
      <li><strong>优先保障：</strong>会员优先预约，节假日服务保障</li>
    </ul>
  `,
  advantages: [
    {
      icon: Money,
      title: '超值优惠',
      description: '包年服务享受超值价格优惠，相比单次服务节省30%以上费用，让您用更少的钱享受更多服务。'
    },
    {
      icon: Clock,
      title: '时间自由',
      description: '全年28次服务，灵活安排服务时间，无需每次预约，省时省力，让您的生活更轻松。'
    },
    {
      icon: User,
      title: '专属服务',
      description: '固定服务人员，熟悉您的家庭环境和需求，提供个性化、专业化的管家式服务。'
    },
    {
      icon: Lock,
      title: '品质保障',
      description: '专业团队服务，统一服务标准，质量有保障。服务不满意可申请重做，确保服务质量。'
    },
    {
      icon: Box,
      title: '会员特权',
      description: '享受会员专属优惠，优先预约服务，节假日服务保障，专属客服支持等多项特权。'
    },
    {
      icon: Setting,
      title: '贴心服务',
      description: '全年管家式服务，定期回访，及时响应您的需求，让您享受无忧的家政服务体验。'
    }
  ],
  guarantees: [
    {
      title: '专业认证',
      description: '所有服务人员均经过专业培训和认证，持证上岗，服务标准统一'
    },
    {
      title: '质量保证',
      description: '服务不满意可申请重做，确保服务质量达到标准，让您满意为止'
    },
    {
      title: '保险保障',
      description: '提供意外险保障，服务过程中出现意外有保障，让您更放心'
    },
    {
      title: '售后服务',
      description: '服务完成后提供售后支持，及时解决后续问题，让您享受无忧服务'
    },
    {
      title: '会员权益',
      description: '享受会员专属优惠和特权，优先预约，节假日服务保障，专属客服支持'
    }
  ]
}))

// 处理预约
const handleAppointment = () => {
  // 跳转到预约页面，传递服务名称
  router.push({
    path: '/appointment',
    query: {
      serviceName: '专业版星级包年·乐享生活4H'
    }
  })
}
</script>

<style scoped lang="scss">
.annual-package-view {
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
      color: #ff9800;
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

.advantages-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30px;

  .advantage-item {
    display: flex;
    align-items: flex-start;
    gap: 20px;
    padding: 25px;
    background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
    border-radius: 12px;
    border-left: 4px solid #4CAF50;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 4px 12px rgba(76, 175, 80, 0.2);
    }

    .advantage-icon-wrapper {
      flex-shrink: 0;
      width: 60px;
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
      border-radius: 12px;

      .advantage-icon {
        color: #fff;
        font-size: 28px;
      }
    }

    .advantage-content {
      flex: 1;

      h4 {
        font-size: 20px;
        color: #333;
        margin-bottom: 10px;
        font-weight: 600;
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

  .advantages-list {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}
</style>
