<!-- src/views/admin/CourseManage.vue -->
<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-ich-dark">课程管理</h1>
      <el-button type="danger" @click="openDialog()">新增课程</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="mb-4">
      <el-input 
        v-model="keyword" 
        placeholder="搜索课程名称" 
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
      <el-table-column prop="title" label="课程名称" min-width="150" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <img :src="row.cover" class="w-12 h-12 object-cover rounded" @error="handleImageError" />
        </template>
      </el-table-column>
      <el-table-column prop="duration" label="时长" width="80">
        <template #default="{ row }">{{ row.duration }}分钟</template>
      </el-table-column>
      <el-table-column prop="viewCount" label="学习次数" width="100" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '上架' : '下架' }}
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
            <el-form-item label="课程名称" required>
              <el-input v-model="form.title" placeholder="请输入课程名称" />
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
        
        <el-form-item label="封面图片">
          <div class="flex gap-3 items-start">
            <el-input v-model="form.cover" placeholder="图片URL" class="flex-1" />
            <el-upload
              class="cover-uploader"
              action="/api/course/upload/cover"
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
        
        <!-- 视频上传区域 -->
        <el-form-item label="教学视频">
          <div class="flex gap-3 items-start flex-wrap">
            <div class="flex-1">
              <el-input v-model="form.videoUrl" placeholder="视频URL（支持上传或输入链接）" class="mb-2" />
              <div class="text-xs text-gray-400">支持 MP4、MOV 等格式，不超过100MB</div>
            </div>
            <el-upload
              class="video-uploader"
              action="/api/course/upload/video"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleVideoSuccess"
              :before-upload="beforeUploadVideo"
              :disabled="uploading"
            >
              <el-button :type="form.videoUrl ? 'success' : 'primary'" :loading="uploading">
                {{ form.videoUrl ? '重新上传' : '上传视频' }}
              </el-button>
            </el-upload>
          </div>
          <div v-if="form.videoUrl" class="mt-3">
            <div class="text-xs text-green-600">✓ 视频已上传</div>
            <video v-if="form.videoUrl" :src="form.videoUrl" class="w-48 h-32 object-cover rounded border mt-2" controls></video>
          </div>
        </el-form-item>
        
        <el-form-item label="课程简介">
          <el-input v-model="form.steps" type="textarea" :rows="4" placeholder="请输入课程详细介绍" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="难度">
              <el-select v-model="form.difficulty" style="width: 100%">
                <el-option :label="'⭐ 入门'" :value="1" />
                <el-option :label="'⭐⭐ 进阶'" :value="2" />
                <el-option :label="'⭐⭐⭐ 高级'" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="时长(分钟)">
              <el-input-number v-model="form.duration" :min="1" :max="300" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">上架</el-radio>
                <el-radio :label="0">下架</el-radio>
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
import { adminGetCourseList, adminSaveCourse, adminDeleteCourse } from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增课程')

const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('adminToken')}`
}

const form = ref({
  id: null,
  title: '',
  category: '传统技艺',
  cover: '',
  videoUrl: '',
  steps: '',
  difficulty: 1,
  duration: 30,
  status: 1
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await adminGetCourseList({ 
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
    dialogTitle.value = '编辑课程'
    form.value = { ...row }
  } else {
    dialogTitle.value = '新增课程'
    form.value = { id: null, title: '', category: '传统技艺', cover: '', videoUrl: '', steps: '', difficulty: 1, duration: 30, status: 1 }
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.title) {
    ElMessage.warning('请输入课程名称')
    return
  }
  
  saving.value = true
  try {
    const res = await adminSaveCourse(form.value)
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
  ElMessageBox.confirm('确定删除该课程吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await adminDeleteCourse(id)
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

// 图片上传
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

// 视频上传
const handleVideoSuccess = (response) => {
  uploading.value = false
  if (response.code === 200) {
    form.value.videoUrl = response.data.url
    ElMessage.success('视频上传成功')
  } else {
    ElMessage.error('上传失败: ' + response.message)
  }
}

const beforeUploadVideo = (file) => {
  const isVideo = file.type.startsWith('video/')
  const isLt100M = file.size / 1024 / 1024 < 100
  
  if (!isVideo) {
    ElMessage.error('只能上传视频文件')
    return false
  }
  if (!isLt100M) {
    ElMessage.error('视频大小不能超过 100MB')
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
.cover-uploader, .video-uploader {
  display: inline-block;
}
</style>