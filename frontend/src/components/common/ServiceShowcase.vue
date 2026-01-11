<!--
  服务展示组件 (ServiceShowcase.vue)
  功能：在首页展示6大类服务，每个服务类别显示一张图片和对应的服务项目
  
  ⚠️ 需要替换图片的位置：
  - services 数组中的每个服务对象的 image 字段
  - 建议图片尺寸：300x200 像素
  - 共6张图片（对应6大服务类别）
-->
<template>
  <div class="service-showcase">
    <!-- 区域标题 -->
    <div class="section-header">
      <span class="section-icon">S</span>
      <div class="header-text">
        <h2>我们的服务</h2>
        <p class="subtitle">Service show 用服务温暖社会</p>
      </div>
    </div>
    
    <!-- 服务卡片网格（3列布局，响应式） -->
    <div class="services-grid">
      <!-- 遍历服务数组，生成服务卡片 -->
      <div
        v-for="service in services"
        :key="service.id"
        class="service-card"
        @click="handleServiceClick(service)"
      >
        <!-- ========== 服务分类图片 ========== -->
        <!-- ⚠️ 这里是服务类别图片，替换 service.image 为您的图片URL -->
        <div class="service-image">
          <img :src="service.image" :alt="service.title" />
        </div>
        
        <!-- 服务内容信息 -->
        <div class="service-content">
          <!-- 服务标题（中英文） -->
          <h3 class="service-title">{{ service.title }}</h3>
          <p class="service-title-en">{{ service.titleEn }}</p>
          
          <!-- 该服务类别下的具体服务项目列表 -->
          <div class="service-items">
            <div
              v-for="item in service.items"
              :key="item.name"
              class="service-item"
            >
              <!-- 服务项图标 -->
              <el-icon class="item-icon">
                <component :is="iconMap[item.icon]" />
              </el-icon>
              <!-- 服务项名称 -->
              <span>{{ item.name }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  House,
  OfficeBuilding,
  Tools,
  Box,
  Clock,
  User,
  Brush,
  ShoppingBag,
  Setting,
  UserFilled
} from '@element-plus/icons-vue'

const router = useRouter()

// 图标映射
const iconMap = {
  House,
  OfficeBuilding,
  WashingMachine: Setting, // 使用Setting代替WashingMachine
  Sofa: ShoppingBag, // 使用ShoppingBag代替Sofa
  Tools,
  Box,
  Brush,
  Van: Tools, // 使用Tools代替Van
  Clock,
  BabyBottle: UserFilled, // 使用UserFilled代替BabyBottle
  FirstAidKit: Setting, // 使用Setting代替FirstAidKit（健康辅助）
  User
}

// ========== 服务展示数据配置 ==========
// ⚠️ 这里是需要替换图片的地方！
// image 字段：替换为您的服务分类图片URL
// 建议：每张图片尺寸 300x200 像素，共6张（对应6大服务类别）
const services = ref([
  {
    id: 1,
    title: '基础家务服务',
    titleEn: 'Basic Household Services',
    // ⚠️ 第1类服务图片 - 基础家务服务
    image: '/images/基础家政服务.png',
    items: [
      { name: '日常保洁', icon: 'House' },
      { name: '餐具处理', icon: 'Box' },
      { name: '管道疏通', icon: 'Sofa' }
    ]
  },
  {
    id: 2,
    title: '专业保洁与养护服务',
    titleEn: 'Professional Cleaning & Maintenance',
    // ⚠️ 第2类服务图片 - 专业保洁
    image: '/images/深度保洁.png',
    items: [
      { name: '衣物护理', icon: 'House' },
      { name: '家居养护', icon: 'Sofa' }
    ]
  },
  {
    id: 3,
    title: '母婴护理服务',
    titleEn: 'Maternal and Child Care',
    // ⚠️ 第3类服务图片 - 母婴护理
    image: '/images/母婴照顾.png',
    items: [
      { name: '月嫂服务', icon: 'BabyBottle' },
      { name: '育儿嫂服务', icon: 'BabyBottle' },
    ]
  },
  {
    id: 4,
    title: '养老护理服务',
    titleEn: 'Elderly Care Services',
    // ⚠️ 第4类服务图片 - 养老护理
    image: '/images/养老陪护.png',
    items: [
      { name: '生活照料', icon: 'User' },
      { name: '健康辅助', icon: 'FirstAidKit' },
    ]
  },
  {
    id: 5,
    title: '家电维修与维护服务',
    titleEn: 'Appliance Repair & Maintenance',
    // ⚠️ 第5类服务图片 - 家电维修
    image: '/images/家电维修.png',
    items: [
      { name: '家电维修', icon: 'Tools' },
      { name: '家电保养', icon: 'WashingMachine' },
      { name: '小型安装', icon: 'Tools' }
    ]
  },
  {
    id: 6,
    title: '特色专项服务',
    titleEn: 'Specialty Services',
    // ⚠️ 第6类服务图片 - 特色服务
    image: '/images/上门炒菜.png',
    items: [
      { name: '膳食服务', icon: 'Box' },
      { name: '宠物照料', icon: 'User' },
    ]
  }
])

const handleServiceClick = (service) => {
  router.push({
    path: '/housekeepers',
    query: { serviceTypeId: service.id }
  })
}
</script>

<style scoped lang="scss">
.service-showcase {
  padding: 60px 20px;
  background-color: #f5f5f5;
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 40px;
  
  .section-icon {
    font-size: 80px;
    font-weight: bold;
    color: #4CAF50;
    margin-right: 20px;
    line-height: 1;
  }
  
  .header-text {
    h2 {
      font-size: 32px;
      color: #333;
      margin: 0 0 5px 0;
    }
    
    .subtitle {
      font-size: 16px;
      color: #999;
      margin: 0;
    }
  }
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
  
  @media (max-width: 992px) {
    grid-template-columns: repeat(2, 1fr);
  }
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.service-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  }
  
  .service-image {
    width: 100%;
    height: 200px;
    overflow: hidden;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s;
    }
  }
  
  &:hover .service-image img {
    transform: scale(1.05);
  }
  
  .service-content {
    padding: 20px;
    
    .service-title {
      font-size: 24px;
      color: #333;
      margin: 0 0 5px 0;
      font-weight: bold;
    }
    
    .service-title-en {
      font-size: 14px;
      color: #999;
      margin: 0 0 15px 0;
      text-transform: uppercase;
    }
    
    .service-items {
      display: flex;
      flex-wrap: wrap;
      gap: 15px;
      
      .service-item {
        display: flex;
        align-items: center;
        gap: 5px;
        font-size: 14px;
        color: #666;
        
        .item-icon {
          font-size: 18px;
          color: #4CAF50;
        }
      }
    }
  }
}
</style>

