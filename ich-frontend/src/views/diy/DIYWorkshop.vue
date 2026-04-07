<!-- src/views/diy/DIYWorkshop.vue -->
<template>
  <div class="min-h-screen bg-ich-bg">
    <Navbar />
    
    <div class="max-w-7xl mx-auto px-4 py-6">
      <div class="text-center mb-4">
        <h2 class="text-3xl font-bold text-ich-dark mb-1">🎨 非遗DIY工坊</h2>
        <p class="text-gray-500">点击非遗元素添加到画布，自由创作你的艺术作品</p>
      </div>
      
      <!-- 顶部工具栏 -->
      <div class="bg-white rounded-xl shadow-lg overflow-hidden border border-gray-200">
        <!-- 工具栏第一行 -->
        <div class="border-b border-gray-200 bg-gray-50 px-3 py-2">
          <div class="flex flex-wrap items-center gap-1">
            <!-- 文件操作 -->
            <div class="flex items-center gap-0.5 pr-2 border-r border-gray-300">
              <button @click="newCanvas" class="p-1.5 rounded hover:bg-gray-200" title="新建">
                <span class="text-sm">📄</span>
              </button>
              <button @click="saveWork" class="p-1.5 rounded hover:bg-gray-200" title="保存">
                <span class="text-sm">💾</span>
              </button>
              <button @click="exportImage" class="p-1.5 rounded hover:bg-gray-200" title="导出图片">
                <span class="text-sm">📸</span>
              </button>
            </div>
            
            <!-- 编辑操作 -->
            <div class="flex items-center gap-0.5 pr-2 border-r border-gray-300">
              <button @click="undo" class="p-1.5 rounded hover:bg-gray-200" title="撤销">
                <span class="text-sm">↩️</span>
              </button>
              <button @click="redo" class="p-1.5 rounded hover:bg-gray-200" title="重做">
                <span class="text-sm">↪️</span>
              </button>
              <button @click="clearCanvas" class="p-1.5 rounded hover:bg-gray-200" title="清空">
                <span class="text-sm">🗑️</span>
              </button>
              <button @click="deleteSelected" class="p-1.5 rounded hover:bg-gray-200" title="删除选中">
                <span class="text-sm">❌</span>
              </button>
            </div>
            
            <!-- 工具 -->
            <div class="flex items-center gap-0.5 pr-2 border-r border-gray-300">
              <button v-for="tool in tools" :key="tool.id"
                  @click="setTool(tool.id)"
                  class="p-1.5 rounded transition"
                  :class="currentTool === tool.id ? 'bg-ich-red text-white' : 'hover:bg-gray-200'"
                  :title="tool.name">
                <span class="text-sm">{{ tool.icon }}</span>
              </button>
            </div>
            
            <!-- 笔刷设置 -->
            <div class="flex items-center gap-2 pr-2 border-r border-gray-300">
              <span class="text-xs">笔刷</span>
              <input type="range" v-model="brushSize" min="1" max="30" class="w-20 h-1" style="accent-color: #C92C2C">
              <span class="text-xs w-8">{{ brushSize }}px</span>
            </div>
            
            <!-- 颜色 -->
            <div class="flex items-center gap-1 pr-2 border-r border-gray-300">
              <div class="w-6 h-6 rounded border" :style="{ backgroundColor: currentColor }"></div>
              <input type="color" v-model="currentColor" class="w-6 h-6 rounded border cursor-pointer">
              <button @click="showColorPicker = !showColorPicker" class="text-xs px-1 py-0.5 rounded hover:bg-gray-200">传统色</button>
            </div>
            
            <!-- 图层信息 -->
            <div class="ml-auto text-xs text-gray-400">
              {{ selectedObjectInfo }} | 共 {{ elements.length }} 个元素
            </div>
          </div>
        </div>
        
        <!-- 传统色面板 -->
        <div v-if="showColorPicker" class="border-b border-gray-200 bg-gray-50 px-3 py-2">
          <div class="flex flex-wrap gap-2">
            <span class="text-xs text-gray-500 mr-2">中国传统色：</span>
            <button v-for="color in traditionalColors" :key="color.name"
                @click="currentColor = color.value"
                class="w-7 h-7 rounded-full border hover:scale-110 transition group relative"
                :style="{ backgroundColor: color.value }"
                :title="color.name">
              <span class="absolute bottom-full left-1/2 -translate-x-1/2 mb-1 px-1 py-0.5 bg-gray-800 text-white text-xs rounded opacity-0 group-hover:opacity-100 whitespace-nowrap">{{ color.name }}</span>
            </button>
          </div>
        </div>
        
        <!-- 非遗素材库 -->
        <div class="border-b border-gray-200 bg-white px-3 py-2">
          <div class="flex gap-2 mb-2 border-b border-gray-100 pb-2">
            <button 
              v-for="cat in heritageCategories" 
              :key="cat.id"
              @click="activeCategory = cat.id"
              class="px-3 py-1 rounded text-sm transition"
              :class="activeCategory === cat.id ? 'bg-ich-red text-white' : 'bg-gray-100 hover:bg-gray-200'"
            >
              {{ cat.name }}
            </button>
          </div>
          <div class="grid grid-cols-6 gap-2">
            <div 
              v-for="item in currentHeritageMaterials" 
              :key="item.id"
              @click="addHeritageElement(item)"
              class="cursor-pointer rounded-lg border hover:border-ich-red hover:bg-ich-red/5 transition p-1 flex flex-col items-center">
              <img :src="item.imageUrl" class="w-12 h-12 object-contain" :alt="item.name" @error="handleImageError">
              <span class="text-xs mt-1 text-gray-700">{{ item.name }}</span>
            </div>
          </div>
        </div>
        
        <!-- 画布区域 -->
        <div class="p-4 bg-gray-100 flex justify-center overflow-auto" :style="{ minHeight: '550px' }" ref="canvasContainer">
          <canvas 
            ref="canvasRef" 
            id="diyCanvas" 
            class="shadow-lg bg-white"
            width="800" 
            height="600"
            @mousedown="handleMouseDown"
            @mousemove="handleMouseMove"
            @mouseup="handleMouseUp"
            @mouseleave="handleMouseUp"
          ></canvas>
        </div>
        
        <!-- 底部状态栏 -->
        <div class="border-t border-gray-200 bg-gray-50 px-3 py-1.5 flex justify-between items-center text-xs text-gray-500">
          <div class="flex gap-3">
            <span>🖱️ 点击选中元素 | 拖动移动 | 拖拽角点缩放旋转</span>
            <span>🗑️ 选中后按 Delete 键删除</span>
            <span>✏️ 画笔模式下可直接绘画</span>
          </div>
          <div>
            <span>画布: 800×600</span>
          </div>
        </div>
      </div>
      
      <!-- 作品集（增加批量下载功能） -->
      <div class="mt-4 bg-white rounded-xl shadow-lg border border-gray-200">
        <div class="px-4 py-2 border-b border-gray-200 bg-gray-50 rounded-t-xl flex justify-between items-center">
          <h4 class="font-medium text-ich-dark text-sm">📁 我的作品集</h4>
          <div class="flex gap-2">
            <button v-if="selectedWorks.length > 0" @click="batchDownload" class="text-xs bg-ich-red text-white px-2 py-1 rounded hover:bg-ich-red/80">
              批量下载 ({{ selectedWorks.length }})
            </button>
            <button v-if="!isBatchMode && myWorks.length > 0" @click="toggleBatchMode" class="text-xs text-ich-red hover:underline">
              批量管理
            </button>
            <button v-if="isBatchMode" @click="cancelBatchMode" class="text-xs text-gray-500 hover:underline">
              取消
            </button>
            <button @click="loadMyWorks" class="text-xs text-ich-red hover:underline">刷新</button>
          </div>
        </div>
        
        <div class="p-3">
          <!-- 批量选择提示 -->
          <div v-if="isBatchMode" class="mb-2 text-xs text-gray-500 flex items-center gap-2">
            <el-checkbox :model-value="allSelected" @change="toggleSelectAll">全选</el-checkbox>
            <span>已选择 {{ selectedWorks.length }} 个作品</span>
          </div>
          
          <div class="flex flex-wrap gap-3 max-h-48 overflow-y-auto">
            <div v-if="myWorks.length === 0" class="text-center py-2 text-gray-400 text-sm w-full">
              暂无作品，创作后自动保存
            </div>
            <div v-for="work in myWorks" :key="work.id" 
                class="relative group cursor-pointer w-20 h-20 rounded-lg border hover:border-ich-red overflow-hidden flex-shrink-0"
                @click="isBatchMode ? toggleSelectWork(work) : previewWork(work)">
              <!-- 批量选择复选框 -->
              <div v-if="isBatchMode" class="absolute top-1 left-1 z-10">
                <el-checkbox :model-value="selectedWorksIds.includes(work.id)" @click.stop @change="() => toggleSelectWork(work)" />
              </div>
              <img :src="work.image" class="w-full h-full object-cover">
              <div class="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 flex items-center justify-center gap-1 transition">
                <button @click.stop="downloadWork(work)" class="text-white text-xs bg-ich-red rounded px-1 py-0.5">下载</button>
                <button @click.stop="previewWork(work)" class="text-white text-xs bg-gray-600 rounded px-1 py-0.5">预览</button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 批量操作栏 -->
        <div v-if="selectedWorks.length > 0" class="px-4 py-2 border-t border-gray-200 bg-gray-50 rounded-b-xl flex justify-between items-center">
          <span class="text-xs text-gray-500">已选择 {{ selectedWorks.length }} 个作品</span>
          <div class="flex gap-2">
            <el-button size="small" @click="cancelBatchMode">取消</el-button>
            <el-button size="small" type="danger" @click="batchDownload">批量下载</el-button>
            <el-button size="small" type="danger" plain @click="batchDelete">批量删除</el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 作品预览弹窗（增加下载按钮） -->
    <el-dialog v-model="previewVisible" title="作品预览" width="500px">
      <div class="text-center">
        <img :src="previewWorkData?.image" class="max-w-full max-h-96 mx-auto rounded-lg shadow">
        <p class="mt-3 font-medium">{{ previewWorkData?.title }}</p>
        <p class="text-xs text-gray-500">{{ previewWorkData?.createTime }}</p>
      </div>
      <template #footer>
        <el-button size="small" @click="downloadWork(previewWorkData)">下载图片</el-button>
        <el-button size="small" @click="previewVisible = false">关闭</el-button>
        <el-button size="small" type="danger" @click="deleteWork">删除作品</el-button>
      </template>
    </el-dialog>
    
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import JSZip from 'jszip'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// ========== 画布双缓冲：元素层 + 绘画层 ==========
const canvasRef = ref(null)
let ctx = null
const CANVAS_W = 800
const CANVAS_H = 600

