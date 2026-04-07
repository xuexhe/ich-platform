<!-- src/views/admin/CategoryManage.vue -->
<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-ich-dark">分类管理</h1>
      <el-button type="danger" @click="openDialog()">新增分类</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" min-width="150" />
      <el-table-column prop="icon" label="图标" width="100">
        <template #default="{ row }">
          <span class="text-2xl">{{ row.icon || '🏷️' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button v-if="row.status === 1" size="small" type="warning" @click="handleToggleStatus(row, 0)">禁用</el-button>
          <el-button v-else size="small" type="success" @click="handleToggleStatus(row, 1)">启用</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
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

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="分类名称" required>
          <el-input v-model="form.name" placeholder="请输入分类名称，如：传统技艺" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="请输入emoji图标，如：🏺" maxlength="2" show-word-limit />
          <div class="mt-2 text-3xl">{{ form.icon || '🏷️' }}</div>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%" />
          <div class="text-xs text-gray-400 mt-1">数字越小越靠前</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminGetCategoryList, adminSaveCategory, adminDeleteCategory } from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const form = ref({
  name: '',
  icon: '',
  sortOrder: 0,
  status: 1
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await adminGetCategoryList({ 
      pageNum: pageNum.value, 
      pageSize: pageSize.value
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

const openDialog = (row) => {
  if (row) {
    dialogTitle.value = '编辑分类'
    form.value = { ...row }
  } else {
    dialogTitle.value = '新增分类'
    form.value = { name: '', icon: '', sortOrder: 0, status: 1 }
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  
  saving.value = true
  try {
    const res = await adminSaveCategory(form.value)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleToggleStatus = (row, status) => {
  const action = status === 1 ? '启用' : '禁用'
  ElMessageBox.confirm(`确定要${action}该分类吗？`, '提示', { type: 'warning' }).then(async () => {
    const updated = { ...row, status }
    try {
      const res = await adminSaveCategory(updated)
      if (res.code === 200) {
        ElMessage.success(`${action}成功`)
        fetchData()
      } else {
        ElMessage.error(`${action}失败`)
      }
    } catch (error) {
      ElMessage.error(`${action}失败`)
    }
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除该分类吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await adminDeleteCategory(id)
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