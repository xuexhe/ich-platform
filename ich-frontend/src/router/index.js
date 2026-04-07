// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { adminCheck } from '@/api/admin'

const routes = [
  // 公共路由
  { path: '/', redirect: '/index' },
  { path: '/index', name: 'Index', component: () => import('@/views/IndexView.vue') },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue') },
  
  // 非遗相关
  { path: '/heritage/list', name: 'HeritageList', component: () => import('@/views/HeritageListView.vue') },
  { path: '/heritage/detail/:id', name: 'HeritageDetail', component: () => import('@/views/HeritageDetail.vue') },
  
  // 3D/AR/AI
  { path: '/3d', name: 'ThreeD', component: () => import('@/views/3dHallView.vue') },
  { path: '/ar', name: 'AR', component: () => import('@/views/ARView.vue') },
  { path: '/ai', name: 'AI', component: () => import('@/views/AIAvatar.vue') },
  
  // 用户相关（需要登录）
  { path: '/profile', name: 'Profile', component: () => import('@/views/ProfileView.vue'), meta: { requiresAuth: true } },
  { path: '/orders', name: 'Orders', component: () => import('@/views/OrdersView.vue'), meta: { requiresAuth: true } },
  { path: '/checkout', name: 'Checkout', component: () => import('@/views/CheckoutView.vue'), meta: { requiresAuth: true } },
  { path: '/cart', name: 'Cart', component: () => import('@/views/CartView.vue'), meta: { requiresAuth: true } },
  
  // 商城相关
  { path: '/market', name: 'Market', component: () => import('@/views/MarketView.vue') },
  { path: '/market/detail/:id', name: 'MarketDetail', component: () => import('@/views/MarketDetail.vue') },

  // 成就墙路由
  { path: '/achievement', name: 'AchievementWall', component: () => import('@/views/achievement/AchievementWall.vue'), meta: { requiresAuth: true } },
  
  // 非遗小课堂路由
  { path: '/course', name: 'CourseList', component: () => import('@/views/course/CourseListView.vue') },
  { path: '/course/detail/:id', name: 'CourseDetail', component: () => import('@/views/course/CourseDetail.vue') },
  { path: '/knowledge/detail/:id', name: 'KnowledgeDetail', component: () => import('@/views/course/KnowledgeDetail.vue') },
  { path: '/knowledge/add', name: 'AddKnowledge', component: () => import('@/views/course/AddKnowledge.vue'), meta: { requiresAuth: true } },

  // 传承人相关
  { path: '/inheritor', name: 'Inheritor', component: () => import('@/views/InheritorView.vue') },
  { path: '/inheritor/detail/:id', name: 'InheritorDetail', component: () => import('@/views/InheritorDetail.vue') },
  
  // 非遗DIY工坊路由
  { path: '/diy', name: 'DIYWorkshop', component: () => import('@/views/diy/DIYWorkshop.vue') },
  
  // 管理员路由
  { path: '/admin/login', name: 'AdminLogin', component: () => import('@/views/admin/AdminLogin.vue') },
  { 
    path: '/admin', 
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'heritage', component: () => import('@/views/admin/HeritageManage.vue') },
      { path: 'inheritor', component: () => import('@/views/admin/InheritorManage.vue') },
      { path: 'product', component: () => import('@/views/admin/ProductManage.vue') },
      { path: 'category', component: () => import('@/views/admin/CategoryManage.vue') },
      { path: 'model', component: () => import('@/views/admin/ModelManage.vue') },
      { path: 'comment', component: () => import('@/views/admin/CommentManage.vue') },
      { path: 'course', component: () => import('@/views/admin/CourseManage.vue') },
      { path: 'knowledge', component: () => import('@/views/admin/KnowledgeAudit.vue') },  // 添加这行
      { path: 'user', component: () => import('@/views/admin/UserManage.vue') },
      { path: 'quiz', component: () => import('@/views/admin/QuizManage.vue') }
    ]
  },
  
  // 404页面（必须放在最后）
  { path: '/404', name: 'NotFound', component: () => import('@/views/NotFound.vue') },
  { path: '/:pathMatch(.*)*', redirect: '/404' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 全局路由守卫
router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  const adminToken = localStorage.getItem('adminToken')
  
  // 需要登录的页面
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }
  
  // 需要管理员权限的页面
  if (to.meta.requiresAdmin) {
    if (!adminToken) {
      next('/admin/login')
      return
    }
    
    try {
      const res = await adminCheck()
      if (res.code !== 200) {
        localStorage.removeItem('adminToken')
        localStorage.removeItem('isAdmin')
        localStorage.removeItem('adminInfo')
        next('/admin/login')
        return
      }
      next()
      return
    } catch (error) {
      console.error('管理员验证失败:', error)
      localStorage.removeItem('adminToken')
      localStorage.removeItem('isAdmin')
      localStorage.removeItem('adminInfo')
      next('/admin/login')
      return
    }
  }
  
  next()
})

export default router