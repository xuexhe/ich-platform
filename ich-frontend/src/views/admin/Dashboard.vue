<template>
  <div>
    <h1 class="text-2xl font-bold text-ich-dark mb-6">数据概览</h1>
    
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div class="bg-white rounded-xl p-6 shadow-sm">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-500 text-sm">非遗项目</p>
            <p class="text-3xl font-bold text-ich-dark">{{ statistics.heritageCount || 0 }}</p>
          </div>
          <div class="w-12 h-12 rounded-full bg-ich-red/10 flex items-center justify-center">
            <el-icon :size="24" class="text-ich-red"><Collection /></el-icon>
          </div>
        </div>
      </div>
      
      <div class="bg-white rounded-xl p-6 shadow-sm">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-500 text-sm">传承人</p>
            <p class="text-3xl font-bold text-ich-dark">{{ statistics.inheritorCount || 0 }}</p>
          </div>
          <div class="w-12 h-12 rounded-full bg-ich-gold/10 flex items-center justify-center">
            <el-icon :size="24" class="text-ich-gold"><User /></el-icon>
          </div>
        </div>
      </div>
      
      <div class="bg-white rounded-xl p-6 shadow-sm">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-500 text-sm">文创商品</p>
            <p class="text-3xl font-bold text-ich-dark">{{ statistics.productCount || 0 }}</p>
          </div>
          <div class="w-12 h-12 rounded-full bg-green-500/10 flex items-center justify-center">
            <el-icon :size="24" class="text-green-500"><Goods /></el-icon>
          </div>
        </div>
      </div>
      
      <div class="bg-white rounded-xl p-6 shadow-sm">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-500 text-sm">注册用户</p>
            <p class="text-3xl font-bold text-ich-dark">{{ statistics.userCount || 0 }}</p>
          </div>
          <div class="w-12 h-12 rounded-full bg-blue-500/10 flex items-center justify-center">
            <el-icon :size="24" class="text-blue-500"><UserFilled /></el-icon>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 待审核评论 -->
    <div class="bg-white rounded-xl shadow-sm p-6">
      <h2 class="text-lg font-bold text-ich-dark mb-4">待审核评论</h2>
      <el-table :data="pendingComments" v-loading="loading">
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="auditComment(row.id, 1)">通过</el-button>
            <el-button size="small" type="danger" @click="auditComment(row.id, 2)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Collection, User, Goods, UserFilled } from '@element-plus/icons-vue'
import { adminGetStatistics, adminGetCommentList, adminAuditComment } from '@/api/admin'

const statistics = ref({})
const pendingComments = ref([])
const loading = ref(false)

const fetchData = async () => {
  try {
    const [statsRes, commentRes] = await Promise.all([
      adminGetStatistics(),
      adminGetCommentList({ pageNum: 1, pageSize: 10, status: 0 })
    ])
    if (statsRes.code === 200) statistics.value = statsRes.data
    if (commentRes.code === 200) pendingComments.value = commentRes.data.records || []
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}

const auditComment = async (id, status) => {
  try {
    const res = await adminAuditComment(id, status)
    if (res.code === 200) {
      ElMessage.success('审核成功')
      fetchData()
    }
  } catch (error) {
    ElMessage.error('审核失败')
  }
}

onMounted(() => fetchData())
</script>