// 元素系统（图案、图片、文字、形状）
const elements = ref([])
let selectedEl = null

// 操作状态
let isDragging = false
let isResizing = false
let isRotating = false
let dragStart = { x:0, y:0 }
let resizeStart = { w:0, h:0, x:0, y:0, sx:0, sy:0 }
let rotateStartAngle = 0

// 绘画（画笔/橡皮）
let isDrawing = false
let drawBuffer = null

// 历史记录
let history = []
let historyIdx = -1

// UI
const currentTool = ref('select')
const currentColor = ref('#C92C2C')
const brushSize = ref(5)
const showColorPicker = ref(false)
const selectedObjectInfo = ref('未选中')
const activeCategory = ref('jianzhi')

// 作品集
const myWorks = ref([])
const previewVisible = ref(false)
const previewWorkData = ref(null)

// 批量管理
const isBatchMode = ref(false)
const selectedWorksIds = ref([])

// 工具
const tools = [
  { id: 'select', name: '选择', icon: '🖱️' },
  { id: 'brush', name: '画笔', icon: '✏️' },
  { id: 'eraser', name: '橡皮', icon: '🧽' },
  { id: 'rect', name: '矩形', icon: '⬜' },
  { id: 'circle', name: '圆形', icon: '⚪' },
  { id: 'text', name: '文字', icon: '🔤' }
]

