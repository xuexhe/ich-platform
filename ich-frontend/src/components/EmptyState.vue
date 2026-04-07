<template>
  <div class="text-center py-16">
    <el-icon :size="48" :color="color"><component :is="icon" /></el-icon>
    <p class="mt-2 text-gray-500">{{ text }}</p>
    <el-button v-if="buttonText" class="mt-4" type="danger" @click="$emit('action')">{{ buttonText }}</el-button>
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, default: 'Document' },
  text: { type: String, default: '暂无数据' },
  buttonText: { type: String, default: '' },
  color: { type: String, default: '#9CA3AF' }
})
defineEmits(['action'])
</script>