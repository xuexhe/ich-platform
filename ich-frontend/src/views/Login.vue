<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-ich-dark to-ich-dark/80">
    <div class="w-full max-w-md p-8 bg-white rounded-2xl shadow-2xl mx-4">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-ich-dark">非遗云境</h1>
        <p class="text-gray-500 mt-2">登录你的账号</p>
      </div>
      
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="用户名"
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
        
        <div class="flex justify-between items-center mb-6">
          <el-checkbox v-model="remember">记住我</el-checkbox>
          <!-- 修复 underline 警告 -->
          <el-link type="primary" underline="never">忘记密码？</el-link>
        </div>
        
        <el-button 
          type="danger" 
          size="large" 
          class="w-full" 
          :loading="loading"
          @click="handleLogin"
        >
          登录
        </el-button>
        
        <div class="text-center mt-6">
          <span class="text-gray-500">还没有账号？</span>
          <!-- 修复 underline 警告 -->
          <el-link type="primary" underline="never" @click="$router.push('/register')">立即注册</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const remember = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      const success = await userStore.login(form)
      if (success) {
        ElMessage.success('登录成功')
        const redirect = route.query.redirect || '/index'
        router.push(redirect)
      } else {
        ElMessage.error('用户名或密码错误')
      }
    } catch (error) {
      ElMessage.error('登录失败，请重试')
    } finally {
      loading.value = false
    }
  })
}
</script>