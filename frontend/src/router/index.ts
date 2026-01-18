import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Login',
      component: Login,
      meta: {
        title: '用户登录'
      }
    },
    {
      path: '/login',
      redirect: '/'
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/Dashboard.vue'),
      meta: {
        title: '控制台',
        requiresAuth: true
      }
    }
  ]
})

// 路由守卫：检查登录状态
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - GCT Reporter`
  }
  
  // 需要登录的页面
  if (to.meta.requiresAuth && !token) {
    next('/')
    return
  }
  
  // 已登录用户访问登录页，重定向到控制台
  if (to.path === '/' && token) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router
