<template>
  <div class="service-list-view">
    <Navigation />
    <div class="content">
      <!-- 顶部Banner占位图 -->
      <div class="banner-section">
        <el-image
          :src="bannerPlaceholder"
          fit="contain"
          class="banner-image"
        >
          <template #placeholder>
            <div class="image-placeholder">
              <el-icon><Picture /></el-icon>
              <span>Banner占位图</span>
            </div>
          </template>
        </el-image>
      </div>

      <!-- 服务类型导航 -->
      <div class="service-types-section">
        <el-row :gutter="20" class="service-types-grid">
          <el-col
            v-for="(service, index) in serviceTypes"
            :key="index"
            :xs="12"
            :sm="8"
            :md="6"
            :lg="4"
            :xl="3"
          >
            <div class="service-type-item" @click="scrollToService(service.name)">
              <div class="service-type-icon">
                <el-icon :size="40">
                  <component :is="iconMap[service.icon]" />
                </el-icon>
              </div>
              <div class="service-type-name">{{ service.name }}</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 服务内容详情 -->
      <div class="service-details-section">
        <div
          v-for="(category, categoryIndex) in serviceCategories"
          :key="categoryIndex"
          class="service-category"
          :id="`category-${categoryIndex}`"
        >
          <div class="category-header">
            <div class="category-number">{{ getCategoryNumber(categoryIndex) }}</div>
            <h2 class="category-title">{{ category.title }}</h2>
          </div>
          
          <div class="category-content">
            <div class="category-image-wrapper">
              <el-image
                :src="category.image"
                fit="cover"
                class="category-image"
              >
                <template #placeholder>
                  <div class="category-image-placeholder">
                    <el-icon><Picture /></el-icon>
                    <span>分类图片</span>
                  </div>
                </template>
              </el-image>
            </div>
            
            <div class="service-items-list">
              <div
                v-for="(item, itemIndex) in category.items"
                :key="itemIndex"
                class="service-item"
                @click="goToServiceDetail(item)"
              >
                <div class="service-item-name">{{ typeof item === 'object' ? item.serviceName : item }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Navigation from '@/components/common/Navigation.vue'
import { getAvailableServices } from '@/api/services'
import { ElMessage } from 'element-plus'
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
  Location,
  Picture
} from '@element-plus/icons-vue'

// 占位图URL
const bannerPlaceholder = '/images/企业宣传图 .png'

// 默认的服务类型列表（用于顶部导航，如果后端数据不完整时使用）
const defaultServiceTypes = [
  { name: '日常保洁', icon: 'Brush' },
  { name: '餐具处理', icon: 'Tools' },
  { name: '管道疏通', icon: 'Link' },
  { name: '衣物护理', icon: 'Box' },
  { name: '家居养护', icon: 'House' },
  { name: '月嫂服务', icon: 'User' },
  { name: '育儿嫂服务', icon: 'UserFilled' },
  { name: '生活照料', icon: 'Reading' },
  { name: '健康辅助', icon: 'FirstAidKit' },
  { name: '家电维修', icon: 'Setting' },
  { name: '家电保养', icon: 'Monitor' },
  { name: '小型安装', icon: 'Grid' },
  { name: '膳食服务', icon: 'Coffee' },
  { name: '宠物照料', icon: 'Location' }
]

// 默认的服务分类详情（如果后端数据不完整时使用）
const defaultServiceCategories = [
  {
    title: '基础家务服务',
    image: '/images/基础家政服务.png',
    items: ['日常保洁', '餐具处理', '管道疏通']
  },
  {
    title: '专业保洁与养护服务',
    image: '/images/深度保洁.png',
    items: ['衣物护理', '家居养护']
  },
  {
    title: '母婴护理服务',
    image: '/images/母婴照顾.png',
    items: ['月嫂服务', '育儿嫂服务']
  },
  {
    title: '养老护理服务',
    image: '/images/养老陪护.png',
    items: ['生活照料', '健康辅助']
  },
  {
    title: '家电维修与维护服务',
    image: '/images/家电维修.png',
    items: ['家电维修', '家电保养', '小型安装']
  },
  {
    title: '特色专项服务',
    image: '/images/上门炒菜.png',
    items: ['膳食服务', '宠物照料']
  }
]

// 服务类型列表（小类型名称，用于顶部导航）
const serviceTypes = ref([...defaultServiceTypes])

// 图标映射（用于服务类型图标，如果没有匹配的图标，使用默认图标）
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

// 服务分类详情（大类型及其下的小类型）
const serviceCategories = ref([...defaultServiceCategories])

// 获取分类编号（中文数字）
const getCategoryNumber = (index) => {
  const numbers = ['一', '二', '三', '四', '五', '六']
  return numbers[index] || ''
}

const router = useRouter()
const route = useRoute()

