<template>
  <el-table :data="data" style="width: 100%">
    <el-table-column
      v-for="column in columns"
      :key="column.prop"
      :prop="column.prop"
      :label="column.label"
      :width="column.width"
    />
    <el-table-column label="操作" width="200" v-if="hasActions">
      <template #default="scope">
        <slot name="actions" :row="scope.row" />
        <el-button
          v-if="scope.row.status === 'pending'"
          size="small"
          type="success"
          @click="handleStatusChange(scope.row.id, 'confirmed')"
        >
          确认
        </el-button>
        <el-button
          v-if="scope.row.status === 'pending'"
          size="small"
          type="danger"
          @click="handleStatusChange(scope.row.id, 'cancelled')"
        >
          取消
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
defineProps({
  data: {
    type: Array,
    default: () => []
  },
  columns: {
    type: Array,
    required: true
  },
  hasActions: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['status-change'])

const handleStatusChange = (id, status) => {
  emit('status-change', id, status)
}
</script>

<style scoped lang="scss">
</style>

