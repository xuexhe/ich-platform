<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-ich-dark">用户管理</h1>
    </div>

    <!-- 搜索栏 -->
    <div class="mb-4">
      <el-input 
        v-model="keyword" 
        placeholder="搜索用户名/昵称" 
        style="width: 300px" 
        clearable 
        @clear="fetchData" 
        @keyup.enter="fetchData"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button class="ml-2" type="primary" @click="fetchData">搜索</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column label="头像" width="80">
        <template #default="{ row }">
          <img :src="getAvatar(row)" class="w-10 h-10 rounded-full object-cover" @error="handleImageError" />
        </template>
      </el-table-column>
      <el-table-column prop="intro" label="简介" min-width="150" show-overflow-tooltip />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'admin' ? 'danger' : 'info'">
            {{ row.role === 'admin' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="160" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button 
            size="small" 
            :type="row.status === 1 ? 'danger' : 'success'" 
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="mt-4 flex justify-center">
      <el-pagination 
        v-model:current-page="pageNum" 
        v-model:page-size="pageSize" 
        :total="total" 
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData" 
        @current-change="fetchData" 
      />
    </div>

    <!-- 用户详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="`用户详情 - ${currentUser.username}`" width="500px">
      <div class="flex flex-col items-center p-4">
        <img :src="getAvatar(currentUser)" class="w-24 h-24 rounded-full object-cover" @error="handleImageError" />
        <h3 class="text-xl font-bold mt-4">{{ currentUser.nickname || currentUser.username }}</h3>
        <p class="text-gray-500 mt-1">{{ currentUser.intro || '这个人很懒，什么都没写' }}</p>
        
        <div class="w-full mt-6 space-y-3">
          <div class="flex justify-between">
            <span class="text-gray-500">用户名：</span>
            <span>{{ currentUser.username }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">角色：</span>
            <span>{{ currentUser.role === 'admin' ? '管理员' : '普通用户' }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">状态：</span>
            <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'" size="small">
              {{ currentUser.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">注册时间：</span>
            <span>{{ currentUser.createTime }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminGetUserList, adminUpdateUserStatus } from '@/api/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const detailVisible = ref(false)
const currentUser = ref({})

const getAvatar = (user) => {
  if (user.avatar && user.avatar !== '/images/default-avatar.png' && !user.avatar.includes('dicebear')) {
    return user.avatar
  }
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.username || 'user'}`
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await adminGetUserList({ 
      pageNum: pageNum.value, 
      pageSize: pageSize.value, 
      keyword: keyword.value 
    })
    if (res.code === 200) {
      list.value = res.data.records || []
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleToggleStatus = (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  const newStatus = row.status === 1 ? 0 : 1
  
  ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await adminUpdateUserStatus(row.id, newStatus)
      if (res.code === 200) {
        ElMessage.success(`${action}成功`)
        fetchData()
      } else {
        ElMessage.error(res.message || `${action}失败`)
      }
    } catch (error) {
      ElMessage.error(`${action}失败`)
    }
  })
}

const handleImageError = (e) => {
  e.target.src = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'
}

onMounted(() => {
  fetchData()
})
</script>