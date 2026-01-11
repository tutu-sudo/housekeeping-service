<!--
  首页视图组件 (HomeView.vue)
  这是网站的主页面，包含以下几个主要部分：
  1. 顶部导航栏
  2. 轮播图（Carousel）- 需要替换图片
  3. 统计数据横幅
  4. 我们的服务展示（ServiceShowcase）- 需要替换图片
  5. 主推产品（FeaturedProducts）- 需要替换图片
  6. 底部信息（Footer）
-->
<template>
  <div class="home-view">
    <!-- 顶部导航栏 -->
    <Navigation />
    
    <!-- ========== 轮播图区域 ========== -->
    <!-- 
      位置：页面最上方，导航栏下方
      功能：展示促销活动、主要服务等
      需要替换图片：在 Carousel.vue 组件中，carouselItems 数组里的 image 字段
    -->
    <Carousel />
    
    <!-- 统计数据横幅（展示公司数据，如服务人数、订单数等） -->
    <StatisticsBanner />
    
    <!-- ========== 我们的服务展示区域 ========== -->
    <!-- 
      位置：统计数据下方
      功能：展示6大类服务（基础家务、专业保洁、母婴护理、养老护理、家电维修、特色服务）
      需要替换图片：在 ServiceShowcase.vue 组件中，services 数组里每个服务对象的 image 字段
      每个服务卡片都有一张图片，共6张
    -->
    <ServiceShowcase />
    
    <!-- ========== 主推产品区域 ========== -->
    <!-- 
      位置：服务展示下方
      功能：展示4个主推产品/套餐（如：专业日常保洁、管道疏通、包月小时工、包年服务等）
      需要替换图片：在 FeaturedProducts.vue 组件中，products 数组里每个产品的 image 字段
      每个产品卡片都有一张图片，共4张
    -->
    <FeaturedProducts />
    
    <!-- 页面底部（联系方式、二维码等） -->
    <Footer />
  </div>
</template>

<script setup>
// 导入各个组件
import Navigation from '@/components/common/Navigation.vue'          // 导航栏组件
import Carousel from '@/components/common/Carousel.vue'              // 轮播图组件（需要替换图片）
import StatisticsBanner from '@/components/common/StatisticsBanner.vue'  // 统计数据横幅
import ServiceShowcase from '@/components/common/ServiceShowcase.vue'    // 服务展示组件（需要替换图片）
import FeaturedProducts from '@/components/common/FeaturedProducts.vue'  // 主推产品组件（需要替换图片）
import Footer from '@/components/common/Footer.vue'                 // 底部组件
</script>

<style scoped lang="scss">
/* 首页容器样式 */
.home-view {
  min-height: 100vh;      /* 最小高度为视口高度 */
  background-color: #fff; /* 白色背景 */
  width: 100%;
  overflow-x: hidden;     /* 防止水平滚动 */
}
</style>

