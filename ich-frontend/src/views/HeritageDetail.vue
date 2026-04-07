<template>
  <div class="min-h-screen bg-ich-bg">
    <Navbar />
    <div class="max-w-7xl mx-auto px-4 py-12">
      <el-button type="text" class="mb-8 text-ich-dark" @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回列表
      </el-button>
      
      <div v-if="loading" class="flex justify-center py-20">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      </div>
      
      <div v-else-if="heritage.id" class="bg-white rounded-xl shadow-lg overflow-hidden">
        <img :src="heritage.cover" class="w-full h-96 object-cover" />
        
        <div class="p-8">
          <h2 class="text-3xl font-bold text-ich-dark mb-4">{{ heritage.name }}</h2>
          
          <div class="flex flex-wrap gap-4 mb-6">
            <div class="flex items-center">
              <span class="text-gray-500 mr-2">分类：</span>
              <span class="bg-ich-gold/20 text-ich-gold px-2 py-1 rounded text-sm">{{ heritage.category }}</span>
            </div>
            <div class="flex items-center">
              <span class="text-gray-500 mr-2">作者：</span>
              <span>{{ heritage.author || '未知' }}</span>
            </div>
            <div class="flex items-center">
              <el-icon><View /></el-icon>
              <span class="ml-1">{{ heritage.viewCount || 0 }}</span>
            </div>
            <div class="flex items-center">
              <el-icon><Star /></el-icon>
              <span class="ml-1">{{ heritage.collectCount || 0 }}</span>
            </div>
          </div>
          
          <div class="prose max-w-none text-gray-700 leading-relaxed">
            <h3 class="text-xl font-bold text-ich-dark mb-4">项目介绍</h3>
            <div v-html="heritage.content || '暂无详细介绍'"></div>
          </div>
          
          <div class="flex gap-4 mt-8">
            <el-button 
              type="danger" 
              :loading="collectLoading"
              @click="handleCollect"
            >
              {{ isCollected ? '取消收藏' : '收藏' }}
            </el-button>
            <el-button type="primary" @click="handleShare">
              <el-icon><Share /></el-icon> 分享
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 评论区 -->
      <div class="mt-12 bg-white rounded-xl shadow-lg p-8">
        <h3 class="text-2xl font-bold text-ich-dark mb-6">网友评论</h3>
        
        <div v-if="userStore.isLoggedIn" class="mb-6">
          <el-input
            v-model="commentContent"
            placeholder="分享你的看法..."
            type="textarea"
            :rows="3"
          />
          <el-button class="mt-3 bg-ich-red text-white" @click="submitComment">
            提交评论
          </el-button>
        </div>
        <div v-else class="mb-6 text-center py-4 bg-gray-50 rounded-lg">
          <p class="text-gray-500">
            <el-link type="primary" @click="$router.push('/login')">登录</el-link> 后发表评论
          </p>
        </div>
        
        <div class="mt-8">
          <div v-if="comments.length === 0" class="text-center py-8 text-gray-500">
            暂无评论，快来抢沙发～
          </div>
          <div v-for="item in comments" :key="item.id" class="border-b border-gray-200 pb-6 mb-6">
            <div class="flex items-center mb-2">
              <img :src="item.avatar || '/images/default-avatar.png'" class="w-10 h-10 rounded-full mr-3 object-cover" />
              <span class="font-medium">{{ item.nickname || '用户' }}</span>
              <span class="text-gray-400 text-sm ml-3">{{ formatTime(item.createTime) }}</span>
            </div>
            <p class="text-gray-700">{{ item.content }}</p>
          </div>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, View, Star, Share, Loading } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import { getHeritageDetail } from '@/api/heritage'
import { addCollect, cancelCollect, getMyCollects } from '@/api/collect'
import { addComment, getComments } from '@/api/comment'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const collectLoading = ref(false)
const heritage = ref({})
const isCollected = ref(false)
const commentContent = ref('')
const comments = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getHeritageDetail(route.params.id)
    if (res.code === 200) {
      heritage.value = res.data
    }
    
    // 获取评论
    const commentRes = await getComments(route.params.id)
    if (commentRes.code === 200) {
      comments.value = commentRes.data
    }
    
    // 检查是否已收藏
    if (userStore.isLoggedIn && userStore.userInfo.id) {
      const collectRes = await getMyCollects(userStore.userInfo.id)
      if (collectRes.code === 200) {
        isCollected.value = collectRes.data.some(c => c.heritageId === heritage.value.id)
      }
    }
  } catch (error) {
    console.error('获取详情失败:', error)
  } finally {
    loading.value = false
  }
}

const handleCollect = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  collectLoading.value = true
  try {
    if (isCollected.value) {
      await cancelCollect(userStore.userInfo.id, heritage.value.id)
      ElMessage.success('取消收藏成功')
      isCollected.value = false
      heritage.value.collectCount = (heritage.value.collectCount || 1) - 1
    } else {
      await addCollect(userStore.userInfo.id, heritage.value.id)
      ElMessage.success('收藏成功')
      isCollected.value = true
      heritage.value.collectCount = (heritage.value.collectCount || 0) + 1
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    collectLoading.value = false
  }
}

const handleShare = () => {
  navigator.clipboard.writeText(window.location.href)
  ElMessage.success('链接已复制，快来分享吧！')
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  try {
    await addComment(heritage.value.id, userStore.userInfo.id, commentContent.value)
    ElMessage.success('评论成功')
    commentContent.value = ''
    // 刷新评论
    const commentRes = await getComments(route.params.id)
    if (commentRes.code === 200) {
      comments.value = commentRes.data
    }
  } catch (error) {
    ElMessage.error('评论失败')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

onMounted(() => {
  fetchData()
})
</script>