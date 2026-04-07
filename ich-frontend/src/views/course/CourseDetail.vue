<!-- src/views/course/CourseDetail.vue -->
<template>
  <div class="min-h-screen bg-ich-bg">
    <Navbar />
    
    <div class="max-w-5xl mx-auto px-4 py-12">
      <el-button type="text" class="mb-6" @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      
      <div v-if="loading" class="flex justify-center py-20">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      </div>
      
      <div v-else-if="course" class="bg-white rounded-xl shadow-lg overflow-hidden">
        <!-- 视频播放器区域 -->
        <div v-if="course.videoUrl" class="aspect-video bg-black">
          <video 
            ref="videoPlayer"
            :src="course.videoUrl" 
            class="w-full h-full"
            controls
            controlsList="nodownload"
            @timeupdate="updateProgress"
            @ended="handleVideoEnd"
          ></video>
        </div>
        
        <!-- 无视频时的封面图 -->
        <div v-else class="aspect-video bg-gray-800 relative flex items-center justify-center">
          <img :src="course.cover" class="w-full h-full object-cover" @error="handleImageError">
          <div class="absolute inset-0 bg-black/50 flex items-center justify-center">
            <div class="text-white text-center">
              <div class="text-5xl mb-2">📹</div>
              <p class="text-sm">视频资源正在准备中</p>
            </div>
          </div>
        </div>
        
        <div class="p-8">
          <div class="flex justify-between items-start mb-4">
            <div>
              <span class="px-3 py-1 bg-ich-gold/20 text-ich-gold text-sm rounded-full">{{ course.category }}</span>
              <h1 class="text-2xl font-bold text-ich-dark mt-3">{{ course.title }}</h1>
            </div>
            <span class="text-gray-400">{{ course.duration }}分钟</span>
          </div>
          
          <!-- 课程信息栏（包含点赞） -->
          <div class="flex items-center gap-6 mb-6 text-sm text-gray-500">
            <span>难度: {{ ['⭐', '⭐⭐', '⭐⭐⭐'][course.difficulty - 1] || '⭐' }}</span>
            <span>👁️ {{ course.viewCount || 0 }} 次学习</span>
            <button 
              @click="handleLike" 
              class="flex items-center gap-1.5 transition-all duration-300"
              :class="isLiked ? 'text-ich-red' : 'text-gray-400 hover:text-ich-red'"
            >
              <span class="text-lg">{{ isLiked ? '❤️' : '🤍' }}</span>
              <span>{{ course.likeCount || 0 }}</span>
            </button>
          </div>
          
          <!-- 课程介绍 -->
          <div class="border-t pt-6">
            <h3 class="text-lg font-bold text-ich-dark mb-4">📖 课程介绍</h3>
            <p class="text-gray-600 leading-relaxed whitespace-pre-wrap">{{ course.steps || '暂无详细介绍' }}</p>
          </div>
          
          <!-- 学习进度 -->
          <div v-if="userStore.isLoggedIn" class="mt-6 p-5 bg-gradient-to-r from-gray-50 to-gray-100 rounded-xl">
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm font-medium text-gray-700">📊 学习进度</span>
              <span class="text-sm font-bold text-ich-red">{{ userProgress }}%</span>
            </div>
            <el-progress :percentage="userProgress" :color="'#C92C2C'" :stroke-width="8" />
            <div class="mt-3 text-xs text-gray-400 flex items-center gap-2">
              <span>💡</span>
              <span>观看视频自动记录进度，完成100%即可获得成就点数</span>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="mt-8 flex gap-4">
            <el-button 
              type="danger" 
              size="large"
              :disabled="!course.videoUrl"
              @click="startLearning"
            >
              {{ course.videoUrl ? '▶️ 开始学习' : '⏳ 等待资源' }}
            </el-button>
            <el-button size="large" @click="$router.back()">返回列表</el-button>
          </div>
        </div>
      </div>
      
      <div v-else class="text-center py-20">
        <el-icon :size="48" class="text-gray-300"><Document /></el-icon>
        <p class="text-gray-500 mt-2">课程不存在</p>
        <el-button class="mt-4" type="danger" @click="$router.push('/course')">返回课程列表</el-button>
      </div>
    </div>
    
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Loading, Document } from '@element-plus/icons-vue'
import request from '@/utils/request'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import { getCourseDetail, updateCourseProgress, getCourseProgress } from '@/api/course'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const course = ref(null)
const userProgress = ref(0)
const isLiked = ref(false)
const videoPlayer = ref(null)
let saveInterval = null

