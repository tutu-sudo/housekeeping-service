<template>
  <div class="appointment-view">
    <AppointmentConfirmView />
  </div>
</template>

<script setup>
import AppointmentConfirmView from '@/views/customer/AppointmentConfirmView.vue'
</script>

<style scoped lang="scss">
.appointment-view {
  min-height: 100vh;
}

.content {
  padding: 20px;
}
</style>

