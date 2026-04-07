<!-- src/views/MarketView.vue -->
<template>
  <div class="min-h-screen bg-ich-bg">
    <Navbar />
    <div class="max-w-7xl mx-auto px-4 py-12">
      <div class="text-center mb-12">
        <h2 class="text-4xl font-bold text-ich-dark mb-2">非遗市集</h2>
        <p class="text-gray-500">匠心好物，云端选购</p>
      </div>
      
      <!-- 购物车和订单入口 -->
      <div class="flex justify-end items-center gap-3 mb-6">
        <el-button type="danger" plain @click="$router.push('/cart')">
          <el-icon><ShoppingCart /></el-icon> 我的购物车
          <span v-if="cartCount > 0" class="ml-1 bg-ich-red text-white rounded-full px-1.5 py-0.5 text-xs">{{ cartCount > 99 ? '99+' : cartCount }}</span>
        </el-button>
        <el-button type="danger" plain @click="$router.push('/orders')">
          <el-icon><Document /></el-icon> 我的订单
        </el-button>
      </div>
      
      <!-- 筛选+搜索 -->
      <div class="flex flex-wrap gap-4 mb-8">
        <el-input
          v-model="searchKey"
          placeholder="搜索非遗商品..."
          class="w-64"
          clearable
          @clear="fetchData"
          @keyup.enter="fetchData"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <div class="flex flex-wrap gap-2">
          <el-button
            :type="activeCate === 'all' ? 'danger' : 'default'"
            @click="activeCate = 'all'; fetchData()"
          >
            全部商品
          </el-button>
          <el-button
            v-for="cat in categories"
            :key="cat"
            :type="activeCate === cat ? 'danger' : 'default'"
            @click="activeCate = cat; fetchData()"
          >
            {{ cat }}
          </el-button>
        </div>
      </div>
      
      <!-- 商品列表 -->
      <div v-loading="loading" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        <div
          v-for="item in productList"
          :key="item.id"
          class="bg-white rounded-xl shadow-lg overflow-hidden hover:scale-105 transition-all duration-300 border border-ich-gold/30 cursor-pointer"
          @click="goToDetail(item.id)"
        >
          <div class="relative">
            <img :src="item.cover" :alt="item.name" class="w-full h-48 object-cover" @error="handleImageError">
            <div v-if="item.discount" class="absolute top-2 left-2 bg-ich-red text-white text-xs px-2 py-1 rounded">
              {{ item.discount }}折
            </div>
            <div v-if="item.stock <= 0" class="absolute inset-0 bg-black/50 flex items-center justify-center">
              <span class="text-white font-bold">售罄</span>
            </div>
          </div>
          <div class="p-4">
            <h3 class="text-lg font-bold text-ich-dark mb-1 line-clamp-1">{{ item.name }}</h3>
            <p class="text-sm text-gray-500 mb-2 line-clamp-2">{{ item.intro }}</p>
            <div class="flex justify-between items-center">
              <div>
                <span class="text-ich-red font-bold text-lg">¥{{ item.price }}</span>
                <span class="text-xs text-gray-400 line-through ml-1">¥{{ item.originalPrice }}</span>
              </div>
              <el-button 
                size="small" 
                type="danger" 
                :disabled="item.stock <= 0"
                @click.stop="addToCart(item)"
              >
                加入购物车
              </el-button>
            </div>
            <div class="flex justify-between items-center mt-3 text-xs text-gray-400">
              <span>销量 {{ item.sales }}+</span>
              <span>库存 {{ item.stock }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="!loading && productList.length === 0" class="text-center py-16 text-gray-500">
        <el-icon :size="48"><ShoppingBag /></el-icon>
        <p class="mt-2">暂无相关商品</p>
      </div>
      
      <!-- 分页 -->
      <div v-if="total > 0" class="mt-12 flex justify-center">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[8, 16, 24]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>
    
    <!-- 购物车侧边栏 -->
    <el-drawer v-model="cartVisible" title="购物车" direction="rtl" size="400px">
      <div v-if="cartItems.length === 0" class="text-center py-20 text-gray-500">
        <el-icon :size="48"><ShoppingCart /></el-icon>
        <p class="mt-2">购物车还是空的</p>
        <el-button class="mt-4" type="danger" @click="cartVisible = false; $router.push('/market')">去逛逛</el-button>
      </div>
      <div v-else>
        <div v-for="item in cartItems" :key="item.id" class="flex gap-4 py-4 border-b">
          <img :src="item.cover" class="w-20 h-20 object-cover rounded" @error="handleImageError">
          <div class="flex-1">
            <h4 class="font-bold">{{ item.name }}</h4>
            <p class="text-ich-red">¥{{ item.price }}</p>
            <div class="flex items-center gap-2 mt-2">
              <el-input-number v-model="item.quantity" :min="1" :max="item.stock" size="small" @change="updateCart" />
              <el-button size="small" type="danger" text @click="removeFromCart(item.id)">删除</el-button>
            </div>
          </div>
        </div>
        <div class="mt-6 pt-4 border-t">
          <div class="flex justify-between text-lg font-bold">
            <span>总计</span>
            <span class="text-ich-red">¥{{ totalPrice }}</span>
          </div>
          <div class="flex gap-3 mt-4">
            <el-button class="flex-1" @click="cartVisible = false">继续购物</el-button>
            <el-button class="flex-1" type="danger" @click="goToCartPage">查看购物车</el-button>
          </div>
        </div>
      </div>
    </el-drawer>
    
    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, ShoppingBag, ShoppingCart, Document } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import { getProductList } from '@/api/product'

const router = useRouter()
const loading = ref(false)
const productList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
const searchKey = ref('')
const activeCate = ref('all')
const cartVisible = ref(false)

const categories = ['手工艺品', '文化周边', '非遗食材']

// 购物车
const cartItems = ref([])

// 购物车总价
const totalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0).toFixed(2)
})

// 购物车数量
const cartCount = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
})

// 加载购物车
const loadCart = () => {
  const cart = localStorage.getItem('cart')
  if (cart) {
    cartItems.value = JSON.parse(cart)
  }
}

// 更新购物车到本地存储
const updateCart = () => {
  localStorage.setItem('cart', JSON.stringify(cartItems.value))
}

// 加入购物车
const addToCart = (item) => {
  const exist = cartItems.value.find(i => i.id === item.id)
  if (exist) {
    exist.quantity++
  } else {
    cartItems.value.push({ ...item, quantity: 1 })
  }
  updateCart()
  ElMessage.success('已加入购物车')
  cartVisible.value = true
}

// 从购物车移除
const removeFromCart = (id) => {
  cartItems.value = cartItems.value.filter(i => i.id !== id)
  updateCart()
  ElMessage.success('已移除')
}

// 跳转到购物车页面
const goToCartPage = () => {
  cartVisible.value = false
  router.push('/cart')
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKey.value || undefined,
      category: activeCate.value === 'all' ? undefined : activeCate.value
    }
    const res = await getProductList(params)
    if (res.code === 200) {
      productList.value = res.data.records || []
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/market/detail/${id}`)
}

const handleImageError = (e) => {
  e.target.src = 'https://picsum.photos/id/1/400/400'
}

onMounted(() => {
  loadCart()
  fetchData()
})
</script>