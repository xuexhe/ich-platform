<template>
  <div class="min-h-screen bg-ich-bg">
    <Navbar />
    <div class="max-w-7xl mx-auto px-4 py-12">
      <div class="text-center mb-12">
        <h2 class="text-4xl font-bold text-ich-dark mb-2">AR非遗实景体验</h2>
        <p class="text-gray-500">虚实结合，让非遗文化走进现实</p>
      </div>
      
      <div class="w-full h-[600px] bg-gray-800 rounded-xl shadow-lg relative overflow-hidden">
        <!-- AR启动提示 -->
        <div v-if="!arStarted" class="absolute inset-0 flex flex-col items-center justify-center bg-black/70 text-white z-10">
          <div class="text-center px-8">
            <div class="text-6xl mb-4">📱</div>
            <h3 class="text-2xl font-bold mb-4">AR实景体验</h3>
            <p class="mb-8 max-w-md">将非遗模型放置到现实场景中，拍照分享你的AR体验</p>
            <el-button type="danger" size="large" round @click="startAR">
              开启AR体验
            </el-button>
            <p class="mt-8 text-sm opacity-70">提示：需要摄像头权限，请允许访问</p>
          </div>
        </div>
        
        <!-- AR视频容器 -->
        <video ref="videoRef" class="w-full h-full object-cover" autoplay playsinline></video>
        <canvas ref="canvasRef" class="absolute top-0 left-0 w-full h-full"></canvas>
        
        <!-- 模型选择面板 -->
        <div v-if="arStarted" class="absolute top-6 left-6 bg-white/90 backdrop-blur rounded-lg p-3 shadow-lg z-20">
          <h4 class="font-bold text-ich-dark mb-2 text-sm">选择模型</h4>
          <div class="flex gap-2">
            <button 
              v-for="model in models" 
              :key="model.id"
              @click="selectModel(model)"
              class="px-3 py-1 text-sm rounded-full transition"
              :class="currentModel.id === model.id ? 'bg-ich-red text-white' : 'bg-gray-100 hover:bg-gray-200'"
            >
              {{ model.name }}
            </button>
          </div>
        </div>
        
        <!-- 拍照按钮 -->
        <div v-if="arStarted" class="absolute bottom-8 left-1/2 -translate-x-1/2 z-20">
          <el-button type="danger" circle size="large" @click="takePhoto">
            <el-icon><Camera /></el-icon>
          </el-button>
        </div>
        
        <!-- 控制说明 -->
        <div v-if="arStarted" class="absolute bottom-8 right-6 bg-black/50 text-white text-xs px-3 py-2 rounded-full z-20">
          💡 点击屏幕放置模型 | 双指缩放
        </div>
      </div>
      
      <!-- 拍照结果预览 -->
      <div v-if="photoTaken" class="mt-8 bg-white rounded-xl shadow-lg p-6">
        <h3 class="text-lg font-bold text-ich-dark mb-4">📸 拍照预览</h3>
        <img :src="photoData" class="w-full max-w-md mx-auto rounded-lg shadow" />
        <div class="flex justify-center gap-4 mt-4">
          <el-button @click="savePhoto">保存图片</el-button>
          <el-button type="danger" @click="photoTaken = false">关闭</el-button>
        </div>
      </div>
      
      <div class="mt-8 text-center text-gray-500 text-sm">
        <p>提示：AR功能需要支持WebXR的浏览器（Chrome 80+、Safari 14+）</p>
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Camera } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'

const videoRef = ref(null)
const canvasRef = ref(null)
const arStarted = ref(false)
const photoTaken = ref(false)
const photoData = ref('')
let stream = null
let animationId = null

const models = [
  { id: 1, name: '青花瓷', icon: '🏺', image: '/images/model-qinghua.png' },
  { id: 2, name: '剪纸', icon: '✂️', image: '/images/model-jianzhi.png' },
  { id: 3, name: '皮影', icon: '🎭', image: '/images/model-piying.png' },
  { id: 4, name: '京剧脸谱', icon: '🎨', image: '/images/model-lianpu.png' }
]

const currentModel = ref(models[0])
let markerX = 0.5, markerY = 0.5, markerScale = 0.2

// 启动AR
const startAR = async () => {
  try {
    stream = await navigator.mediaDevices.getUserMedia({ video: true })
    if (videoRef.value) {
      videoRef.value.srcObject = stream
      await videoRef.value.play()
      arStarted.value = true
      startARCanvas()
    }
  } catch (error) {
    console.error('获取摄像头失败:', error)
    alert('无法获取摄像头权限，请检查设置')
  }
}

// AR画布渲染
const startARCanvas = () => {
  const video = videoRef.value
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')
  
  const draw = () => {
    if (!video || !canvas || !arStarted.value) return
    
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    
    // 绘制视频帧
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
    
    // 绘制AR模型（简单2D图片）
    const img = new Image()
    img.onload = () => {
      const width = canvas.width * markerScale
      const height = canvas.height * markerScale
      const x = canvas.width * markerX - width / 2
      const y = canvas.height * markerY - height / 2
      
      // 添加半透明背景
      ctx.save()
      ctx.shadowColor = 'rgba(0,0,0,0.5)'
      ctx.shadowBlur = 10
      ctx.drawImage(img, x, y, width, height)
      ctx.restore()
      
      // 添加光晕效果
      ctx.beginPath()
      ctx.arc(canvas.width * markerX, canvas.height * markerY, 8, 0, Math.PI * 2)
      ctx.fillStyle = 'rgba(212, 175, 55, 0.6)'
      ctx.fill()
    }
    img.src = currentModel.value.image || 'https://picsum.photos/200/200'
    
    animationId = requestAnimationFrame(draw)
  }
  
  draw()
  
  // 点击放置模型
  canvas.addEventListener('click', (e) => {
    const rect = canvas.getBoundingClientRect()
    markerX = (e.clientX - rect.left) / rect.width
    markerY = (e.clientY - rect.top) / rect.height
  })
  
  // 滚轮缩放
  canvas.addEventListener('wheel', (e) => {
    e.preventDefault()
    markerScale += e.deltaY * -0.001
    markerScale = Math.min(0.5, Math.max(0.1, markerScale))
  })
}

// 选择模型
const selectModel = (model) => {
  currentModel.value = model
}

// 拍照
const takePhoto = () => {
  const canvas = canvasRef.value
  if (canvas) {
    photoData.value = canvas.toDataURL('image/png')
    photoTaken.value = true
  }
}

// 保存图片
const savePhoto = () => {
  const link = document.createElement('a')
  link.download = `非遗AR体验_${new Date().getTime()}.png`
  link.href = photoData.value
  link.click()
  alert('图片已保存')
}

// 停止AR
const stopAR = () => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  if (stream) {
    stream.getTracks().forEach(track => track.stop())
  }
}

onUnmounted(() => {
  stopAR()
})
</script>