// 传统色
const traditionalColors = [
  { name: '朱砂', value: '#C92C2C' }, { name: '黛蓝', value: '#1A365D' },
  { name: '藤黄', value: '#FFD700' }, { name: '胭脂', value: '#DC143C' },
  { name: '石绿', value: '#2E7D32' }, { name: '赭石', value: '#8B4513' },
  { name: '鸦青', value: '#424242' }, { name: '月白', value: '#E8F4F8' },
  { name: '秋香', value: '#D4AF37' }, { name: '天青', value: '#2196F3' }
]

// 分类
const heritageCategories = [
  { id: 'jianzhi', name: '剪纸' },
  { id: 'lianpu', name: '脸谱' },
  { id: 'piying', name: '皮影' },
  { id: 'qinghuaci', name: '青花瓷' },
  { id: 'wushi', name: '舞狮' },
  { id: 'suxiu', name: '苏绣' }
]

// 素材
const heritageMaterials = ref([
  { id: 'jianzhi1', name: '剪纸1', category: 'jianzhi', imageUrl: '/uploads/heritage/jianzhi1.png' },
  { id: 'jianzhi2', name: '剪纸2', category: 'jianzhi', imageUrl: '/uploads/heritage/jianzhi2.png' },
  { id: 'jianzhi3', name: '剪纸3', category: 'jianzhi', imageUrl: '/uploads/heritage/jianzhi3.png' },
  { id: 'jianzhi4', name: '剪纸4', category: 'jianzhi', imageUrl: '/uploads/heritage/jianzhi4.png' },
  { id: 'jianzhi5', name: '剪纸5', category: 'jianzhi', imageUrl: '/uploads/heritage/jianzhi5.png' },
  { id: 'lianpu1', name: '脸谱1', category: 'lianpu', imageUrl: '/uploads/heritage/lianpu1.png' },
  { id: 'lianpu2', name: '脸谱2', category: 'lianpu', imageUrl: '/uploads/heritage/lianpu2.png' },
  { id: 'lianpu3', name: '脸谱3', category: 'lianpu', imageUrl: '/uploads/heritage/lianpu3.png' },
  { id: 'piying1', name: '皮影1', category: 'piying', imageUrl: '/uploads/heritage/piying1.png' },
  { id: 'piying2', name: '皮影2', category: 'piying', imageUrl: '/uploads/heritage/piying2.png' },
  { id: 'piying3', name: '皮影3', category: 'piying', imageUrl: '/uploads/heritage/piying3.png' },
  { id: 'piying4', name: '皮影4', category: 'piying', imageUrl: '/uploads/heritage/piying4.png' },
  { id: 'piying5', name: '皮影5', category: 'piying', imageUrl: '/uploads/heritage/piying5.png' },
  { id: 'qinghuaci1', name: '青花瓷1', category: 'qinghuaci', imageUrl: '/uploads/heritage/qinghuaci1.png' },
  { id: 'qinghuaci2', name: '青花瓷2', category: 'qinghuaci', imageUrl: '/uploads/heritage/qinghuaci2.png' },
  { id: 'qinghuaci3', name: '青花瓷3', category: 'qinghuaci', imageUrl: '/uploads/heritage/qinghuaci3.png' },
  { id: 'qinghuaci4', name: '青花瓷4', category: 'qinghuaci', imageUrl: '/uploads/heritage/qinghuaci4.png' },
  { id: 'wushi1', name: '舞狮1', category: 'wushi', imageUrl: '/uploads/heritage/wushi1.png' },
  { id: 'wushi2', name: '舞狮2', category: 'wushi', imageUrl: '/uploads/heritage/wushi2.png' },
  { id: 'wushi3', name: '舞狮3', category: 'wushi', imageUrl: '/uploads/heritage/wushi3.png' },
  { id: 'suxiu1', name: '苏绣1', category: 'suxiu', imageUrl: '/uploads/heritage/suxiu1.png' },
  { id: 'suxiu2', name: '苏绣2', category: 'suxiu', imageUrl: '/uploads/heritage/suxiu2.png' },
  { id: 'suxiu3', name: '苏绣3', category: 'suxiu', imageUrl: '/uploads/heritage/suxiu3.png' },
  { id: 'suxiu4', name: '苏绣4', category: 'suxiu', imageUrl: '/uploads/heritage/suxiu4.png' },
])

