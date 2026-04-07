<!-- src/views/admin/InheritorManage.vue -->
<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-ich-dark">传承人管理</h1>
      <el-button type="danger" @click="openDialog()">新增传承人</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="mb-4">
      <el-input 
        v-model="keyword" 
        placeholder="搜索传承人名称" 
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
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column label="头像" width="100">
        <template #default="{ row }">
          <img :src="row.avatar" class="w-10 h-10 rounded-full object-cover" @error="handleImageError" />
        </template>
      </el-table-column>
      <el-table-column prop="heritageId" label="关联非遗ID" width="100" />
      <el-table-column prop="intro" label="简介" min-width="200" show-overflow-tooltip />
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="请输入传承人姓名" />
        </el-form-item>
        
        <el-form-item label="头像">
          <div class="flex gap-3 items-start">
            <el-input v-model="form.avatar" placeholder="请输入头像图片URL" class="flex-1" />
            <el-upload
              class="avatar-uploader"
              action="/api/upload/image"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeUpload"
            >
              <el-button type="primary" size="small">上传图片</el-button>
            </el-upload>
          </div>
          <div v-if="form.avatar" class="mt-2">
            <img :src="form.avatar" class="w-16 h-16 rounded-full object-cover" @error="handleImageError" />
          </div>
          <div class="text-xs text-gray-400 mt-1">支持 jpg/png，不超过 2MB</div>
        </el-form-item>
        
        <el-form-item label="关联非遗ID">
          <el-input-number v-model="form.heritageId" :min="1" style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="简介">
          <el-input v-model="form.intro" type="textarea" :rows="3" placeholder="请输入传承人简介" />
        </el-form-item>
        
        <el-form-item label="传承经历">
          <el-input v-model="form.experience" type="textarea" :rows="4" placeholder="请输入传承经历" />
        </el-form-item>
        
        <el-form-item label="荣誉成就">
          <el-input v-model="form.honor" type="textarea" :rows="3" placeholder="请输入荣誉成就" />
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
import { adminGetInheritorList, adminSaveInheritor, adminDeleteInheritor } from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增传承人')

const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('adminToken')}`
}

const form = ref({
  name: '',
  avatar: '',
  heritageId: null,
  intro: '',
  experience: '',
  honor: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await adminGetInheritorList({ 
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
    dialogTitle.value = '编辑传承人'
    form.value = { ...row }
  } else {
    dialogTitle.value = '新增传承人'
    form.value = { name: '', avatar: '', heritageId: null, intro: '', experience: '', honor: '' }
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入传承人姓名')
    return
  }
  
  saving.value = true
  try {
    const res = await adminSaveInheritor(form.value)
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
  ElMessageBox.confirm('确定删除该传承人吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await adminDeleteInheritor(id)
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

const handleAvatarSuccess = (response, file) => {
  if (response.code === 200) {
    form.value.avatar = response.data.url
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

const beforeUpload = (file) => {
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

const handleImageError = (e) => {
  e.target.src = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.avatar-uploader {
  display: inline-block;
}
</style>