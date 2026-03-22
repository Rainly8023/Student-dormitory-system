<template>
  <div class="app-layout">
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ collapsed }">
      <div class="sidebar-logo">
        <div class="logo-icon">🏠</div>
        <transition name="fade">
          <div v-show="!collapsed">
            <div class="logo-name">DormMS</div>
            <div class="logo-sub">宿舍管理系统</div>
          </div>
        </transition>
      </div>

      <div class="sidebar-user" v-show="!collapsed">
        <div class="user-avatar">{{ auth.user?.realName?.charAt(0) }}</div>
        <div style="overflow:hidden">
          <div class="user-name">{{ auth.user?.realName }}</div>
          <div class="user-role">{{ roleLabel }}</div>
        </div>
      </div>

      <nav class="sidebar-nav">
        <template v-for="group in navGroups" :key="group.title">
          <div class="nav-group-title" v-show="!collapsed">{{ group.title }}</div>
          <router-link
            v-for="item in group.items"
            :key="item.path"
            :to="item.path"
            custom
            v-slot="{ isActive, navigate }"
          >
            <div
              :class="['nav-item', { active: isActive }]"
              @click="navigate"
              :title="collapsed ? item.label : ''"
            >
              <el-icon :size="18"><component :is="item.icon" /></el-icon>
              <span v-show="!collapsed" class="nav-label">{{ item.label }}</span>
              <el-badge
                v-if="item.badge && item.badge() > 0 && !collapsed"
                :value="item.badge()"
                type="danger"
                style="margin-left:auto"
              />
            </div>
          </router-link>
        </template>
      </nav>

      <div class="sidebar-footer">
        <div class="nav-item" @click="toggleCollapse" :title="collapsed ? '展开' : '收起'">
          <el-icon :size="18"><component :is="collapsed ? 'Expand' : 'Fold'" /></el-icon>
          <span v-show="!collapsed">收起菜单</span>
        </div>
        <div class="nav-item logout" @click="handleLogout">
          <el-icon :size="18"><SwitchButton /></el-icon>
          <span v-show="!collapsed">退出登录</span>
        </div>
      </div>
    </aside>

    <!-- Main Area -->
    <div class="main-area">
      <header class="topbar">
        <div class="topbar-title">{{ currentTitle }}</div>
        <div class="topbar-right">
          <span class="topbar-time">{{ timeStr }}</span>
          <el-dropdown>
            <div class="topbar-user">
              <div class="avatar-sm">{{ auth.user?.realName?.charAt(0) }}</div>
              <span>{{ auth.user?.realName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="showPwd = true">修改密码</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>

    <!-- Change Password Dialog -->
    <el-dialog v-model="showPwd" title="修改密码" width="400px">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdRef" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPwd = false">取消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="submitPwd">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Odometer, OfficeBuilding, HomeFilled, UserFilled,
  Tools, Avatar, Wallet, Bell, Setting,
  SwitchButton, ArrowDown, Fold, Expand
} from '@element-plus/icons-vue'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const collapsed = ref(false)
const showPwd = ref(false)
const pwdLoading = ref(false)
const pwdRef = ref()
const timeStr = ref('')

const pwdForm = ref({ oldPassword: '', newPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码' }],
  newPassword: [{ required: true, message: '请输入新密码', min: 6 }],
}

const roleLabel = computed(() => {
  const map = { ADMIN: '系统管理员', MANAGER: '宿管', STUDENT: '学生' }
  return map[auth.user?.role] || ''
})

const currentTitle = computed(() => route.meta?.title || '宿舍管理系统')

const navGroups = computed(() => {
  const groups = [
    {
      title: '概览',
      items: [
        { path: '/dashboard', label: '数据看板', icon: 'Odometer' },
        { path: '/notices',   label: '公告管理', icon: 'Bell' },
      ],
    },
    {
      title: '宿舍管理',
      items: [],
    },
    {
      title: '事务管理',
      items: [
        { path: '/repairs',  label: '报修管理', icon: 'Tools' },
        { path: '/visitors', label: '访客管理', icon: 'Avatar' },
        { path: '/fees',     label: '水电费',   icon: 'Wallet' },
      ],
    },
  ]

  if (auth.isManager) {
    groups[1].items.push(
      { path: '/buildings', label: '楼栋管理', icon: 'OfficeBuilding' },
      { path: '/rooms',     label: '房间管理', icon: 'HomeFilled' },
      { path: '/students',  label: '学生管理', icon: 'UserFilled' },
    )
  } else {
    groups[1].items.push(
      { path: '/students', label: '我的宿舍', icon: 'HomeFilled' },
    )
  }

  if (auth.isAdmin) {
    groups.push({
      title: '系统',
      items: [{ path: '/users', label: '用户管理', icon: 'Setting' }],
    })
  }

  return groups.filter(g => g.items.length > 0)
})

function toggleCollapse() { collapsed.value = !collapsed.value }

async function handleLogout() {
  await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
  auth.logout()
  router.push('/login')
}

async function submitPwd() {
  await pwdRef.value.validate()
  pwdLoading.value = true
  try {
    await authApi.changePassword(pwdForm.value)
    ElMessage.success('密码修改成功，请重新登录')
    showPwd.value = false
    auth.logout()
    router.push('/login')
  } finally {
    pwdLoading.value = false
  }
}

let timer
onMounted(() => {
  const update = () => {
    const now = new Date()
    timeStr.value = now.toLocaleString('zh-CN', { hour12: false })
  }
  update()
  timer = setInterval(update, 1000)
})
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.app-layout { display: flex; height: 100vh; overflow: hidden; }

.sidebar {
  width: 220px;
  min-width: 220px;
  background: #1e293b;
  display: flex;
  flex-direction: column;
  transition: width 0.25s, min-width 0.25s;
  overflow: hidden;
}
.sidebar.collapsed { width: 64px; min-width: 64px; }

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 14px;
  border-bottom: 1px solid rgba(255,255,255,0.07);
  overflow: hidden;
  white-space: nowrap;
}
.logo-icon {
  width: 36px; height: 36px; flex-shrink: 0;
  background: linear-gradient(135deg, #3b82f6, #06b6d4);
  border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 17px;
}
.logo-name { font-size: 15px; font-weight: 700; color: #f8fafc; }
.logo-sub  { font-size: 11px; color: #64748b; }

.sidebar-user {
  margin: 10px;
  padding: 10px 12px;
  background: rgba(255,255,255,0.05);
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  overflow: hidden;
  white-space: nowrap;
}
.user-avatar {
  width: 32px; height: 32px; flex-shrink: 0;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 700; color: white;
}
.user-name { font-size: 13px; font-weight: 600; color: #e2e8f0; overflow: hidden; text-overflow: ellipsis; }
.user-role { font-size: 11px; color: #64748b; }

.sidebar-nav { flex: 1; padding: 6px 0; overflow-y: auto; overflow-x: hidden; }
.nav-group-title {
  padding: 10px 16px 4px;
  font-size: 10px; font-weight: 700; color: #475569;
  text-transform: uppercase; letter-spacing: 0.08em;
  white-space: nowrap;
}
.nav-item {
  display: flex; align-items: center; gap: 10px;
  padding: 9px 14px; margin: 1px 8px;
  border-radius: 9px; cursor: pointer;
  transition: all 0.15s; color: #94a3b8;
  font-size: 14px; font-weight: 500; white-space: nowrap;
  overflow: hidden;
}
.nav-item:hover { background: rgba(255,255,255,0.07); color: #e2e8f0; }
.nav-item.active { background: rgba(59,130,246,0.2); color: #93c5fd; }
.nav-item.logout:hover { background: rgba(239,68,68,0.12); color: #fca5a5; }

.sidebar-footer {
  border-top: 1px solid rgba(255,255,255,0.07);
  padding: 8px 0 10px;
}

/* MAIN */
.main-area { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.topbar {
  height: 56px; flex-shrink: 0;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  display: flex; align-items: center;
  padding: 0 24px; gap: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.topbar-title { font-size: 17px; font-weight: 700; color: #111827; }
.topbar-right { margin-left: auto; display: flex; align-items: center; gap: 16px; }
.topbar-time  { font-size: 13px; color: #9ca3af; }
.topbar-user  {
  display: flex; align-items: center; gap: 8px;
  cursor: pointer; padding: 4px 10px; border-radius: 8px;
  font-size: 14px; color: #374151;
  transition: background 0.15s;
}
.topbar-user:hover { background: #f3f4f6; }
.avatar-sm {
  width: 28px; height: 28px;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700; color: white;
}

.main-content { flex: 1; overflow-y: auto; padding: 24px; background: #f0f2f5; }

/* Transitions */
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.page-enter-active, .page-leave-active { transition: opacity 0.15s, transform 0.15s; }
.page-enter-from { opacity: 0; transform: translateY(6px); }
.page-leave-to   { opacity: 0; }
</style>
