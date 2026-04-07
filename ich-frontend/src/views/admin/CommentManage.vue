<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-ich-dark">评论审核管理</h1>
    </div>

    <!-- 状态筛选 -->
    <div class="mb-4 flex gap-2">
      <el-button 
        :type="statusFilter === null ? 'danger' : 'default'" 
        @click="statusFilter = null; fetchData()"
      >
        全部
      </el-button>
      <el-button 
        :type="statusFilter === 1 ? 'danger' : 'default'" 
        @click="statusFilter = 1; fetchData()"
      >
        已通过
      </el-button>
      <el-button 
        :type="statusFilter === 0 ? 'danger' : 'default'" 
        @click="statusFilter = 0; fetchData()"
      >
        待审核
      </el-button>
      <el-button 
        :type="statusFilter === 2 ? 'danger' : 'default'" 
        @click="statusFilter = 2; fetchData()"
      >
        已驳回
      </el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="content" label="评论内容" min-width="250" show-overflow-tooltip />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="heritageId" label="非遗ID" width="100" />
      <el-table-column prop="likeCount" label="点赞数" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="评论时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button size="small" type="success" @click="handleAudit(row.id, 1)">通过</el-button>
            <el-button size="small" type="danger" @click="handleAudit(row.id, 2)">驳回</el-button>
          </template>
          <el-button size="small" type="danger" plain @click="handleDelete(row.id)">删除</el-button>
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

    <!-- 评论详情弹窗 -->
    <el-dialog v-model="detailVisible" title="评论详情" width="500px">
      <div class="p-4">
        <p class="text-gray-500 text-sm">评论内容：</p>
        <p class="mt-1">{{ currentComment.content }}</p>
        <p class="text-gray-500 text-sm mt-4">用户ID：{{ currentComment.userId }}</p>
        <p class="text-gray-500 text-sm mt-2">非遗ID：{{ currentComment.heritageId }}</p>
        <p class="text-gray-500 text-sm mt-2">评论时间：{{ currentComment.createTime }}</p>
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
import { adminGetCommentList, adminAuditComment, adminDeleteComment } from '@/api/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const statusFilter = ref(null)
const detailVisible = ref(false)
const currentComment = ref({})

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已驳回' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await adminGetCommentList({ 
      pageNum: pageNum.value, 
      pageSize: pageSize.value, 
      status: statusFilter.value 
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

const handleAudit = async (id, status) => {
  const statusText = status === 1 ? '通过' : '驳回'
  try {
    const res = await adminAuditComment(id, status)
    if (res.code === 200) {
      ElMessage.success(`审核${statusText}成功`)
      fetchData()
    } else {
      ElMessage.error(res.message || `审核${statusText}失败`)
    }
  } catch (error) {
    ElMessage.error(`审核${statusText}失败`)
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除该评论吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await adminDeleteComment(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchData()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

onMounted(() => {
  fetchData()
})
</script>