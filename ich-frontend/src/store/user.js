// src/store/user.js
import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.nickname || state.userInfo?.username || '游客',
    userId: (state) => state.userInfo?.id || null,
    userRole: (state) => state.userInfo?.role || 'user',
    isAdmin: (state) => state.userInfo?.role === 'admin',
    avatar: (state) => {
      if (state.userInfo?.avatar && 
          state.userInfo.avatar !== '/images/default-avatar.png' &&
          !state.userInfo.avatar.includes('dicebear')) {
        if (state.userInfo.avatar.startsWith('http') || state.userInfo.avatar.startsWith('/uploads')) {
          return state.userInfo.avatar
        }
      }
      const seed = state.userInfo?.username || 'user'
      return `https://api.dicebear.com/7.x/avataaars/svg?seed=${seed}&backgroundColor=b6e3f4`
    }
  },
  
  actions: {
    // 登录
    async login(data) {
      try {
        const res = await request.post('/user/login', data)
        if (res.code === 200) {
          this.token = res.data.token
          this.userInfo = res.data.userInfo
          localStorage.setItem('token', res.data.token)
          localStorage.setItem('userInfo', JSON.stringify(res.data.userInfo))
          console.log('登录成功，用户信息:', this.userInfo)
          return true
        }
        return false
      } catch (error) {
        console.error('登录失败:', error)
        return false
      }
    },
    
    // 注册
    async register(data) {
      try {
        const res = await request.post('/user/register', data)
        return res.code === 200
      } catch (error) {
        console.error('注册失败:', error)
        return false
      }
    },
    
    // 获取用户信息
    async fetchUserInfo() {
      if (!this.userInfo?.id) return
      try {
        const res = await request.get(`/user/info/${this.userInfo.id}`)
        if (res.code === 200) {
          this.userInfo = res.data
          localStorage.setItem('userInfo', JSON.stringify(res.data))
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },
    
    // 更新用户信息
    async updateUserInfo(data) {
      try {
        const res = await request.put('/user/update', data)
        if (res.code === 200) {
          await this.fetchUserInfo()
          return true
        }
        return false
      } catch (error) {
        console.error('更新用户信息失败:', error)
        return false
      }
    },
    
    // 上传头像
    async uploadAvatar(file) {
      try {
        console.log('准备上传头像, 用户ID:', this.userInfo.id)
        
        const formData = new FormData()
        formData.append('file', file)
        formData.append('userId', this.userInfo.id)
        
        const res = await request.post('/user/uploadAvatar', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          },
          timeout: 30000
        })
        
        console.log('上传响应:', res)
        
        if (res.code === 200) {
          this.userInfo.avatar = res.data.avatarUrl
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
          console.log('头像更新成功:', res.data.avatarUrl)
          return true
        } else {
          console.error('上传失败:', res.message)
          return false
        }
      } catch (error) {
        console.error('上传头像异常:', error)
        if (error.response) {
          console.error('响应状态:', error.response.status)
          console.error('响应数据:', error.response.data)
        }
        return false
      }
    },
    
    // 修改密码
    async changePassword(oldPassword, newPassword) {
      try {
        const res = await request.post('/user/changePassword', {
          userId: this.userInfo.id,
          oldPassword: oldPassword,
          newPassword: newPassword
        })
        if (res.code === 200) {
          console.log('密码修改成功')
          return true
        } else {
          console.error('密码修改失败:', res.message)
          return false
        }
      } catch (error) {
        console.error('修改密码失败:', error)
        if (error.response?.data?.message) {
          throw new Error(error.response.data.message)
        }
        throw new Error('网络错误，请稍后重试')
      }
    },
    
    // 注销账号
    async deleteAccount(password) {
      try {
        const res = await request.post('/user/delete', {
          userId: this.userInfo.id,
          password: password
        })
        if (res.code === 200) {
          console.log('账号注销成功')
          this.logout()
          return true
        } else {
          console.error('注销失败:', res.message)
          return false
        }
      } catch (error) {
        console.error('注销失败:', error)
        if (error.response?.data?.message) {
          throw new Error(error.response.data.message)
        }
        throw new Error('网络错误，请稍后重试')
      }
    },
    
    // 找回密码（发送验证码）
    async sendResetCode(username) {
      try {
        const res = await request.post('/user/sendResetCode', { username })
        return res.code === 200
      } catch (error) {
        console.error('发送验证码失败:', error)
        return false
      }
    },
    
    // 重置密码
    async resetPassword(username, code, newPassword) {
      try {
        const res = await request.post('/user/resetPassword', {
          username,
          code,
          newPassword
        })
        return res.code === 200
      } catch (error) {
        console.error('重置密码失败:', error)
        return false
      }
    },
    
    // 退出登录
    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      console.log('已退出登录')
    }
  }
})