<template>
  <div class="dashboard">
    <!-- Stat Cards -->
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="6" v-for="card in statCards" :key="card.key">
        <div class="stat-card" :style="{ '--accent': card.color }">
          <div class="stat-icon">{{ card.icon }}</div>
          <div class="stat-info">
            <div class="stat-num">{{ stats[card.key] ?? '—' }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-bottom:20px">
      <!-- Repair status chart -->
      <el-col :span="8">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="card-title">报修状态分布</span></template>
          <div ref="repairChartRef" style="height:220px"></div>
        </el-card>
      </el-col>
      <!-- Fee status chart -->
      <el-col :span="8">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="card-title">房间状态分布</span></template>
          <div ref="roomChartRef" style="height:220px"></div>
        </el-card>
      </el-col>
      <!-- Notices -->
      <el-col :span="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span class="card-title">最新公告</span>
            <router-link to="/notices" class="card-more">查看全部</router-link>
          </template>
          <div v-if="notices.length === 0" class="empty-tip">暂无公告</div>
          <div v-for="n in notices" :key="n.id" class="notice-item">
            <el-tag :type="n.isPinned ? 'danger' : 'info'" size="small" style="margin-right:8px">
              {{ n.isPinned ? '置顶' : n.category }}
            </el-tag>
            <span class="notice-title">{{ n.title }}</span>
            <span class="notice-time">{{ dayjs(n.createdAt).format('MM-DD') }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Quick actions -->
    <el-row :gutter="16">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header><span class="card-title">快捷入口</span></template>
          <div class="quick-actions">
            <div class="quick-btn" v-for="q in quickActions" :key="q.path" @click="router.push(q.path)">
              <div class="quick-icon" :style="{ background: q.bg }">{{ q.icon }}</div>
              <div class="quick-label">{{ q.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { statsApi, noticeApi } from '@/api'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

const router = useRouter()
const stats = ref({})
const notices = ref([])
const repairChartRef = ref()
const roomChartRef = ref()

const statCards = [
  { key: 'totalStudents',  label: '在住学生', icon: '🎓', color: '#3b82f6' },
  { key: 'totalRooms',     label: '宿舍总数', icon: '🏠', color: '#8b5cf6' },
  { key: 'pendingRepairs', label: '待处理报修', icon: '🔧', color: '#f59e0b' },
  { key: 'unpaidFees',     label: '待缴费账单', icon: '💰', color: '#ef4444' },
]

const quickActions = [
  { path: '/students', label: '学生入住', icon: '👤', bg: '#eff6ff' },
  { path: '/repairs',  label: '提交报修', icon: '🔧', bg: '#fffbeb' },
  { path: '/visitors', label: '访客登记', icon: '🚪', bg: '#f0fdf4' },
  { path: '/fees',     label: '水电缴费', icon: '⚡', bg: '#fef3c7' },
  { path: '/rooms',    label: '房间管理', icon: '🏘', bg: '#f5f3ff' },
  { path: '/notices',  label: '发布公告', icon: '📢', bg: '#ecfeff' },
]

onMounted(async () => {
  const [sRes, nRes] = await Promise.all([
    statsApi.overview(),
    noticeApi.list({ page: 1, size: 5 }),
  ])
  stats.value = sRes.data
  notices.value = nRes.data.records || []

  await nextTick()
  initRepairChart()
  initRoomChart()
})

function initRepairChart() {
  const chart = echarts.init(repairChartRef.value)
  const s = stats.value
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 12 } },
    series: [{
      type: 'pie', radius: ['45%', '70%'],
      center: ['50%', '42%'],
      label: { show: false },
      data: [
        { name: '待处理', value: s.pendingRepairs || 0,   itemStyle: { color: '#f59e0b' } },
        { name: '处理中', value: s.processingRepairs || 0, itemStyle: { color: '#3b82f6' } },
      ],
    }],
  })
}

function initRoomChart() {
  const chart = echarts.init(roomChartRef.value)
  const s = stats.value
  const available = s.availableRooms || 0
  const total     = s.totalRooms || 0
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 12 } },
    series: [{
      type: 'pie', radius: ['45%', '70%'],
      center: ['50%', '42%'],
      label: { show: false },
      data: [
        { name: '可入住', value: available,          itemStyle: { color: '#22c55e' } },
        { name: '已住满', value: total - available,  itemStyle: { color: '#94a3b8' } },
      ],
    }],
  })
}
</script>

<style scoped>
.stat-card {
  background: white;
  border-radius: 14px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #e5e7eb;
  transition: box-shadow 0.2s, transform 0.2s;
}
.stat-card:hover { box-shadow: 0 4px 20px rgba(0,0,0,0.08); transform: translateY(-2px); }
.stat-icon { font-size: 32px; line-height: 1; }
.stat-num  { font-size: 28px; font-weight: 800; color: var(--accent); line-height: 1; }
.stat-label { font-size: 13px; color: #6b7280; margin-top: 4px; }

.chart-card :deep(.el-card__header) {
  display: flex; align-items: center; justify-content: space-between; padding: 14px 18px;
}
.card-title { font-size: 15px; font-weight: 600; color: #111827; }
.card-more  { font-size: 13px; color: #3b82f6; text-decoration: none; }
.card-more:hover { text-decoration: underline; }

.notice-item {
  display: flex; align-items: center; gap: 4px;
  padding: 9px 0; border-bottom: 1px solid #f3f4f6; font-size: 14px;
}
.notice-item:last-child { border-bottom: none; }
.notice-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #374151; }
.notice-time  { font-size: 12px; color: #9ca3af; flex-shrink: 0; }
.empty-tip    { text-align: center; color: #9ca3af; font-size: 14px; padding: 30px 0; }

.quick-actions { display: flex; gap: 16px; flex-wrap: wrap; }
.quick-btn {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  width: 100px; padding: 18px 12px;
  border-radius: 12px; cursor: pointer; transition: all 0.15s;
  border: 1px solid #f3f4f6;
}
.quick-btn:hover { border-color: #3b82f6; transform: translateY(-2px); box-shadow: 0 4px 12px rgba(59,130,246,0.15); }
.quick-icon  { font-size: 28px; width: 52px; height: 52px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.quick-label { font-size: 13px; color: #374151; font-weight: 500; }
</style>
