<!-- src/views/ProfileView.vue -->
<template>
  <div class="min-h-screen bg-ich-bg pt-16">
    <Navbar />
    <div class="max-w-4xl mx-auto px-4 py-12">
      <!-- 个人信息卡片 -->
      <div class="bg-white rounded-xl shadow-lg overflow-hidden mb-6">
        <div class="bg-gradient-to-r from-ich-dark to-ich-dark/80 p-8 text-white text-center">
          <div class="relative w-32 h-32 rounded-full bg-ich-bg mx-auto mb-4 overflow-hidden cursor-pointer group" @click="triggerFileInput">
            <img :src="userStore.avatar" class="w-full h-full object-cover transition group-hover:brightness-75" @error="handleAvatarError" />
            <div class="absolute inset-0 bg-black/50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition">
              <el-icon :size="32" color="white"><Camera /></el-icon>
            </div>
            <input ref="fileInput" type="file" class="hidden" accept="image/jpeg,image/png,image/jpg,image/gif,image/webp" @change="handleFileSelect" />
          </div>
          <h3 class="text-2xl font-bold">{{ userStore.userInfo.nickname || userStore.userInfo.username }}</h3>
          <p class="opacity-80 mt-1">注册时间：{{ formatTime(userStore.userInfo.createTime) }}</p>
          <p class="text-sm mt-2 text-ich-gold">点击头像更换图片</p>
        </div>
        
        <div class="p-8">
          <el-form :model="form" label-width="80px">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input v-model="form.intro" type="textarea" :rows="3" placeholder="介绍一下自己吧" />
            </el-form-item>
            <el-form-item>
              <el-button type="danger" @click="saveInfo" :loading="saving">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <!-- 成就墙入口卡片 -->
      <div class="bg-white rounded-xl shadow-lg overflow-hidden mb-6 cursor-pointer hover:shadow-xl transition-all" @click="$router.push('/achievement')">
        <div class="p-6 flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="w-12 h-12 rounded-full bg-ich-gold/20 flex items-center justify-center">
              <span class="text-2xl">🏅</span>
            </div>
            <div>
              <h3 class="text-lg font-bold text-ich-dark">我的成就墙</h3>
              <p class="text-sm text-gray-500">查看已获得的勋章和成就点数</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <div class="text-right">
              <p class="text-xs text-gray-400">成就进度</p>
              <p class="text-sm font-bold text-ich-red">{{ achievementStats.obtainedCount }}/{{ achievementStats.totalCount }}</p>
            </div>
            <el-icon class="text-gray-400"><ArrowRight /></el-icon>
          </div>
        </div>
        <div class="px-6 pb-4">
          <div class="w-full bg-gray-200 rounded-full h-2">
            <div class="bg-ich-gold rounded-full h-2 transition-all" :style="{ width: achievementProgress + '%' }"></div>
          </div>
        </div>
      </div>
      <!-- ==================== 我的收藏卡片 ==================== -->
      <div class="bg-white rounded-xl shadow-lg overflow-hidden mb-6">
        <div class="p-6 border-b border-gray-100 flex justify-between items-center">
          <h3 class="text-xl font-bold text-ich-dark">我的收藏</h3>
          <el-button type="text" class="text-ich-red" @click="$router.push('/heritage/list')">去逛逛 →</el-button>
        </div>
        <div class="p-6">
          <div v-if="loadingCollects" class="flex justify-center py-12">
            <el-icon class="is-loading" :size="32"><Loading /></el-icon>
          </div>
          <div v-else-if="collectList.length === 0" class="text-center py-12 text-gray-500">
            <el-icon :size="48"><Star /></el-icon>
            <p class="mt-2">暂无收藏，快去收藏喜欢的非遗项目吧～</p>
          </div>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div v-for="item in collectList" :key="item.id" class="border border-gray-100 rounded-lg p-4 flex gap-4 hover:shadow-md transition-shadow">
              <img :src="item.cover" class="w-20 h-20 object-cover rounded" @error="handleImageError" />
              <div class="flex-1">
                <h4 class="font-bold text-ich-dark">{{ item.name }}</h4>
                <p class="text-sm text-gray-500 line-clamp-2">{{ item.intro }}</p>
                <el-button type="text" class="text-ich-red text-sm mt-2" @click="removeCollect(item.id)">取消收藏</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 安全设置卡片 -->
      <div class="bg-white rounded-xl shadow-lg overflow-hidden mb-6">
        <div class="p-6 border-b border-gray-100">
          <h3 class="text-xl font-bold text-ich-dark">安全设置</h3>
        </div>
        <div class="p-6">
          <el-form :model="passwordForm" label-width="100px">
            <el-form-item label="原密码">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password style="width: 300px" placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="passwordForm.newPassword" type="password" show-password style="width: 300px" placeholder="至少6位" />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password style="width: 300px" placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="danger" @click="changePassword" :loading="changingPwd">修改密码</el-button>
            </el-form-item>
          </el-form>
          
          <div class="border-t pt-6 mt-4">
            <h4 class="font-bold text-ich-dark mb-2">危险操作</h4>
            <el-button type="danger" plain @click="confirmDeleteAccount">注销账号</el-button>
            <p class="text-xs text-gray-400 mt-2">注销后所有数据将被清除，且无法恢复</p>
          </div>
        </div>
      </div>
      
      <!-- 退出登录按钮 -->
      <div class="text-center">
        <el-button type="danger" plain @click="handleLogout">
          <el-icon><SwitchButton /></el-icon> 退出登录
        </el-button>
      </div>
    </div>
    
    <!-- 上传进度弹窗 -->
    <el-dialog v-model="uploadDialogVisible" title="上传头像" width="300px" :close-on-click-modal="false">
      <div class="text-center py-4">
        <el-progress v-if="uploadProgress > 0" :percentage="uploadProgress" :stroke-width="8" />
        <p class="mt-4 text-gray-500">{{ uploadStatus }}</p>
      </div>
    </el-dialog>
    
    <!-- 注销确认弹窗 -->
    <el-dialog v-model="showDeleteDialog" title="注销账号" width="400px">
      <div class="text-center py-4">
        <div class="text-6xl mb-4">⚠️</div>
        <p class="text-gray-700 mb-4">确定要注销账号吗？</p>
        <p class="text-sm text-red-500 mb-4">此操作不可恢复，所有数据将被清除</p>
        <el-input v-model="deletePassword" type="password" placeholder="请输入密码确认" show-password class="mb-4" />
        <div class="flex gap-3 justify-center">
          <el-button @click="showDeleteDialog = false">取消</el-button>
          <el-button type="danger" @click="deleteAccount" :loading="deleting">确认注销</el-button>
        </div>
      </div>
    </el-dialog>
    
    <Footer />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Camera, Star, SwitchButton, Loading, ArrowRight } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import { useUserStore } from '@/store/user'
