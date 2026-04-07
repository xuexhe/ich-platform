<!-- src/views/achievement/AchievementWall.vue -->
<template>
  <div class="min-h-screen bg-ich-bg">
    <Navbar />
    
    <div class="max-w-6xl mx-auto px-4 py-12">
      <div class="text-center mb-8">
        <h2 class="text-3xl font-bold text-ich-dark mb-2">🏅 非遗成就墙</h2>
        <p class="text-gray-500">完成任务，解锁成就，成为非遗守护人</p>
      </div>
      
      <!-- 成就统计卡片 -->
      <div class="bg-gradient-to-r from-ich-dark to-ich-dark/80 rounded-2xl p-6 text-white mb-8">
        <div class="flex justify-between items-center">
          <div>
            <p class="text-sm opacity-80">成就进度</p>
            <p class="text-3xl font-bold">{{ stats.obtainedCount || 0 }} / {{ stats.totalCount || 0 }}</p>
          </div>
          <div class="w-32">
            <div class="w-full bg-white/30 rounded-full h-2">
              <div class="bg-ich-gold rounded-full h-2" :style="{ width: progressPercent + '%' }"></div>
            </div>
          </div>
          <div class="text-right">
            <p class="text-sm opacity-80">总成就点数</p>
            <p class="text-3xl font-bold text-ich-gold">{{ stats.totalPoints || 0 }}</p>
          </div>
        </div>
      </div>
      
      <!-- 成就列表 -->
      <div v-if="loading" class="flex justify-center py-20">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      </div>
      
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <AchievementCard
          v-for="achievement in achievements"
          :key="achievement.id"
          :id="achievement.id"
          :name="achievement.name"
          :description="achievement.description"
          :icon="achievement.icon"
          :points="achievement.points"
          :is-obtained="achievement.isObtained"
        />
      </div>
      
      <div v-if="!loading && achievements.length === 0" class="text-center py-16">
        <div class="text-6xl mb-4">🏆</div>
        <p class="text-gray-500">暂无成就数据</p>
        <p class="text-sm text-gray-400 mt-2">请确保数据库中已有成就数据</p>
      </div>
    </div>
    
    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import AchievementCard from '@/components/AchievementCard.vue'
import { useUserStore } from '@/store/user'
import { getUserAchievements, getUserAchievementStats } from '@/api/achievement'

const userStore = useUserStore()
const loading = ref(false)
const achievements = ref([])
const stats = ref({ obtainedCount: 0, totalCount: 0, totalPoints: 0 })

const progressPercent = computed(() => {
  if (stats.value.totalCount === 0) return 0
  return (stats.value.obtainedCount / stats.value.totalCount) * 100
})

const fetchAchievements = async () => {
  if (!userStore.isLoggedIn) {
    loading.value = false
    return
  }
  
  loading.value = true
  try {
    const res = await getUserAchievements(userStore.userInfo.id)
    if (res.code === 200) {
      achievements.value = res.data
      console.log('成就数据:', achievements.value)
    } else {
      console.error('获取成就失败:', res.message)
    }
  } catch (error) {
    console.error('获取成就列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getUserAchievementStats(userStore.userInfo.id)
    if (res.code === 200) {
      stats.value = res.data
      console.log('成就统计:', stats.value)
    }
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchAchievements()
    fetchStats()
  }
})
</script>

<style scoped>
:deep(.is-loading) {
  animation: rotating 1s linear infinite;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>