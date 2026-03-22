import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  { path: '/login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard',  name: 'Dashboard',  component: () => import('@/views/Dashboard.vue'),  meta: { title: '数据看板' } },
      { path: 'buildings',  name: 'Buildings',  component: () => import('@/views/Buildings.vue'),  meta: { title: '楼栋管理', roles: ['ADMIN', 'MANAGER'] } },
      { path: 'rooms',      name: 'Rooms',      component: () => import('@/views/Rooms.vue'),      meta: { title: '房间管理', roles: ['ADMIN', 'MANAGER'] } },
      { path: 'students',   name: 'Students',   component: () => import('@/views/Students.vue'),   meta: { title: '学生管理' } },
      { path: 'repairs',    name: 'Repairs',    component: () => import('@/views/Repairs.vue'),    meta: { title: '报修管理' } },
      { path: 'visitors',   name: 'Visitors',   component: () => import('@/views/Visitors.vue'),   meta: { title: '访客管理' } },
      { path: 'fees',       name: 'Fees',       component: () => import('@/views/Fees.vue'),       meta: { title: '水电费管理' } },
      { path: 'notices',    name: 'Notices',    component: () => import('@/views/Notices.vue'),    meta: { title: '公告管理' } },
      { path: 'users',      name: 'Users',      component: () => import('@/views/Users.vue'),      meta: { title: '用户管理', roles: ['ADMIN'] } },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.isLoggedIn) return '/login'
  if (to.path === '/login' && auth.isLoggedIn) return '/'
  if (to.meta.roles && !to.meta.roles.includes(auth.user?.role)) return '/'
})

export default router
