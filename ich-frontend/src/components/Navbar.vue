<!-- src/components/Navbar.vue -->
<template>
  <!-- 修复：导航栏提升到最高层级 z-[9999] + 加定位 + 加背景防止穿透 -->
  <div class="w-full z-[9999] glass relative bg-[#1a1a1a]/90">
    <div class="max-w-7xl mx-auto px-4">
      <div class="flex items-center justify-between h-16">
        <!-- Logo -->
        <div class="flex items-center cursor-pointer" @click="$router.push('/index')">
          <span class="text-2xl font-bold text-ich-gold">非遗云境</span>
        </div>
        
        <!-- 导航菜单 -->
        <div class="hidden md:flex items-center space-x-6">
          <router-link to="/index" class="text-white hover:text-ich-gold transition">首页</router-link>
          <router-link to="/heritage/list" class="text-white hover:text-ich-gold transition">非遗项目</router-link>
          <router-link to="/3d" class="text-white hover:text-ich-gold transition">3D展厅</router-link>
          <router-link to="/ai" class="text-white hover:text-ich-gold transition">AI讲解</router-link>
          <router-link to="/market" class="text-white hover:text-ich-gold transition">非遗市集</router-link>
          <router-link to="/inheritor" class="text-white hover:text-ich-gold transition">传承人</router-link>
          <router-link to="/course" class="text-white hover:text-ich-gold transition">非遗小课堂</router-link>
          <router-link to="/diy" class="text-white hover:text-ich-gold transition">DIY工坊</router-link>
        </div>
        
        <!-- 用户区域 -->
        <div class="flex items-center space-x-4">
          <!-- 购物车图标 -->
          <div class="relative cursor-pointer hover:text-ich-gold transition" @click="$router.push('/cart')">
            <el-icon :size="22" class="text-white"><ShoppingCart /></el-icon>
            <span v-if="cartCount > 0" class="absolute -top-2 -right-2 bg-ich-red text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
              {{ cartCount > 99 ? '99+' : cartCount }}
            </span>
          </div>
          
          <!-- 管理员入口 -->
          <router-link 
            v-if="userStore.isAdmin" 
            to="/admin/dashboard" 
            class="hidden md:flex items-center text-white hover:text-ich-gold transition"
          >
            <el-icon><Setting /></el-icon>
            <span class="ml-1 text-sm">管理后台</span>
          </router-link>
          
          <div v-if="userStore.isLoggedIn" class="relative" ref="userMenuRef">
            <div 
              class="flex items-center cursor-pointer rounded-full hover:bg-white/10 p-1 transition"
              @click="toggleMenu"
            >
              <img 
                :src="userStore.avatar" 
                class="w-8 h-8 rounded-full object-cover border-2 border-ich-gold" 
                @error="handleAvatarError"
              />
              <span class="ml-2 text-white hidden md:inline">{{ userStore.username }}</span>
              <el-icon class="ml-1 text-white" :class="{ 'rotate-180': menuVisible }">
                <ArrowDown />
              </el-icon>
            </div>
            
            <!-- 修复：下拉菜单层级拉满 z-[99999]，绝对定位不被遮挡 -->
            <div 
              v-show="menuVisible"
              class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg overflow-hidden z-[99999]"
              @mouseleave="closeMenu"
            >
              <div class="py-2">
                <div 
                  class="px-4 py-3 hover:bg-gray-50 cursor-pointer flex items-center gap-3 border-b border-gray-100"
                  @click="goToProfile"
                >
                  <el-icon class="text-ich-dark"><User /></el-icon>
                  <span class="text-gray-700">个人中心</span>
                </div>
                <div 
                  class="px-4 py-3 hover:bg-gray-50 cursor-pointer flex items-center gap-3 border-b border-gray-100"
                  @click="goToOrders"
                >
                  <el-icon class="text-ich-dark"><Document /></el-icon>
                  <span class="text-gray-700">我的订单</span>
                </div>
                <div 
                  class="px-4 py-3 hover:bg-gray-50 cursor-pointer flex items-center gap-3 border-b border-gray-100"
                  @click="goToAchievement"
                >
                  <el-icon class="text-ich-dark"><Trophy /></el-icon>
                  <span class="text-gray-700">我的成就</span>
                </div>
                <div 
                  class="px-4 py-3 hover:bg-gray-50 cursor-pointer flex items-center gap-3 text-red-500"
                  @click="handleLogout"
                >
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else>
            <el-button size="small" @click="$router.push('/login')">登录</el-button>
            <el-button size="small" type="danger" @click="$router.push('/register')">注册</el-button>
          </div>
          
          <!-- 移动端菜单按钮 -->
          <el-button class="md:hidden" :icon="Menu" text @click="mobileMenuVisible = !mobileMenuVisible"></el-button>
        </div>
      </div>
    </div>
    
    <!-- 移动端菜单 -->
    <div v-show="mobileMenuVisible" class="md:hidden bg-ich-dark/95 px-4 py-4 z-[9999] relative">
      <div class="flex flex-col space-y-3">
        <router-link to="/index" class="text-white hover:text-ich-gold py-2" @click="mobileMenuVisible = false">首页</router-link>
        <router-link to="/heritage/list" class="text-white hover:text-ich-gold py-2" @click="mobileMenuVisible = false">非遗项目</router-link>
        <router-link to="/3d" class="text-white hover:text-ich-gold py-2" @click="mobileMenuVisible = false">3D展厅</router-link>
        <router-link to="/ai" class="text-white hover:text-ich-gold py-2" @click="mobileMenuVisible = false">AI讲解</router-link>
        <router-link to="/market" class="text-white hover:text-ich-gold py-2" @click="mobileMenuVisible = false">非遗市集</router-link>
        <router-link to="/inheritor" class="text-white hover:text-ich-gold py-2" @click="mobileMenuVisible = false">传承人</router-link>
        <router-link to="/quiz" class="text-white hover:text-ich-gold py-2" @click="mobileMenuVisible = false">每日一题</router-link>
        <router-link to="/cart" class="text-white hover:text-ich-gold py-2" @click="mobileMenuVisible = false">购物车</router-link>
        <router-link to="/orders" class="text-white hover:text-ich-gold py-2" @click="mobileMenuVisible = false">我的订单</router-link>
        <router-link to="/achievement" class="text-ich-gold hover:text-ich-gold/80 py-2" @click="mobileMenuVisible = false">我的成就</router-link>
        <router-link v-if="userStore.isAdmin" to="/admin/dashboard" class="text-ich-gold hover:text-ich-gold/80 py-2" @click="mobileMenuVisible = false">
          管理后台
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, User, SwitchButton, Menu, Setting, ShoppingCart, Document, Trophy } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const mobileMenuVisible = ref(false)
const menuVisible = ref(false)
const userMenuRef = ref(null)