// 获取课程详情
const fetchCourse = async () => {
  loading.value = true
  try {
    const res = await getCourseDetail(route.params.id)
    if (res.code === 200) {
      course.value = res.data
    }
  } catch (error) {
    console.error('获取课程失败:', error)
    ElMessage.error('获取课程失败')
  } finally {
    loading.value = false
  }
}

// 获取学习进度
const fetchProgress = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getCourseProgress(userStore.userInfo.id, route.params.id)
    if (res.code === 200 && res.data) {
      userProgress.value = res.data.progress || 0
    }
  } catch (error) {
    console.error('获取进度失败:', error)
  }
}

// 检查是否已点赞
const checkIsLiked = async () => {
  if (!userStore.isLoggedIn || !course.value) return
  try {
    const res = await request.get(`/course/liked/${course.value.id}`, {
      params: { userId: userStore.userInfo.id }
    })
    if (res.code === 200) {
      isLiked.value = res.data
    }
  } catch (error) {
    console.error('检查点赞状态失败:', error)
  }
}

// 点赞/取消点赞
const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    let res
    if (isLiked.value) {
      res = await request.post('/course/unlike', {
        courseId: course.value.id,
        userId: userStore.userInfo.id
      })
      if (res.code === 200) {
        isLiked.value = false
        course.value.likeCount = Math.max(0, (course.value.likeCount || 1) - 1)
        ElMessage.success('已取消点赞')
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } else {
      res = await request.post('/course/like', {
        courseId: course.value.id,
        userId: userStore.userInfo.id
      })
      if (res.code === 200) {
        isLiked.value = true
        course.value.likeCount = (course.value.likeCount || 0) + 1
        ElMessage.success('点赞成功')
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 更新视频播放进度
const updateProgress = () => {
  if (!videoPlayer.value || !userStore.isLoggedIn) return
  
  const video = videoPlayer.value
  if (video.duration && video.duration > 0 && !isNaN(video.duration)) {
    const progress = Math.min(99, Math.floor((video.currentTime / video.duration) * 100))
    if (progress > userProgress.value && progress <= 99) {
      userProgress.value = progress
    }
  }
}

// 定期保存进度
const startAutoSave = () => {
  if (saveInterval) clearInterval(saveInterval)
  saveInterval = setInterval(async () => {
    if (userProgress.value > 0 && userStore.isLoggedIn && course.value && userProgress.value < 100) {
      try {
        await updateCourseProgress({
          userId: userStore.userInfo.id,
          courseId: course.value.id,
          progress: userProgress.value
        })
        console.log('进度已保存:', userProgress.value)
      } catch (error) {
        console.error('保存进度失败:', error)
      }
    }
  }, 5000)
}

// 视频结束
const handleVideoEnd = async () => {
  if (userStore.isLoggedIn && course.value) {
    try {
      const res = await updateCourseProgress({
        userId: userStore.userInfo.id,
        courseId: course.value.id,
        progress: 100
      })
      userProgress.value = 100
      if (res.data?.isNewCompleted) {
        const points = res.data?.totalPoints || 0
        ElMessage.success(`🎉 恭喜完成课程学习！获得成就点数 ${points} 点！`)
        // 刷新成就墙数据
        if (window.showAchievement) {
          window.showAchievement({
            name: '学习达人',
            description: '完成一门非遗课程',
            icon: '📚',
            points: 40
          })
        }
      } else if (res.data?.progress === 100) {
        ElMessage.success('🎉 恭喜完成课程学习！')
      }
    } catch (error) {
      console.error('完成课程失败:', error)
    }
  }
}

// 开始学习
const startLearning = () => {
  if (course.value?.videoUrl) {
    const videoElement = document.querySelector('video')
    if (videoElement) {
      videoElement.scrollIntoView({ behavior: 'smooth', block: 'center' })
      setTimeout(() => {
        videoElement.play().catch(e => console.log('自动播放被阻止:', e))
      }, 500)
    }
    startAutoSave()
  } else {
    ElMessage.info('视频资源正在准备中...')
  }
}

const handleImageError = (e) => {
  e.target.src = 'https://picsum.photos/id/1/400/300'
}

onMounted(async () => {
  await fetchCourse()
  await fetchProgress()
  await checkIsLiked()
})

onUnmounted(() => {
  if (saveInterval) {
    clearInterval(saveInterval)
  }
})
</script>

<style scoped>
.aspect-video {
  aspect-ratio: 16 / 9;
}

:deep(.el-progress-bar__outer) {
  background-color: #e5e7eb;
}

:deep(.el-progress-bar__inner) {
  transition: width 0.3s ease;
}
</style>