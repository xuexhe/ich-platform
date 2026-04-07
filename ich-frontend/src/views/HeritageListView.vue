<template>
  <div class="min-h-screen bg-ich-bg">
    <Navbar />
    <div class="max-w-7xl mx-auto px-4 py-12">
      <div class="text-center mb-12">
        <h2 class="text-4xl font-bold text-ich-dark mb-2">非遗项目大全</h2>
        <p class="text-gray-500">探索千年传承的文化瑰宝</p>
      </div>
      
      <!-- 筛选+搜索 -->
      <div class="flex flex-wrap gap-4 mb-8">
        <el-input
          v-model="searchKey"
          placeholder="搜索非遗名称..."
          class="w-64"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <div class="flex flex-wrap gap-2">
          <el-button
            :type="activeCate === 'all' ? 'danger' : 'default'"
            @click="activeCate = 'all'"
          >
            全部
          </el-button>
          <el-button
            v-for="cat in categoryList"
            :key="cat"
            :type="activeCate === cat ? 'danger' : 'default'"
            @click="activeCate = cat"
          >
            {{ cat }}
          </el-button>
        </div>
      </div>
      
      <!-- 非遗列表 -->
      <div v-loading="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        <div
          v-for="item in heritageList"
          :key="item.id"
          class="bg-white rounded-xl shadow-lg overflow-hidden cursor-pointer hover:scale-105 transition-all duration-300 border border-ich-gold/30"
          @click="$router.push(`/heritage/detail/${item.id}`)"
        >
          <img :src="item.cover" :alt="item.name" class="w-full h-48 object-cover">
          <div class="p-6">
            <h3 class="text-xl font-bold text-ich-dark mb-2">{{ item.name }}</h3>
            <div class="inline-block bg-ich-gold/20 text-ich-gold px-2 py-1 rounded text-sm mb-3">
              {{ item.category }}
            </div>
            <p class="text-gray-600 line-clamp-2">{{ item.intro }}</p>
            <div class="flex justify-between items-center mt-4 text-sm text-gray-400">
              <span><el-icon><View /></el-icon> {{ item.viewCount || 0 }}</span>
              <span><el-icon><Star /></el-icon> {{ item.collectCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="!loading && heritageList.length === 0" class="text-center py-16 text-gray-500">
        <el-icon :size="48"><Document /></el-icon>
        <p class="mt-2">暂无相关非遗项目</p>
      </div>
      
      <!-- 分页 -->
      <div v-if="total > 0" class="mt-12 flex justify-center">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[12, 24, 36]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Search, View, Star, Document } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import { getHeritageList, getCategories } from '@/api/heritage'

const route = useRoute()
const loading = ref(false)
const heritageList = ref([])
const categoryList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
const searchKey = ref('')
const activeCate = ref('all')

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKey.value || undefined,
      category: activeCate.value === 'all' ? undefined : activeCate.value
    }
    const res = await getHeritageList(params)
    if (res.code === 200) {
      heritageList.value = res.data.records || []
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取非遗列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchData()
}

// 监听路由参数
watch(() => route.query.category, (newVal) => {
  if (newVal) {
    activeCate.value = newVal
    fetchData()
  }
})

// 监听分类变化
watch(activeCate, () => {
  pageNum.value = 1
  fetchData()
})

// 获取分类列表
const fetchCategories = async () => {
  const res = await getCategories()
  if (res.code === 200) {
    categoryList.value = res.data
  }
}

onMounted(() => {
  if (route.query.category) {
    activeCate.value = route.query.category
  }
  fetchCategories()
  fetchData()
})
</script>