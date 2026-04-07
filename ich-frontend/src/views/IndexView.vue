<!-- src/views/IndexView.vue -->
<template>
  <div class="min-h-screen bg-ich-bg">
    <Navbar />
    
    <!-- ==================== 1. 首屏 Hero 区 - 动态粒子背景 ==================== -->
    <section class="relative h-screen flex items-center justify-center overflow-hidden">
      <!-- 3D粒子背景 -->
      <div ref="heroCanvas" class="absolute inset-0 z-0"></div>
      <div class="absolute inset-0 bg-gradient-to-b from-ich-dark/70 via-ich-dark/50 to-ich-bg/90 z-10"></div>
      
      <div class="relative z-20 text-center text-white px-4 animate-fade-in-up">
        <!-- 装饰性书法字 -->
        <div class="mb-6">
          <span class="inline-block px-4 py-1 rounded-full bg-white/20 backdrop-blur text-sm tracking-wider">
            国家级非物质文化遗产
          </span>
        </div>
        
        <!-- 主标题 - 书法风格 -->
        <h1 class="text-5xl md:text-7xl lg:text-8xl font-bold mb-4">
          <span class="text-ich-gold">非遗</span>
          <span class="text-white">云境</span>
        </h1>
        
        <!-- 副标题 -->
        <p class="text-lg md:text-xl mb-8 max-w-2xl mx-auto opacity-90 leading-relaxed">
          以数字技术赋能非遗传承<br>
          让千年文化在云端永生
        </p>
        
        <!-- 装饰性分隔线 -->
        <div class="flex justify-center gap-3 mb-8">
          <div class="w-12 h-px bg-ich-gold"></div>
          <div class="w-2 h-2 rounded-full bg-ich-gold"></div>
          <div class="w-12 h-px bg-ich-gold"></div>
        </div>
        
        <!-- 统计数据 -->
        <div class="flex justify-center gap-8 md:gap-16">
          <div class="text-center">
            <div class="text-2xl md:text-3xl font-bold text-ich-gold">8+</div>
            <div class="text-xs md:text-sm opacity-80">非遗项目</div>
          </div>
          <div class="text-center">
            <div class="text-2xl md:text-3xl font-bold text-ich-gold">5+</div>
            <div class="text-xs md:text-sm opacity-80">传承人</div>
          </div>
          <div class="text-center">
            <div class="text-2xl md:text-3xl font-bold text-ich-gold">1000+</div>
            <div class="text-xs md:text-sm opacity-80">用户</div>
          </div>
        </div>
        
        <!-- 滚动提示 -->
        <div class="absolute bottom-8 left-1/2 -translate-x-1/2 animate-bounce">
          <el-icon :size="24" color="white"><ArrowDown /></el-icon>
        </div>
      </div>
    </section>
    
    <!-- ==================== 2. 分类导航区 ==================== -->
    <section class="py-16 px-4 max-w-7xl mx-auto">
      <div class="text-center mb-12">
        <h2 class="text-3xl md:text-4xl font-bold text-ich-dark mb-2">非遗分类</h2>
        <p class="text-gray-500">探索四大非遗门类，感受文化魅力</p>
      </div>
      
      <div class="grid grid-cols-2 md:grid-cols-4 gap-6">
        <div 
          v-for="item in categories" 
          :key="item.name" 
          class="group cursor-pointer text-center p-6 rounded-xl bg-white shadow-lg hover:shadow-xl transition-all hover:-translate-y-2"
          @click="$router.push(`/heritage/list?category=${item.name}`)"
        >
          <div class="text-6xl mb-4 transform group-hover:scale-110 transition-transform">
            {{ item.icon }}
          </div>
          <h3 class="text-lg font-bold text-ich-dark group-hover:text-ich-red">{{ item.name }}</h3>
          <p class="text-sm text-gray-500 mt-1">{{ item.count }}个项目</p>
          <div class="w-8 h-px bg-ich-gold mx-auto mt-3 opacity-0 group-hover:opacity-100 transition-opacity"></div>
        </div>
      </div>
    </section>
    
    <!-- ==================== 3. 3D展厅入口 - 沉浸式预览（真实3D模型） ==================== -->
    <section class="py-20 px-4 bg-gradient-to-r from-ich-dark to-ich-dark/90 text-white">
      <div class="max-w-7xl mx-auto">
        <div class="flex flex-col lg:flex-row items-center gap-12">
          <!-- 左侧：3D模型展示 -->
          <div class="lg:w-1/2">
            <div class="relative group">
              <div class="absolute -inset-1 bg-gradient-to-r from-ich-gold to-ich-red rounded-2xl blur opacity-30 group-hover:opacity-50 transition"></div>
              <div class="relative bg-white/10 backdrop-blur rounded-2xl p-3">
                <div ref="previewCanvas" class="w-full h-[400px] rounded-lg overflow-hidden bg-ich-dark/30"></div>
                <div class="absolute bottom-4 right-4 bg-black/50 rounded-full px-3 py-1 text-xs">
                  {{ previewModelName }}
                </div>
              </div>
            </div>
          </div>
          
          <!-- 右侧：文字介绍 -->
          <div class="lg:w-1/2 text-center lg:text-left">
            <div class="inline-block px-3 py-1 rounded-full bg-ich-gold/20 text-ich-gold text-sm mb-4">
              🎨 沉浸式体验
            </div>
            <h2 class="text-3xl md:text-4xl font-bold mb-4">3D数字展厅</h2>
            <p class="text-white/80 mb-6 leading-relaxed">
              沉浸式欣赏非遗文物，360°无死角观察细节。<br>
              剪纸、青花瓷、皮影戏... 每一件文物都栩栩如生。
            </p>
            <div class="flex gap-4 justify-center lg:justify-start">
              <el-button type="danger" size="large" round @click="$router.push('/3d')">
                进入展厅 <el-icon class="ml-1"><Right /></el-icon>
              </el-button>
              <el-button plain size="large" round @click="$router.push('/3d')">
                了解更多
              </el-button>
            </div>
            
            <!-- 模型切换按钮 -->
            <div class="flex justify-center lg:justify-start gap-3 mt-8 flex-wrap">
              <button 
                v-for="model in previewModels" 
                :key="model.id"
                @click="switchPreviewModel(model)"
                class="px-4 py-2 rounded-full text-sm transition-all"
                :class="currentPreviewModelId === model.id 
                  ? 'bg-ich-red text-white' 
                  : 'bg-white/20 text-white hover:bg-white/30'"
              >
                {{ model.icon }} {{ model.name }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>
    
    <!-- ==================== 4. 热门非遗推荐 ==================== -->
    <section class="py-16 px-4 max-w-7xl mx-auto">
      <div class="text-center mb-12">
        <h2 class="text-3xl md:text-4xl font-bold text-ich-dark mb-2">热门推荐</h2>
        <p class="text-gray-500">探索最受欢迎的非遗项目</p>
      </div>
      
      <div v-if="loading" class="flex justify-center py-20">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      </div>
      
      <div v-else-if="hotHeritage.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        <div
          v-for="item in hotHeritage"
          :key="item.id"
          class="group bg-white rounded-2xl shadow-lg overflow-hidden cursor-pointer transition-all duration-500 hover:shadow-2xl hover:-translate-y-2"
          @click="$router.push(`/heritage/detail/${item.id}`)"
        >
          <div class="relative overflow-hidden h-56">
            <img 
              :src="item.cover" 
              :alt="item.name" 
              class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
              @error="handleImageError"
            >
            <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
            <div class="absolute top-4 left-4">
              <span class="px-3 py-1 rounded-full bg-ich-red/90 text-white text-xs">
                {{ item.category }}
              </span>
            </div>
          </div>
          <div class="p-5">
            <h3 class="text-xl font-bold text-ich-dark mb-2 line-clamp-1">{{ item.name }}</h3>
            <p class="text-gray-600 line-clamp-2 mb-3">{{ item.intro }}</p>
            <div class="flex justify-between items-center text-sm text-gray-400">
              <div class="flex items-center gap-3">
                <span><el-icon><View /></el-icon> {{ item.viewCount || 0 }}</span>
                <span><el-icon><Star /></el-icon> {{ item.collectCount || 0 }}</span>
              </div>
              <span class="text-ich-red group-hover:translate-x-1 transition-transform">查看详情 →</span>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="text-center py-20 text-gray-500">
        <el-icon :size="48"><Document /></el-icon>
        <p class="mt-2">暂无非遗项目数据</p>
      </div>
    </section>
    
    <!-- ==================== 5. 传承人故事 ==================== -->
    <section class="py-16 px-4 bg-ich-paper">
      <div class="max-w-7xl mx-auto">
        <div class="text-center mb-12">
          <h2 class="text-3xl md:text-4xl font-bold text-ich-dark mb-2">匠心传承人</h2>
          <p class="text-gray-500">致敬非遗守护者</p>
        </div>
        
        <div v-if="inheritorsLoading" class="flex justify-center py-20">
          <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        </div>
        
        <div v-else-if="inheritors.length > 0" class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div 
            v-for="item in inheritors" 
            :key="item.id" 
            class="group bg-white rounded-2xl shadow-lg overflow-hidden cursor-pointer transition-all duration-500 hover:shadow-2xl hover:-translate-y-2"
            @click="$router.push(`/inheritor/detail/${item.id}`)"
          >
            <div class="relative">
              <img 
                :src="item.avatar" 
                :alt="item.name" 
                class="w-full h-72 object-cover transition-transform duration-700 group-hover:scale-105"
                @error="handleAvatarError"
              >
              <div class="absolute inset-0 bg-gradient-to-t from-ich-dark via-transparent to-transparent"></div>
              <div class="absolute bottom-4 left-4 right-4">
                <h3 class="text-xl font-bold text-white">{{ item.name }}</h3>
                <p class="text-ich-gold text-sm">{{ item.heritageName || '非遗传承人' }}</p>
              </div>
            </div>
            <div class="p-5">
              <p class="text-gray-600 text-sm line-clamp-3">{{ item.intro }}</p>
              <div class="mt-4 flex justify-end">
                <span class="text-ich-red text-sm group-hover:translate-x-1 transition-transform flex items-center gap-1">
                  了解故事 <el-icon><Right /></el-icon>
                </span>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else class="text-center py-20 text-gray-500">
          <el-icon :size="48"><User /></el-icon>
          <p class="mt-2">暂无传承人数据</p>
        </div>
      </div>
    </section>
    
    <!-- ==================== 6. AI讲解员入口 ==================== -->
    <section class="py-20 px-4">
      <div class="max-w-7xl mx-auto">
        <div class="bg-gradient-to-r from-ich-dark to-ich-red/80 rounded-3xl overflow-hidden shadow-2xl">
          <div class="flex flex-col md:flex-row items-center">
            <div class="flex-1 p-8 md:p-12 text-white">
              <div class="flex items-center gap-2 mb-4">
                <span class="text-3xl">🤖</span>
                <span class="px-3 py-1 rounded-full bg-white/20 text-sm">AI 智能讲解</span>
              </div>
              <h2 class="text-3xl md:text-4xl font-bold mb-4">非遗数字讲解员</h2>
              <p class="text-white/80 mb-6 leading-relaxed">
                基于通义千问大模型，有问必答，为你详解非遗文化。<br>
                剪纸、苏绣、皮影戏... 任何问题都可以问我。
              </p>
              <div class="flex flex-wrap gap-3">
                <div class="flex items-center gap-2 bg-white/20 rounded-full px-4 py-2">
                  <span>✂️</span> <span class="text-sm">剪纸怎么做？</span>
                </div>
                <div class="flex items-center gap-2 bg-white/20 rounded-full px-4 py-2">
                  <span>🎭</span> <span class="text-sm">皮影戏的历史</span>
                </div>
                <div class="flex items-center gap-2 bg-white/20 rounded-full px-4 py-2">
                  <span>🏺</span> <span class="text-sm">青花瓷鉴赏</span>
                </div>
              </div>
              <el-button type="danger" size="large" round class="mt-8" @click="$router.push('/ai')">
                开始对话 <el-icon class="ml-1"><ChatDotRound /></el-icon>
              </el-button>
            </div>
            <div class="flex-1 flex justify-center p-8">
              <div class="relative">
                <div class="w-48 h-48 md:w-64 md:h-64 rounded-full bg-white/10 backdrop-blur flex items-center justify-center">
                  <span class="text-8xl">🎭</span>
                </div>
                <div class="absolute -top-4 -right-4 w-16 h-16 rounded-full bg-ich-gold/30 animate-pulse"></div>
                <div class="absolute -bottom-4 -left-4 w-12 h-12 rounded-full bg-ich-red/30 animate-pulse delay-300"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
    
    <!-- ==================== 7. 底部合作伙伴 ==================== -->
    <section class="py-12 px-4 border-t border-gray-200">
      <div class="max-w-7xl mx-auto">
        <div class="text-center mb-8">
          <h3 class="text-lg font-bold text-ich-dark">合作机构</h3>
        </div>
        <div class="flex flex-wrap justify-center gap-8 md:gap-16 opacity-60">
          <span class="text-gray-400 text-sm">中国非物质文化遗产网</span>
          <span class="text-gray-400 text-sm">国家文物局</span>
          <span class="text-gray-400 text-sm">中国民间文艺家协会</span>
          <span class="text-gray-400 text-sm">传统文化数字博物馆</span>
        </div>
      </div>
    </section>
    
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as THREE from 'three'
import { GLTFLoader } from 'three/addons/loaders/GLTFLoader.js'
import { OrbitControls } from 'three/addons/controls/OrbitControls.js'
import { ArrowDown, View, Star, Document, Loading, User, Right, ChatDotRound } from '@element-plus/icons-vue'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import { getHeritageList, getHotHeritage, getCategories } from '@/api/heritage'
import { getInheritorList } from '@/api/inheritor'
import { getModelList } from '@/api/model'

// 3D相关变量
const heroCanvas = ref(null)
const previewCanvas = ref(null)
let scene, camera, renderer, particles
let previewScene, previewCamera, previewRenderer, previewModel, previewControls
let previewAnimationId = null

// 数据变量
const loading = ref(false)
const inheritorsLoading = ref(false)
const categories = ref([
  { name: '传统技艺', icon: '🏺', count: 0 },
  { name: '传统戏剧', icon: '🎭', count: 0 },
  { name: '传统美术', icon: '🎨', count: 0 },
  { name: '民俗', icon: '🏮', count: 0 }
])
const hotHeritage = ref([])
const inheritors = ref([])

// 预览模型相关
const previewModels = ref([])
const currentPreviewModelId = ref('')
const previewModelName = ref('加载中...')
const loader = new GLTFLoader()

// 获取3D模型列表
const fetchPreviewModels = async () => {
  try {
    const res = await getModelList()
    if (res.code === 200 && res.data && res.data.length > 0) {
      previewModels.value = res.data.slice(0, 3).map(m => ({
        id: m.id,
        name: m.name,
        icon: getModelIcon(m.category),
        modelUrl: m.modelUrl
      }))
      if (previewModels.value.length > 0) {
        currentPreviewModelId.value = previewModels.value[0].id
        previewModelName.value = previewModels.value[0].name
        loadPreviewModel(previewModels.value[0].modelUrl)
      }
    } else {
      previewModelName.value = '暂无模型'
    }
  } catch (error) {
    console.error('获取模型列表失败:', error)
  }
}

const getModelIcon = (category) => {
  const icons = { '瓷器': '🏺', '青铜器': '🏛️', '玉器': '💎', '剪纸': '✂️', '皮影': '🎭', '苏绣': '🪡' }
  return icons[category] || '🏺'
}

// 加载预览模型
const loadPreviewModel = (modelUrl) => {
  if (!previewScene) return
  
  if (previewModel) {
    previewScene.remove(previewModel)
    if (previewModel.geometry) previewModel.geometry.dispose()
    if (previewModel.material) previewModel.material.dispose()
  }
  
  loader.load(modelUrl, (gltf) => {
    const model = gltf.scene
    
    const box = new THREE.Box3().setFromObject(model)
    const size = box.getSize(new THREE.Vector3())
    const maxDim = Math.max(size.x, size.y, size.z)
    const scale = 1.2 / maxDim
    model.scale.set(scale, scale, scale)
    
    const newBox = new THREE.Box3().setFromObject(model)
    const newCenter = newBox.getCenter(new THREE.Vector3())
    model.position.set(-newCenter.x, -newCenter.y, -newCenter.z)
    
    model.traverse((child) => {
      if (child.isMesh && child.material) {
        if (Array.isArray(child.material)) {
          child.material.forEach(mat => mat.side = THREE.DoubleSide)
        } else {
          child.material.side = THREE.DoubleSide
        }
      }
    })
    
    previewModel = model
    previewScene.add(previewModel)
    previewModelName.value = previewModels.value.find(m => m.modelUrl === modelUrl)?.name || '3D文物'
  }, undefined, (error) => {
    console.error('模型加载失败:', error)
    previewModelName.value = '模型加载失败'
  })
}

// 切换预览模型
const switchPreviewModel = (model) => {
  currentPreviewModelId.value = model.id
  previewModelName.value = model.name
  loadPreviewModel(model.modelUrl)
}

// 初始化首页3D粒子背景
const initHeroParticles = () => {
  if (!heroCanvas.value) return
  
  const container = heroCanvas.value
  const width = container.clientWidth
  const height = container.clientHeight
  
  scene = new THREE.Scene()
  camera = new THREE.PerspectiveCamera(75, width / height, 0.1, 1000)
  camera.position.z = 25
  
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setSize(width, height)
  renderer.setClearColor(0x000000, 0)
  container.appendChild(renderer.domElement)
  
  const particlesCount = 2000
  const particlesGeometry = new THREE.BufferGeometry()
  const posArray = new Float32Array(particlesCount * 3)
  const colorArray = new Float32Array(particlesCount * 3)
  
  for (let i = 0; i < particlesCount; i++) {
    const radius = 15 + Math.random() * 10
    const theta = Math.random() * Math.PI * 2
    const phi = Math.acos(2 * Math.random() - 1)
    posArray[i * 3] = radius * Math.sin(phi) * Math.cos(theta)
    posArray[i * 3 + 1] = radius * Math.sin(phi) * Math.sin(theta) * 0.5
    posArray[i * 3 + 2] = radius * Math.cos(phi)
    
    const isGold = Math.random() > 0.7
    colorArray[i * 3] = isGold ? 0.83 : 0.79
    colorArray[i * 3 + 1] = isGold ? 0.69 : 0.29
    colorArray[i * 3 + 2] = isGold ? 0.22 : 0.29
  }
  
  particlesGeometry.setAttribute('position', new THREE.BufferAttribute(posArray, 3))
  particlesGeometry.setAttribute('color', new THREE.BufferAttribute(colorArray, 3))
  
  const particlesMaterial = new THREE.PointsMaterial({
    size: 0.08,
    vertexColors: true,
    transparent: true,
    opacity: 0.6,
    blending: THREE.AdditiveBlending
  })
  
  particles = new THREE.Points(particlesGeometry, particlesMaterial)
  scene.add(particles)
  
  const ringParticlesCount = 500
  const ringGeometry = new THREE.BufferGeometry()
  const ringPositions = new Float32Array(ringParticlesCount * 3)
  for (let i = 0; i < ringParticlesCount; i++) {
    const angle = (i / ringParticlesCount) * Math.PI * 2
    const radius = 12
    ringPositions[i * 3] = Math.cos(angle) * radius
    ringPositions[i * 3 + 1] = Math.sin(angle) * radius * 0.3
    ringPositions[i * 3 + 2] = Math.sin(angle) * radius * 0.5
  }
  ringGeometry.setAttribute('position', new THREE.BufferAttribute(ringPositions, 3))
  const ringMaterial = new THREE.PointsMaterial({ color: 0xd4af37, size: 0.05, transparent: true, opacity: 0.4 })
  const ringParticles = new THREE.Points(ringGeometry, ringMaterial)
  scene.add(ringParticles)
  
  let time = 0
  const animate = () => {
    requestAnimationFrame(animate)
    time += 0.005
    
    if (particles) {
      particles.rotation.y = time * 0.2
      particles.rotation.x = Math.sin(time * 0.1) * 0.1
    }
    if (ringParticles) {
      ringParticles.rotation.y = time * 0.3
      ringParticles.rotation.x = time * 0.2
    }
    if (renderer && scene && camera) {
      renderer.render(scene, camera)
    }
  }
  animate()
  
  window.addEventListener('resize', onHeroResize)
}

// 初始化预览3D场景
const initPreviewScene = () => {
  if (!previewCanvas.value) return
  
  const container = previewCanvas.value
  const width = container.clientWidth
  const height = container.clientHeight
  
  previewScene = new THREE.Scene()
  previewScene.background = new THREE.Color(0x1a1a2e)
  
  previewCamera = new THREE.PerspectiveCamera(45, width / height, 0.1, 1000)
  previewCamera.position.set(2, 1.5, 3)
  
  previewRenderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  previewRenderer.setSize(width, height)
  previewRenderer.setClearColor(0x1a1a2e, 1)
  container.appendChild(previewRenderer.domElement)
  
  previewControls = new OrbitControls(previewCamera, previewRenderer.domElement)
  previewControls.enableDamping = true
  previewControls.dampingFactor = 0.05
  previewControls.autoRotate = true
  previewControls.autoRotateSpeed = 1.5
  previewControls.enableZoom = true
  previewControls.enablePan = false
  previewControls.target.set(0, 0, 0)
  
  const ambientLight = new THREE.AmbientLight(0x404060, 0.6)
  previewScene.add(ambientLight)
  
  const mainLight = new THREE.DirectionalLight(0xffffff, 1)
  mainLight.position.set(2, 3, 4)
  previewScene.add(mainLight)
  
  const fillLight = new THREE.PointLight(0xd4af37, 0.5)
  fillLight.position.set(1, 2, 2)
  previewScene.add(fillLight)
  
  const backLight = new THREE.PointLight(0x88aaff, 0.3)
  backLight.position.set(-1, 1, -2)
  previewScene.add(backLight)
  
  const gridHelper = new THREE.GridHelper(5, 20, 0xd4af37, 0x88aaff)
  gridHelper.position.y = -0.8
  gridHelper.material.transparent = true
  gridHelper.material.opacity = 0.2
  previewScene.add(gridHelper)
  
  const animate = () => {
    previewAnimationId = requestAnimationFrame(animate)
    if (previewControls) previewControls.update()
    if (previewRenderer && previewScene && previewCamera) {
      previewRenderer.render(previewScene, previewCamera)
    }
  }
  animate()
}

const onHeroResize = () => {
  if (!heroCanvas.value) return
  const width = heroCanvas.value.clientWidth
  const height = heroCanvas.value.clientHeight
  if (camera) {
    camera.aspect = width / height
    camera.updateProjectionMatrix()
  }
  if (renderer) {
    renderer.setSize(width, height)
  }
}

const onPreviewResize = () => {
  if (!previewCanvas.value) return
  const width = previewCanvas.value.clientWidth
  const height = previewCanvas.value.clientHeight
  if (previewCamera) {
    previewCamera.aspect = width / height
    previewCamera.updateProjectionMatrix()
  }
  if (previewRenderer) {
    previewRenderer.setSize(width, height)
  }
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const hotRes = await getHotHeritage(6)
    if (hotRes.code === 200) {
      hotHeritage.value = hotRes.data || []
    }
  } catch (error) {
    console.error('获取热门非遗失败:', error)
    hotHeritage.value = []
  } finally {
    loading.value = false
  }
  
  inheritorsLoading.value = true
  try {
    const inheritorRes = await getInheritorList()
    if (inheritorRes.code === 200) {
      inheritors.value = (inheritorRes.data || []).slice(0, 3)
    }
  } catch (error) {
    console.error('获取传承人失败:', error)
    inheritors.value = []
  } finally {
    inheritorsLoading.value = false
  }
  
  try {
    const listRes = await getHeritageList({ pageSize: 100 })
    if (listRes.code === 200) {
      const items = listRes.data?.records || []
      categories.value.forEach(cat => {
        cat.count = items.filter(item => item.category === cat.name).length
      })
    }
  } catch (error) {
    console.error('获取分类数量失败:', error)
  }
}

const handleImageError = (e) => {
  e.target.src = 'https://picsum.photos/id/1/400/300'
}

const handleAvatarError = (e) => {
  e.target.src = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'
}

onMounted(() => {
  initHeroParticles()
  initPreviewScene()
  fetchPreviewModels()
  fetchData()
  
  window.addEventListener('resize', onPreviewResize)
})

onUnmounted(() => {
  if (previewAnimationId) {
    cancelAnimationFrame(previewAnimationId)
  }
  if (renderer) {
    renderer.dispose()
  }
  if (previewRenderer) {
    previewRenderer.dispose()
  }
  if (particles) {
    particles.geometry.dispose()
    particles.material.dispose()
  }
  window.removeEventListener('resize', onHeroResize)
  window.removeEventListener('resize', onPreviewResize)
})
</script>

<style scoped>
@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in-up { animation: fade-in-up 1s ease-out; }
.animate-bounce { animation: bounce 2s infinite; }
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
.animate-pulse { animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite; }
@keyframes pulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.6; }
}
.delay-300 { animation-delay: 300ms; }
@media (max-width: 768px) {
  .text-5xl { font-size: 2.5rem; }
}
:deep(.is-loading) { animation: rotating 2s linear infinite; }
@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>