const currentHeritageMaterials = computed(() => 
  heritageMaterials.value.filter(i => i.category === activeCategory.value)
)

// 批量管理计算属性
const selectedWorks = computed(() => {
  return myWorks.value.filter(w => selectedWorksIds.value.includes(w.id))
})

const allSelected = computed({
  get: () => myWorks.value.length > 0 && selectedWorksIds.value.length === myWorks.value.length,
  set: (val) => {
    if (val) {
      selectedWorksIds.value = myWorks.value.map(w => w.id)
    } else {
      selectedWorksIds.value = []
    }
  }
})

// ==============================================
// 初始化
// ==============================================
function initCanvas() {
  ctx = canvasRef.value.getContext('2d')
  drawBuffer = document.createElement('canvas')
  drawBuffer.width = CANVAS_W
  drawBuffer.height = CANVAS_H
  fullRedraw()
  saveHistory()
}

function fullRedraw() {
  ctx.fillStyle = '#FDF8F0'
  ctx.fillRect(0,0,CANVAS_W,CANVAS_H)
  ctx.drawImage(drawBuffer,0,0)
  elements.value.forEach(el => drawElement(el))
  if (selectedEl) drawSelection(selectedEl)
}

function drawElement(el) {
  if (!el.img) return
  ctx.save()
  ctx.translate(el.x + el.w/2, el.y + el.h/2)
  ctx.rotate(el.rot * Math.PI/180)
  ctx.drawImage(el.img, -el.w/2, -el.h/2, el.w, el.h)
  ctx.restore()
}

