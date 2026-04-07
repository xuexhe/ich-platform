<!-- src/views/course/KnowledgeDetail.vue -->
<template>
  <div class="min-h-screen bg-ich-bg">
    <Navbar />
    
    <div class="max-w-4xl mx-auto px-4 py-12">
      <div class="flex justify-between items-center mb-6">
        <el-button type="text" @click="goBack">
          <el-icon><ArrowLeft /></el-icon> 返回知识库
        </el-button>
        <!-- 编辑按钮：作者本人即可编辑（所有状态） -->
        <el-button 
          v-if="canEdit" 
          type="danger" 
          plain 
          size="small"
          @click="goToEdit"
        >
          <el-icon><Edit /></el-icon> 编辑
        </el-button>
      </div>
      
      <div v-if="loading" class="flex justify-center py-20">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      </div>
      
      <div v-else-if="item" class="bg-white rounded-xl shadow-lg overflow-hidden">
        <img :src="item.cover" class="w-full h-64 object-cover" @error="handleImageError">
        
        <div class="p-8">
          <div class="flex justify-between items-start">
            <div>
              <span class="px-3 py-1 bg-ich-gold/20 text-ich-gold text-sm rounded-full">{{ item.category }}</span>
              <h1 class="text-2xl font-bold text-ich-dark mt-3">{{ item.title }}</h1>
            </div>
            <button 
              @click="handleLike" 
              class="flex items-center gap-2 px-4 py-2 rounded-full transition-all"
              :class="isLiked ? 'bg-ich-red text-white' : 'bg-gray-100 text-gray-600 hover:bg-ich-red/10'"
            >
              <span class="text-xl">{{ isLiked ? '❤️' : '🤍' }}</span>
              <span>{{ likeCount }}</span>
            </button>
          </div>
          
          <div class="flex items-center gap-4 mt-4 text-sm text-gray-500">
            <span>👁️ {{ item.viewCount || 0 }} 次阅读</span>
            <span>✍️ {{ item.authorName || '系统' }}</span>
            <span>📅 {{ formatDate(item.createTime) }}</span>
            <span v-if="item.status !== undefined" class="px-2 py-0.5 rounded text-xs" :class="getStatusClass(item.status)">
              {{ getStatusText(item.status) }}
            </span>
          </div>
          
          <div class="border-t pt-6 mt-6">
            <div class="prose max-w-none" v-html="item.content || item.summary"></div>
          </div>
          
          <div class="mt-8 flex gap-4">
            <el-button type="danger" @click="handleLike" :loading="liking">
              {{ isLiked ? '已点赞' : '点赞' }} ({{ likeCount }})
            </el-button>
            <el-button @click="goBack">返回知识库</el-button>
          </div>
        </div>
      </div>
      
      <div v-else class="text-center py-20">
        <p class="text-gray-500">内容不存在</p>
      </div>
    </div>
    
    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Loading, Edit } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import { getKnowledgeDetail, likeKnowledge, unlikeKnowledge, checkLiked } from '@/api/knowledge'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const item = ref(null)
const isLiked = ref(false)
const likeCount = ref(0)
const liking = ref(false)

// 判断是否可以编辑：作者本人即可（所有状态）
const canEdit = computed(() => {
  if (!userStore.isLoggedIn || !item.value) return false
  return item.value.userId === userStore.userInfo.id
})

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已发布', 2: '已驳回' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'bg-yellow-100 text-yellow-700', 1: 'bg-green-100 text-green-700', 2: 'bg-red-100 text-red-700' }
  return map[status] || 'bg-gray-100 text-gray-700'
}

const goBack = () => {
  router.push('/course?tab=knowledge')
}

const goToEdit = () => {
  router.push(`/knowledge/add?from=detail&id=${item.value.id}`)
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getKnowledgeDetail(route.params.id)
    if (res.code === 200) {
      item.value = res.data
      likeCount.value = item.value.likeCount || 0
      if (userStore.isLoggedIn) {
        const likedRes = await checkLiked(route.params.id, userStore.userInfo.id)
        if (likedRes.code === 200) {
          isLiked.value = likedRes.data
        }
      }
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  } finally {
    loading.value = false
  }
}

const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  liking.value = true
  try {
    if (isLiked.value) {
      const res = await unlikeKnowledge(route.params.id, userStore.userInfo.id)
      if (res.code === 200) {
        isLiked.value = false
        likeCount.value--
        ElMessage.success('已取消点赞')
      }
    } else {
      const res = await likeKnowledge(route.params.id, userStore.userInfo.id)
      if (res.code === 200) {
        isLiked.value = true
        likeCount.value++
        ElMessage.success('点赞成功')
      }
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    liking.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
}

const handleImageError = (e) => {
  e.target.src = 'https://picsum.photos/id/1/400/300'
}

onMounted(() => {
  fetchDetail()
})
</script>