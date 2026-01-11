<template>
  <div class="statistics-banner">
    <div class="statistics-content">
      <div class="stat-item">
        <div class="stat-number">50+</div>
        <div class="stat-label">集团下设直属机构</div>
      </div>
      <div class="stat-item">
        <div class="stat-number">100+</div>
        <div class="stat-label">为社会提供100余项服务</div>
      </div>
      <div class="stat-item">
        <div class="stat-number">15000+</div>
        <div class="stat-label">拥有1.5万多名在册服务人员</div>
      </div>
      <div class="stat-item">
        <div class="stat-number">1992</div>
        <div class="stat-label">公司创立于1992年</div>
        <div class="stat-subtitle">ABOUT US</div>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped lang="scss">
.statistics-banner {
  background: linear-gradient(135deg, #2e7d32 0%, #4CAF50 100%);
  padding: 40px 20px;
  margin: 0;
}

.statistics-content {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 30px;
  
  @media (max-width: 992px) {
    grid-template-columns: repeat(2, 1fr);
  }
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
  
  .stat-item {
    text-align: center;
    color: white;
    
    .stat-number {
      font-size: 48px;
      font-weight: bold;
      margin-bottom: 10px;
      line-height: 1;
    }
    
    .stat-label {
      font-size: 16px;
      opacity: 0.9;
      line-height: 1.4;
    }
    
    .stat-subtitle {
      font-size: 12px;
      opacity: 0.7;
      margin-top: 5px;
      text-transform: uppercase;
      letter-spacing: 1px;
    }
  }
}
</style>

