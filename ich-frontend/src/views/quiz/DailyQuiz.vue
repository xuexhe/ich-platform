<!-- src/views/quiz/DailyQuiz.vue -->
<template>
  <div>
    <!-- 答题统计卡片 -->
    <div v-if="userStore.isLoggedIn" class="bg-white rounded-xl shadow-lg p-6 mb-8">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="w-16 h-16 rounded-full bg-ich-gold/20 flex items-center justify-center">
            <span class="text-3xl">🏆</span>
          </div>
          <div>
            <p class="text-gray-500 text-sm">累计答对</p>
            <p class="text-2xl font-bold text-ich-red">{{ stats.correctCount }} 题</p>
          </div>
        </div>
        <div class="w-px h-12 bg-gray-200"></div>
        <div class="flex items-center gap-4">
          <div class="w-16 h-16 rounded-full bg-green-500/20 flex items-center justify-center">
            <span class="text-3xl">📅</span>
          </div>
          <div>
            <p class="text-gray-500 text-sm">今日状态</p>
            <p class="text-xl font-bold" :class="stats.todayAnswered ? 'text-green-500' : 'text-orange-500'">
              {{ stats.todayAnswered ? '已完成' : '未答题' }}
            </p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 题目卡片 -->
    <div class="bg-white rounded-2xl shadow-xl overflow-hidden">
      <div class="bg-gradient-to-r from-ich-dark to-ich-dark/80 p-6 text-white">
        <div class="flex justify-between items-center">
          <div>
            <span class="text-sm opacity-80">📅 {{ todayDate }}</span>
            <h3 class="text-xl font-bold mt-1">今日非遗知识挑战</h3>
          </div>
          <div class="text-right">
            <span class="text-3xl">🎯</span>
          </div>
        </div>
      </div>
      
      <!-- 未答题状态 - 显示题目 -->
      <div class="p-8" v-if="quiz && !stats.todayAnswered && !result">
        <p class="text-xl font-medium text-ich-dark mb-6">{{ quiz.question }}</p>
        
        <div class="space-y-3">
          <div 
            v-for="(option, key) in options" 
            :key="key"
            class="quiz-option"
            :class="{ 'selected': selectedAnswer === key }"
            @click="selectAnswer(key)"
          >
            <span class="option-letter">{{ key }}</span>
            <span class="option-text">{{ option }}</span>
          </div>
        </div>
        
        <div class="mt-8 flex justify-center">
          <el-button 
            type="danger" 
            size="large" 
            round
            :disabled="!selectedAnswer || submitting"
            :loading="submitting"
            @click="handleSubmitAnswer"
          >
            提交答案
          </el-button>
        </div>
      </div>
      
      <!-- 刚答完题 - 显示结果和答案 -->
      <div class="p-8" v-else-if="result">
        <div class="text-center">
          <div class="text-6xl mb-4">
            {{ result.isCorrect ? '🎉' : '😢' }}
          </div>
          <h3 class="text-2xl font-bold mb-2" :class="result.isCorrect ? 'text-green-500' : 'text-red-500'">
            {{ result.message }}
          </h3>
          
          <!-- 显示正确答案和解析 -->
          <div class="bg-gray-50 rounded-xl p-4 text-left mt-4">
            <p class="text-sm text-gray-600 mb-2">
              <span class="font-bold">📖 正确答案：</span>
              <span class="text-ich-red font-medium">{{ correctAnswerLetter }}. {{ correctAnswerText }}</span>
            </p>
            <p class="text-sm text-gray-600">
              <span class="font-bold">📖 解析：</span>{{ result.explanation }}
            </p>
          </div>
          
          <el-button class="mt-6" type="danger" plain @click="resetAndRefresh">关闭</el-button>
        </div>
      </div>
      
      <!-- 已答题状态（今天已经答过题）- 显示题目和答案回顾 -->
      <div class="p-8" v-else-if="stats.todayAnswered && quiz && !result">
        <div class="text-center mb-6">
          <div class="text-5xl mb-2">✅</div>
          <h3 class="text-xl font-bold text-ich-dark mb-2">今日已完成答题</h3>
          <p class="text-gray-500">明天再来挑战吧！</p>
        </div>
        
        <!-- 显示今日题目和答案回顾 -->
        <div class="bg-gray-50 rounded-xl p-5 mt-4">
          <p class="font-bold text-ich-dark mb-3 flex items-center gap-2">
            <span>📖</span> 今日题目回顾
          </p>
          <p class="text-gray-700 mb-4">{{ quiz.question }}</p>
          
          <div class="bg-white rounded-lg p-3 mb-3">
            <p class="text-sm text-gray-600">
              <span class="font-bold text-ich-red">正确答案：</span>
              {{ correctAnswerLetter }}. {{ correctAnswerText }}
            </p>
          </div>
          
          <p class="text-sm text-gray-600">
            <span class="font-bold">📖 解析：</span>{{ quiz.explanation }}
          </p>
        </div>
        
        <div class="flex justify-center mt-6">
          <el-button type="danger" plain @click="refreshQuiz">刷新</el-button>
        </div>
      </div>
      
      <!-- 加载中 -->
      <div class="p-12 text-center" v-if="loading">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p class="mt-2 text-gray-500">加载题目中...</p>
      </div>
      
      <!-- 无题目 -->
      <div class="p-12 text-center" v-else-if="!quiz && !loading">
        <div class="text-6xl mb-4">📚</div>
        <h3 class="text-xl font-bold text-ich-dark mb-2">暂无今日题目</h3>
        <p class="text-gray-500">管理员尚未设置今日题目，请稍后再来</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getTodayQuiz, submitAnswer, getUserQuizStats } from '@/api/quiz'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const quiz = ref(null)
