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
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { View } from '@element-plus/icons-vue'
import { getAvailableServices } from '@/api/services'

const router = useRouter()

const STATIC_PROMO_PRODUCT_ID = 4

// 前3个卡片会和后端服务数据同步，第4个宣传卡保持静态不变
const products = ref([
  {
    id: 1,
    title: '日常保洁',
    description: '自营保洁师,日常保洁含专业擦玻璃。',
    price: '待确认',
    unit: '/小时',
    image: '/images/daily-cleaning.png',
    bookedCount: 506733,
    serviceId: null
  },
  {
    id: 2,
    title: '管道疏通',
    description: '统一定价 价格优惠 安全便捷专业师傅上门维修',
    price: '待确认',
    unit: '/小时',
    image: '/images/疏通管道.png',
    bookedCount: 325378,
    serviceId: null
  },
  {
    id: 3,
    title: '健康辅助',
    description: '适用于健康照料、洗衣煲汤做饭',
    price: '待确认',
    unit: '/小时',
    image: '/images/养老陪护.png',
    bookedCount: 168794,
    serviceId: null
  },
  {
    id: STATIC_PROMO_PRODUCT_ID,
    title: '专业版星级包年·乐享生活4H...',
    description: '两周一次,全年定制管家式服务',
    price: '7288起',
    unit: '',
    // ⚠️ 第4个主推产品图片 - 包年服务
    image: '/images/包年保洁.png',
    bookedCount: 112625
  }
])

const featuredServiceTargets = [
  { productId: 1, expectedNames: ['日常保洁'] },
  { productId: 2, expectedNames: ['管道疏通'] },
  { productId: 3, expectedNames: ['健康辅助'] }
]

const normalizeName = (name) => String(name || '').trim().toLowerCase()

const toNumberPrice = (value) => {
  if (value === null || value === undefined || value === '') return null
  if (typeof value === 'number' && Number.isFinite(value)) return value
  const parsed = Number(String(value).replace(/[^\d.]/g, ''))
  return Number.isFinite(parsed) ? parsed : null
}

const findMatchedService = (services, expectedNames = []) => {
  if (!Array.isArray(services) || services.length === 0) return null
  const normalizedExpected = expectedNames.map(normalizeName).filter(Boolean)
  if (normalizedExpected.length === 0) return null

  const exact = services.find((service) => {
    const serviceName = normalizeName(service.serviceName || service.service_name)
    return normalizedExpected.includes(serviceName)
  })
  if (exact) return exact

  return services.find((service) => {
    const serviceName = normalizeName(service.serviceName || service.service_name)
    return normalizedExpected.some((name) => serviceName.includes(name) || name.includes(serviceName))
  }) || null
}

const syncFeaturedProductsWithBackend = async () => {
  try {
    const response = await getAvailableServices()
    const serviceList = response?.data?.data || response?.data || []
    if (!Array.isArray(serviceList) || serviceList.length === 0) return

    featuredServiceTargets.forEach((target) => {
      const matched = findMatchedService(serviceList, target.expectedNames)
      if (!matched) return

      const product = products.value.find((item) => item.id === target.productId)
      if (!product) return

      const rawPrice = matched.price ?? matched.servicePrice ?? matched.hourlyRate
      const parsedPrice = toNumberPrice(rawPrice)

      product.title = matched.serviceName || matched.service_name || product.title
      product.description = matched.description || product.description
      product.serviceId = matched.serviceId || matched.service_id || null
      product.price = parsedPrice === null ? '待确认' : String(parsedPrice)
      product.unit = '/小时'
    })
  } catch (error) {
    console.error('sync featured products failed:', error)
  }
}

const handleBook = (product) => {
  try {
    // 专业版星级包年服务跳转到专门的详情页
    if (product.id === STATIC_PROMO_PRODUCT_ID) {
      router.push({
        path: '/service/annual-package'
      }).catch(err => {
        console.error('Navigation to annual package failed:', err)
        // 如果路由跳转失败，尝试使用 replace
        router.replace({
          path: '/service/annual-package'
        })
      })
      return
    }
    
    // 其他服务跳转到预约页面，优先传递真实 serviceId 与服务名称
    router.push({
      path: '/appointment',
      query: {
        serviceId: product.serviceId || '',
        serviceName: product.title
      }
    }).catch(err => {
      console.error('Navigation to appointment failed:', err)
    })
  } catch (error) {
    console.error('handleBook error:', error)
  }
}

const viewMore = () => {
  router.push('/housekeepers')
}

onMounted(() => {
  syncFeaturedProductsWithBackend()
})
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