import { getMyCollects, cancelCollect } from '@/api/collect'
import { getHeritageList } from '@/api/heritage'
import { getUserAchievementStats } from '@/api/achievement'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

// 个人信息表单
const form = reactive({
  username: '',
  nickname: '',
  intro: ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 成就统计
const achievementStats = ref({ obtainedCount: 0, totalCount: 0, totalPoints: 0 })

// 成就进度百分比
const achievementProgress = computed(() => {
  if (achievementStats.value.totalCount === 0) return 0
  return (achievementStats.value.obtainedCount / achievementStats.value.totalCount) * 100
})

// 状态变量
const saving = ref(false)
const changingPwd = ref(false)
const deleting = ref(false)
const collectList = ref([])
const loadingCollects = ref(false)
const showDeleteDialog = ref(false)
const deletePassword = ref('')

// 头像上传相关
const fileInput = ref(null)
const uploadDialogVisible = ref(false)
const uploadProgress = ref(0)
const uploadStatus = ref('')

// 获取用户信息
const fetchUserInfo = () => {
  form.username = userStore.userInfo.username || ''
  form.nickname = userStore.userInfo.nickname || ''
  form.intro = userStore.userInfo.intro || ''
}

// 获取成就统计
const fetchAchievementStats = async () => {
  if (!userStore.userInfo.id) return
  try {
    const res = await getUserAchievementStats(userStore.userInfo.id)
    if (res.code === 200) {
      achievementStats.value = res.data
    }
  } catch (error) {
    console.error('获取成就统计失败:', error)
  }
}

// 获取收藏列表
const fetchCollects = async () => {
  if (!userStore.userInfo.id) return
  loadingCollects.value = true
  try {
    const res = await getMyCollects(userStore.userInfo.id)
    if (res.code === 200) {
      const collectIds = res.data.map(c => c.heritageId)
      if (collectIds.length > 0) {
        const heritageRes = await getHeritageList({ pageSize: 100 })
        if (heritageRes.code === 200) {
          const allHeritage = heritageRes.data.records || []
          collectList.value = allHeritage.filter(h => collectIds.includes(h.id))
        }
      } else {
        collectList.value = []
      }
    }
  } catch (error) {
    console.error('获取收藏失败:', error)
  } finally {
    loadingCollects.value = false
  }
}

// 保存个人信息
const saveInfo = async () => {
  saving.value = true
  try {
    const updateData = {
      id: userStore.userInfo.id,
      nickname: form.nickname,
      intro: form.intro
    }
    const success = await userStore.updateUserInfo(updateData)
    if (success) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error('保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 修改密码
const changePassword = async () => {
  if (!passwordForm.oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  
  changingPwd.value = true
  try {
    const res = await request.post('/user/changePassword', {
      userId: userStore.userInfo.id,
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      setTimeout(() => {
        userStore.logout()
        router.push('/login')
      }, 1500)
    } else {
      ElMessage.error(res.message || '原密码错误')
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.response?.data?.message || '修改失败')
  } finally {
    changingPwd.value = false
  }
}

// 确认注销
const confirmDeleteAccount = () => {
  ElMessageBox.confirm('确定要注销账号吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定注销',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  }).then(() => {
    showDeleteDialog.value = true
  }).catch(() => {})
}

// 注销账号
const deleteAccount = async () => {
  if (!deletePassword.value) {
    ElMessage.warning('请输入密码')
    return
  }
  
  deleting.value = true
  try {
    const res = await request.post('/user/delete', {
      userId: userStore.userInfo.id,
      password: deletePassword.value
    })
    if (res.code === 200) {
      ElMessage.success('账号已注销，再见')
      showDeleteDialog.value = false
      router.push('/index')
    } else {
      ElMessage.error(res.message || '密码错误')
    }
  } catch (error) {
    console.error('注销失败:', error)
    ElMessage.error(error.response?.data?.message || '注销失败')
  } finally {
    deleting.value = false
    deletePassword.value = ''
  }
}

// 取消收藏
const removeCollect = async (heritageId) => {
  try {
    await cancelCollect(userStore.userInfo.id, heritageId)
    collectList.value = collectList.value.filter(item => item.id !== heritageId)
    ElMessage.success('取消收藏成功')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/index')
  }).catch(() => {})
}

// 头像上传相关
const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只支持 JPG、PNG、GIF、WEBP 格式的图片')
    return
  }
  
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }
  
  uploadDialogVisible.value = true
  uploadProgress.value = 0
  uploadStatus.value = '正在上传...'
  
  const interval = setInterval(() => {
    if (uploadProgress.value < 90) uploadProgress.value += 10
  }, 100)
  
  try {
    const success = await userStore.uploadAvatar(file)
    clearInterval(interval)
    if (success) {
      uploadProgress.value = 100
      uploadStatus.value = '上传成功！'
      setTimeout(() => {
        uploadDialogVisible.value = false
        ElMessage.success('头像更新成功')
        window.location.reload()
      }, 500)
    } else {
      uploadDialogVisible.value = false
      ElMessage.error('上传失败')
    }
  } catch (error) {
    clearInterval(interval)
    uploadDialogVisible.value = false
    ElMessage.error('上传失败')
  }
  event.target.value = ''
}

// 辅助函数
const handleAvatarError = (e) => {
  e.target.src = `https://api.dicebear.com/7.x/avataaars/svg?seed=${userStore.userInfo?.username || 'user'}`
}

const handleImageError = (e) => {
  e.target.src = 'https://picsum.photos/id/1/400/300'
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

onMounted(() => {
  fetchUserInfo()
  fetchCollects()
  fetchAchievementStats()
})
</script>

<style scoped>
:deep(.el-dialog) { border-radius: 16px; }
:deep(.el-icon.is-loading) { animation: rotating 1s linear infinite; }
@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>