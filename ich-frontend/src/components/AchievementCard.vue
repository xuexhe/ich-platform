<!-- src/components/AchievementCard.vue -->
<template>
  <div class="achievement-card" :class="{ 'obtained': isObtained, 'locked': !isObtained }">
    <div class="achievement-icon">
      <span class="text-4xl">{{ icon || '🏅' }}</span>
      <div v-if="!isObtained" class="lock-overlay">
        <el-icon><Lock /></el-icon>
      </div>
    </div>
    <div class="achievement-info">
      <h4 class="font-bold text-ich-dark">{{ name }}</h4>
      <p class="text-xs text-gray-500">{{ description }}</p>
      <div class="flex justify-between items-center mt-2">
        <span class="text-xs text-ich-gold">+{{ points }} 点</span>
        <span v-if="isObtained" class="text-xs text-green-500">
          <el-icon><Check /></el-icon> 已获得
        </span>
        <span v-else class="text-xs text-gray-400">
          未解锁
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Lock, Check } from '@element-plus/icons-vue'

defineProps({
  id: Number,
  name: String,
  description: String,
  icon: String,
  points: Number,
  isObtained: Boolean
})
</script>

<style scoped>
.achievement-card {
  @apply bg-white rounded-xl p-4 shadow-sm transition-all duration-300 flex gap-4;
}

.achievement-card.obtained {
  @apply border-l-4 border-l-ich-gold shadow-md;
}

.achievement-card.locked {
  @apply opacity-60 grayscale;
}

.achievement-icon {
  @apply relative w-16 h-16 flex items-center justify-center;
}

.lock-overlay {
  @apply absolute inset-0 bg-black/50 rounded-full flex items-center justify-center;
}
</style>