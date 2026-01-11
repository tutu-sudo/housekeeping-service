<!--
  主推产品组件 (FeaturedProducts.vue)
  功能：在首页展示4个主推产品/套餐，每个产品显示一张图片、价格、描述等信息
  
  ⚠️ 需要替换图片的位置：
  - products 数组中的每个产品对象的 image 字段
  - 建议图片尺寸：300x200 像素
  - 共4张图片（对应4个主推产品）
-->
<template>
  <div class="featured-products">
    <!-- 区域标题和"查看更多"按钮 -->
    <div class="section-header">
      <div class="header-left">
        <span class="section-icon">S</span>
        <div class="header-text">
          <h2>主推产品</h2>
          <p class="subtitle">ervice products 品质服务伴您住进理想的家</p>
        </div>
      </div>
      <!-- 查看更多按钮 -->
      <el-button class="view-more-btn" @click="viewMore">
        <el-icon><View /></el-icon>
        查看更多
      </el-button>
    </div>
    
    <!-- 产品卡片网格（4列布局，响应式） -->
    <div class="products-grid">
      <!-- 遍历产品数组，生成产品卡片 -->
      <div
        v-for="product in products"
        :key="product.id"
        class="product-card"
        @click="handleBook(product)"
      >
        <!-- ========== 主推产品图片 ========== -->
        <!-- ⚠️ 这里是主推产品图片，替换 product.image 为您的图片URL -->
        <div class="product-image">
          <img :src="product.image" :alt="product.title" />
        </div>
        
        <!-- 产品内容信息 -->
        <div class="product-content">
          <!-- 产品标题 -->
          <h3 class="product-title">{{ product.title }}</h3>
          <!-- 产品描述 -->
          <p class="product-description">{{ product.description }}</p>
          
          <!-- 产品底部：价格和预约按钮 -->
          <div class="product-footer">
            <!-- 价格显示 -->
            <div class="product-price">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ product.price }}</span>
              <span class="price-unit">{{ product.unit }}</span>
            </div>
            <!-- 立即预约按钮 -->
            <el-button class="book-btn" @click.stop="handleBook(product)">
              立即预约
            </el-button>
          </div>
          
          <!-- 已预约人数 -->
          <div class="booked-count">
            已有{{ product.bookedCount }}人预定
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { View } from '@element-plus/icons-vue'

const router = useRouter()

// ========== 主推产品数据配置 ==========
// ⚠️ 这里是需要替换图片的地方！
// image 字段：替换为您的主推产品图片URL
// 建议：每张图片尺寸 300x200 像素，共4张（对应4个主推产品）
const products = ref([
  {
    id: 1,
    title: '日常保洁',
    description: '自营保洁师,日常保洁含专业擦玻璃。',
    price: '40',
    unit: '/时',
    // ⚠️ 第1个主推产品图片 - 专业日常保洁
    image: '/images/daily-cleaning.png',
    bookedCount: 506733  // 已预约人数
  },
  {
    id: 2,
    title: '管道疏通',
    description: '统一定价 价格优惠 安全便捷专业师傅上门维修',
    price: '150起',
    unit: '/次',
    // ⚠️ 第2个主推产品图片 - 管道疏通
    image: '/images/疏通管道.png',
    bookedCount: 325378
  },
  {
    id: 3,
    title: '健康辅助',
    description: '适用于健康照料、洗衣煲汤做饭',
    price: '2500起',
    unit: '/月',
    // ⚠️ 第3个主推产品图片 - 包月小时工
    image: '/images/养老陪护.png',
    bookedCount: 168794
  },
  {
    id: 4,
    title: '专业版星级包年·乐享生活4H...',
    description: '两周一次,全年定制管家式服务',
    price: '7288起',
    unit: '',
    // ⚠️ 第4个主推产品图片 - 包年服务
    image: '/images/包年保洁.png',
    bookedCount: 112625
  }
])

const handleBook = (product) => {
  router.push({
    path: '/appointment',
    query: { serviceId: product.id }
  })
}

const viewMore = () => {
  router.push('/housekeepers')
}
</script>

<style scoped lang="scss">
.featured-products {
  padding: 60px 20px;
  background-color: #fff;
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 40px;
  
  .header-left {
    display: flex;
    align-items: center;
    
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
  
  .view-more-btn {
    border-color: #ddd;
    color: #666;
    
    &:hover {
      border-color: #4CAF50;
      color: #4CAF50;
    }
  }
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  
  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, 1fr);
  }
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.product-card {
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
  
  .product-image {
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
  
  &:hover .product-image img {
    transform: scale(1.05);
  }
  
  .product-content {
    padding: 20px;
    
    .product-title {
      font-size: 18px;
      color: #333;
      margin: 0 0 10px 0;
      font-weight: bold;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    
    .product-description {
      font-size: 14px;
      color: #666;
      margin: 0 0 15px 0;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    
    .product-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
      
      .product-price {
        display: flex;
        align-items: baseline;
        color: #ff6600;
        
        .price-symbol {
          font-size: 16px;
        }
        
        .price-value {
          font-size: 24px;
          font-weight: bold;
          margin: 0 2px;
        }
        
        .price-unit {
          font-size: 14px;
        }
      }
      
      .book-btn {
        background-color: #f5f5f5;
        border-color: #ddd;
        color: #666;
        
        &:hover {
          background-color: #4CAF50;
          border-color: #4CAF50;
          color: white;
        }
      }
    }
    
    .booked-count {
      font-size: 12px;
      color: #999;
    }
  }
}
</style>

