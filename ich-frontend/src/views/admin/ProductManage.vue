<!-- src/views/admin/ProductManage.vue -->
<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-ich-dark">商品管理</h1>
      <el-button type="danger" @click="openDialog()">新增商品</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="mb-4">
      <el-input 
        v-model="keyword" 
        placeholder="搜索商品名称" 
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
      <el-table-column prop="name" label="商品名称" min-width="150" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <img :src="row.cover" class="w-12 h-12 object-cover rounded" @error="handleImageError" />
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="originalPrice" label="原价" width="100">
        <template #default="{ row }">¥{{ row.originalPrice }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" required>
              <el-input v-model="form.name" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="form.category" style="width: 100%">
                <el-option label="手工艺品" value="手工艺品" />
                <el-option label="文化周边" value="文化周边" />
                <el-option label="非遗食材" value="非遗食材" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="封面URL">
          <div class="flex gap-3 items-start">
            <el-input v-model="form.cover" placeholder="请输入商品封面图片URL" class="flex-1" />
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
            <img :src="form.cover" class="w-20 h-20 object-cover rounded" @error="handleImageError" />
          </div>
        </el-form-item>
        
        <el-form-item label="简介">
          <el-input v-model="form.intro" type="textarea" :rows="2" placeholder="请输入商品简介" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="价格">
              <el-input-number 
                v-model="form.price" 
                :precision="2" 
                :min="0" 
                :step="1"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原价">
              <el-input-number 
                v-model="form.originalPrice" 
                :precision="2" 
                :min="0" 
                :step="1"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="折扣">
              <el-input-number 
                v-model="form.discount" 
                :precision="1" 
                :min="0" 
                :max="10"
                :step="0.5"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="库存">
              <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
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
import { adminGetProductList, adminSaveProduct, adminDeleteProduct } from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('新增商品')

const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('adminToken')}`
}

const form = ref({
  name: '',
  category: '手工艺品',
  cover: '',
  intro: '',
  price: 0,
  originalPrice: 0,
  discount: 10,
  stock: 0,
  status: 1
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await adminGetProductList({ 
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
    dialogTitle.value = '编辑商品'
    form.value = { ...row }
  } else {
    dialogTitle.value = '新增商品'
    form.value = { name: '', category: '手工艺品', cover: '', intro: '', price: 0, originalPrice: 0, discount: 10, stock: 0, status: 1 }
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入商品名称')
    return
  }
  if (form.value.price <= 0) {
    ElMessage.warning('请输入正确的价格')
    return
  }
  
  saving.value = true
  try {
    const res = await adminSaveProduct(form.value)
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
  ElMessageBox.confirm('确定删除该商品吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      const res = await adminDeleteProduct(id)
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

const handleCoverSuccess = (response, file) => {
  if (response.code === 200) {
    form.value.cover = response.data.url
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