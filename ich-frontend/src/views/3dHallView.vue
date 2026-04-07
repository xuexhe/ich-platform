<!-- src/views/3dHallView.vue -->
<template>
  <div class="min-h-screen bg-ich-bg">
    <Navbar />
    
    <div class="max-w-7xl mx-auto px-4 py-12">
      <div class="text-center mb-8">
        <h2 class="text-4xl font-bold text-ich-dark mb-2">3D数字展厅</h2>
        <p class="text-gray-500">沉浸式欣赏非遗文物，360°无死角观察细节</p>
      </div>
      
      <!-- 展厅分类选择 -->
      <div class="flex justify-center gap-3 mb-8 flex-wrap">
        <button 
          v-for="hall in exhibitionHalls" 
          :key="hall.id"
          @click="switchHall(hall.id)"
          class="px-5 py-2 rounded-full transition-all"
          :class="currentHallId === hall.id 
            ? 'bg-ich-red text-white shadow-lg' 
            : 'bg-white text-ich-dark hover:bg-ich-gold/20 border border-gray-200'"
        >
          <span class="mr-2">{{ hall.icon }}</span>
          {{ hall.name }}
          <span class="ml-1 text-xs opacity-70">({{ getHallModelCount(hall.id) }})</span>
        </button>
      </div>
      
      <!-- 当前展厅信息 -->
      <div class="text-center mb-4">
        <h3 class="text-2xl font-bold text-ich-dark">{{ currentHallName }}</h3>
        <p class="text-gray-400 text-sm">{{ currentHallDesc }}</p>
      </div>
      
      <!-- 3D容器 -->
      <div class="relative w-full h-[550px] bg-gradient-to-b from-ich-paper to-white rounded-xl shadow-lg overflow-hidden">
        <div ref="canvasContainer" class="w-full h-full"></div>
        
        <!-- 左右切换按钮 -->
        <div v-if="currentHallModels.length > 1" class="absolute inset-y-0 left-0 right-0 flex items-center justify-between px-4 z-20 pointer-events-none">
          <button 
            @click="prevModel" 
            class="w-12 h-12 rounded-full bg-white/80 backdrop-blur shadow-lg flex items-center justify-center hover:bg-ich-red hover:text-white transition-all pointer-events-auto"
            title="上一个"
          >
            <el-icon :size="28"><ArrowLeft /></el-icon>
          </button>
          <button 
            @click="nextModel" 
            class="w-12 h-12 rounded-full bg-white/80 backdrop-blur shadow-lg flex items-center justify-center hover:bg-ich-red hover:text-white transition-all pointer-events-auto"
            title="下一个"
          >
            <el-icon :size="28"><ArrowRight /></el-icon>
          </button>
        </div>
        
        <!-- 模型信息卡片 -->
        <div v-if="currentModel" class="absolute bottom-6 left-6 bg-white/95 backdrop-blur rounded-xl p-4 shadow-lg max-w-xs z-10">
          <div class="flex justify-between items-start">
            <div>
              <h3 class="text-lg font-bold text-ich-dark">{{ currentModel.name }}</h3>
              <p class="text-sm text-gray-500 mt-1">{{ currentModel.category || currentHallName }} · 非遗瑰宝</p>
              <p class="text-xs text-gray-400 mt-2">{{ currentModel.description || '3D文物模型' }}</p>
            </div>
            <div class="text-center ml-3">
              <div class="text-xs text-gray-400">第 {{ currentIndex + 1 }} / {{ currentHallModels.length }} 件</div>
            </div>
          </div>
        </div>
        
        <!-- 控制按钮组（增加下载按钮） -->
        <div class="absolute bottom-6 right-6 flex gap-3 z-10">
          <button 
            @click="downloadModel" 
            class="w-10 h-10 rounded-full bg-white shadow-lg flex items-center justify-center hover:bg-ich-gold/20 transition"
            :class="{ 'bg-ich-gold/30': currentModel?.modelUrl }"
            title="下载模型"
          >
            <el-icon><Download /></el-icon>
          </button>
          <button 
            @click="resetCamera" 
            class="w-10 h-10 rounded-full bg-white shadow-lg flex items-center justify-center hover:bg-ich-gold/20 transition"
            title="重置视角"
          >
            <el-icon><RefreshRight /></el-icon>
          </button>
          <button 
            @click="toggleAutoRotate" 
            class="w-10 h-10 rounded-full bg-white shadow-lg flex items-center justify-center hover:bg-ich-gold/20 transition"
            :class="{ 'bg-ich-gold/30': autoRotate }"
            title="自动旋转"
          >
            <el-icon><Refresh /></el-icon>
          </button>
          <button 
            @click="toggleFullscreen" 
            class="w-10 h-10 rounded-full bg-white shadow-lg flex items-center justify-center hover:bg-ich-gold/20 transition"
            title="全屏模式"
          >
            <el-icon><FullScreen /></el-icon>
          </button>
        </div>
        
        <!-- 模型指示器（小圆点） -->
        <div v-if="currentHallModels.length > 1" class="absolute bottom-6 left-1/2 -translate-x-1/2 flex gap-2 z-10">
          <button 
            v-for="(_, idx) in currentHallModels" 
            :key="idx"
            @click="goToModel(idx)"
            class="w-2 h-2 rounded-full transition-all"
            :class="currentIndex === idx ? 'bg-ich-red w-4' : 'bg-gray-300 hover:bg-gray-400'"
          ></button>
        </div>
        
        <!-- 加载进度条 -->
        <div v-if="loading" class="absolute inset-0 flex flex-col items-center justify-center bg-black/70 z-20">
          <div class="bg-white rounded-2xl p-6 text-center w-72 shadow-2xl">
            <div class="mb-4">
              <el-icon class="is-loading text-ich-red" :size="32"><Loading /></el-icon>
            </div>
            <el-progress 
              :percentage="loadProgress" 
              :stroke-width="8" 
              :color="progressColor"
              :show-text="false"
            />
            <p class="mt-4 text-gray-700 font-medium">{{ loadText }}</p>
            <p class="text-xs text-gray-400 mt-2">{{ loadProgress }}%</p>
          </div>
        </div>
        
        <!-- 错误提示 -->
        <div v-if="error" class="absolute inset-0 flex items-center justify-center bg-black/70 z-20">
          <div class="bg-white rounded-2xl p-6 text-center w-80 shadow-2xl">
            <div class="text-5xl mb-4">⚠️</div>
            <p class="text-gray-700 mb-4">{{ error }}</p>
            <el-button type="danger" @click="retryLoad">重试</el-button>
          </div>
        </div>
        
        <!-- 空状态提示 -->
        <div v-if="!loading && currentHallModels.length === 0 && !error" class="absolute inset-0 flex flex-col items-center justify-center bg-white/80 z-10">
          <div class="text-center">
            <div class="text-6xl mb-4">🏺</div>
            <h3 class="text-xl font-bold text-ich-dark mb-2">暂无{{ currentHallName }}模型</h3>
            <p class="text-gray-500 mb-4">请管理员在后台添加3D模型文件</p>
            <el-button v-if="userStore.isAdmin" type="danger" @click="$router.push('/admin/model')">
              前往添加模型
            </el-button>
          </div>
        </div>
      </div>
      
      <div class="mt-6 text-center text-gray-400 text-sm">
        <p>💡 鼠标拖拽旋转视角 | 右键拖拽平移 | 滚轮缩放 | 点击左右箭头切换展品 | 点击下载按钮保存模型</p>
      </div>
    </div>
    
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive, computed, nextTick } from 'vue'
import * as THREE from 'three'
import { GLTFLoader } from 'three/addons/loaders/GLTFLoader.js'
import { OrbitControls } from 'three/addons/controls/OrbitControls.js'
import { RefreshRight, Refresh, Loading, FullScreen, ArrowLeft, ArrowRight, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import { getModelList } from '@/api/model'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const canvasContainer = ref(null)
let scene, camera, renderer, controls, currentModelObject
let animationId = null
let currentLoadInterval = null

// 展厅配置（包含刺绣展厅）
const exhibitionHalls = [
  { id: 'porcelain', name: '瓷器展厅', icon: '🏺', desc: '青花瓷、粉彩瓷、青白釉等精美瓷器' },
  { id: 'bronze', name: '青铜器展厅', icon: '🏛️', desc: '青铜鼎、青铜剑等商周青铜器' },
  { id: 'jade', name: '玉器展厅', icon: '💎', desc: '玉琮、玉璧等古代玉器' },
  { id: 'embroidery', name: '刺绣展厅', icon: '🪡', desc: '苏绣、湘绣等精美刺绣艺术' },
  { id: 'papercut', name: '剪纸展厅', icon: '✂️', desc: '传统剪纸艺术' },
  { id: 'shadow', name: '皮影展厅', icon: '🎭', desc: '皮影戏人物' },
  { id: 'other', name: '其他文物', icon: '🏺', desc: '其他非遗文物' }
]

const currentHallId = ref('porcelain')
const currentHallName = computed(() => exhibitionHalls.find(h => h.id === currentHallId.value)?.name || '瓷器展厅')
const currentHallDesc = computed(() => exhibitionHalls.find(h => h.id === currentHallId.value)?.desc || '')

// 模型数据
const allModels = ref([])  // 所有模型
const currentHallModels = ref([])  // 当前展厅模型
const currentIndex = ref(0)
const currentModel = ref(null)

// 3D相关
const loading = ref(false)
const error = ref(null)
const autoRotate = ref(true)
const loadProgress = ref(0)
const loadText = ref('初始化场景...')

const progressColor = computed(() => {
  if (loadProgress.value < 30) return '#D4AF37'
  if (loadProgress.value < 70) return '#C92C2C'
  return '#67C23A'
})

const loader = new GLTFLoader()

// 下载模型文件
const downloadModel = () => {
  if (!currentModel.value || !currentModel.value.modelUrl) {
    ElMessage.warning('当前模型没有可下载的文件')
    return
  }
  
  const modelUrl = currentModel.value.modelUrl
  // 从URL中提取文件名
  let fileName = `${currentModel.value.name || '3d_model'}.glb`
  if (modelUrl.includes('/')) {
    const urlParts = modelUrl.split('/')
    const lastPart = urlParts[urlParts.length - 1]
    if (lastPart.includes('.')) {
      fileName = lastPart
    }
  }
  
  // 创建下载链接
  const link = document.createElement('a')
  link.href = modelUrl
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  ElMessage.success(`开始下载：${fileName}`)
}

// 获取模型列表
const fetchModels = async () => {
  try {
    const res = await getModelList()
    if (res.code === 200 && res.data) {
      allModels.value = res.data.map(m => ({
        id: m.id,
        name: m.name,
        category: m.category,
        categoryId: getCategoryId(m.category),
        icon: getCategoryIcon(m.category),
        description: m.description || '3D文物模型',
        modelUrl: m.modelUrl,
        cover: m.cover
      }))
    }
  } catch (error) {
    console.error('获取模型列表失败:', error)
  }
}

// 根据分类获取展厅ID（包含苏绣/刺绣映射）
const getCategoryId = (category) => {
  const map = {
    // 瓷器类
    '瓷器': 'porcelain', 
    '青花瓷': 'porcelain', 
    '粉彩瓷': 'porcelain', 
    '青白釉': 'porcelain',
    '景德镇陶瓷': 'porcelain',
    '唐三彩': 'porcelain',
    // 青铜器类
    '青铜器': 'bronze', 
    '青铜': 'bronze',
    // 玉器类
    '玉器': 'jade', 
    '玉': 'jade',
    '玉琮': 'jade',
    '玉璧': 'jade',
    // 刺绣/苏绣类（新增）
    '苏绣': 'embroidery',
    '刺绣': 'embroidery',
    '湘绣': 'embroidery',
    '蜀绣': 'embroidery',
    '粤绣': 'embroidery',
    // 其他
    '剪纸': 'papercut',
    '皮影': 'shadow',
    '皮影戏': 'shadow'
  }
  return map[category] || 'other'
}

// 获取分类图标
const getCategoryIcon = (category) => {
  const icons = { 
    '瓷器': '🏺', 
    '青铜器': '🏛️', 
    '玉器': '💎', 
    '苏绣': '🪡', 
    '刺绣': '🪡',
    '剪纸': '✂️', 
    '皮影': '🎭' 
  }
  return icons[category] || '🏺'
}

// 切换展厅
const switchHall = (hallId) => {
  currentHallId.value = hallId
  // 筛选当前展厅的模型
  currentHallModels.value = allModels.value.filter(m => m.categoryId === hallId)
  currentIndex.value = 0
  if (currentHallModels.value.length > 0) {
    loadModel(currentHallModels.value[0])
  } else {
    // 无模型时清空显示
    if (currentModelObject) {
      scene.remove(currentModelObject)
      currentModelObject = null
    }
    currentModel.value = null
  }
}

// 获取展厅模型数量
const getHallModelCount = (hallId) => {
  return allModels.value.filter(m => m.categoryId === hallId).length
}

// 上一个模型
const prevModel = () => {
  if (currentHallModels.value.length === 0) return
  currentIndex.value = (currentIndex.value - 1 + currentHallModels.value.length) % currentHallModels.value.length
  loadModel(currentHallModels.value[currentIndex.value])
}

// 下一个模型
const nextModel = () => {
  if (currentHallModels.value.length === 0) return
  currentIndex.value = (currentIndex.value + 1) % currentHallModels.value.length
  loadModel(currentHallModels.value[currentIndex.value])
}

// 跳转到指定模型
const goToModel = (index) => {
  if (index < 0 || index >= currentHallModels.value.length) return
  currentIndex.value = index
  loadModel(currentHallModels.value[currentIndex.value])
}

// 加载模型
const loadModel = async (modelData) => {
  if (!modelData || !modelData.modelUrl) return
  
  loading.value = true
  error.value = null
  currentModel.value = modelData
  loadProgress.value = 0
  loadText.value = '加载模型...'
  
  const steps = [
    { progress: 10, text: '下载模型文件...' },
    { progress: 30, text: '解析几何数据...' },
    { progress: 50, text: '加载纹理贴图...' },
    { progress: 70, text: '构建材质系统...' },
    { progress: 90, text: '优化渲染...' }
  ]
  
  let stepIndex = 0
  if (currentLoadInterval) clearInterval(currentLoadInterval)
  currentLoadInterval = setInterval(() => {
    if (stepIndex < steps.length) {
      loadProgress.value = steps[stepIndex].progress
      loadText.value = steps[stepIndex].text
      stepIndex++
    }
  }, 200)
  
  try {
    const model = await loadExternalModel(modelData.modelUrl)
    
    clearInterval(currentLoadInterval)
    loadProgress.value = 100
    loadText.value = '加载完成！'
    
    if (currentModelObject) {
      scene.remove(currentModelObject)
      disposeModel(currentModelObject)
    }
    
    currentModelObject = model
    scene.add(currentModelObject)
    resetCamera()
    
    setTimeout(() => {
      loading.value = false
    }, 300)
  } catch (err) {
    clearInterval(currentLoadInterval)
    console.error('模型加载失败:', err)
    error.value = '模型加载失败，请检查文件格式'
    loading.value = false
  }
}

// 加载外部模型
const loadExternalModel = (modelUrl) => {
  return new Promise((resolve, reject) => {
    loader.load(modelUrl, (gltf) => {
      const model = gltf.scene
      
      // 双面渲染 + 材质优化
      model.traverse((child) => {
        if (child.isMesh) {
          if (child.material) {
            if (Array.isArray(child.material)) {
              child.material.forEach(mat => {
                mat.side = THREE.DoubleSide
                mat.needsUpdate = true
              })
            } else {
              child.material.side = THREE.DoubleSide
              child.material.needsUpdate = true
            }
          }
        }
      })
      
      // 调整大小和位置
      const box = new THREE.Box3().setFromObject(model)
      const size = box.getSize(new THREE.Vector3())
      const maxDim = Math.max(size.x, size.y, size.z)
      const scale = 1.2 / maxDim
      model.scale.set(scale, scale, scale)
      
      const newBox = new THREE.Box3().setFromObject(model)
      const newCenter = newBox.getCenter(new THREE.Vector3())
      model.position.set(-newCenter.x, -newCenter.y + 0.2, -newCenter.z)
      
      model.castShadow = true
      model.receiveShadow = true
      
      resolve(model)
    }, undefined, (error) => {
      reject(error)
    })
  })
}

// 初始化3D场景
const initScene = () => {
  if (!canvasContainer.value) return false
  
  const container = canvasContainer.value
  const width = container.clientWidth
  const height = container.clientHeight
  
  if (width === 0 || height === 0) return false
  
  try {
    scene = new THREE.Scene()
    scene.background = new THREE.Color(0xf5f5dc)
    scene.fog = new THREE.FogExp2(0xf5f5dc, 0.008)
    
    camera = new THREE.PerspectiveCamera(45, width / height, 0.01, 1000)
    camera.position.set(2, 1.5, 4)
    
    renderer = new THREE.WebGLRenderer({ antialias: true })
    renderer.setSize(width, height)
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
    renderer.shadowMap.enabled = true
    container.appendChild(renderer.domElement)
    
    controls = new OrbitControls(camera, renderer.domElement)
    controls.enableDamping = true
    controls.dampingFactor = 0.05
    controls.autoRotate = autoRotate.value
    controls.autoRotateSpeed = 1.5
    controls.enableZoom = true
    controls.enablePan = true
    controls.zoomSpeed = 1.5
    controls.panSpeed = 0.8
    controls.target.set(0, 0, 0)
    controls.minDistance = 0.5
    controls.maxDistance = 8
    
    setupLights()
    setupParticles()
    
    return true
  } catch (err) {
    console.error('初始化场景失败:', err)
    error.value = '3D场景初始化失败'
    return false
  }
}

const setupLights = () => {
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.8)
  scene.add(ambientLight)
  
  const mainLight = new THREE.DirectionalLight(0xffffff, 1.0)
  mainLight.position.set(3, 5, 4)
  mainLight.castShadow = true
  scene.add(mainLight)
  
  const backLight = new THREE.DirectionalLight(0xffffff, 0.6)
  backLight.position.set(-2, 1, -3)
  scene.add(backLight)
  
  const leftLight = new THREE.DirectionalLight(0xffcc88, 0.5)
  leftLight.position.set(-3, 2, 1)
  scene.add(leftLight)
  
  const rightLight = new THREE.DirectionalLight(0x88ccff, 0.5)
  rightLight.position.set(3, 2, 1)
  scene.add(rightLight)
  
  // 环绕点光源
  const positions = [[1.5, 1, 1.5], [-1.5, 1, 1.5], [1.5, 1, -1.5], [-1.5, 1, -1.5]]
  positions.forEach(pos => {
    const pointLight = new THREE.PointLight(0xd4af37, 0.3)
    pointLight.position.set(pos[0], pos[1], pos[2])
    scene.add(pointLight)
  })
  
  const bottomLight = new THREE.PointLight(0xd4af37, 0.3)
  bottomLight.position.set(0, -1.2, 0)
  scene.add(bottomLight)
}

