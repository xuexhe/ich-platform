<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-ich-dark to-ich-dark/80">
    <div class="w-full max-w-md p-8 bg-white rounded-2xl shadow-2xl mx-4">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-ich-dark">注册账号</h1>
        <p class="text-gray-500 mt-2">加入非遗云境，探索文化之美</p>
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
        
        <el-form-item prop="nickname">
          <el-input 
            v-model="form.nickname" 
            placeholder="昵称"
            :prefix-icon="Avatar"
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
        
        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="form.confirmPassword" 
            type="password" 
            placeholder="确认密码"
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
          @click="handleRegister"
        >
          注册
        </el-button>
        
        <div class="text-center mt-6">
          <span class="text-gray-500">已有账号？</span>
          <!-- 修复 underline 警告 -->
          <el-link type="primary" underline="never" @click="$router.push('/login')">立即登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Avatar } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirm, trigger: 'blur' }]
}

const handleRegister = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      const success = await userStore.register({
        username: form.username,
        password: form.password,
        nickname: form.nickname || form.username
      })
      if (success) {
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } else {
        ElMessage.error('用户名已存在')
      }
    } catch (error) {
      ElMessage.error('注册失败，请重试')
    } finally {
      loading.value = false
    }
  })
}
</script>