function drawSelection(el) {
  ctx.save()
  ctx.translate(el.x+el.w/2, el.y+el.h/2)
  ctx.rotate(el.rot * Math.PI/180)
  ctx.strokeStyle = '#C92C2C'
  ctx.lineWidth = 2
  ctx.setLineDash([5,5])
  ctx.strokeRect(-el.w/2,-el.h/2,el.w,el.h)
  ctx.setLineDash([])
  
  const sz = 8
  ctx.fillStyle = '#C92C2C'
  ctx.fillRect(-el.w/2-sz/2,-el.h/2-sz/2,sz,sz)
  ctx.fillRect(el.w/2-sz/2,-el.h/2-sz/2,sz,sz)
  ctx.fillRect(-el.w/2-sz/2,el.h/2-sz/2,sz,sz)
  ctx.fillRect(el.w/2-sz/2,el.h/2-sz/2,sz,sz)
  
  ctx.fillStyle = '#FFC107'
  ctx.fillRect(-sz/2, -el.h/2 - 16, sz, sz)
  ctx.restore()
}

// ==============================================
// 添加图片
// ==============================================
function addHeritageElement(item) {
  const img = new Image()
  img.crossOrigin = 'anonymous'
  img.onload = () => {
    const scale = Math.min(160 / img.width, 160 / img.height)
    const el = {
      id: Date.now(),
      img: img,
      x: CANVAS_W/2 - (img.width*scale)/2,
      y: CANVAS_H/2 - (img.height*scale)/2,
      w: img.width * scale,
      h: img.height * scale,
      rot: 0,
      type: 'image'
    }
    elements.value.push(el)
    selectElement(el)
    fullRedraw()
    saveHistory()
    ElMessage.success('已添加：' + item.name)
  }
  img.onerror = () => ElMessage.error('图片加载失败')
  img.src = item.imageUrl
}

// ==============================================
// 鼠标
// ==============================================
function getPos(e) {
  const r = canvasRef.value.getBoundingClientRect()
  return {
    x: (e.clientX - r.left) * (CANVAS_W / r.width),
    y: (e.clientY - r.top) * (CANVAS_H / r.height)
  }
}

function hitTest(x, y, el) {
  ctx.save()
  ctx.translate(el.x+el.w/2, el.y+el.h/2)
  ctx.rotate(-el.rot * Math.PI/180)
  const ok = (x >= -el.w/2 && x <= el.w/2 && y >= -el.h/2 && y <= el.h/2)
  ctx.restore()
  return ok
}