let particles = null
const setupParticles = () => {
  const particleCount = 600
  const particlesGeo = new THREE.BufferGeometry()
  const positions = new Float32Array(particleCount * 3)
  for (let i = 0; i < particleCount; i++) {
    positions[i * 3] = (Math.random() - 0.5) * 12
    positions[i * 3 + 1] = (Math.random() - 0.5) * 5
    positions[i * 3 + 2] = (Math.random() - 0.5) * 10 - 3
  }
  particlesGeo.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  const particlesMat = new THREE.PointsMaterial({ color: 0xd4af37, size: 0.03, transparent: true, opacity: 0.4, blending: THREE.AdditiveBlending })
  particles = new THREE.Points(particlesGeo, particlesMat)
  scene.add(particles)
}

const disposeModel = (model) => {
  if (!model) return
  model.traverse((child) => {
    if (child.isMesh) {
      if (child.geometry) child.geometry.dispose()
      if (child.material) {
        if (Array.isArray(child.material)) {
          child.material.forEach(m => m.dispose())
        } else {
          child.material.dispose()
        }
      }
    }
  })
}

const resetCamera = () => {
  if (!camera || !controls) return
  camera.position.set(2, 1.5, 4)
  controls.target.set(0, 0, 0)
  controls.update()
}

const toggleAutoRotate = () => {
  autoRotate.value = !autoRotate.value
  if (controls) controls.autoRotate = autoRotate.value
}

