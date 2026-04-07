<!-- src/views/admin/ModelManage.vue -->
<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-ich-dark">3D模型管理</h1>
      <el-button type="danger" @click="openDialog()">新增模型</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="mb-4">
      <el-input 
        v-model="keyword" 
        placeholder="搜索模型名称" 
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
      <el-table-column prop="name" label="模型名称" min-width="150" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <img :src="row.cover" class="w-12 h-12 object-cover rounded" @error="handleImageError" />
        </template>
      </el-table-column>
      <el-table-column prop="modelUrl" label="模型文件" min-width="200" show-overflow-tooltip />
      <el-table-column prop="viewCount" label="浏览数" width="80" />
      <el-table-column prop="status" label="状态" width="80">
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模型名称" required>
              <el-input v-model="form.name" placeholder="请输入模型名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="form.category" style="width: 100%">
                <el-option label="瓷器" value="瓷器" />
                <el-option label="青铜器" value="青铜器" />  
                <el-option label="玉器" value="玉器" />
                <el-option label="苏绣" value="苏绣" />
                <el-option label="剪纸" value="剪纸" />
                <el-option label="皮影" value="皮影" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 封面上传 -->
        <el-form-item label="封面图片">
          <div class="flex gap-3 items-start">
            <el-input v-model="form.cover" placeholder="封面URL" class="flex-1" />
            <el-upload
              class="cover-uploader"
              action="/api/model/upload/cover"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleCoverSuccess"
              :before-upload="beforeUploadImage"
            >
              <el-button type="primary" size="small">上传图片</el-button>
            </el-upload>
          </div>
          <div v-if="form.cover" class="mt-2">
            <img :src="form.cover" class="w-24 h-24 object-cover rounded border" />
          </div>
        </el-form-item>
        
        <!-- 3D模型文件上传 -->
        <el-form-item label="模型文件" required>
          <div class="flex gap-3 items-start flex-wrap">
            <div class="flex-1">
              <el-input v-model="form.modelUrl" placeholder="模型文件URL（.glb/.gltf）" class="mb-2" />
              <div class="text-xs text-gray-400">支持 .glb、.gltf、.obj 格式，不超过50MB</div>
            </div>
            <el-upload
              class="model-uploader"
              action="/api/model/upload/model"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleModelSuccess"
              :before-upload="beforeUploadModel"
              :disabled="uploading"
              :timeout="180000"
            >
              <el-button :type="form.modelUrl ? 'success' : 'primary'" :loading="uploading">
                {{ form.modelUrl ? '重新上传' : '上传模型' }}
              </el-button>
            </el-upload>
          </div>
          <div v-if="form.modelUrl" class="mt-2 text-xs text-green-600">
            ✓ 模型文件已上传
          </div>
        </el-form-item>
        
        <el-form-item label="模型描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入模型描述" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
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
import { adminGetModelList, adminSaveModel, adminDeleteModel } from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增模型')

const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('adminToken')}`
}

const form = ref({
  id: null,
  name: '',
  category: '其他',
  cover: '',
  modelUrl: '',
  description: '',
  sortOrder: 0,
  status: 1
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await adminGetModelList({ 
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

const openDialog = (row) => {
  if (row) {
    dialogTitle.value = '编辑模型'
    form.value = { ...row }
  } else {
    dialogTitle.value = '新增模型'
    form.value = { id: null, name: '', category: '其他', cover: '', modelUrl: '', description: '', sortOrder: 0, status: 1 }
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入模型名称')
    return
  }
  if (!form.value.modelUrl) {
    ElMessage.warning('请上传模型文件')
    return
  }
  
  saving.value = true
  try {
    const res = await adminSaveModel(form.value)
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

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除该模型吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await adminDeleteModel(id)
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

const handleCoverSuccess = (response) => {
  if (response.code === 200) {
    form.value.cover = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

const beforeUploadImage = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式图片')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const handleModelSuccess = (response) => {
  uploading.value = false
  if (response.code === 200) {
    form.value.modelUrl = response.data.url
    ElMessage.success('模型上传成功')
  } else {
    ElMessage.error('上传失败: ' + response.message)
  }
}

const beforeUploadModel = (file) => {
  const fileName = file.name.toLowerCase()
  const isValid = fileName.endsWith('.glb') || fileName.endsWith('.gltf') || fileName.endsWith('.obj')
  const isLt200M = file.size / 1024 / 1024 < 200  // 改为200MB
  
  if (!isValid) {
    ElMessage.error('只能上传 .glb、.gltf、.obj 格式的3D模型文件')
    return false
  }
  if (!isLt200M) {
    ElMessage.error('模型文件大小不能超过 200MB')
    return false
  }
  uploading.value = true
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
.cover-uploader, .model-uploader {
  display: inline-block;
}
</style>