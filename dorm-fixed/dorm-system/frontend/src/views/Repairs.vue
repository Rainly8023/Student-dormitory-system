<template>
  <div>
    <PageHeader title="报修管理" subtitle="宿舍设施报修工单流转">
      <el-button type="primary" :icon="Plus" @click="openCreate">提交报修</el-button>
    </PageHeader>

    <el-card shadow="never" style="margin-bottom:16px">
      <el-row :gutter="12">
        <el-col :span="5">
          <el-select v-model="filters.status" placeholder="工单状态" clearable @change="loadData" style="width:100%">
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-select v-model="filters.repairType" placeholder="报修类型" clearable @change="loadData" style="width:100%">
            <el-option v-for="t in repairTypes" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button @click="resetFilters" :icon="Refresh">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never">
      <el-table :data="repairs" v-loading="loading" stripe border>
        <el-table-column label="标题"       prop="title"          min-width="160" />
        <el-table-column label="类型"       width="100" align="center">
          <template #default="{ row }">{{ typeLabel(row.repairType) }}</template>
        </el-table-column>
        <el-table-column label="宿舍"       width="150">
          <template #default="{ row }">{{ row.buildingName }}-{{ row.roomNumber }}</template>
        </el-table-column>
        <el-table-column label="报修人"     prop="studentName" width="90" align="center">
          <template #default="{ row }">{{ row.studentName || '—' }}</template>
        </el-table-column>
        <el-table-column label="优先级"     width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="priorityMap[row.priority]?.type" size="small">{{ priorityMap[row.priority]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态"       width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type" size="small">{{ statusMap[row.status]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间"   prop="createdAt" width="160" align="center">
          <template #default="{ row }">{{ dayjs(row.createdAt).format('YYYY-MM-DD HH:mm') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
            <el-button size="small" type="primary" @click="openHandle(row)"
              v-if="auth.isManager && row.status !== 'RESOLVED' && row.status !== 'REJECTED'">
              处理
            </el-button>
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

    <!-- Create Dialog -->
    <el-dialog v-model="createVisible" title="提交报修" width="480px">
      <el-form :model="createForm" :rules="createRules" ref="createRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="createForm.title" placeholder="简要描述问题" />
        </el-form-item>
        <el-form-item label="类型" prop="repairType">
          <el-select v-model="createForm.repairType" style="width:100%">
            <el-option v-for="t in repairTypes" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="所在房间" prop="roomId">
          <el-select v-model="createForm.roomId" placeholder="选择房间" style="width:100%">
            <el-option v-for="r in allRooms" :key="r.id"
              :label="`${r.buildingName}-${r.roomNumber}`" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="createForm.priority">
            <el-radio value="LOW">低</el-radio>
            <el-radio value="NORMAL">普通</el-radio>
            <el-radio value="HIGH">高</el-radio>
            <el-radio value="URGENT">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">提交</el-button>
      </template>
    </el-dialog>

    <!-- Handle Dialog -->
    <el-dialog v-model="handleVisible" title="处理工单" width="440px">
      <el-form :model="handleForm" label-width="80px">
        <el-form-item label="处理状态">
          <el-radio-group v-model="handleForm.status">
            <el-radio value="PROCESSING">处理中</el-radio>
            <el-radio value="RESOLVED">已解决</el-radio>
            <el-radio value="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="handleForm.handlerNote" type="textarea" :rows="3" placeholder="描述处理结果或原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitHandle">确认</el-button>
      </template>
    </el-dialog>

    <!-- Detail Drawer -->
    <el-drawer v-model="detailVisible" title="报修详情" size="400px">
      <div v-if="currentDetail">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="标题">{{ currentDetail.title }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ typeLabel(currentDetail.repairType) }}</el-descriptions-item>
          <el-descriptions-item label="宿舍">{{ currentDetail.buildingName }}-{{ currentDetail.roomNumber }}</el-descriptions-item>
          <el-descriptions-item label="报修人">{{ currentDetail.studentName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="priorityMap[currentDetail.priority]?.type" size="small">{{ priorityMap[currentDetail.priority]?.label }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusMap[currentDetail.status]?.type" size="small">{{ statusMap[currentDetail.status]?.label }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="描述">{{ currentDetail.description || '—' }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ currentDetail.handlerName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="处理备注">{{ currentDetail.handlerNote || '—' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ dayjs(currentDetail.createdAt).format('YYYY-MM-DD HH:mm') }}</el-descriptions-item>
          <el-descriptions-item label="解决时间" v-if="currentDetail.resolvedAt">{{ dayjs(currentDetail.resolvedAt).format('YYYY-MM-DD HH:mm') }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { repairApi, roomApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import dayjs from 'dayjs'

const auth = useAuthStore()
const repairs = ref([])
const allRooms = ref([])
const loading = ref(false)
const saving  = ref(false)
const createVisible = ref(false)
const handleVisible = ref(false)
const detailVisible = ref(false)
const createRef = ref()
const currentDetail = ref(null)
const currentId = ref(null)
const filters    = ref({ status: null, repairType: null })
const pagination = ref({ page: 1, size: 20, total: 0 })
const createForm = ref({ priority: 'NORMAL' })
const handleForm = ref({ status: 'PROCESSING', handlerNote: '' })

const repairTypes = [
  { value: 'AIR_CONDITIONER', label: '空调' },
  { value: 'PLUMBING',        label: '水管/卫浴' },
  { value: 'ELECTRICAL',      label: '电路/电器' },
  { value: 'NETWORK',         label: '网络/宽带' },
  { value: 'DOOR',            label: '门窗锁具' },
  { value: 'FURNITURE',       label: '家具设施' },
  { value: 'OTHER',           label: '其他' },
]
const typeLabel = (v) => repairTypes.find(t => t.value === v)?.label || v

const statusMap = {
  PENDING:    { label: '待处理', type: 'warning' },
  PROCESSING: { label: '处理中', type: 'primary' },
  RESOLVED:   { label: '已解决', type: 'success' },
  REJECTED:   { label: '已驳回', type: 'danger'  },
}
const priorityMap = {
  LOW:    { label: '低',   type: 'info'    },
  NORMAL: { label: '普通', type: ''        },
  HIGH:   { label: '高',   type: 'warning' },
  URGENT: { label: '紧急', type: 'danger'  },
}

const createRules = {
  title:      [{ required: true, message: '请输入标题' }],
  repairType: [{ required: true, message: '请选择类型' }],
  roomId:     [{ required: true, message: '请选择房间' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await repairApi.list({
      status: filters.value.status,
      repairType: filters.value.repairType,
      page: pagination.value.page,
      size: pagination.value.size,
    })
    repairs.value = res.data.records
    pagination.value.total = res.data.total
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = { status: null, repairType: null }
  loadData()
}

function openCreate() {
  createForm.value = { priority: 'NORMAL' }
  createVisible.value = true
}

async function submitCreate() {
  await createRef.value.validate()
  saving.value = true
  try {
    await repairApi.create(createForm.value)
    ElMessage.success('报修提交成功')
    createVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

function openHandle(row) {
  currentId.value = row.id
  handleForm.value = { status: 'PROCESSING', handlerNote: '' }
  handleVisible.value = true
}

async function submitHandle() {
  saving.value = true
  try {
    await repairApi.update(currentId.value, handleForm.value)
    ElMessage.success('处理成功')
    handleVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

function viewDetail(row) {
  currentDetail.value = row
  detailVisible.value = true
}

onMounted(async () => {
  const rRes = await roomApi.list({ page: 1, size: 200 })
  allRooms.value = rRes.data.records
  loadData()
})
</script>
