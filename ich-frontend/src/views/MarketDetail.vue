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
      
      <div v-else-if="product" class="bg-white rounded-xl shadow-lg overflow-hidden">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-8 p-8">
          <img 
            :src="product.cover" 
            class="w-full h-96 object-cover rounded-lg"
            @error="handleImageError"
          />
          <div>
            <h2 class="text-3xl font-bold text-ich-dark mb-4">{{ product.name }}</h2>
            <div class="inline-block bg-ich-gold/20 text-ich-gold px-3 py-1 rounded-full text-sm mb-4">
              {{ product.category }}
            </div>
            <p class="text-gray-600 mb-6">{{ product.intro }}</p>
            <div class="flex items-center gap-4 mb-6">
              <span class="text-3xl font-bold text-ich-red">¥{{ product.price }}</span>
              <span class="text-gray-400 line-through">¥{{ product.originalPrice }}</span>
              <span v-if="product.discount" class="bg-ich-red text-white px-2 py-1 rounded text-sm">
                {{ product.discount }}折
              </span>
            </div>
            <div class="border-t border-gray-100 pt-4 mb-6">
              <p class="text-gray-500 text-sm">库存：{{ product.stock }}件</p>
              <p class="text-gray-500 text-sm">销量：{{ product.sales }}+</p>
            </div>
            <div class="flex gap-4">
              <el-button type="danger" size="large" @click="addToCart" :disabled="product.stock <= 0">
                {{ product.stock <= 0 ? '已售罄' : '加入购物车' }}
              </el-button>
              <el-button size="large" @click="$router.back()">
                继续逛逛
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="text-center py-20">
        <el-icon :size="48"><Goods /></el-icon>
        <p class="mt-4 text-gray-500">商品不存在</p>
        <el-button class="mt-4" type="danger" @click="$router.push('/market')">
          返回市集
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
import { ArrowLeft, Loading, Goods } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import { getProductDetail } from '@/api/product'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const product = ref(null)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getProductDetail(route.params.id)
    if (res.code === 200 && res.data) {
      product.value = res.data
    } else {
      ElMessage.error('商品不存在')
      router.push('/market')
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    // 使用模拟数据避免404
    product.value = getMockProduct(route.params.id)
  } finally {
    loading.value = false
  }
}

// 模拟商品数据（后端接口未返回时使用）
const getMockProduct = (id) => {
  const mockProducts = {
    1: {
      id: 1,
      name: '苏绣手帕',
      category: '手工艺品',
      cover: 'https://picsum.photos/id/20/400/400',
      intro: '传统苏绣工艺，手工刺绣，图案精美，每一件都是独一无二的艺术品。',
      price: 128,
      originalPrice: 168,
      discount: 7.6,
      sales: 126,
      stock: 50
    },
    2: {
      id: 2,
      name: '皮影摆件',
      category: '手工艺品',
      cover: 'https://picsum.photos/id/30/400/400',
      intro: '非遗皮影戏手工雕刻摆件，家居装饰，传承千年文化。',
      price: 89,
      originalPrice: 109,
      discount: 8.2,
      sales: 98,
      stock: 80
    },
    3: {
      id: 3,
      name: '剪纸礼盒',
      category: '文化周边',
      cover: 'https://picsum.photos/id/40/400/400',
      intro: '传统剪纸艺术礼盒，节日送礼佳品，精美包装。',
      price: 69,
      originalPrice: 89,
      discount: 7.7,
      sales: 156,
      stock: 120
    },
    4: {
      id: 4,
      name: '陶瓷茶杯',
      category: '手工艺品',
      cover: 'https://picsum.photos/id/60/400/400',
      intro: '手工陶瓷茶杯，自然窑变，独一无二的艺术品。',
      price: 199,
      originalPrice: 259,
      discount: 7.7,
      sales: 76,
      stock: 30
    },
    5: {
      id: 5,
      name: '京剧脸谱钥匙扣',
      category: '文化周边',
      cover: 'https://picsum.photos/id/50/400/400',
      intro: '京剧脸谱创意钥匙扣，非遗文化小礼品，精致可爱。',
      price: 39,
      originalPrice: 49,
      discount: 7.9,
      sales: 218,
      stock: 200
    },
    6: {
      id: 6,
      name: '非遗茶叶礼盒',
      category: '非遗食材',
      cover: 'https://picsum.photos/id/70/400/400',
      intro: '传统茶艺非遗茶叶，手工制作，口感醇厚。',
      price: 258,
      originalPrice: 328,
      discount: 7.9,
      sales: 56,
      stock: 40
    }
  }
  return mockProducts[id] || null
}

const addToCart = () => {
  if (!product.value) return
  const cart = JSON.parse(localStorage.getItem('cart') || '[]')
  const exist = cart.find(item => item.id === product.value.id)
  if (exist) {
    exist.quantity++
  } else {
    cart.push({ ...product.value, quantity: 1 })
  }
  localStorage.setItem('cart', JSON.stringify(cart))
  ElMessage.success('已加入购物车')
}

const handleImageError = (e) => {
  e.target.src = 'https://picsum.photos/400/400?random=' + product.value?.id
}

onMounted(() => {
  fetchData()
})
</script>