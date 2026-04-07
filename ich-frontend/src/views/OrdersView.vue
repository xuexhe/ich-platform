<!-- src/views/OrdersView.vue -->
<template>
  <div class="min-h-screen bg-ich-bg pt-16">
    <Navbar />
    <div class="max-w-4xl mx-auto px-4 py-12">
      <div class="flex justify-between items-center mb-8">
        <h2 class="text-3xl font-bold text-ich-dark">我的订单</h2>
        <el-button type="danger" plain @click="$router.push('/market')">
          <el-icon><ShoppingCart /></el-icon> 继续购物
        </el-button>
      </div>
      
      <!-- 订单状态Tab -->
      <div class="flex gap-2 mb-6 border-b">
        <button 
          v-for="tab in tabs" 
          :key="tab.value" 
          class="px-6 py-2 transition relative"
          :class="activeTab === tab.value ? 'text-ich-red border-b-2 border-ich-red font-medium' : 'text-gray-500 hover:text-gray-700'"
          @click="activeTab = tab.value; fetchOrders()"
        >
          {{ tab.label }}
        </button>
      </div>
      
      <div v-loading="loading">
        <div v-for="order in orders" :key="order.id" class="bg-white rounded-xl shadow-md mb-4 overflow-hidden">
          <!-- 订单头部 -->
          <div class="p-4 border-b bg-gray-50 flex justify-between items-center">
            <span class="text-sm text-gray-500">订单号：{{ order.orderNo }}</span>
            <div class="flex items-center gap-3">
              <span class="text-sm px-2 py-1 rounded" :class="getStatusClass(order.status)">
                {{ getStatusText(order.status) }}
              </span>
              <!-- 删除按钮（已完成或已取消的订单可删除） -->
              <el-button 
                v-if="order.status === 2 || order.status === 3" 
                size="small" 
                type="danger" 
                plain
                @click="handleDeleteOrder(order.id)"
              >
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </div>
          </div>
          
          <!-- 订单商品 -->
          <div v-for="item in order.items" :key="item.id" class="p-4 flex gap-4 border-b">
            <img :src="item.productCover" class="w-20 h-20 object-cover rounded" @error="handleImageError" />
            <div class="flex-1">
              <h4 class="font-bold">{{ item.productName }}</h4>
              <p class="text-ich-red">¥{{ item.price }} × {{ item.quantity }}</p>
            </div>
          </div>
          
          <!-- 订单底部 -->
          <div class="p-4 flex justify-between items-center">
            <div class="text-gray-500 text-sm">
              收货人：{{ order.receiver }} {{ order.phone }}<br>
              地址：{{ order.address }}
            </div>
            <div class="text-right">
              <p class="text-lg font-bold text-ich-red">实付：¥{{ order.totalAmount }}</p>
              <div class="flex gap-2 mt-2">
                <el-button v-if="order.status === 0" size="small" type="danger" @click="handlePay(order.id)">去支付</el-button>
                <el-button v-if="order.status === 0" size="small" @click="handleCancel(order.id)">取消订单</el-button>
                <el-button v-if="order.status === 1" size="small" type="success" plain @click="handleConfirm(order.id)">确认收货</el-button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-if="!loading && orders.length === 0" class="text-center py-16">
          <el-icon :size="48" class="text-gray-300"><Document /></el-icon>
          <p class="mt-2 text-gray-500">暂无{{ getEmptyText }}订单</p>
          <el-button class="mt-4" type="danger" @click="$router.push('/market')">去逛逛</el-button>
        </div>
        
        <!-- 分页 -->
        <div v-if="total > 0" class="mt-6 flex justify-center">
          <el-pagination 
            v-model:current-page="pageNum" 
            v-model:page-size="pageSize" 
            :total="total" 
            :page-sizes="[5, 10, 20]"
            layout="total, sizes, prev, pager, next"
            @size-change="fetchOrders" 
            @current-change="fetchOrders" 
          />
        </div>
      </div>
    </div>
    <Footer />
    
    <!-- 支付弹窗 -->
    <el-dialog v-model="payDialogVisible" title="模拟支付" width="350px">
      <div class="text-center py-4">
        <div class="text-6xl mb-4">💰</div>
        <p class="text-gray-500">订单号：{{ payingOrder?.orderNo }}</p>
        <p class="text-gray-500">订单金额：<span class="text-ich-red text-xl font-bold">¥{{ payingOrder?.totalAmount }}</span></p>
        <p class="text-sm text-gray-400 mt-2">这是演示支付，点击确认即完成支付</p>
        <el-button class="mt-6" type="danger" size="large" @click="confirmPay" :loading="paying">确认支付</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, ShoppingCart, Delete } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import { getOrders, payOrder, cancelOrder, confirmOrder, deleteOrder } from '@/api/order'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const orders = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const activeTab = ref('all')