function findElement(p) {
  for (let i=elements.value.length-1; i>=0; i--) {
    const el = elements.value[i]
    const localX = p.x - (el.x+el.w/2)
    const localY = p.y - (el.y+el.h/2)
    if (hitTest(localX, localY, el)) return el
  }
  return null
}

function getHandle(p, el) {
  const cx = el.x + el.w/2
  const cy = el.y + el.h/2
  const dx = p.x - cx
  const dy = p.y - cy
  const rad = -el.rot * Math.PI/180
  const lx = dx*Math.cos(rad) - dy*Math.sin(rad)
  const ly = dx*Math.sin(rad) + dy*Math.cos(rad)
  const sz = 12
  if (Math.abs(lx - el.w/2) < sz && Math.abs(ly - el.h/2) < sz) return 'se'
  if (Math.abs(lx + el.w/2) < sz && Math.abs(ly - el.h/2) < sz) return 'sw'
  if (Math.abs(lx - el.w/2) < sz && Math.abs(ly + el.h/2) < sz) return 'ne'
  if (Math.abs(lx + el.w/2) < sz && Math.abs(ly + el.h/2) < sz) return 'nw'
  if (Math.abs(lx) < sz && Math.abs(ly + el.h/2 + 16) < sz) return 'rot'
  return null
}

function handleMouseDown(e) {
  const p = getPos(e)
  if (currentTool.value === 'select') {
    if (selectedEl) {
      const h = getHandle(p, selectedEl)
      if (h === 'rot') {
        isRotating = true
        const cx = selectedEl.x + selectedEl.w/2
        const cy = selectedEl.y + selectedEl.h/2
        rotateStartAngle = Math.atan2(p.y-cy, p.x-cx) * 180/Math.PI - selectedEl.rot
        return
      }
      if (h) {
        isResizing = true
        resizeStart = {
          w: selectedEl.w,
          h: selectedEl.h,
          sx: p.x,
          sy: p.y
        }
        return
      }
    }
    const el = findElement(p)
    if (el) {
      selectElement(el)
      isDragging = true
      dragStart = { x: p.x - el.x, y: p.y - el.y }
    } else {
      unselect()
    }
    fullRedraw()
  } else {
    isDrawing = true
    startDraw(p)
  }
}

function handleMouseMove(e) {
  const p = getPos(e)
  if (isDragging && selectedEl) {
    selectedEl.x = p.x - dragStart.x
    selectedEl.y = p.y - dragStart.y
    fullRedraw()
  } else if (isResizing && selectedEl) {
    const dx = p.x - resizeStart.sx
    const dy = p.y - resizeStart.sy
    selectedEl.w = Math.max(40, resizeStart.w + dx)
    selectedEl.h = Math.max(40, resizeStart.h + dy)
    fullRedraw()
  } else if (isRotating && selectedEl) {
    const cx = selectedEl.x + selectedEl.w/2
    const cy = selectedEl.y + selectedEl.h/2
    const now = Math.atan2(p.y-cy, p.x-cx) * 180/Math.PI
    selectedEl.rot = now - rotateStartAngle
    fullRedraw()
  } else if (isDrawing) {
    onDraw(p)
  }
}

function handleMouseUp() {
  if (isDragging || isResizing || isRotating) saveHistory()
  isDragging = isResizing = isRotating = isDrawing = false
}

// ==============================================
// 画笔
// ==============================================
function startDraw(p) {
  const dCtx = drawBuffer.getContext('2d')
  dCtx.beginPath()
  dCtx.moveTo(p.x, p.y)
  dCtx.lineCap = 'round'
  dCtx.lineWidth = brushSize.value
  dCtx.strokeStyle = currentTool.value === 'eraser' ? '#FDF8F0' : currentColor.value
}

function onDraw(p) {
  const dCtx = drawBuffer.getContext('2d')
  dCtx.lineTo(p.x, p.y)
  dCtx.stroke()
  fullRedraw()
}

// ==============================================
// 形状 + 文字
// ==============================================
function addRectangle() {
  const c = document.createElement('canvas')
  c.width=100,c.height=100
  const cx = c.getContext('2d')
  cx.fillStyle=currentColor.value
  cx.fillRect(0,0,100,100)
  addShape(c)
}

