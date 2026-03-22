<template>
  <div>
    <PageHeader title="水电费管理" subtitle="宿舍水电费核算与缴费管理">
      <el-button type="primary" :icon="Plus" @click="openCreate" v-if="auth.isManager">生成账单</el-button>
    </PageHeader>

    <el-card shadow="never" style="margin-bottom:16px">
      <el-row :gutter="12">
        <el-col :span="5">
          <el-select v-model="filters.status" placeholder="缴费状态" clearable @change="loadData" style="width:100%">
            <el-option label="未缴费" value="UNPAID" />
            <el-option label="已缴费" value="PAID" />
            <el-option label="已逾期" value="OVERDUE" />
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-input v-model="filters.month" placeholder="账单月份 2024-03" clearable @input="debounceLoad" />
        </el-col>
        <el-col :span="4">
          <el-button @click="resetFilters" :icon="Refresh">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never">
      <el-table :data="fees" v-loading="loading" stripe border>
        <el-table-column label="楼栋"      prop="buildingName"  width="130" />
        <el-table-column label="房间"      prop="roomNumber"    width="80"  align="center" />
        <el-table-column label="账单月份"  prop="billingMonth"  width="110" align="center" />
        <el-table-column label="用水(吨)"  prop="waterUsage"    width="100" align="right" />
        <el-table-column label="水费(元)"  prop="waterFee"      width="100" align="right" />
        <el-table-column label="用电(度)"  prop="electricityUsage" width="100" align="right" />
        <el-table-column label="电费(元)"  prop="electricityFee"   width="100" align="right" />
        <el-table-column label="合计(元)"  width="110" align="right">
          <template #default="{ row }">
            <span style="font-weight:700;color:#ef4444">{{ row.totalFee }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态"      width="90"  align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type" size="small">{{ statusMap[row.status]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="缴费时间" prop="paidAt" width="160" align="center">
          <template #default="{ row }">{{ row.paidAt ? dayjs(row.paidAt).format('YYYY-MM-DD HH:mm') : '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="handlePay(row)"
              v-if="auth.isManager && row.status === 'UNPAID'">
              标记缴费
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
          :total="pagination.total" layout="total, prev, pager, next" @current-change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="createVisible" title="生成水电账单" width="480px">
      <el-form :model="createForm" :rules="createRules" ref="createRef" label-width="90px">
        <el-form-item label="选择房间" prop="roomId">
          <el-select v-model="createForm.roomId" placeholder="选择房间" style="width:100%" filterable>
            <el-option v-for="r in allRooms" :key="r.id"
              :label="`${r.buildingName}-${r.roomNumber}`" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="账单月份" prop="billingMonth">
          <el-input v-model="createForm.billingMonth" placeholder="格式：2024-03" />
        </el-form-item>
        <el-form-item label="用水量(吨)">
          <el-input-number v-model="createForm.waterUsage" :precision="2" :step="0.5" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="用电量(度)">
          <el-input-number v-model="createForm.electricityUsage" :precision="1" :step="10" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="其他费用">
          <el-input-number v-model="createForm.otherFee" :precision="2" :min="0" style="width:100%" />
        </el-form-item>
        <el-alert type="info" :closable="false" style="margin-top:8px">
          水费单价：2.00元/吨 · 电费单价：0.50元/度
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { feeApi, roomApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import dayjs from 'dayjs'

const auth = useAuthStore()
const fees = ref([])
const allRooms = ref([])
const loading = ref(false), saving = ref(false)
const createVisible = ref(false)
const createRef = ref()
const filters = ref({ status: null, month: '' })
const pagination = ref({ page: 1, size: 20, total: 0 })
const createForm = ref({ waterUsage: 0, electricityUsage: 0, otherFee: 0 })

const statusMap = {
  UNPAID:  { label: '待缴费', type: 'warning' },
  PAID:    { label: '已缴费', type: 'success' },
  OVERDUE: { label: '已逾期', type: 'danger'  },
}
const createRules = {
  roomId:       [{ required: true, message: '请选择房间' }],
  billingMonth: [{ required: true, message: '请输入账单月份' }],
}

let debounceTimer = null
function debounceLoad() { clearTimeout(debounceTimer); debounceTimer = setTimeout(loadData, 500) }

async function loadData() {
  loading.value = true
  try {
    const res = await feeApi.list({ status: filters.value.status, month: filters.value.month || null, page: pagination.value.page, size: pagination.value.size })
    fees.value = res.data.records
    pagination.value.total = res.data.total
  } finally { loading.value = false }
}
function resetFilters() { filters.value = { status: null, month: '' }; loadData() }
function openCreate() { createForm.value = { waterUsage: 0, electricityUsage: 0, otherFee: 0 }; createVisible.value = true }
async function submitCreate() {
  await createRef.value.validate()
  saving.value = true
  try {
    await feeApi.create(createForm.value)
    ElMessage.success('账单生成成功')
    createVisible.value = false
    loadData()
  } finally { saving.value = false }
}
async function handlePay(row) {
  await ElMessageBox.confirm(`确认标记「${row.buildingName}-${row.roomNumber} ${row.billingMonth}」账单已缴费？`, '确认缴费')
  await feeApi.pay(row.id)
  ElMessage.success('已标记缴费')
  loadData()
}
onMounted(async () => {
  const rRes = await roomApi.list({ page: 1, size: 500 })
  allRooms.value = rRes.data.records
  loadData()
})
</script>
