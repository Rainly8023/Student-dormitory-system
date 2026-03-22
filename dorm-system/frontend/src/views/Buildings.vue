<template>
  <div>
    <PageHeader title="楼栋管理" subtitle="管理宿舍楼栋基本信息">
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增楼栋</el-button>
    </PageHeader>

    <el-row :gutter="16">
      <el-col :span="6" v-for="b in buildings" :key="b.id">
        <el-card shadow="never" class="building-card">
          <div class="building-header">
            <div class="building-icon">{{ b.gender === 'MALE' ? '🏢' : '🏛' }}</div>
            <el-tag :type="b.gender === 'MALE' ? 'primary' : 'danger'" size="small">
              {{ b.gender === 'MALE' ? '男生宿舍' : b.gender === 'FEMALE' ? '女生宿舍' : '混合' }}
            </el-tag>
          </div>
          <div class="building-name">{{ b.name }}</div>
          <div class="building-meta">
            <span>{{ b.floors }} 层</span>
            <span>{{ b.roomCount }} 间</span>
            <span>{{ b.availableCount }} 间可住</span>
          </div>
          <div class="building-manager" v-if="b.managerName">
            <el-icon><Avatar /></el-icon> {{ b.managerName }}
          </div>
          <el-progress
            :percentage="b.roomCount ? Math.round((b.roomCount - b.availableCount) / b.roomCount * 100) : 0"
            :color="progressColor"
            :stroke-width="6"
            style="margin-top:12px"
          />
          <div class="building-actions">
            <el-button size="small" :icon="Edit" @click="openDialog(b)">编辑</el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(b)">删除</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑楼栋' : '新增楼栋'" width="480px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="楼栋名称" prop="name">
          <el-input v-model="form.name" placeholder="例：男生一号楼" />
        </el-form-item>
        <el-form-item label="楼栋性质" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio value="MALE">男生宿舍</el-radio>
            <el-radio value="FEMALE">女生宿舍</el-radio>
            <el-radio value="MIXED">混合</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="楼层数" prop="floors">
          <el-input-number v-model="form.floors" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="form.managerId" placeholder="选择宿管" clearable style="width:100%">
            <el-option v-for="u in managers" :key="u.id" :label="u.realName" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { buildingApi, userApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Avatar } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'

const buildings = ref([])
const managers  = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = ref({})

const rules = {
  name:   [{ required: true, message: '请输入楼栋名称' }],
  gender: [{ required: true, message: '请选择楼栋性质' }],
  floors: [{ required: true, message: '请输入楼层数' }],
}

const progressColor = [
  { color: '#22c55e', percentage: 50 },
  { color: '#f59e0b', percentage: 80 },
  { color: '#ef4444', percentage: 100 },
]

async function loadData() {
  const [bRes, uRes] = await Promise.all([buildingApi.list(), userApi.list()])
  buildings.value = bRes.data
  managers.value  = (uRes.data || []).filter(u => u.role === 'MANAGER')
}

function openDialog(row = null) {
  form.value = row
    ? { id: row.id, name: row.name, gender: row.gender, floors: row.floors, description: row.description, managerId: row.managerId }
    : { gender: 'MALE', floors: 6 }
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.value.id) {
      await buildingApi.update(form.value.id, form.value)
    } else {
      await buildingApi.create(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除「${row.name}」？`, '警告', { type: 'warning' })
  await buildingApi.delete(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.building-card { border-radius: 14px; transition: box-shadow 0.2s; }
.building-card:hover { box-shadow: 0 6px 24px rgba(0,0,0,0.1); }
.building-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.building-icon { font-size: 28px; }
.building-name { font-size: 17px; font-weight: 700; color: #111827; margin-bottom: 8px; }
.building-meta { display: flex; gap: 12px; font-size: 13px; color: #6b7280; margin-bottom: 6px; }
.building-manager { font-size: 13px; color: #6b7280; display: flex; align-items: center; gap: 4px; }
.building-actions { display: flex; gap: 8px; margin-top: 14px; }
</style>
