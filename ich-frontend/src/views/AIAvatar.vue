<!-- src/views/AIAvatar.vue -->
<template>
  <div class="min-h-screen bg-ich-bg">
    <Navbar />
    <div class="max-w-4xl mx-auto px-4 py-12">
      <div class="text-center mb-12">
        <h2 class="text-4xl font-bold text-ich-dark mb-2">AI非遗数字讲解员</h2>
        <p class="text-gray-500">基于通义千问大模型，有问必答，为你详解非遗文化</p>
        <div class="mt-2 flex items-center justify-center gap-3">
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white shadow-sm">
            <span class="w-2 h-2 rounded-full" :class="aiStatus === 'connected' ? 'bg-green-500 animate-pulse' : 'bg-red-500'"></span>
            <span class="text-sm text-gray-600">
              {{ aiStatus === 'connected' ? '通义千问已连接' : aiStatus === 'checking' ? '检测中...' : '离线模式（使用本地知识库）' }}
            </span>
          </div>
          <el-button v-if="userStore.isLoggedIn" size="small" text type="primary" @click="showHistory = !showHistory">
            <el-icon><Clock /></el-icon> {{ showHistory ? '返回对话' : '历史记录' }}
          </el-button>
          <el-button v-if="userStore.isLoggedIn && showHistory" size="small" text type="danger" @click="clearHistory">
            <el-icon><Delete /></el-icon> 清空历史
          </el-button>
        </div>
      </div>
      
      <div class="bg-white rounded-xl shadow-lg overflow-hidden">
        <!-- AI头像区域 -->
        <div class="bg-gradient-to-r from-ich-dark to-ich-dark/80 text-white p-8 text-center">
          <div class="w-24 h-24 rounded-full bg-ich-gold mx-auto flex items-center justify-center mb-4">
            <span class="text-5xl">🎭</span>
          </div>
          <h3 class="text-2xl font-bold">非遗小助手</h3>
          <p class="opacity-80 mt-1">基于通义千问大模型，为你详解非遗文化</p>
          <div class="mt-3 flex justify-center gap-4 text-xs opacity-70">
            <span>📚 非遗知识库</span>
            <span>🤖 AI大模型</span>
            <span>💬 多轮对话</span>
            <span>📜 历史记录</span>
          </div>
        </div>
        
        <!-- 历史记录面板 -->
        <div v-if="showHistory && userStore.isLoggedIn" class="p-4 border-b bg-gray-50">
          <div class="flex justify-between items-center mb-3">
            <h4 class="font-bold text-ich-dark">对话历史</h4>
            <el-button size="small" type="danger" plain @click="clearHistory" :loading="clearingHistory">
              清空全部
            </el-button>
          </div>
          <div v-if="historyLoading" class="text-center py-8">
            <el-icon class="is-loading"><Loading /></el-icon>
          </div>
          <div v-else-if="historyList.length === 0" class="text-center py-8 text-gray-400">
            暂无对话历史
          </div>
          <div v-else class="space-y-2 max-h-64 overflow-y-auto">
            <div v-for="item in historyList" :key="item.id" 
                 class="p-3 bg-white rounded-lg cursor-pointer hover:shadow-md transition flex justify-between items-center"
                 @click="loadHistoryConversation(item)">
              <div class="flex-1 overflow-hidden">
                <p class="font-medium text-ich-dark truncate">{{ item.question }}</p>
                <p class="text-xs text-gray-400">{{ formatTime(item.createTime) }}</p>
              </div>
              <el-button size="small" text type="danger" @click.stop="deleteHistoryItem(item.id)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 聊天内容区 -->
        <div class="p-6 h-[480px] overflow-y-auto bg-ich-paper space-y-4" ref="chatContainer">
          <!-- 欢迎消息 -->
          <div class="flex gap-3">
            <div class="w-10 h-10 rounded-full bg-ich-gold flex items-center justify-center text-white shrink-0">
              AI
            </div>
            <div class="bg-white px-4 py-3 rounded-2xl shadow-sm max-w-[85%]">
              <p>你好！我是非遗数字讲解员，基于通义千问大模型为你服务。✨</p>
              <p class="text-sm text-gray-400 mt-2">你可以问我关于剪纸、苏绣、皮影戏、青花瓷、京剧等相关问题～</p>
            </div>
          </div>
          
          <!-- 聊天记录 -->
          <div v-for="(msg, idx) in chatList" :key="idx" class="flex gap-3" :class="{ 'justify-end': msg.role === 'user' }">
            <div v-if="msg.role === 'ai'" class="w-10 h-10 rounded-full bg-ich-gold flex items-center justify-center text-white shrink-0">
              AI
            </div>
            <div :class="[
              'px-4 py-3 rounded-2xl shadow-sm max-w-[85%]',
              msg.role === 'user' ? 'bg-ich-red text-white rounded-br-none' : 'bg-white rounded-bl-none'
            ]">
              <p class="whitespace-pre-wrap">{{ msg.content }}</p>
              <p v-if="msg.model" class="text-xs opacity-50 mt-1">{{ msg.model }}</p>
            </div>
            <div v-if="msg.role === 'user'" class="w-10 h-10 rounded-full bg-ich-dark flex items-center justify-center text-white shrink-0 order-1">
              <el-icon><User /></el-icon>
            </div>
          </div>
          
          <!-- 加载中 -->
          <div v-if="loading" class="flex gap-3">
            <div class="w-10 h-10 rounded-full bg-ich-gold flex items-center justify-center text-white shrink-0">
              AI
            </div>
            <div class="bg-white px-4 py-3 rounded-2xl shadow-sm">
              <div class="flex gap-1">
                <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0ms"></span>
                <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 150ms"></span>
                <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 300ms"></span>
              </div>
              <p class="text-xs text-gray-400 mt-1">通义千问正在思考中...</p>
            </div>
          </div>
        </div>
        
        <!-- 专属提示词区域 -->
        <div class="p-4 bg-white border-t border-gray-100">
          <div class="flex items-center gap-2 mb-2">
            <el-icon class="text-ich-gold"><MagicStick /></el-icon>
            <span class="text-sm font-medium text-gray-600">非遗专属提示词</span>
          </div>
          <div class="flex flex-wrap gap-2">
            <el-button 
              v-for="prompt in promptList" 
              :key="prompt.title"
              size="small"
              plain
              @click="sendQuickQuestion(prompt.content)"
              :disabled="loading"
            >
              {{ prompt.title }}
            </el-button>
          </div>
        </div>
        
        <!-- 快捷提问 -->
        <div class="p-4 bg-white border-t border-gray-100">
          <div class="flex flex-wrap gap-2">
            <el-button 
              v-for="q in quickQuestions" 
              :key="q"
              size="small"
              plain
              @click="sendQuickQuestion(q)"
              :disabled="loading"
            >
              {{ q }}
            </el-button>
          </div>
        </div>
        
        <!-- 输入区域 -->
        <div class="p-5 bg-white border-t border-gray-100">
          <div class="flex gap-3">
            <el-input
              v-model="inputContent"
              placeholder="输入你的问题..."
              class="flex-1"
              @keyup.enter="sendMessage"
              :disabled="loading"
              size="large"
            />
            <el-button type="danger" @click="sendMessage" :loading="loading" size="large">
              发送
            </el-button>
          </div>
          <p class="text-xs text-gray-400 mt-3 flex items-center gap-2">
            <span>✨ 通义千问大模型</span>
            <span>|</span>
            <span>💬 支持多轮对话</span>
            <span>|</span>
            <span>🎯 非遗知识库增强</span>
            <span>|</span>
            <span>📜 对话自动保存</span>
          </p>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { User, Clock, Delete, Loading, MagicStick } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import { useUserStore } from '@/store/user'
