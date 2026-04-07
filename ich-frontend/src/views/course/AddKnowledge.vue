<!-- src/views/course/AddKnowledge.vue -->
<template>
  <div class="min-h-screen bg-ich-bg">
    <Navbar />
    
    <div class="max-w-4xl mx-auto px-4 py-12">
      <div class="flex items-center gap-3 mb-6">
        <el-button type="text" @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <h2 class="text-2xl font-bold text-ich-dark">📝 {{ isEdit ? '编辑知识' : '添加非遗知识' }}</h2>
      </div>
      
      <div class="bg-white rounded-xl shadow-lg p-8">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入知识标题" maxlength="100" show-word-limit />
          </el-form-item>
          
          <el-form-item label="分类" prop="category">
            <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
              <el-option label="剪纸" value="剪纸" />
              <el-option label="陶瓷" value="陶瓷" />
              <el-option label="戏曲" value="戏曲" />
              <el-option label="刺绣" value="刺绣" />
              <el-option label="民俗" value="民俗" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="封面图片">
            <div class="flex gap-3 items-start">
              <el-input v-model="form.cover" placeholder="图片URL" class="flex-1" />
              <el-upload
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
              <img :src="form.cover" class="w-32 h-24 object-cover rounded border" />
            </div>
          </el-form-item>
          
          <el-form-item label="简介" prop="summary">
            <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="请输入简短介绍" maxlength="200" show-word-limit />
          </el-form-item>
          
          <el-form-item label="详细内容" prop="content">
            <el-input v-model="form.content" type="textarea" :rows="10" placeholder="请输入详细内容（支持HTML标签）" />
          </el-form-item>
          
          <el-form-item>
            <div class="bg-gray-50 rounded-lg p-4 mb-4">
              <p class="text-sm text-gray-500">📌 提交须知：</p>
              <ul class="text-xs text-gray-400 mt-2 space-y-1">
                <li>• 内容需与非遗文化相关</li>
                <li>• 编辑已发布的内容后需要重新审核</li>
                <li>• 审核通过后才会在知识库展示</li>
                <li>• 请勿提交违规内容</li>
              </ul>
            </div>
            <el-button type="danger" size="large" @click="submitForm" :loading="submitting">
              {{ isEdit ? '保存修改' : '提交审核' }}
            </el-button>
            <el-button size="large" @click="$router.back()">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 我的投稿列表 -->
      <div class="mt-8">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-xl font-bold text-ich-dark">📋 我的投稿</h3>
          <el-button size="small" text @click="fetchMyKnowledge">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
        
        <div v-if="myKnowledge.length === 0" class="bg-white rounded-xl p-8 text-center text-gray-500">
          暂无投稿，快去添加第一条知识吧～
        </div>
        <div v-else class="space-y-3">
          <div v-for="item in myKnowledge" :key="item.id" class="bg-white rounded-xl p-4 shadow-sm">
            <div class="flex justify-between items-start">
              <div class="flex-1">
                <h4 class="font-bold text-ich-dark">{{ item.title }}</h4>
                <p class="text-sm text-gray-500 mt-1">{{ item.summary }}</p>
                <div class="flex items-center gap-3 mt-2 text-xs">
                  <span class="px-2 py-1 rounded" :class="getStatusClass(item.status)">
                    {{ getStatusText(item.status) }}
                  </span>
                  <span class="text-gray-400">{{ formatDate(item.createTime) }}</span>
                </div>
              </div>
              <div class="flex gap-2">
                <!-- 所有状态都可以编辑 -->
                <el-button size="small" type="primary" plain @click="editKnowledge(item)">
                  <el-icon><Edit /></el-icon> 编辑
                </el-button>
                <!-- 只有待审核或已驳回可以删除 -->
                <el-button v-if="item.status === 0 || item.status === 2" size="small" type="danger" plain @click="handleDeleteKnowledge(item.id)">
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <Footer />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import { addKnowledge, getUserKnowledge, deleteKnowledge, updateKnowledge } from '@/api/knowledge'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)
const myKnowledge = ref([])
const isEdit = ref(false)
const editId = ref(null)

const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('token')}`
}

const form = reactive({
  id: null,
  title: '',
  category: '剪纸',
  cover: '',
  summary: '',
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  summary: [{ required: true, message: '请输入简介', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已发布', 2: '已驳回' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'bg-yellow-100 text-yellow-700', 1: 'bg-green-100 text-green-700', 2: 'bg-red-100 text-red-700' }
  return map[status] || 'bg-gray-100 text-gray-700'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

const fetchMyKnowledge = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getUserKnowledge(userStore.userInfo.id, { pageNum: 1, pageSize: 50 })
    if (res.code === 200) {
      myKnowledge.value = res.data.records || []
    }
  } catch (error) {
    console.error('获取投稿失败:', error)
  }
}

// 编辑知识（所有状态都可编辑）
const editKnowledge = async (item) => {
  isEdit.value = true
  editId.value = item.id
  form.id = item.id
  form.title = item.title
  form.category = item.category
  form.cover = item.cover
  form.summary = item.summary
  form.content = item.content
  
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
  ElMessage.info('编辑后需要重新审核')
}

const resetForm = () => {
  isEdit.value = false
  editId.value = null
  form.id = null
  form.title = ''
  form.category = '剪纸'
  form.cover = ''
  form.summary = ''
  form.content = ''
  formRef.value?.resetFields()
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      let res
      if (isEdit.value && editId.value) {
        res = await updateKnowledge(editId.value, form, userStore.userInfo.id)
      } else {
        res = await addKnowledge(form, userStore.userInfo.id)
      }
      
      if (res.code === 200) {
        ElMessage.success(res.message || (isEdit.value ? '保存成功，请等待审核' : '提交成功'))
        resetForm()
        fetchMyKnowledge()
        if (route.query.from === 'detail') {
          router.push('/course?tab=knowledge')
        }
      } else {
        ElMessage.error(res.message || (isEdit.value ? '保存失败' : '提交失败'))
      }
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleDeleteKnowledge = (id) => {
  ElMessageBox.confirm('确定要删除这条知识吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await deleteKnowledge(id, userStore.userInfo.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchMyKnowledge()
        if (editId.value === id) {
          resetForm()
        }
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
    form.cover = response.data.url
    ElMessage.success('图片上传成功')
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

onMounted(() => {
  fetchMyKnowledge()
})
</script>