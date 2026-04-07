<!-- src/views/course/CourseListView.vue -->
<template>
  <div class="min-h-screen bg-ich-bg">
    <Navbar />
    
    <div class="max-w-7xl mx-auto px-4 py-12">
      <!-- 页面标题 -->
      <div class="text-center mb-12">
        <h2 class="text-4xl font-bold text-ich-dark mb-2">📚 非遗小课堂</h2>
        <p class="text-gray-500">学习非遗知识，传承中华文化</p>
      </div>
      
      <!-- Tab 切换 -->
      <div class="flex justify-center gap-4 mb-8 border-b pb-2">
        <button 
          @click="activeTab = 'course'; updateUrl()" 
          class="px-6 py-2 text-lg transition-all"
          :class="activeTab === 'course' ? 'text-ich-red border-b-2 border-ich-red font-bold' : 'text-gray-500 hover:text-gray-700'"
        >
          📖 非遗课程
        </button>
        <button 
          @click="activeTab = 'quiz'" 
          class="px-6 py-2 text-lg transition-all"
          :class="activeTab === 'quiz' ? 'text-ich-red border-b-2 border-ich-red font-bold' : 'text-gray-500 hover:text-gray-700'"
        >
          📝 每日一题
        </button>
        <button 
          @click="activeTab = 'knowledge'; updateUrl()" 
          class="px-6 py-2 text-lg transition-all"
          :class="activeTab === 'knowledge' ? 'text-ich-red border-b-2 border-ich-red font-bold' : 'text-gray-500 hover:text-gray-700'"
        >
          📖 非遗知识库
        </button>
      </div>
      
      <!-- 非遗课程 Tab -->
      <div v-if="activeTab === 'course'">
        <!-- 课程分类筛选 -->
        <div class="flex flex-wrap gap-2 mb-6">
          <el-button 
            :type="courseCategory === 'all' ? 'danger' : 'default'"
            size="small"
            @click="courseCategory = 'all'; loadCourses()"
          >
            全部
          </el-button>
          <el-button 
            v-for="cat in courseCategories"
            :key="cat"
            :type="courseCategory === cat ? 'danger' : 'default'"
            size="small"
            @click="courseCategory = cat; loadCourses()"
          >
            {{ cat }}
          </el-button>
        </div>
        
        <!-- 课程列表 -->
        <div v-loading="courseLoading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="item in courseList" :key="item.id" 
               class="bg-white rounded-xl shadow-lg overflow-hidden cursor-pointer hover:shadow-xl transition-all hover:-translate-y-1"
               @click="goToCourseDetail(item.id)">
            <img :src="item.cover" class="w-full h-48 object-cover" @error="handleImageError">
            <div class="p-5">
              <div class="flex justify-between items-start mb-2">
                <span class="px-2 py-1 bg-ich-gold/20 text-ich-gold text-xs rounded">{{ item.category }}</span>
                <span class="text-xs text-gray-400">{{ item.duration }}分钟</span>
              </div>
              <h3 class="text-lg font-bold text-ich-dark mb-2 line-clamp-1">{{ item.title }}</h3>
              <p class="text-gray-500 text-sm line-clamp-2">{{ item.steps?.substring(0, 100) || '暂无简介' }}</p>
              <div class="flex justify-between items-center mt-4 text-xs text-gray-400">
                <span>👁️ {{ item.viewCount || 0 }}</span>
                <span>难度: {{ ['⭐', '⭐⭐', '⭐⭐⭐'][item.difficulty - 1] || '⭐' }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="!courseLoading && courseList.length === 0" class="text-center py-16 text-gray-500">
          <el-icon :size="48"><Document /></el-icon>
          <p class="mt-2">暂无课程</p>
        </div>
      </div>
      
      <!-- 每日一题 Tab -->
      <div v-if="activeTab === 'quiz'">
        <DailyQuiz />
      </div>
      
      <!-- 非遗知识库 Tab -->
      <div v-if="activeTab === 'knowledge'">
        <div class="flex justify-between items-center mb-6 flex-wrap gap-3">
          <div class="flex flex-wrap gap-2">
            <el-button 
              :type="knowledgeCategory === 'all' ? 'danger' : 'default'"
              size="small"
              @click="knowledgeCategory = 'all'; loadKnowledge()"
            >
              全部
            </el-button>
            <el-button 
              v-for="cat in knowledgeCategories"
              :key="cat"
              :type="knowledgeCategory === cat ? 'danger' : 'default'"
              size="small"
              @click="knowledgeCategory = cat; loadKnowledge()"
            >
              {{ cat }}
            </el-button>
          </div>
          <el-button type="danger" size="small" plain @click="$router.push('/knowledge/add')">
            <el-icon><Plus /></el-icon> 添加知识
          </el-button>
        </div>
        
        <div v-loading="knowledgeLoading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="item in knowledgeList" :key="item.id" 
               class="bg-white rounded-xl shadow-lg overflow-hidden cursor-pointer hover:shadow-xl transition-all hover:-translate-y-1"
               @click="goToKnowledgeDetail(item.id)">
            <img :src="item.cover" class="w-full h-48 object-cover" @error="handleImageError">
            <div class="p-5">
              <div class="flex justify-between items-start mb-2">
                <span class="px-2 py-1 bg-ich-gold/20 text-ich-gold text-xs rounded">{{ item.category }}</span>
                <span class="text-xs text-gray-400">{{ item.authorName || '系统' }}</span>
              </div>
              <h3 class="text-lg font-bold text-ich-dark mt-2 mb-1 line-clamp-1">{{ item.title }}</h3>
              <p class="text-gray-500 text-sm line-clamp-2">{{ item.summary }}</p>
              <div class="flex justify-between items-center mt-3 text-xs text-gray-400">
                <span>👁️ {{ item.viewCount || 0 }}</span>
                <span class="flex items-center gap-1">
                  <span>{{ item.isLiked ? '❤️' : '🤍' }}</span>
                  <span>{{ item.likeCount || 0 }}</span>
                </span>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="!knowledgeLoading && knowledgeList.length === 0" class="text-center py-16 text-gray-500">
          <el-icon :size="48"><Document /></el-icon>
          <p class="mt-2">暂无知识内容</p>
          <p class="text-sm mt-1">成为第一个分享非遗知识的人～</p>
        </div>
      </div>
    </div>
    
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Plus } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import DailyQuiz from '@/views/quiz/DailyQuiz.vue'
import { getCourseList, getCourseCategories } from '@/api/course'
import { getKnowledgeList, getKnowledgeCategories, checkLiked } from '@/api/knowledge'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('course')