import { aiChat, getAIStatus, getAIHistory, clearAIHistory, deleteAIHistory, getAIPrompts } from '@/api/ai'

const userStore = useUserStore()
const inputContent = ref('')
const chatList = ref([])
const loading = ref(false)
const aiStatus = ref('checking')
const chatContainer = ref(null)
const showHistory = ref(false)
const historyList = ref([])
const historyLoading = ref(false)
const clearingHistory = ref(false)
const promptList = ref([])

const quickQuestions = [
  '什么是非物质文化遗产？',
  '苏绣的特点是什么？',
  '皮影戏的起源',
  '剪纸有哪些寓意？',
  '青花瓷是怎么制作的？',
  '京剧脸谱颜色代表什么？'
]

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 加载历史记录
const loadHistory = async () => {
  if (!userStore.isLoggedIn || !userStore.userId) return
  historyLoading.value = true
  try {
    const res = await getAIHistory(userStore.userId, 50)
    if (res.code === 200) {
      historyList.value = res.data || []
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
  } finally {
    historyLoading.value = false
  }
}

// 清空历史记录
const clearHistory = async () => {
  if (!userStore.isLoggedIn) return
  ElMessageBox.confirm('确定要清空所有对话历史吗？', '提示', { type: 'warning' }).then(async () => {
    clearingHistory.value = true
    try {
      const res = await clearAIHistory(userStore.userId)
      if (res.code === 200) {
        ElMessage.success('清空成功')
        historyList.value = []
        if (!showHistory.value) {
          // 如果不在历史面板，不清空当前对话
        }
      } else {
        ElMessage.error('清空失败')
      }
    } catch (error) {
      ElMessage.error('清空失败')
    } finally {
      clearingHistory.value = false
    }
  })
}

// 删除单条历史记录
const deleteHistoryItem = async (id) => {
  try {
    const res = await deleteAIHistory(id, userStore.userId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      historyList.value = historyList.value.filter(item => item.id !== id)
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 加载历史对话到当前聊天
const loadHistoryConversation = (item) => {
  // 清空当前对话
  chatList.value = []
  // 添加历史问答
  chatList.value.push({ role: 'user', content: item.question })
  chatList.value.push({ role: 'ai', content: item.answer, model: item.model })
  showHistory.value = false
  scrollToBottom()
}

// 检查 AI 服务状态
const checkAIStatus = async () => {
  try {
    const res = await getAIStatus()
    if (res.code === 200 && res.data?.status === 'connected') {
      aiStatus.value = 'connected'
    } else {
      aiStatus.value = 'offline'
    }
  } catch (error) {
    aiStatus.value = 'offline'
  }
}

// 加载专属提示词
const loadPrompts = async () => {
  try {
    const res = await getAIPrompts()
    if (res.code === 200) {
      promptList.value = res.data || []
    }
  } catch (error) {
    console.error('加载提示词失败:', error)
    // 默认提示词
    promptList.value = [
      { title: '介绍京剧', content: '请介绍一下京剧的起源和主要特点' },
      { title: '剪纸怎么做', content: '请教我如何制作简单的剪纸' },
      { title: '苏绣艺术', content: '苏绣有哪些独特的针法和艺术特点' },
      { title: '皮影戏历史', content: '皮影戏的历史渊源和表演形式' },
      { title: '青花瓷鉴赏', content: '如何鉴赏青花瓷的艺术价值' },
      { title: '端午节习俗', content: '端午节有哪些传统习俗和文化内涵' }
    ]
  }
}

const sendQuickQuestion = (text) => {
  inputContent.value = text
  sendMessage()
}

const sendMessage = async () => {
  const content = inputContent.value.trim()
  if (!content) return
  
  chatList.value.push({ role: 'user', content })
  inputContent.value = ''
  scrollToBottom()
  
  loading.value = true
  
  try {
    const res = await aiChat(content, userStore.userId || null)
    if (res.code === 200 && res.data?.answer) {
      chatList.value.push({ 
        role: 'ai', 
        content: res.data.answer,
        model: res.data.model || '通义千问'
      })
      // 刷新历史记录
      if (userStore.isLoggedIn) {
        await loadHistory()
      }
    } else {
      chatList.value.push({ 
        role: 'ai', 
        content: '抱歉，暂时无法回答你的问题，请换个话题试试～',
        model: '本地知识库'
      })
    }
  } catch (error) {
    console.error('AI调用失败:', error)
    chatList.value.push({ 
      role: 'ai', 
      content: aiStatus.value === 'connected' 
        ? '网络连接异常，请检查网络后重试。' 
        : 'AI服务未连接，当前使用本地知识库。请确保后端已配置通义千问API Key。',
      model: '本地知识库'
    })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

onMounted(() => {
  checkAIStatus()
  loadPrompts()
  if (userStore.isLoggedIn) {
    loadHistory()
  }
})
</script>

<style scoped>
.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
.animate-bounce {
  animation: bounce 0.6s infinite;
}
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}
</style>