const toggleFullscreen = () => {
  const container = canvasContainer.value
  if (!container) return
  if (!document.fullscreenElement) {
    container.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const retryLoad = () => {
  if (currentModel.value) {
    loadModel(currentModel.value)
  }
}

const onWindowResize = () => {
  if (!canvasContainer.value || !camera || !renderer) return
  const width = canvasContainer.value.clientWidth
  const height = canvasContainer.value.clientHeight
  if (width === 0 || height === 0) return
  camera.aspect = width / height
  camera.updateProjectionMatrix()
  renderer.setSize(width, height)
}

let animateFrameId = null
const startAnimation = () => {
  const animate = () => {
    animateFrameId = requestAnimationFrame(animate)
    if (controls) controls.update()
    if (particles) particles.rotation.y += 0.002
    if (renderer && scene && camera) renderer.render(scene, camera)
  }
  animate()
}

onMounted(async () => {
  await nextTick()
  await fetchModels()
  
  // 初始化展厅
  currentHallModels.value = allModels.value.filter(m => m.categoryId === currentHallId.value)
  if (currentHallModels.value.length > 0) {
    currentModel.value = currentHallModels.value[0]
  }
  
  let retryCount = 0
  const waitForSize = () => {
    const width = canvasContainer.value?.clientWidth
    const height = canvasContainer.value?.clientHeight
    if (width > 0 && height > 0) {
      if (initScene()) {
        startAnimation()
        if (currentHallModels.value.length > 0) {
          loadModel(currentHallModels.value[0])
        }
      }
    } else if (retryCount < 10) {
      retryCount++
      setTimeout(waitForSize, 100)
    } else {
      error.value = '容器尺寸异常，请刷新页面重试'
    }
  }
  waitForSize()
  window.addEventListener('resize', onWindowResize)
})

onUnmounted(() => {
  if (animateFrameId) cancelAnimationFrame(animateFrameId)
  if (currentLoadInterval) clearInterval(currentLoadInterval)
  if (currentModelObject) disposeModel(currentModelObject)
  if (renderer) renderer.dispose()
  if (controls) controls.dispose()
  window.removeEventListener('resize', onWindowResize)
})
</script>

<style scoped>
:deep(.el-icon.is-loading) { animation: rotating 1s linear infinite; }
@keyframes rotating { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
:deep(.el-progress-bar__outer) { background-color: #e5e7eb; border-radius: 10px; }
:deep(.el-progress-bar__inner) { transition: width 0.3s ease; border-radius: 10px; }
</style>