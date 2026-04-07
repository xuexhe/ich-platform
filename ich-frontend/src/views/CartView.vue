<!-- src/views/CartView.vue -->
<template>
  <div class="min-h-screen bg-ich-bg">
    <Navbar />
    <div class="max-w-6xl mx-auto px-4 py-12">
      <div class="flex items-center gap-3 mb-8">
        <el-button type="text" @click="$router.back()" class="text-ich-dark">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <h2 class="text-3xl font-bold text-ich-dark">我的购物车</h2>
      </div>
      
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- 购物车商品列表 -->
        <div class="lg:col-span-2">
          <div v-if="cartItems.length === 0" class="bg-white rounded-xl shadow-lg p-12 text-center">
            <el-icon :size="64" class="text-gray-300"><ShoppingCart /></el-icon>
            <p class="text-gray-500 mt-4">购物车还是空的</p>
            <el-button class="mt-6" type="danger" @click="$router.push('/market')">去逛逛</el-button>
          </div>
          
          <div v-else class="bg-white rounded-xl shadow-lg overflow-hidden">
            <div class="p-4 bg-gray-50 border-b flex items-center">
              <el-checkbox v-model="selectAll" @change="toggleSelectAll" class="mr-4">全选</el-checkbox>
              <span class="text-gray-500 text-sm">共 {{ cartItems.length }} 件商品</span>
            </div>
            
            <div v-for="item in cartItems" :key="item.id" class="p-4 border-b hover:bg-gray-50 transition">
              <div class="flex gap-4">
                <el-checkbox v-model="item.selected" @change="updateSelected" class="self-center" />
                <img :src="item.cover" class="w-24 h-24 object-cover rounded-lg" @error="handleImageError" />
                <div class="flex-1">
                  <div class="flex justify-between">
                    <h4 class="font-bold text-ich-dark">{{ item.name }}</h4>
                    <el-button type="danger" text size="small" @click="removeFromCart(item.id)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                  <p class="text-gray-500 text-sm mt-1 line-clamp-2">{{ item.intro }}</p>
                  <div class="flex justify-between items-center mt-3">
                    <div>
                      <span class="text-ich-red font-bold text-lg">¥{{ item.price }}</span>
                      <span class="text-gray-400 text-sm line-through ml-2">¥{{ item.originalPrice }}</span>
                    </div>
                    <el-input-number 
                      v-model="item.quantity" 
                      :min="1" 
                      :max="item.stock" 
                      size="small"
                      @change="updateCart"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 订单摘要 -->
        <div class="lg:col-span-1">
          <div class="bg-white rounded-xl shadow-lg p-6 sticky top-24">
            <h3 class="text-lg font-bold text-ich-dark mb-4 pb-2 border-b">订单摘要</h3>
            
            <div class="space-y-3">
              <div class="flex justify-between">
                <span class="text-gray-500">商品金额</span>
                <span>¥{{ selectedTotalPrice }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">运费</span>
                <span>¥0.00</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">优惠</span>
                <span>-¥0.00</span>
              </div>
              <div class="border-t pt-3 mt-3 flex justify-between">
                <span class="font-bold">合计</span>
                <span class="text-2xl font-bold text-ich-red">¥{{ selectedTotalPrice }}</span>
              </div>
              <p class="text-xs text-gray-400">已选 {{ selectedCount }} 件商品</p>
            </div>
            
            <el-button 
              type="danger" 
              size="large" 
              class="w-full mt-6"
              :disabled="selectedCount === 0"
              @click="goToCheckout"
            >
              去结算 ({{ selectedCount }})
            </el-button>
          </div>
        </div>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ShoppingCart, Delete } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'

const router = useRouter()
const cartItems = ref([])
const selectAll = ref(false)

// 计算选中商品总价
const selectedTotalPrice = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + item.price * item.quantity, 0)
    .toFixed(2)
})

// 计算选中商品数量
const selectedCount = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + item.quantity, 0)
})

// 加载购物车数据
const loadCart = () => {
  const cart = localStorage.getItem('cart')
  if (cart) {
    cartItems.value = JSON.parse(cart).map(item => ({
      ...item,
      selected: true
    }))
  }
  updateSelectAllState()
}

// 更新购物车到本地存储
const updateCart = () => {
  const toSave = cartItems.value.map(({ selected, ...item }) => item)
  localStorage.setItem('cart', JSON.stringify(toSave))
  updateSelectAllState()
}

// 移除商品
const removeFromCart = (id) => {
  cartItems.value = cartItems.value.filter(item => item.id !== id)
  updateCart()
  ElMessage.success('已移除')
}

// 全选/取消全选
const toggleSelectAll = () => {
  cartItems.value.forEach(item => {
    item.selected = selectAll.value
  })
  updateCart()
}

// 更新选中状态
const updateSelected = () => {
  updateSelectAllState()
  updateCart()
}

// 更新全选按钮状态
const updateSelectAllState = () => {
  if (cartItems.value.length === 0) {
    selectAll.value = false
    return
  }
  selectAll.value = cartItems.value.every(item => item.selected)
}

// 去结算
const goToCheckout = () => {
  if (selectedCount.value === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  // 保存选中的商品到 sessionStorage
  const selectedItems = cartItems.value.filter(item => item.selected)
  sessionStorage.setItem('checkoutItems', JSON.stringify(selectedItems))
  router.push('/checkout')
}

const handleImageError = (e) => {
  e.target.src = 'https://picsum.photos/id/1/100/100'
}

onMounted(() => {
  loadCart()
})
</script>