const payDialogVisible = ref(false)
const payingOrder = ref(null)
const paying = ref(false)

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待支付', value: '0' },
  { label: '已支付', value: '1' },
  { label: '已完成', value: '3' },
  { label: '已取消', value: '2' }
]

const getStatusText = (status) => {
  const map = { 0: '待支付', 1: '已支付', 2: '已取消', 3: '已完成' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 
    0: 'text-orange-500 bg-orange-50', 
    1: 'text-green-500 bg-green-50', 
    2: 'text-gray-400 bg-gray-50', 
    3: 'text-blue-500 bg-blue-50' 
  }
  return map[status] || 'text-gray-500 bg-gray-50'
}

const getEmptyText = computed(() => {
  const tab = tabs.find(t => t.value === activeTab.value)
  return tab ? tab.label : ''
})

const fetchOrders = async () => {
  if (!userStore.userInfo?.id) return
  
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    
    if (activeTab.value !== 'all') {
      params.status = parseInt(activeTab.value)
    }
    
    const res = await getOrders(userStore.userInfo.id, params)
    if (res.code === 200) {
      if (res.data && res.data.records) {
        orders.value = res.data.records
        total.value = res.data.total
      } else if (Array.isArray(res.data)) {
        orders.value = res.data
        total.value = res.data.length
      } else {
        orders.value = []
        total.value = 0
      }
    }
  } catch (error) {
    console.error('获取订单失败:', error)
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handlePay = (orderId) => {
  const order = orders.value.find(o => o.id === orderId)
  payingOrder.value = order
  payDialogVisible.value = true
}

const confirmPay = async () => {
  paying.value = true
  try {
    const res = await payOrder(payingOrder.value.id)
    if (res.code === 200) {
      ElMessage.success('支付成功')
      payDialogVisible.value = false
      fetchOrders()
    } else {
      ElMessage.error(res.message || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败')
  } finally {
    paying.value = false
  }
}

const handleCancel = async (orderId) => {
  ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await cancelOrder(orderId)
      if (res.code === 200) {
        ElMessage.success('取消成功')
        fetchOrders()
      } else {
        ElMessage.error(res.message || '取消失败')
      }
    } catch (error) {
      ElMessage.error('取消失败')
    }
  })
}

const handleConfirm = async (orderId) => {
  ElMessageBox.confirm('确认已收到商品？', '提示', { 
    type: 'info',
    confirmButtonText: '确认收货',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      const res = await confirmOrder(orderId)
      if (res.code === 200) {
        ElMessage.success('确认收货成功')
        fetchOrders()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } catch (error) {
      console.error('确认收货失败:', error)
      ElMessage.error('操作失败')
    }
  })
}

// 删除订单
const handleDeleteOrder = async (orderId) => {
  ElMessageBox.confirm('确定要删除该订单吗？删除后无法恢复。', '提示', { 
    type: 'warning',
    confirmButtonText: '确定删除',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    try {
      const res = await deleteOrder(orderId, userStore.userInfo.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchOrders()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除订单失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

const handleImageError = (e) => {
  e.target.src = 'https://picsum.photos/id/1/100/100'
}

onMounted(() => {
  fetchOrders()
})
</script>