const selectedAnswer = ref('')
const result = ref(null)
const stats = ref({ correctCount: 0, todayAnswered: false })

const options = computed(() => {
  if (!quiz.value) return {}
  return {
    'A': quiz.value.optionA,
    'B': quiz.value.optionB,
    'C': quiz.value.optionC,
    'D': quiz.value.optionD
  }
})

const correctAnswerLetter = computed(() => {
  if (!quiz.value) return ''
  return quiz.value.correctAnswer
})

const correctAnswerText = computed(() => {
  if (!quiz.value || !quiz.value.correctAnswer) return ''
  const map = {
    'A': quiz.value.optionA,
    'B': quiz.value.optionB,
    'C': quiz.value.optionC,
    'D': quiz.value.optionD
  }
  return map[quiz.value.correctAnswer] || ''
})

const todayDate = computed(() => {
  const date = new Date()
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
})

const selectAnswer = (key) => {
  selectedAnswer.value = key
}

const resetAndRefresh = () => {
  result.value = null
  selectedAnswer.value = ''
  fetchTodayQuiz()
  fetchStats()
}

const refreshQuiz = () => {
  fetchTodayQuiz()
  fetchStats()
}

const handleSubmitAnswer = async () => {
  if (!selectedAnswer.value) {
    ElMessage.warning('请选择一个答案')
    return
  }
  
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  submitting.value = true
  try {
    const res = await submitAnswer({
      userId: userStore.userInfo.id,
      quizId: quiz.value.id,
      answer: selectedAnswer.value
    })
    
    if (res.code === 200) {
      result.value = res.data
      await fetchStats()
      ElMessage.success(res.data.message)
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('提交答案失败:', error)
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

const fetchTodayQuiz = async () => {
  loading.value = true
  try {
    const userId = userStore.isLoggedIn ? userStore.userInfo.id : null
    const res = await getTodayQuiz(userId)
    if (res.code === 200) {
      quiz.value = res.data.quiz
      if (res.data.todayAnswered !== undefined) {
        stats.value.todayAnswered = res.data.todayAnswered
      }
    } else {
      console.error('获取题目失败:', res.message)
    }
  } catch (error) {
    console.error('获取题目失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getUserQuizStats(userStore.userInfo.id)
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

onMounted(() => {
  fetchTodayQuiz()
  if (userStore.isLoggedIn) {
    fetchStats()
  }
})
</script>

<style scoped>
.quiz-option {
  @apply flex items-center gap-4 p-4 rounded-xl border border-gray-200 cursor-pointer transition-all hover:border-ich-gold hover:bg-ich-gold/5;
}
.quiz-option.selected {
  @apply border-ich-red bg-ich-red/5;
}
.option-letter {
  @apply w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center font-bold text-gray-600;
}
.quiz-option.selected .option-letter {
  @apply bg-ich-red text-white;
}
.option-text {
  @apply flex-1 text-gray-700;
}
:deep(.is-loading) {
  animation: rotating 1s linear infinite;
}
@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>