// 滚动到指定服务
const scrollToService = (serviceName) => {
  // 找到包含该服务的分类
  const categoryIndex = serviceCategories.value.findIndex(category =>
    category.items.some(item => {
      const itemName = typeof item === 'object' ? item.serviceName : item
      return itemName === serviceName
    })
  )
  
  if (categoryIndex !== -1) {
    const element = document.getElementById(`category-${categoryIndex}`)
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  }
}

// 跳转到服务详情页
const goToServiceDetail = (service) => {
  // service 是服务对象，包含 serviceId 和 serviceName
  const serviceId = typeof service === 'object' ? service.serviceId : null
  const serviceName = typeof service === 'object' ? service.serviceName : service
  
  // 确保只有有效的 serviceId 才传递，如果 serviceId 无效，不跳转
  if (!serviceId) {
    ElMessage.warning('服务信息不完整，无法查看详情')
    return
  }
  
  router.push({
    path: '/service/detail',
    query: {
      serviceId: serviceId, // 只传递 serviceId，确保是数字类型
      serviceName: serviceName
    }
  })
}

// 加载服务类型数据
const loadServiceCategories = async () => {
  try {
    // 1. 获取所有可用服务（用户端不应调用管理员API）
    const servicesRes = await getAvailableServices()
    const services = servicesRes.data?.data || servicesRes.data || []
    
    if (!Array.isArray(services) || services.length === 0) {
      console.warn('未获取到服务数据，使用默认数据')
      serviceCategories.value = [...defaultServiceCategories]
      serviceTypes.value = [...defaultServiceTypes]
      return
    }
    
    // 2. 使用固定的六大类结构（用户端不需要调用管理员API）
    const sixMainCategories = [
      '基础家务服务',
      '专业保洁与养护服务',
      '母婴护理服务',
      '养老护理服务',
      '家电维修与维护服务',
      '特色专项服务'
    ]
    
    // 3. 按照 mainCategory 分组服务
    const categoriesMap = new Map()
    const serviceTypeSet = new Set()
    
    // 初始化六大类结构
    sixMainCategories.forEach(mainCategoryName => {
      categoriesMap.set(mainCategoryName, {
        title: mainCategoryName,
        image: getDefaultImage(mainCategoryName),
        items: []
      })
    })
    
    // 将服务按照 mainCategory 分组
    services.forEach(service => {
      const mainCategory = service.mainCategory || service.main_category
      if (mainCategory && categoriesMap.has(mainCategory)) {
        categoriesMap.get(mainCategory).items.push({
          serviceId: service.serviceId || service.service_id,
          serviceName: service.serviceName || service.service_name,
          description: service.description,
          price: service.price,
          imageUrl: service.imageUrl || service.image_url
        })
        
        // 添加到顶部导航列表
        const serviceName = service.serviceName || service.service_name
        if (serviceName && !serviceTypeSet.has(serviceName)) {
          serviceTypeSet.add(serviceName)
        }
      }
    })
    
    // 转换为数组格式
    const categories = Array.from(categoriesMap.values())
    
    // 构建顶部导航服务类型列表
    const allSubTypes = Array.from(serviceTypeSet).map(name => ({
      name: name,
      icon: getIconForServiceType(name)
    }))
    
    serviceCategories.value = categories
    serviceTypes.value = allSubTypes.length > 0 ? allSubTypes : [...defaultServiceTypes]
    
    console.log('成功加载服务数据:', {
      categories: categories.length,
      services: services.length,
      subTypes: allSubTypes.length
    })
  } catch (error) {
    console.error('加载服务数据失败，使用默认数据:', error)
    // API调用失败时，使用默认数据
    serviceCategories.value = [...defaultServiceCategories]
    serviceTypes.value = [...defaultServiceTypes]
  }
}

// 根据大类型名称获取默认图片
const getDefaultImage = (categoryName) => {
  const imageMap = {
    '基础家务服务': '/images/基础家政服务.png',
    '专业保洁与养护服务': '/images/深度保洁.png',
    '母婴护理服务': '/images/母婴照顾.png',
    '养老护理服务': '/images/养老陪护.png',
    '家电维修与维护服务': '/images/家电维修.png',
    '特色专项服务': '/images/上门炒菜.png'
  }
  return imageMap[categoryName] || '/images/default-service.png'
}

// 根据服务类型名称获取图标
const getIconForServiceType = (serviceTypeName) => {
  const iconNameMap = {
    '日常保洁': 'Brush',
    '餐具处理': 'Tools',
    '管道疏通': 'Link',
    '衣物护理': 'Box',
    '家居养护': 'House',
    '月嫂服务': 'User',
    '育儿嫂服务': 'UserFilled',
    '生活照料': 'Reading',
    '健康辅助': 'FirstAidKit',
    '家电维修': 'Setting',
    '家电保养': 'Monitor',
    '小型安装': 'Grid',
    '膳食服务': 'Coffee',
    '宠物照料': 'Location'
  }
  return iconNameMap[serviceTypeName] || 'Box' // 默认使用 Box 图标
}

