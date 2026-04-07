<!-- src/views/admin/QuizManage.vue -->
<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-ich-dark">每日一题管理</h1>
      <el-button type="danger" @click="openDialog()">新增题目</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="mb-4">
      <el-input 
        v-model="keyword" 
        placeholder="搜索题目" 
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
      <el-table-column prop="question" label="题目" min-width="250" show-overflow-tooltip />
      <el-table-column prop="optionA" label="A选项" width="120" show-overflow-tooltip />
      <el-table-column prop="optionB" label="B选项" width="120" show-overflow-tooltip />
      <el-table-column prop="optionC" label="C选项" width="120" show-overflow-tooltip />
      <el-table-column prop="optionD" label="D选项" width="120" show-overflow-tooltip />
      <el-table-column prop="correctAnswer" label="答案" width="80" />
      <el-table-column prop="quizDate" label="题目日期" width="120" />
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="题目" required>
          <el-input v-model="form.question" type="textarea" :rows="2" placeholder="请输入题目" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="A选项" required>
              <el-input v-model="form.optionA" placeholder="请输入A选项" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="B选项" required>
              <el-input v-model="form.optionB" placeholder="请输入B选项" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="C选项" required>
              <el-input v-model="form.optionC" placeholder="请输入C选项" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="D选项" required>
              <el-input v-model="form.optionD" placeholder="请输入D选项" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="正确答案" required>
              <el-select v-model="form.correctAnswer" style="width: 100%">
                <el-option label="A" value="A" />
                <el-option label="B" value="B" />
                <el-option label="C" value="C" />
                <el-option label="D" value="D" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="题目日期" required>
              <el-date-picker v-model="form.quizDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="解析">
          <el-input v-model="form.explanation" type="textarea" :rows="3" placeholder="请输入题目解析" />
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
import { adminGetQuizList, adminSaveQuiz, adminDeleteQuiz } from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增题目')

const form = ref({
  id: null,
  question: '',
  optionA: '',
  optionB: '',
  optionC: '',
  optionD: '',
  correctAnswer: 'A',
  explanation: '',
  quizDate: new Date().toISOString().split('T')[0]
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await adminGetQuizList({ 
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
    dialogTitle.value = '编辑题目'
    form.value = { ...row }
  } else {
    dialogTitle.value = '新增题目'
    form.value = {
      id: null,
      question: '',
      optionA: '',
      optionB: '',
      optionC: '',
      optionD: '',
      correctAnswer: 'A',
      explanation: '',
      quizDate: new Date().toISOString().split('T')[0]
    }
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.question) {
    ElMessage.warning('请输入题目')
    return
  }
  if (!form.value.optionA || !form.value.optionB || !form.value.optionC || !form.value.optionD) {
    ElMessage.warning('请填写所有选项')
    return
  }
  if (!form.value.quizDate) {
    ElMessage.warning('请选择日期')
    return
  }
  
  saving.value = true
  try {
    const res = await adminSaveQuiz(form.value)
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
  ElMessageBox.confirm('确定删除该题目吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await adminDeleteQuiz(id)
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