// 购物车数量
const cartCount = computed(() => {
  const cart = localStorage.getItem('cart')
  if (!cart) return 0
  try {
    const items = JSON.parse(cart)
    return items.reduce((sum, item) => sum + (item.quantity || 1), 0)
  } catch (e) {
    return 0
  }
})

// 切换菜单显示
const toggleMenu = () => {
  menuVisible.value = !menuVisible.value
}

// 关闭菜单
const closeMenu = () => {
  menuVisible.value = false
}

// 跳转个人中心
const goToProfile = () => {
  menuVisible.value = false
  router.push('/profile')
}

// 跳转订单页面
const goToOrders = () => {
  menuVisible.value = false
  router.push('/orders')
}

// 跳转成就页面
const goToAchievement = () => {
  menuVisible.value = false
  router.push('/achievement')
}

// 退出登录
const handleLogout = () => {
  menuVisible.value = false
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/index')
}

// 头像加载失败时的处理
const handleAvatarError = (e) => {
  e.target.src = 'https://api.dicebear.com/7.x/initials/svg?seed=User'
}

// 点击外部关闭菜单
const handleClickOutside = (event) => {
  if (userMenuRef.value && !userMenuRef.value.contains(event.target)) {
    menuVisible.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.rotate-180 {
  transform: rotate(180deg);
  transition: transform 0.2s;
}
</style>