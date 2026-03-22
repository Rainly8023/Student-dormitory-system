<!-- Visitors.vue -->
<template>
  <div>
    <PageHeader title="访客管理" subtitle="宿舍访客进出登记记录">
      <el-button type="primary" :icon="Plus" @click="openCreate">登记访客</el-button>
    </PageHeader>

    <el-card shadow="never" style="margin-bottom:16px">
      <el-row :gutter="12">
        <el-col :span="5">
          <el-select v-model="filters.status" placeholder="访问状态" clearable @change="loadData" style="width:100%">
            <el-option label="访问中" value="VISITING" />
            <el-option label="已离开" value="LEFT" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker v-model="filters.date" type="date" value-format="YYYY-MM-DD"
            placeholder="按日期筛选" clearable @change="loadData" style="width:100%" />
        </el-col>
        <el-col :span="4">
          <el-button @click="resetFilters" :icon="Refresh">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never">
      <el-table :data="visitors" v-loading="loading" stripe border>
        <el-table-column label="访客姓名"  prop="visitorName"  width="100" />
        <el-table-column label="联系电话"  prop="visitorPhone" width="130" />
        <el-table-column label="被访学生"  prop="studentName"  width="100" />
        <el-table-column label="所在宿舍"  width="150">
          <template #default="{ row }">{{ row.buildingName }}-{{ row.roomNumber }}</template>
        </el-table-column>
        <el-table-column label="访问日期"  prop="visitDate"    width="110" align="center" />
        <el-table-column label="到访/离开" width="130" align="center">
          <template #default="{ row }">{{ row.visitTime || '—' }} / {{ row.leaveTime || '—' }}</template>
        </el-table-column>
        <el-table-column label="来访目的"  prop="purpose"      min-width="120" />
        <el-table-column label="状态"      width="90"  align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type" size="small">{{ statusMap[row.status]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="markLeft(row)" v-if="row.status === 'VISITING'">登记离开</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display:flex;justify-content:flex-end;margin-top:16px">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
          :total="pagination.total" layout="total, prev, pager, next" @current-change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="createVisible" title="登记访客" width="480px">
      <el-form :model="createForm" :rules="createRules" ref="createRef" label-width="85px">
        <el-form-item label="被访学生" prop="studentId">
          <el-select v-model="createForm.studentId" placeholder="选择学生" style="width:100%" filterable>
            <el-option v-for="s in students" :key="s.id" :label="`${s.realName} (${s.studentNo})`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="访客姓名" prop="visitorName">
          <el-input v-model="createForm.visitorName" />
        </el-form-item>
        <el-form-item label="访客电话" prop="visitorPhone">
          <el-input v-model="createForm.visitorPhone" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="createForm.visitorIdCard" />
        </el-form-item>
        <el-form-item label="到访日期" prop="visitDate">
          <el-date-picker v-model="createForm.visitDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="到访时间">
          <el-input v-model="createForm.visitTime" placeholder="例：14:30" />
        </el-form-item>
        <el-form-item label="来访目的">
          <el-input v-model="createForm.purpose" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">登记</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { visitorApi, studentApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import dayjs from 'dayjs'

const visitors = ref([])
const students = ref([])
const loading = ref(false), saving = ref(false)
const createVisible = ref(false)
const createRef = ref()
const filters = ref({ status: null, date: null })
const pagination = ref({ page: 1, size: 20, total: 0 })
const createForm = ref({ visitDate: dayjs().format('YYYY-MM-DD') })

const statusMap = {
  VISITING:  { label: '访问中', type: 'success' },
  LEFT:      { label: '已离开', type: 'info'    },
  CANCELLED: { label: '已取消', type: 'danger'  },
}
const createRules = {
  studentId:   [{ required: true, message: '请选择被访学生' }],
  visitorName: [{ required: true, message: '请输入访客姓名' }],
  visitorPhone:[{ required: true, message: '请输入访客电话' }],
  visitDate:   [{ required: true, message: '请选择到访日期' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await visitorApi.list({ status: filters.value.status, date: filters.value.date, page: pagination.value.page, size: pagination.value.size })
    visitors.value = res.data.records
    pagination.value.total = res.data.total
  } finally { loading.value = false }
}
function resetFilters() { filters.value = { status: null, date: null }; loadData() }
function openCreate() { createForm.value = { visitDate: dayjs().format('YYYY-MM-DD') }; createVisible.value = true }
async function submitCreate() {
  await createRef.value.validate()
  saving.value = true
  try {
    await visitorApi.create(createForm.value)
    ElMessage.success('登记成功')
    createVisible.value = false
    loadData()
  } finally { saving.value = false }
}
async function markLeft(row) {
  await visitorApi.update(row.id, { status: 'LEFT', leaveTime: dayjs().format('HH:mm') })
  ElMessage.success('已登记离开')
  loadData()
}
onMounted(async () => {
  const sRes = await studentApi.list({ status: 'ACTIVE', page: 1, size: 500 })
  students.value = sRes.data.records
  loadData()
})
</script>
