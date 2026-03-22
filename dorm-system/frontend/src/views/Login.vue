<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand">
        <div class="brand-icon">🏠</div>
        <div>
          <div class="brand-name">DormMS</div>
          <div class="brand-sub">学生宿舍综合管理系统</div>
        </div>
      </div>
      <h1 class="hero-title">智慧宿舍<br><span class="accent">精细管理</span></h1>
      <p class="hero-desc">集楼栋管理、学生入住、报修工单、访客登记、水电费核算于一体的数字化宿舍管理平台。</p>
      <ul class="feature-list">
        <li v-for="f in features" :key="f"><span class="dot"></span>{{ f }}</li>
      </ul>
    </div>

    <div class="login-right">
      <el-card class="login-card" shadow="never">
        <h2 class="card-title">欢迎登录</h2>
        <p class="card-sub">请选择角色并输入账号密码</p>

        <div class="role-tabs">
          <button
            v-for="r in roles" :key="r.key"
            :class="['role-btn', { active: form.role === r.key }]"
            @click="selectRole(r)"
          >{{ r.icon }} {{ r.label }}</button>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" size="large"
              :prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" style="width:100%;margin-top:8px"
            @click="handleLogin">
            登 录
          </el-button>
        </el-form>

        <el-alert type="info" :closable="false" style="margin-top:20px">
          <template #title>
            <div style="font-weight:600;margin-bottom:4px">🔑 测试账号</div>
            <div style="font-size:13px;line-height:1.8;color:#555">
              管理员：admin / admin123<br>
              宿管：manager1 / manager123<br>
              学生：S2021001 / student123
            </div>
          </template>
        </el-alert>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)

const roles = [
  { key: 'admin',   label: '管理员', icon: '👤', user: 'admin' },
  { key: 'manager', label: '宿管',   icon: '🏢', user: 'manager1' },
  { key: 'student', label: '学生',   icon: '🎓', user: 'S2021001' },
]

const form = reactive({ username: 'admin', password: '', role: 'admin' })
const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }],
}

const features = [
  '楼栋与房间管理 — 可视化床位分配',
  '学生入住/退宿/调换宿舍全流程',
  '报修工单流转 — 在线提交与进度跟踪',
  '访客登记 — 实时进出记录',
  '水电费核算 — 自动计算与缴费提醒',
  '多角色权限管理 — 管理员 / 宿管 / 学生',
]

function selectRole(r) {
  form.role = r.key
  form.username = r.user
  form.password = ''
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background: #0a0f1e;
}

.login-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 80px;
  background: linear-gradient(135deg, #0a0f1e 0%, #1e2a3b 100%);
}

.brand { display: flex; align-items: center; gap: 14px; margin-bottom: 48px; }
.brand-icon {
  width: 48px; height: 48px;
  background: linear-gradient(135deg, #3b82f6, #06b6d4);
  border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px;
}
.brand-name { font-size: 18px; font-weight: 700; color: #f8fafc; }
.brand-sub  { font-size: 12px; color: #64748b; }

.hero-title {
  font-size: 52px; font-weight: 800; line-height: 1.15;
  color: #f8fafc; margin-bottom: 20px; letter-spacing: -0.02em;
}
.hero-title .accent { color: #60a5fa; }
.hero-desc  { font-size: 16px; color: #94a3b8; line-height: 1.7; max-width: 420px; margin-bottom: 40px; }
.feature-list { list-style: none; display: flex; flex-direction: column; gap: 12px; }
.feature-list li { display: flex; align-items: center; gap: 10px; font-size: 14px; color: #cbd5e1; }
.dot { width: 7px; height: 7px; border-radius: 50%; background: #06b6d4; flex-shrink: 0; }

.login-right {
  width: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #f0f2f5;
}

.login-card { width: 100%; border-radius: 16px; padding: 12px; }
.card-title { font-size: 24px; font-weight: 700; color: #1f2937; margin-bottom: 6px; }
.card-sub   { font-size: 14px; color: #6b7280; margin-bottom: 24px; }

.role-tabs { display: flex; gap: 8px; margin-bottom: 24px; }
.role-btn {
  flex: 1; padding: 10px 8px; border: 1.5px solid #e5e7eb;
  border-radius: 10px; background: white; color: #6b7280;
  font-size: 13px; cursor: pointer; transition: all 0.15s; font-weight: 500;
}
.role-btn:hover { border-color: #3b82f6; color: #3b82f6; }
.role-btn.active { border-color: #3b82f6; background: #eff6ff; color: #1d4ed8; font-weight: 600; }

@media (max-width: 800px) {
  .login-left { display: none; }
  .login-right { width: 100%; }
}
</style>
