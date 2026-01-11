<!--
  轮播图组件 (Carousel.vue)
  功能：首页顶部轮播展示，用于展示促销活动、主要服务等
  
  ⚠️ 需要替换图片的位置：
  - carouselItems 数组中的 image 字段
  - 建议图片尺寸：1200x500 像素
  - 图片格式：JPG/PNG
  - 共3张轮播图片
-->
<template>
  <div class="carousel-container">
    <!-- Element Plus 轮播图组件：参照示例2，只展示一张完整大图 -->
    <el-carousel
      :interval="5000"              <!-- 自动切换间隔5秒 -->
      :loop="true"                  <!-- 循环播放，切回第一张 -->
      :autoplay="true"              <!-- 自动播放 -->
      height="520px"               <!-- 固定高度，保证比例接近示例2 -->
      indicator-position="outside" <!-- 指示器位置：外部 -->
      arrow="hover"                <!-- 显示左右切换箭头，可手动切换 -->
    >
      <!-- 遍历轮播项，生成多个轮播图片 -->
      <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
        <div class="carousel-item">
          <!-- ========== 这里是轮播图片 ========== -->
          <!-- ⚠️ 替换图片：将 item.image 改为您的图片URL -->
          <img :src="item.image" :alt="item.title" />
          
          <!-- 轮播图上的文字内容（标题、副标题、描述、按钮） -->
          <div class="carousel-content">
            <h2 class="carousel-title">{{ item.title }}</h2>
            <p class="carousel-subtitle">{{ item.subtitle }}</p>
            <p class="carousel-description">{{ item.description }}</p>
            
            <!-- 操作按钮 -->
            <el-button type="primary" size="large" class="carousel-button" @click="handleBook">
              立即预约
            </el-button>
            <el-button size="large" class="carousel-button-secondary" @click="goToServices">
              查看服务
            </el-button>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 跳转到预约页面
const handleBook = () => {
  router.push('/appointment')
}

// 跳转到服务页面
const goToServices = () => {
  router.push('/housekeepers')
}

// ========== 轮播图数据配置 ==========
// ⚠️ 这里是需要替换图片的地方！
// image 字段：替换为您的轮播图片URL
// 建议：每张图片尺寸 1200x500 像素，共3张
const carouselItems = ref([
  {
    // ⚠️ 第1张轮播图 - 替换此处的图片URL
    image: '/images/daily-cleaning.png',
    title: '家庭',
    subtitle: '开荒保洁',
    description: '有效清除胶迹 涂料点、污渍，让您享受新家的洁净与温馨'
  },
  {
    // ⚠️ 第2张轮播图 - 替换此处的图片URL
    image: '/images/elderly-care.png',
    title: '专业养老陪护',
    subtitle: '情感陪护',
    description: '专业陪护服务，让老人享受温暖陪护'
  },
  {
    // ⚠️ 第3张轮播图 - 替换此处的图片URL
    image: '/images/nanny-service.png',
    title: '母婴护理',
    subtitle: '专业月嫂育儿嫂',
    description: '专业护理，贴心服务，让您和宝宝安心舒适'
  }
])
</script>

<style scoped lang="scss">
.carousel-container {
  width: 100vw; /* 拉满视口宽度，避免左右留白 */
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  margin-bottom: 0;
  background: #fff;
  
  :deep(.el-carousel__container) {
    width: 100%;
    height: 520px;
  }
  
  :deep(.el-carousel__item) {
    border-radius: 0;
    overflow: hidden;
    display: flex;
    align-items: stretch;
  }
  
  :deep(.el-carousel__indicator) {
    button {
      background-color: rgba(255, 255, 255, 0.5);
    }
    
    &.is-active button {
      background-color: #4CAF50;
    }
  }
}

.carousel-item {
  position: relative;
  width: 100%;
  height: 100%;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .carousel-content {
    position: absolute;
    top: 50%;
    right: 10%;
    transform: translateY(-50%);
    color: #333;
    text-align: right;
    max-width: 500px;
    
    .carousel-title {
      font-size: 48px;
      font-weight: bold;
      color: #4CAF50;
      margin-bottom: 10px;
      line-height: 1.2;
    }
    
    .carousel-subtitle {
      font-size: 36px;
      font-weight: bold;
      color: #4CAF50;
      margin-bottom: 15px;
    }
    
    .carousel-description {
      font-size: 16px;
      color: #666;
      margin-bottom: 30px;
      line-height: 1.6;
    }
    
    .carousel-button {
      background-color: #4CAF50;
      border-color: #4CAF50;
      padding: 12px 40px;
      font-size: 16px;
      
      &:hover {
        background-color: #45a049;
        border-color: #45a049;
      }
    }
  }
}
</style>