// 页面加载时，先加载数据，然后处理滚动
onMounted(async () => {
  await loadServiceCategories()
  nextTick(() => {
    const categoryIndex = route.query.category
    if (categoryIndex !== undefined) {
      const index = parseInt(categoryIndex)
      if (!isNaN(index) && index >= 0 && index < serviceCategories.value.length) {
        const element = document.getElementById(`category-${index}`)
        if (element) {
          setTimeout(() => {
            element.scrollIntoView({ behavior: 'smooth', block: 'start' })
          }, 300)
        }
      }
    }
  })
})
</script>

<style scoped lang="scss">
.service-list-view {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0;
}

// Banner区域
.banner-section {
  width: 100vw;
  margin-left: calc(-50vw + 50%);
  margin-bottom: 40px;
  overflow: hidden;
  
  .banner-image {
    width: 100%;
    display: block;
    
    :deep(.el-image__inner) {
      width: 100%;
      height: auto;
      object-fit: contain; // 完整显示图片，不裁剪
      object-position: left top; // 调整图片位置，确保左侧上方人物脸部显示
    }
    
    .image-placeholder {
      width: 100%;
      min-height: 300px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background-color: #e8f5e9;
      color: #4caf50;
      font-size: 18px;
      
      .el-icon {
        font-size: 48px;
        margin-bottom: 10px;
      }
    }
  }
}

// 服务类型导航区域
.service-types-section {
  padding: 40px 20px;
  background-color: #fff;
  margin-bottom: 30px;
  
  .service-types-grid {
    max-width: 1200px;
    margin: 0 auto;
  }
  
  .service-type-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 20px 10px;
    cursor: pointer;
    transition: all 0.3s;
    border-radius: 8px;
    
    &:hover {
      background-color: #f5f7fa;
      transform: translateY(-5px);
    }
    
    .service-type-icon {
      width: 80px;
      height: 80px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #e8f5e9;
      border-radius: 50%;
      margin-bottom: 12px;
      color: #4caf50;
      transition: all 0.3s;
    }
    
    &:hover .service-type-icon {
      background-color: #4caf50;
      color: #fff;
    }
    
    .service-type-name {
      font-size: 14px;
      color: #333;
      text-align: center;
      font-weight: 500;
    }
  }
}

// 服务详情区域
.service-details-section {
  padding: 0 20px 40px;
  
  .service-category {
    margin-bottom: 60px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .category-header {
      display: flex;
      align-items: center;
      margin-bottom: 30px;
      padding-bottom: 15px;
      border-bottom: 2px solid #4caf50;
      
      .category-number {
        font-size: 32px;
        font-weight: bold;
        color: #4caf50;
        margin-right: 15px;
        min-width: 40px;
      }
      
      .category-title {
        font-size: 28px;
        color: #333;
        margin: 0;
        font-weight: 600;
      }
    }
    
    .category-content {
      display: flex;
      gap: 30px;
      align-items: flex-start;
      
      .category-image-wrapper {
        flex: 0 0 400px;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        
        .category-image {
          width: 100%;
          height: 300px;
          display: block;
          
          :deep(.el-image__inner) {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
          
          .category-image-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            background-color: #f5f7fa;
            color: #c0c4cc;
            
            .el-icon {
              font-size: 48px;
              margin-bottom: 10px;
            }
            
            span {
              font-size: 14px;
              color: #909399;
            }
          }
        }
      }
      
      .service-items-list {
        flex: 1;
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 16px;
        
        .service-item {
          padding: 16px 20px;
          background-color: #fff;
          border: 1px solid #e4e7ed;
          border-radius: 8px;
          text-align: center;
          transition: all 0.3s;
          cursor: pointer;
          
          &:hover {
            border-color: #4caf50;
            box-shadow: 0 2px 8px rgba(76, 175, 80, 0.2);
            transform: translateY(-2px);
          }
          
          .service-item-name {
            font-size: 16px;
            color: #333;
            font-weight: 500;
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .banner-section {
    .banner-image {
      :deep(.el-image__inner) {
        height: auto;
      }
    }
  }
  
  .service-types-section {
    padding: 30px 15px;
    
    .service-type-item {
      padding: 15px 5px;
      
      .service-type-icon {
        width: 60px;
        height: 60px;
        margin-bottom: 8px;
      }
      
      .service-type-name {
        font-size: 12px;
      }
    }
  }
  
  .service-details-section {
    padding: 0 15px 30px;
    
    .service-category {
      margin-bottom: 40px;
      
      .category-header {
        .category-number {
          font-size: 24px;
          margin-right: 10px;
        }
        
        .category-title {
          font-size: 22px;
        }
      }
      
      .category-content {
        flex-direction: column;
        
        .category-image-wrapper {
          flex: none;
          width: 100%;
          
          .category-image {
            height: 200px;
          }
        }
        
        .service-items-list {
          grid-template-columns: repeat(2, 1fr);
          gap: 12px;
          
          .service-item {
            padding: 12px 16px;
            
            .service-item-name {
              font-size: 14px;
            }
          }
        }
      }
    }
  }
}
</style>

