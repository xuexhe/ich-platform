<!-- src/views/admin/HeritageManage.vue -->
<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-ich-dark">非遗管理</h1>
      <el-button type="danger" @click="openDialog()">新增非遗</el-button>
    </div>
    
    <!-- 搜索栏 -->
    <div class="mb-4">
      <el-input v-model="keyword" placeholder="搜索非遗名称" style="width: 300px" clearable @clear="fetchData" @keyup.enter="fetchData">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button class="ml-2" type="primary" @click="fetchData">搜索</el-button>
    </div>
    
    <!-- 表格 -->
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" min-width="150" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column label="封面" width="100">
        <template #default="{ row }">
          <img :src="row.cover" class="w-12 h-12 object-cover rounded" @error="handleImageError" />
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览量" width="80" />
      <el-table-column prop="collectCount" label="收藏量" width="80" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="mt-4 flex justify-center">
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" @current-change="fetchData" />
    </div>
    
    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="名称" required>
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="form.category" style="width: 100%">
                <el-option label="传统技艺" value="传统技艺" />
                <el-option label="传统戏剧" value="传统戏剧" />
                <el-option label="传统美术" value="传统美术" />
                <el-option label="民俗" value="民俗" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 图片上传区域 -->
        <el-form-item label="封面图片">
          <div class="flex gap-3 items-start">
            <el-input v-model="form.cover" placeholder="图片URL" class="flex-1" />
            <el-upload
              class="cover-uploader"
              action="/api/upload/image"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleCoverSuccess"
              :before-upload="beforeUpload"
            >
              <el-button type="primary" size="small">上传图片</el-button>
            </el-upload>
          </div>
          <div v-if="form.cover" class="mt-2">
            <img :src="form.cover" class="w-24 h-24 object-cover rounded border" @error="handleImageError" />
            <div class="text-xs text-gray-400 mt-1">点击上传按钮可更换图片</div>
          </div>
          <div v-else class="mt-2 text-gray-400 text-sm">
            支持 JPG、PNG 格式，不超过 2MB
          </div>
        </el-form-item>
        
        <el-form-item label="简介">
          <el-input v-model="form.intro" type="textarea" :rows="3" />
        </el-form-item>
        
        <el-form-item label="详情内容">
          <el-input v-model="form.content" type="textarea" :rows="5" />
        </el-form-item>
        
        <el-form-item label="作者">
          <el-input v-model="form.author" placeholder="请输入作者" />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
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
import { Search } from '@element-plus/icons-vue'
import { adminGetHeritageList, adminSaveHeritage, adminDeleteHeritage } from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增非遗')

// 上传请求头
const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('adminToken')}`
}

const form = ref({
  id: null,
  name: '',
  category: '',
  cover: '',
  intro: '',
  content: '',
  author: '',
  status: 1
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await adminGetHeritageList({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value })
    if (res.code === 200) {
      list.value = res.data.records || []
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取数据失败:', error)
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  if (row) {
    dialogTitle.value = '编辑非遗'
    form.value = { ...row }
  } else {
    dialogTitle.value = '新增非遗'
    form.value = { id: null, name: '', category: '', cover: '', intro: '', content: '', author: '', status: 1 }
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入非遗名称')
    return
  }
  
  saving.value = true
  try {
    const res = await adminSaveHeritage(form.value)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' }).then(async () => {
    const res = await adminDeleteHeritage(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    }
  })
}

// 图片上传成功回调
const handleCoverSuccess = (response, file) => {
  if (response.code === 200) {
    form.value.cover = response.data.url
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

// 上传前校验
const beforeUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg'
  const isLt2M = file.size / 1024 / 1024 < 10
  
  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式图片')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

const handleImageError = (e) => {
  e.target.src = 'https://picsum.photos/id/1/100/100'
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.cover-uploader {
  display: inline-block;
}
</style>