function addCircle() {
  const c = document.createElement('canvas')
  c.width=100,c.height=100
  const cx = c.getContext('2d')
  cx.fillStyle=currentColor.value
  cx.arc(50,50,50,0,Math.PI*2)
  cx.fill()
  addShape(c)
}

function addText() {
  const t = prompt('请输入文字','非遗')
  if (!t) return
  const c = document.createElement('canvas')
  c.width=240
  c.height=80
  const cx = c.getContext('2d')
  cx.font='bold 40px 宋体, Microsoft YaHei'
  cx.fillStyle=currentColor.value
  cx.textAlign='center'
  cx.textBaseline='middle'
  cx.fillText(t, 120, 40)
  addShape(c)
  setTool('select')
}

function addShape(canvas) {
  const img = new Image()
  img.src = canvas.toDataURL()
  const el = {
    id: Date.now(),
    img,
    x: CANVAS_W/2 - 50,
    y: CANVAS_H/2 - 50,
    w: 100,
    h: 100,
    rot: 0,
    type:'shape'
  }
  elements.value.push(el)
  selectElement(el)
  fullRedraw()
  saveHistory()
}

// ==============================================
// 工具切换
// ==============================================
function setTool(id) {
  currentTool.value = id
  if (id === 'text') {
    addText()
    return
  }
  if (id !== 'select') unselect()
  fullRedraw()
}

function selectElement(el) {
  selectedEl = el
  selectedObjectInfo.value = `元素 (${el.x|0},${el.y|0})`
}

function unselect() {
  selectedEl = null
  selectedObjectInfo.value = '未选中'
}

// ==============================================
// 历史 / 删除 / 清空
// ==============================================
function saveHistory() {
  const snap = JSON.parse(JSON.stringify({
    elements: elements.value.map(e => ({ ...e, img: e.img ? { src: e.img.src } : null })),
    draw: drawBuffer.toDataURL()
  }))
  history = history.slice(0, historyIdx+1)
  history.push(snap)
  historyIdx++
  if (history.length>30) history.shift()
}

function undo() {
  if (historyIdx<=0) return
  historyIdx--
  loadHistory(history[historyIdx])
}

function redo() {
  if (historyIdx>=history.length-1) return
  historyIdx++
  loadHistory(history[historyIdx])
}

function loadHistory(snap) {
  // 简化历史恢复
  fullRedraw()
}

function deleteSelected() {
  if (!selectedEl) return
  elements.value = elements.value.filter(e => e.id !== selectedEl.id)
  unselect()
  fullRedraw()
  saveHistory()
  ElMessage.success('已删除')
}

function clearCanvas() {
  elements.value = []
  unselect()
  drawBuffer.getContext('2d').clearRect(0,0,CANVAS_W,CANVAS_H)
  fullRedraw()
  saveHistory()
  ElMessage.success('清空完成')
}

function newCanvas() {
  ElMessageBox.confirm('确定要新建画布吗？当前作品将丢失。', '提示', { type: 'warning' }).then(() => {
    clearCanvas()
  }).catch(() => {})
}

// ==============================================
// 作品下载功能
// ==============================================
function downloadWork(work) {
  if (!work || !work.image) {
    ElMessage.warning('作品图片不存在')
    return
  }
  const link = document.createElement('a')
  link.download = `${work.title || 'diy_work'}.png`
  link.href = work.image
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  ElMessage.success(`已下载：${work.title}`)
}

async function batchDownload() {
  if (selectedWorks.value.length === 0) {
    ElMessage.warning('请先选择要下载的作品')
    return
  }
  
  const loading = ElMessage.loading('正在打包下载...', { duration: 0 })
  
  try {
    const zip = new JSZip()
    
    for (const work of selectedWorks.value) {
      const base64Data = work.image.split(',')[1]
      if (base64Data) {
        zip.file(`${work.title || 'untitled'}.png`, base64Data, { base64: true })
      }
    }
    
    const content = await zip.generateAsync({ type: 'blob' })
    const link = document.createElement('a')
    link.download = `非遗DIY作品集_${new Date().toLocaleDateString()}.zip`
    link.href = URL.createObjectURL(content)
    link.click()
    URL.revokeObjectURL(link.href)
    
    ElMessage.success(`已下载 ${selectedWorks.value.length} 个作品`)
    cancelBatchMode()
  } catch (error) {
    console.error('打包失败:', error)
    ElMessage.error('打包失败，请重试')
  } finally {
    loading.close()
  }
}

