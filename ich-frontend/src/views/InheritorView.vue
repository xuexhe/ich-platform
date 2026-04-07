<template>
  <div class="min-h-screen bg-ich-bg">
    <Navbar />
    <div class="max-w-7xl mx-auto px-4 py-12">
      <div class="text-center mb-12">
        <h2 class="text-4xl font-bold text-ich-dark mb-2">非遗传承人</h2>
        <p class="text-gray-500">匠心传承，守正创新</p>
      </div>
      
      <div v-loading="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        <div
          v-for="item in inheritorList"
          :key="item.id"
          class="bg-white rounded-xl shadow-lg overflow-hidden hover:scale-105 transition-all duration-300 border border-ich-gold/30 cursor-pointer"
          @click="goToDetail(item.id)"
        >
          <div class="relative">
            <img :src="item.avatar" :alt="item.name" class="w-full h-64 object-cover">
            <div class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/70 to-transparent p-6">
              <h3 class="text-xl font-bold text-white">{{ item.name }}</h3>
              <p class="text-ich-gold text-sm">{{ item.heritageName || '非遗传承人' }}</p>
            </div>
          </div>
          <div class="p-6">
            <h4 class="font-bold text-ich-dark mb-2">传承简介</h4>
            <p class="text-gray-600 mb-4 line-clamp-2">{{ item.intro }}</p>
            <h4 class="font-bold text-ich-dark mb-2">传承经历</h4>
            <p class="text-gray-500 text-sm line-clamp-3">{{ item.experience }}</p>
            <div class="mt-4 flex justify-end">
              <el-button type="text" class="text-ich-red" @click.stop="goToDetail(item.id)">
                查看更多 →
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <div v-if="!loading && inheritorList.length === 0" class="text-center py-16 text-gray-500">
        <el-icon :size="48"><User /></el-icon>
        <p class="mt-2">暂无传承人数据</p>
      </div>
    </div>
    
    <!-- 传承人详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="currentInheritor.name" width="600px">
      <div class="flex flex-col md:flex-row gap-6">
        <img :src="currentInheritor.avatar" class="w-40 h-40 rounded-full object-cover mx-auto md:mx-0" />
        <div class="flex-1">
          <div class="mb-4">
            <h4 class="font-bold text-ich-dark">传承项目</h4>
            <p class="text-gray-600">{{ currentInheritor.heritageName || '非遗项目' }}</p>
          </div>
          <div class="mb-4">
            <h4 class="font-bold text-ich-dark">传承简介</h4>
            <p class="text-gray-600">{{ currentInheritor.intro }}</p>
          </div>
          <div class="mb-4">
            <h4 class="font-bold text-ich-dark">传承经历</h4>
            <p class="text-gray-600 whitespace-pre-wrap">{{ currentInheritor.experience }}</p>
          </div>
          <div v-if="currentInheritor.honor">
            <h4 class="font-bold text-ich-dark">荣誉成就</h4>
            <p class="text-gray-600">{{ currentInheritor.honor }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
    
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import { getInheritorList } from '@/api/inheritor'
import { getHeritageList } from '@/api/heritage'

const loading = ref(false)
const inheritorList = ref([])
const detailVisible = ref(false)
const currentInheritor = ref({})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getInheritorList()
    if (res.code === 200) {
      // 获取非遗项目名称
      const heritageRes = await getHeritageList({ pageSize: 100 })
      const heritageMap = {}
      if (heritageRes.code === 200) {
        (heritageRes.data.records || []).forEach(h => {
          heritageMap[h.id] = h.name
        })
      }
      inheritorList.value = (res.data || []).map(item => ({
        ...item,
        heritageName: heritageMap[item.heritageId] || '非遗项目'
      }))
    }
  } catch (error) {
    console.error('获取传承人列表失败:', error)
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  const inheritor = inheritorList.value.find(i => i.id === id)
  if (inheritor) {
    currentInheritor.value = inheritor
    detailVisible.value = true
  }
}

onMounted(() => {
  fetchData()
})
</script>