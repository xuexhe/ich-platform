<!-- src/views/admin/AdminLogin.vue -->
<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-ich-dark to-ich-dark/80">
    <div class="w-full max-w-md p-8 bg-white rounded-2xl shadow-2xl mx-4">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-ich-dark">非遗云境</h1>
        <p class="text-gray-500 mt-2">管理员登录</p>
      </div>
      
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="管理员账号" 
            :prefix-icon="User" 
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="密码" 
            :prefix-icon="Lock" 
            size="large" 
            show-password
          />
        </el-form-item>
        <el-button 
          type="danger" 
          size="large" 
          class="w-full" 
          :loading="loading" 
          @click="handleLogin"
        >
          登录
        </el-button>
      </el-form>
      
      <div class="text-center mt-6 flex justify-center gap-4">
        <el-link type="primary" underline="never" @click="$router.push('/login')">用户登录</el-link>
        <el-link type="info" underline="never" @click="$router.push('/index')">返回首页</el-link>
      </div>
      
      <div class="text-center mt-4">
        <p class="text-xs text-gray-400">
          默认管理员账号：admin / admin123
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { adminLogin } from '@/api/admin'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ 
  username: '', 
  password: '' 
})

const rules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      const res = await adminLogin(form)
      if (res.code === 200) {
        localStorage.setItem('adminToken', res.data.token)
        localStorage.setItem('isAdmin', 'true')
        localStorage.setItem('adminInfo', JSON.stringify(res.data.adminInfo))
        ElMessage.success('登录成功')
        router.push('/admin/dashboard')
      } else {
        ElMessage.error(res.message || '账号或密码错误')
      }
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error('登录失败，请检查网络')
    } finally {
      loading.value = false
    }
  })
}
</script>