<template>
  <div class="review-component">
    <div class="review-header">
      <el-avatar :size="40" :src="review.customerAvatar" />
      <div class="reviewer-info">
        <span class="reviewer-name">{{ review.customerName }}</span>
        <el-rate
          v-model="review.rating"
          disabled
          show-score
          text-color="#ff9900"
        />
      </div>
      <span class="review-date">{{ review.createdAt }}</span>
    </div>
    <div class="review-content">
      <p>{{ review.content }}</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  review: {
    type: Object,
    required: true
  }
})
</script>

<style scoped lang="scss">
.review-component {
  padding: 15px;
  border-bottom: 1px solid #eee;
  margin-bottom: 15px;
  
  &:last-child {
    border-bottom: none;
  }
}

.review-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
  
  .reviewer-info {
    flex: 1;
    
    .reviewer-name {
      font-weight: bold;
      margin-right: 10px;
    }
  }
  
  .review-date {
    color: #999;
    font-size: 12px;
  }
}

.review-content {
  p {
    margin: 0;
    color: #666;
    line-height: 1.6;
  }
}
</style>

