<!-- src/views/admin/KnowledgeAudit.vue -->
<template>
  <div>
    <div class="flex justify-between items-center mb-6 flex-wrap gap-3">
      <h1 class="text-2xl font-bold text-ich-dark">知识库审核</h1>
      <div class="flex gap-2">
        <el-button 
          :type="statusFilter === null ? 'danger' : 'default'"
          size="small"
          @click="statusFilter = null; fetchData()"
        >
          全部
        </el-button>
        <el-button 
          :type="statusFilter === 0 ? 'danger' : 'default'"
          size="small"
          @click="statusFilter = 0; fetchData()"
        >
          待审核
        </el-button>
        <el-button 
          :type="statusFilter === 1 ? 'danger' : 'default'"
          size="small"
          @click="statusFilter = 1; fetchData()"
        >
          已通过
        </el-button>
        <el-button 
          :type="statusFilter === 2 ? 'danger' : 'default'"
          size="small"
          @click="statusFilter = 2; fetchData()"
        >
          已驳回
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="mb-4">
      <el-input 
        v-model="keyword" 
        placeholder="搜索知识标题" 
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
      <el-table-column prop="title" label="标题" min-width="150" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <img :src="row.cover" class="w-12 h-12 object-cover rounded" @error="handleImageError" />
        </template>
      </el-table-column>
      <el-table-column prop="authorName" label="作者" width="120" />
      <el-table-column prop="summary" label="简介" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <div class="flex gap-2">
            <el-button size="small" type="primary" @click="viewDetail(row)">查看</el-button>
            <template v-if="row.status === 0">
              <el-button size="small" type="success" @click="handleAudit(row.id, 1)">通过</el-button>
              <el-button size="small" type="danger" @click="handleAudit(row.id, 2)">驳回</el-button>
            </template>
          </div>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="`知识详情 - ${currentItem?.title}`" width="700px">
      <div class="space-y-4">
        <div>
          <h4 class="font-bold text-ich-dark">标题</h4>
          <p>{{ currentItem?.title }}</p>
        </div>
        <div>
          <h4 class="font-bold text-ich-dark">分类</h4>
          <p>{{ currentItem?.category }}</p>
        </div>
        <div>
          <h4 class="font-bold text-ich-dark">封面</h4>
          <img :src="currentItem?.cover" class="w-32 h-24 object-cover rounded border" @error="handleImageError" />
        </div>
        <div>
          <h4 class="font-bold text-ich-dark">简介</h4>
          <p>{{ currentItem?.summary }}</p>
        </div>
        <div>
          <h4 class="font-bold text-ich-dark">详细内容</h4>
          <div class="bg-gray-50 p-4 rounded-lg max-h-96 overflow-y-auto" v-html="currentItem?.content"></div>
        </div>
        <div>
          <h4 class="font-bold text-ich-dark">作者</h4>
          <p>{{ currentItem?.authorName }} (ID: {{ currentItem?.userId }})</p>
        </div>
        <div>
          <h4 class="font-bold text-ich-dark">提交时间</h4>
          <p>{{ currentItem?.createTime }}</p>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-2">
          <el-button @click="detailVisible = false">关闭</el-button>
          <template v-if="currentItem?.status === 0">
            <el-button type="success" @click="handleAudit(currentItem.id, 1)">通过</el-button>
            <el-button type="danger" @click="handleAudit(currentItem.id, 2)">驳回</el-button>
          </template>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminGetPendingKnowledge, adminAuditKnowledge } from '@/api/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const statusFilter = ref(0)
const detailVisible = ref(false)
const currentItem = ref(null)

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
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value
    }
    const res = await adminGetPendingKnowledge(params)
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

const viewDetail = (row) => {
  currentItem.value = row
  detailVisible.value = true
}

const handleAudit = async (id, status) => {
  const statusText = status === 1 ? '通过' : '驳回'
  ElMessageBox.confirm(`确定要${statusText}这条知识吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await adminAuditKnowledge(id, status)
      if (res.code === 200) {
        ElMessage.success(`审核${statusText}成功`)
        detailVisible.value = false
        fetchData()
      } else {
        ElMessage.error(res.message || `审核${statusText}失败`)
      }
    } catch (error) {
      ElMessage.error(`审核${statusText}失败`)
    }
  })
}

const handleImageError = (e) => {
  e.target.src = 'https://picsum.photos/id/1/100/100'
}

onMounted(() => {
  fetchData()
})
</script>