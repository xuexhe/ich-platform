<!-- src/views/CheckoutView.vue -->
<template>
  <div class="min-h-screen bg-ich-bg">
    <Navbar />
    <div class="max-w-4xl mx-auto px-4 py-12">
      <el-button type="text" class="mb-6 text-ich-dark" @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      
      <h2 class="text-2xl font-bold text-ich-dark mb-6">确认订单</h2>
      
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 订单商品列表 -->
        <div class="lg:col-span-2">
          <div class="bg-white rounded-xl shadow-lg p-6">
            <h3 class="text-lg font-bold text-ich-dark mb-4">商品列表</h3>
            <div v-for="item in cartItems" :key="item.id" class="flex gap-4 py-4 border-b last:border-0">
              <img :src="item.cover" class="w-20 h-20 object-cover rounded" @error="handleImageError" />
              <div class="flex-1">
                <h4 class="font-bold">{{ item.name }}</h4>
                <p class="text-gray-500 text-sm">{{ item.intro }}</p>
                <div class="flex justify-between items-center mt-2">
                  <span class="text-ich-red font-bold">¥{{ item.price }}</span>
                  <span class="text-gray-400">x{{ item.quantity }}</span>
                </div>
              </div>
            </div>
            
            <div class="mt-4 pt-4 border-t flex justify-between">
              <span class="text-gray-500">共 {{ totalCount }} 件商品</span>
              <span class="text-lg font-bold text-ich-red">合计：¥{{ totalPrice }}</span>
            </div>
          </div>
        </div>
        
        <!-- 收货信息 + 支付 -->
        <div class="lg:col-span-1">
          <div class="bg-white rounded-xl shadow-lg p-6 mb-6">
            <h3 class="text-lg font-bold text-ich-dark mb-4">收货信息</h3>
            <el-form :model="form" label-width="80px">
              <el-form-item label="收货人" required>
                <el-input v-model="form.receiver" placeholder="请输入收货人姓名" />
              </el-form-item>
              <el-form-item label="联系电话" required>
                <el-input v-model="form.phone" placeholder="请输入联系电话" />
              </el-form-item>
              <el-form-item label="收货地址" required>
                <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入详细收货地址" />
              </el-form-item>
            </el-form>
          </div>
          
          <div class="bg-white rounded-xl shadow-lg p-6">
            <h3 class="text-lg font-bold text-ich-dark mb-4">订单信息</h3>
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-gray-500">商品金额</span>
                <span>¥{{ totalPrice }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">运费</span>
                <span>¥0.00</span>
              </div>
              <div class="border-t pt-2 mt-2 flex justify-between">
                <span class="font-bold">实付金额</span>
                <span class="text-2xl font-bold text-ich-red">¥{{ totalPrice }}</span>
              </div>
            </div>
            
            <el-button type="danger" size="large" class="w-full mt-6" @click="submitOrder" :loading="submitting">
              提交订单
            </el-button>
          </div>
        </div>
      </div>
    </div>
    <Footer />
    
    <!-- 支付弹窗 -->
    <el-dialog v-model="payDialogVisible" title="模拟支付" width="350px">
      <div class="text-center py-4">
        <div class="text-6xl mb-4">💰</div>
        <p class="text-gray-500">订单号：{{ newOrder?.orderNo }}</p>
        <p class="text-gray-500">订单金额：<span class="text-ich-red text-xl font-bold">¥{{ newOrder?.totalAmount }}</span></p>
        <p class="text-sm text-gray-400 mt-2">这是演示支付，点击确认即完成支付</p>
        <el-button class="mt-6" type="danger" size="large" @click="confirmPay" :loading="paying">确认支付</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import { createOrder, payOrder } from '@/api/order'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const cartItems = ref([])
const submitting = ref(false)
const paying = ref(false)
const payDialogVisible = ref(false)
const newOrder = ref(null)

const form = ref({
  receiver: '',
  phone: '',
  address: ''
})

const totalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0).toFixed(2)
})

const totalCount = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
})

const submitOrder = async () => {
  if (!form.value.receiver) {
    ElMessage.warning('请输入收货人姓名')
    return
  }
  if (!form.value.phone) {
    ElMessage.warning('请输入联系电话')
    return
  }
  if (!form.value.address) {
    ElMessage.warning('请输入收货地址')
    return
  }
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车为空')
    router.push('/market')
    return
  }
  
  submitting.value = true
  try {
    const orderData = {
      userId: userStore.userInfo.id,
      totalAmount: parseFloat(totalPrice.value),
      receiver: form.value.receiver,
      phone: form.value.phone,
      address: form.value.address,
      items: cartItems.value.map(item => ({
        productId: item.id,
        productName: item.name,
        productCover: item.cover,
        quantity: item.quantity,
        price: item.price
      }))
    }
    
    const res = await createOrder(orderData)
    if (res.code === 200) {
      newOrder.value = res.data
      payDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '创建订单失败')
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error('创建订单失败')
  } finally {
    submitting.value = false
  }
}

const confirmPay = async () => {
  paying.value = true
  try {
    const res = await payOrder(newOrder.value.id)
    if (res.code === 200) {
      ElMessage.success('支付成功')
      payDialogVisible.value = false
      // 清空购物车中已结算的商品
      const remainingCart = JSON.parse(localStorage.getItem('cart') || '[]')
      const currentIds = cartItems.value.map(item => item.id)
      const newCart = remainingCart.filter(item => !currentIds.includes(item.id))
      localStorage.setItem('cart', JSON.stringify(newCart))
      // 跳转到订单列表
      router.push('/orders')
    } else {
      ElMessage.error('支付失败')
    }
  } catch (error) {
    ElMessage.error('支付失败')
  } finally {
    paying.value = false
  }
}

const handleImageError = (e) => {
  e.target.src = 'https://picsum.photos/id/1/100/100'
}

onMounted(() => {
  // 优先使用 sessionStorage 中选中的商品
  const checkoutItems = sessionStorage.getItem('checkoutItems')
  if (checkoutItems) {
    cartItems.value = JSON.parse(checkoutItems)
    sessionStorage.removeItem('checkoutItems')
  } else {
    const cart = localStorage.getItem('cart')
    if (cart) {
      cartItems.value = JSON.parse(cart)
    }
  }
  
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车为空')
    router.push('/market')
  }
  
  // 预填用户信息
  if (userStore.userInfo) {
    form.value.receiver = userStore.userInfo.nickname || userStore.userInfo.username
  }
})
</script>