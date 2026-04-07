<template>
  <div class="min-h-screen bg-ich-bg">
    <Navbar />
    <div class="max-w-4xl mx-auto px-4 py-12">
      <el-button type="text" class="mb-8 text-ich-dark" @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回列表
      </el-button>
      
      <div v-if="loading" class="flex justify-center py-20">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      </div>
      
      <div v-else-if="inheritor" class="bg-white rounded-xl shadow-lg overflow-hidden">
        <div class="relative h-64 bg-gradient-to-r from-ich-dark to-ich-dark/80">
          <div class="absolute bottom-0 left-0 right-0 p-8 text-white">
            <h1 class="text-3xl font-bold">{{ inheritor.name }}</h1>
            <p class="text-ich-gold mt-2">{{ inheritor.heritageName || '非遗传承人' }}</p>
          </div>
        </div>
        
        <div class="p-8">
          <div class="flex flex-col md:flex-row gap-8">
            <img 
              :src="inheritor.avatar" 
              class="w-48 h-48 rounded-full object-cover border-4 border-ich-gold"
              @error="handleImageError"
            />
            <div class="flex-1">
              <div class="mb-6">
                <h3 class="text-xl font-bold text-ich-dark mb-2">📖 传承简介</h3>
                <p class="text-gray-700 leading-relaxed">{{ inheritor.intro }}</p>
              </div>
              <div class="mb-6">
                <h3 class="text-xl font-bold text-ich-dark mb-2">🏆 传承经历</h3>
                <p class="text-gray-700 leading-relaxed">{{ inheritor.experience }}</p>
              </div>
              <div v-if="inheritor.honor">
                <h3 class="text-xl font-bold text-ich-dark mb-2">✨ 荣誉成就</h3>
                <p class="text-gray-700 leading-relaxed">{{ inheritor.honor }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="text-center py-20">
        <el-icon :size="48"><User /></el-icon>
        <p class="mt-4 text-gray-500">传承人信息不存在</p>
        <el-button class="mt-4" type="danger" @click="$router.push('/inheritor')">
          返回列表
        </el-button>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Loading, User } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import { getInheritorDetail } from '@/api/inheritor'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const inheritor = ref(null)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getInheritorDetail(route.params.id)
    if (res.code === 200 && res.data) {
      inheritor.value = res.data
    } else {
      // 模拟数据
      inheritor.value = getMockInheritor(route.params.id)
    }
  } catch (error) {
    inheritor.value = getMockInheritor(route.params.id)
  } finally {
    loading.value = false
  }
}

const getMockInheritor = (id) => {
  const mockData = {
    1: {
      id: 1,
      name: '姚惠芬',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Yao',
      intro: '苏绣国家级代表性传承人，自幼学习苏绣技艺。从事苏绣创作40余年，作品多次获得国家级金奖。',
      experience: '8岁开始学习苏绣，师从多位苏绣大师。作品《猫》系列被多家博物馆收藏。致力于苏绣技艺的传承与创新，培养传承人近百名。',
      honor: '中国工艺美术大师、国家级非遗传承人',
      heritageName: '苏绣'
    },
    2: {
      id: 2,
      name: '汪天稳',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Wang',
      intro: '皮影戏国家级代表性传承人，皮影雕刻大师。',
      experience: '从事皮影艺术50余年，雕刻作品数千件。独创的雕刻技法被誉为"汪氏皮影"。培养传承人近百名，作品被国内外多家博物馆收藏。',
      honor: '中国皮影艺术大师、国家级非遗传承人',
      heritageName: '皮影戏'
    },
    3: {
      id: 3,
      name: '库淑兰',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Ku',
      intro: '剪纸艺术大师，被誉为"剪花娘子"。',
      experience: '独创彩色剪纸艺术，将传统剪纸与现代审美相结合。作品风格独特，色彩艳丽，被多家博物馆收藏。',
      honor: '中国民间文艺山花奖得主',
      heritageName: '剪纸'
    }
  }
  return mockData[id] || null
}

const handleImageError = (e) => {
  e.target.src = 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + inheritor.value?.name
}

onMounted(() => {
  fetchData()
})
</script>