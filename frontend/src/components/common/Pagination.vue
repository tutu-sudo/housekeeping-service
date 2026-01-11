<template>
  <div class="pagination-container">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  total: {
    type: Number,
    default: 0
  },
  page: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 10
  }
})

const emit = defineEmits(['page-change', 'size-change'])

const currentPage = ref(props.page)
const pageSize = ref(props.pageSize)

watch(() => props.page, (newVal) => {
  currentPage.value = newVal
})

watch(() => props.pageSize, (newVal) => {
  pageSize.value = newVal
})

const handleSizeChange = (size) => {
  pageSize.value = size
  emit('size-change', size)
  emit('page-change', 1)
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  emit('page-change', page)
}
</script>

<style scoped lang="scss">
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>

