<!-- src/App.vue -->
<template>
  <router-view />
  <AchievementToast ref="achievementToast" />
</template>

<script setup>
import { ref } from 'vue'
import AchievementToast from '@/components/AchievementToast.vue'

const achievementToast = ref(null)

window.showAchievement = (achievement) => {
  if (achievementToast.value) {
    achievementToast.value.show(achievement)
  }
}
</script>