<!-- src/components/AchievementToast.vue -->
<template>
  <Teleport to="body">
    <Transition name="toast">
      <div v-if="visible" class="fixed top-20 right-4 z-[9999] w-80">
        <div class="bg-white rounded-xl shadow-2xl overflow-hidden border-l-4 border-ich-gold">
          <div class="p-4 flex items-center gap-3">
            <div class="w-12 h-12 rounded-full bg-ich-gold/20 flex items-center justify-center">
              <span class="text-2xl">{{ currentAchievement.icon || '🏅' }}</span>
            </div>
            <div class="flex-1">
              <p class="text-xs text-ich-gold font-bold">🎉 获得新成就</p>
              <p class="font-bold text-ich-dark">{{ currentAchievement.name }}</p>
              <p class="text-xs text-gray-500">{{ currentAchievement.description }}</p>
            </div>
            <button @click="close" class="text-gray-400 hover:text-gray-600">
              <el-icon><Close /></el-icon>
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue'
import { Close } from '@element-plus/icons-vue'

const visible = ref(false)
const currentAchievement = ref({})
let timer = null

const show = (achievement) => {
  if (timer) clearTimeout(timer)
  currentAchievement.value = achievement
  visible.value = true
  timer = setTimeout(() => {
    visible.value = false
  }, 3000)
}

const close = () => {
  visible.value = false
  if (timer) clearTimeout(timer)
}

defineExpose({ show })
</script>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
</style>