function batchDelete() {
  if (selectedWorks.value.length === 0) {
    ElMessage.warning('请先选择要删除的作品')
    return
  }
  
  ElMessageBox.confirm(`确定要删除选中的 ${selectedWorks.value.length} 个作品吗？`, '提示', { 
    type: 'warning',
    confirmButtonText: '确定删除',
    confirmButtonClass: 'el-button--danger'
  }).then(() => {
    const works = JSON.parse(localStorage.getItem('diy_works') || '[]')
    const remainingWorks = works.filter(w => !selectedWorksIds.value.includes(w.id))
    localStorage.setItem('diy_works', JSON.stringify(remainingWorks))
    loadMyWorks()
    cancelBatchMode()
    ElMessage.success('删除成功')
  }).catch(() => {})
}

function toggleBatchMode() {
  isBatchMode.value = true
  selectedWorksIds.value = []
}

function cancelBatchMode() {
  isBatchMode.value = false
  selectedWorksIds.value = []
}

function toggleSelectWork(work) {
  const index = selectedWorksIds.value.indexOf(work.id)
  if (index === -1) {
    selectedWorksIds.value.push(work.id)
  } else {
    selectedWorksIds.value.splice(index, 1)
  }
}

function toggleSelectAll(val) {
  if (val) {
    selectedWorksIds.value = myWorks.value.map(w => w.id)
  } else {
    selectedWorksIds.value = []
  }
}

// ==============================================
// 保存 / 导出
// ==============================================
function saveWork() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  const data = canvasRef.value.toDataURL()
  const list = JSON.parse(localStorage.getItem('diy_works')||'[]')
  list.unshift({
    id:Date.now(),
    title:'DIY作品',
    image:data,
    createTime:new Date().toLocaleString(),
    userId:userStore.userInfo.id
  })
  localStorage.setItem('diy_works',JSON.stringify(list.slice(0,30)))
  loadMyWorks()
  ElMessage.success('保存成功')
}

function exportImage() {
  const a = document.createElement('a')
  a.download='非遗DIY.png'
  a.href=canvasRef.value.toDataURL()
  a.click()
  ElMessage.success('导出成功')
}

function loadMyWorks() {
  const list = JSON.parse(localStorage.getItem('diy_works')||'[]')
  myWorks.value = list.filter(w => w.userId === userStore.userInfo?.id).slice(0, 30)
}

function previewWork(work) {
  previewWorkData.value = work
  previewVisible.value = true
}

function deleteWork() {
  if (!previewWorkData.value) return
  ElMessageBox.confirm('确定要删除这个作品吗？', '提示', { type: 'warning' }).then(() => {
    const list = JSON.parse(localStorage.getItem('diy_works')||'[]')
    localStorage.setItem('diy_works', JSON.stringify(list.filter(w=>w.id!==previewWorkData.value.id)))
    loadMyWorks()
    previewVisible.value = false
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// ==============================================
// 其他
// ==============================================
function handleImageError(e) {
  e.target.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24"%3E%3Crect fill="%23eee" x="2" y="2" width="20" height="20" rx="2"/%3E%3Ctext x="12" y="17" text-anchor="middle" font-size="6" fill="%23999"%3E图片%3C/text%3E%3C/svg%3E'
}

function handleKey(e) {
  if (e.key==='Delete') deleteSelected()
}

onMounted(() => {
  nextTick(() => {
    initCanvas()
    loadMyWorks()
    window.addEventListener('keydown', handleKey)
  })
})

onUnmounted(() => window.removeEventListener('keydown', handleKey))
</script>

<style scoped>
input[type="range"] { accent-color: #C92C2C; }
canvas { display: block; }
</style>