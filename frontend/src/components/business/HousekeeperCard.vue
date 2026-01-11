<template>
  <el-card class="housekeeper-card" @click="$emit('click')">
    <div class="card-content">
      <el-avatar :size="80" :src="housekeeper.avatar" />
      <div class="info">
        <h3>{{ housekeeper.name }}</h3>
        <p class="rating">
          <el-rate
            v-model="housekeeper.rating"
            disabled
            show-score
            text-color="#ff9900"
          />
        </p>
        <p class="meta">
          <span>服务年限: {{ housekeeper.yearsOfService }}年</span>
          <span>籍贯: {{ housekeeper.origin }}</span>
        </p>
        <p class="bio">{{ housekeeper.bio || '暂无简介' }}</p>
      </div>
    </div>
  </el-card>
</template>

<script setup>
defineProps({
  housekeeper: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])
</script>

<style scoped lang="scss">
.housekeeper-card {
  cursor: pointer;
  margin-bottom: 20px;
  transition: transform 0.3s, box-shadow 0.3s;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.card-content {
  display: flex;
  gap: 15px;
  
  .info {
    flex: 1;
    
    h3 {
      margin: 0 0 10px 0;
      color: #333;
    }
    
    .rating {
      margin: 10px 0;
    }
    
    .meta {
      display: flex;
      gap: 15px;
      margin: 10px 0;
      color: #666;
      font-size: 14px;
    }
    
    .bio {
      margin-top: 10px;
      color: #999;
      font-size: 13px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
  }
}
</style>

