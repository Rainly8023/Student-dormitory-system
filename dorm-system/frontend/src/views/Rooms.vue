<template>
  <div>
    <PageHeader title="房间管理" subtitle="管理宿舍房间分配与状态">
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增房间</el-button>
    </PageHeader>

    <!-- Filter Bar -->
    <el-card shadow="never" style="margin-bottom:16px">
      <el-row :gutter="12" align="middle">
        <el-col :span="6">
          <el-select v-model="filters.buildingId" placeholder="选择楼栋" clearable @change="loadData" style="width:100%">
            <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="filters.status" placeholder="房间状态" clearable @change="loadData" style="width:100%">
            <el-option label="可入住" value="AVAILABLE" />
            <el-option label="已住满" value="FULL" />
            <el-option label="维修中" value="MAINTENANCE" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button @click="resetFilters" :icon="Refresh">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- Table -->
    <el-card shadow="never">
      <el-table :data="rooms" v-loading="loading" stripe border>
        <el-table-column label="楼栋"  prop="buildingName" width="130" />
        <el-table-column label="楼层"  prop="floor"        width="70"  align="center" />
        <el-table-column label="房间号" prop="roomNumber"   width="90"  align="center" />
        <el-table-column label="类型"  prop="roomType"     width="90"  align="center" />
        <el-table-column label="床位" align="center" width="120">
          <template #default="{ row }">
            <el-progress
              :percentage="row.capacity ? Math.round(row.currentCount / row.capacity * 100) : 0"
              :stroke-width="8"
              style="width:80px;display:inline-block"
            />
            <span style="font-size:12px;color:#6b7280;margin-left:6px">{{ row.currentCount }}/{{ row.capacity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态"  width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type" size="small">{{ statusMap[row.status]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
            <el-button size="small" :icon="Edit" @click="openDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑房间' : '新增房间'" width="440px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="所属楼栋" prop="buildingId">
          <el-select v-model="form.buildingId" style="width:100%">
            <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层" prop="floor">
          <el-input-number v-model="form.floor" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="房间号" prop="roomNumber">
          <el-input v-model="form.roomNumber" placeholder="例：101" />
        </el-form-item>
        <el-form-item label="床位数" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="12" />
        </el-form-item>
        <el-form-item label="房间类型">
          <el-select v-model="form.roomType" style="width:100%">
            <el-option label="标准间（4人）" value="标准间" />
            <el-option label="六人间" value="六人间" />
            <el-option label="单人间" value="单人间" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="可入住" value="AVAILABLE" />
            <el-option label="维修中" value="MAINTENANCE" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- Detail Drawer -->
    <el-drawer v-model="drawerVisible" title="房间详情" size="420px">
      <div v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="楼栋">{{ detail.buildingName }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ detail.roomNumber }}</el-descriptions-item>
          <el-descriptions-item label="楼层">{{ detail.floor }} 层</el-descriptions-item>
          <el-descriptions-item label="床位">{{ detail.currentCount }}/{{ detail.capacity }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detail.roomType }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusMap[detail.status]?.type" size="small">{{ statusMap[detail.status]?.label }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div style="margin-top:20px;font-weight:600;margin-bottom:10px">住宿学生</div>
        <el-empty v-if="!detail.students?.length" description="暂无在住学生" :image-size="60" />
        <div v-for="s in detail.students" :key="s.id" class="student-row">
          <div class="bed-badge">{{ s.bedNumber }}号床</div>
          <div>
            <div style="font-weight:500">{{ s.realName }}</div>
            <div style="font-size:12px;color:#6b7280">{{ s.studentNo }} · {{ s.major }}</div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { roomApi, buildingApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Refresh } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'

const rooms    = ref([])
const buildings = ref([])
const loading  = ref(false)
const saving   = ref(false)
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const detail   = ref(null)
const formRef  = ref()
const form     = ref({})
const filters  = ref({ buildingId: null, status: null })
const pagination = ref({ page: 1, size: 20, total: 0 })

const statusMap = {
  AVAILABLE:   { label: '可入住', type: 'success' },
  FULL:        { label: '已住满', type: 'warning' },
  MAINTENANCE: { label: '维修中', type: 'danger'  },
  CLOSED:      { label: '已关闭', type: 'info'    },
}

const rules = {
  buildingId: [{ required: true, message: '请选择楼栋' }],
  floor:      [{ required: true, message: '请输入楼层' }],
  roomNumber: [{ required: true, message: '请输入房间号' }],
  capacity:   [{ required: true, message: '请输入床位数' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await roomApi.list({
      buildingId: filters.value.buildingId,
      status: filters.value.status,
      page: pagination.value.page,
      size: pagination.value.size,
    })
    rooms.value = res.data.records
    pagination.value.total = res.data.total
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = { buildingId: null, status: null }
  loadData()
}

function openDialog(row = null) {
  form.value = row
    ? { id: row.id, buildingId: row.buildingId, floor: row.floor, roomNumber: row.roomNumber, capacity: row.capacity, roomType: row.roomType, status: row.status }
    : { capacity: 4, roomType: '标准间', status: 'AVAILABLE' }
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.value.id) {
      await roomApi.update(form.value.id, form.value)
    } else {
      await roomApi.create(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function viewDetail(row) {
  const res = await roomApi.detail(row.id)
  detail.value = res.data
  drawerVisible.value = true
}

onMounted(async () => {
  const bRes = await buildingApi.list()
  buildings.value = bRes.data
  loadData()
})
</script>

<style scoped>
.student-row {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 0; border-bottom: 1px solid #f3f4f6;
}
.student-row:last-child { border-bottom: none; }
.bed-badge {
  background: #eff6ff; color: #1d4ed8;
  font-size: 12px; font-weight: 600;
  padding: 4px 8px; border-radius: 6px; white-space: nowrap;
}
</style>