// 课程相关
const courseLoading = ref(false)
const courseList = ref([])
const courseCategories = ref([])
const courseCategory = ref('all')

// 知识库相关
const knowledgeLoading = ref(false)
const knowledgeList = ref([])
const knowledgeCategories = ref([])
const knowledgeCategory = ref('all')

// 更新URL参数
const updateUrl = () => {
  router.replace({ query: { tab: activeTab.value } })
}

// 加载课程
const loadCourses = async () => {
  courseLoading.value = true
  try {
    const params = {
      pageNum: 1,
      pageSize: 12,
      category: courseCategory.value === 'all' ? undefined : courseCategory.value
    }
    const res = await getCourseList(params)
    if (res.code === 200) {
      courseList.value = res.data.records || []
    }
  } catch (error) {
    console.error('获取课程失败:', error)
  } finally {
    courseLoading.value = false
  }
}

const loadCourseCategories = async () => {
  try {
    const res = await getCourseCategories()
    if (res.code === 200) {
      courseCategories.value = res.data
    }
  } catch (error) {
    courseCategories.value = ['传统技艺', '传统戏剧', '传统美术', '民俗']
  }
}

// 加载知识库
const loadKnowledge = async () => {
  knowledgeLoading.value = true
  try {
    const params = {
      pageNum: 1,
      pageSize: 12,
      category: knowledgeCategory.value === 'all' ? undefined : knowledgeCategory.value
    }
    const res = await getKnowledgeList(params)
    if (res.code === 200) {
      knowledgeList.value = res.data.records || []
      if (userStore.isLoggedIn && knowledgeList.value.length > 0) {
        for (const item of knowledgeList.value) {
          try {
            const likedRes = await checkLiked(item.id, userStore.userInfo.id)
            item.isLiked = likedRes.code === 200 && likedRes.data
          } catch {
            item.isLiked = false
          }
        }
      }
    }
  } catch (error) {
    console.error('获取知识库失败:', error)
  } finally {
    knowledgeLoading.value = false
  }
}

const loadKnowledgeCategories = async () => {
  try {
    const res = await getKnowledgeCategories()
    if (res.code === 200) {
      knowledgeCategories.value = res.data
    }
  } catch (error) {
    knowledgeCategories.value = ['剪纸', '陶瓷', '戏曲', '刺绣', '民俗', '其他']
  }
}

const goToCourseDetail = (id) => {
  router.push(`/course/detail/${id}`)
}

const goToKnowledgeDetail = (id) => {
  router.push(`/knowledge/detail/${id}`)
}

const handleImageError = (e) => {
  e.target.src = 'https://picsum.photos/id/1/400/300'
}

// 监听路由参数
watch(() => route.query.tab, (newTab) => {
  if (newTab === 'course' || newTab === 'knowledge') {
    activeTab.value = newTab
  }
}, { immediate: true })

onMounted(() => {
  loadCourses()
  loadCourseCategories()
  loadKnowledge()
  loadKnowledgeCategories()
})
</script>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>