<!-- src/views/admin/AdminLayout.vue -->
<template>
  <div class="admin-layout min-h-screen bg-gray-100">
    <!-- 侧边栏 -->
    <aside class="sidebar fixed left-0 top-0 h-full w-64 bg-ich-dark text-white z-50 flex flex-col">
      <!-- Logo 区域 - 固定顶部 -->
      <div class="p-5 text-center border-b border-white/20 flex-shrink-0">
        <h2 class="text-xl font-bold text-ich-gold">非遗云境</h2>
        <p class="text-xs opacity-70 mt-1">管理后台</p>
      </div>
      
      <!-- 菜单区域 - 可滚动 -->
      <nav class="flex-1 overflow-y-auto p-4">
        <!-- 返回首页按钮 -->
        <div class="mb-4 pb-4 border-b border-white/20">
          <button @click="goToHome" class="nav-item w-full text-left text-ich-gold hover:text-white">
            <el-icon><House /></el-icon> 返回首页
          </button>
        </div>
        
        <router-link to="/admin/dashboard" class="nav-item" active-class="active">
          <el-icon><DataLine /></el-icon> 数据概览
        </router-link>
        <router-link to="/admin/heritage" class="nav-item" active-class="active">
          <el-icon><Collection /></el-icon> 非遗管理
        </router-link>
        <router-link to="/admin/inheritor" class="nav-item" active-class="active">
          <el-icon><User /></el-icon> 传承人管理
        </router-link>
        <router-link to="/admin/product" class="nav-item" active-class="active">
          <el-icon><Goods /></el-icon> 商品管理
        </router-link>
        <router-link to="/admin/category" class="nav-item" active-class="active">
          <el-icon><CollectionTag /></el-icon> 分类管理
        </router-link>
        <router-link to="/admin/course" class="nav-item" active-class="active">
          <el-icon><VideoCamera /></el-icon> 课程管理
        </router-link>
        <router-link to="/admin/model" class="nav-item" active-class="active">
          <el-icon><Box /></el-icon> 3D模型管理
        </router-link>
        <router-link to="/admin/knowledge" class="nav-item" active-class="active">
          <el-icon><Notebook /></el-icon> 知识库审核
        </router-link>
        <router-link to="/admin/quiz" class="nav-item" active-class="active">
          <el-icon><Edit /></el-icon> 每日一题管理
        </router-link>
        <router-link to="/admin/comment" class="nav-item" active-class="active">
          <el-icon><ChatDotRound /></el-icon> 评论审核
        </router-link>
        <router-link to="/admin/user" class="nav-item" active-class="active">
          <el-icon><UserFilled /></el-icon> 用户管理
        </router-link>
      </nav>
      
      <!-- 退出登录按钮 - 固定在底部 -->
      <div class="p-4 border-t border-white/20 flex-shrink-0">
        <button @click="handleLogout" class="nav-item w-full text-left">
          <el-icon><SwitchButton /></el-icon> 退出登录
        </button>
      </div>
    </aside>
    
    <!-- 主内容区 -->
    <main class="ml-64 p-6">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  House, DataLine, Collection, User, Goods, ChatDotRound, 
  UserFilled, SwitchButton, CollectionTag, VideoCamera, Box, Notebook, Edit
} from '@element-plus/icons-vue'

const router = useRouter()

const goToHome = () => {
  window.location.href = '/'
}

const handleLogout = () => {
  localStorage.removeItem('adminToken')
  localStorage.removeItem('isAdmin')
  localStorage.removeItem('adminInfo')
  ElMessage.success('已退出')
  router.push('/admin/login')
}
</script>

<style scoped>
.nav-item {
  @apply flex items-center gap-3 px-4 py-3 rounded-lg mb-1 transition-all hover:bg-white/10;
}
.nav-item.active {
  @apply bg-ich-gold text-ich-dark;
}

/* 自定义滚动条 */
.overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}
.overflow-y-auto::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}
.overflow-y-auto::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
}
.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}
</style>