<template>
  <div>
    <PageHeader title="学生管理" subtitle="管理学生入住、退宿、调换宿舍">
      <el-button type="primary" :icon="Plus" @click="openDialog()" v-if="auth.isManager">新增学生</el-button>
    </PageHeader>

    <el-card shadow="never" style="margin-bottom:16px">
      <el-row :gutter="12" align="middle">
        <el-col :span="8">
          <el-input v-model="filters.keyword" placeholder="搜索姓名、学号、专业" clearable :prefix-icon="Search" @input="debounceLoad" />
        </el-col>
        <el-col :span="5">
          <el-select v-model="filters.status" placeholder="状态" clearable @change="loadData" style="width:100%">
            <el-option label="在住" value="ACTIVE" />
            <el-option label="已退宿" value="CHECKED_OUT" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button @click="resetFilters" :icon="Refresh">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never">
      <el-table :data="students" v-loading="loading" stripe border>
        <el-table-column label="学号"   prop="studentNo"  width="120" />
        <el-table-column label="姓名"   prop="realName"   width="90" />
        <el-table-column label="性别"   width="70" align="center">
          <template #default="{ row }">{{ row.gender === 'MALE' ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column label="专业"   prop="major"      min-width="120" />
        <el-table-column label="班级"   prop="className"  width="110" />
        <el-table-column label="宿舍"   width="160">
          <template #default="{ row }">
            <span v-if="row.buildingName">{{ row.buildingName }}-{{ row.roomNumber }}-{{ row.bedNumber }}号床</span>
            <span v-else style="color:#9ca3af">未分配</span>
          </template>
        </el-table-column>
        <el-table-column label="入住日期" prop="checkInDate" width="110" align="center" />
        <el-table-column label="状态"  width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status === 'ACTIVE' ? '在住' : '已退宿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }" v-if="auth.isManager">
            <el-button size="small" :icon="Edit" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" @click="openTransfer(row)" v-if="row.status === 'ACTIVE'">调宿</el-button>
            <el-button size="small" type="danger" @click="handleCheckout(row)" v-if="row.status === 'ACTIVE'">退宿</el-button>
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

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑学生' : '新增学生'" width="520px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="85px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo" :disabled="!!form.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio value="MALE">男</el-radio>
                <el-radio value="FEMALE">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专业">
              <el-input v-model="form.major" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班级">
              <el-input v-model="form.className" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分配房间">
              <el-select v-model="form.roomId" placeholder="选择房间" clearable style="width:100%">
                <el-option v-for="r in availableRooms" :key="r.id"
                  :label="`${r.buildingName}-${r.roomNumber}（${r.currentCount}/${r.capacity}）`"
                  :value="r.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="床位号">
              <el-input-number v-model="form.bedNumber" :min="1" :max="12" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入住日期">
              <el-date-picker v-model="form.checkInDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- Transfer Dialog -->
    <el-dialog v-model="transferVisible" title="调换宿舍" width="400px">
      <el-form :model="transferForm" label-width="85px">
        <el-form-item label="目标房间">
          <el-select v-model="transferForm.newRoomId" placeholder="选择新房间" style="width:100%">
            <el-option v-for="r in availableRooms" :key="r.id"
              :label="`${r.buildingName}-${r.roomNumber}（剩余${r.capacity - r.currentCount}床）`"
              :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="新床位号">
          <el-input-number v-model="transferForm.bedNumber" :min="1" :max="12" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleTransfer">确认调换</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { studentApi, roomApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Search, Refresh } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'

const auth = useAuthStore()
const students = ref([])
const availableRooms = ref([])
const loading = ref(false)
const saving  = ref(false)
const dialogVisible  = ref(false)
const transferVisible = ref(false)
const formRef = ref()
const form    = ref({})
const transferForm = ref({})
const currentStudentId = ref(null)
const filters    = ref({ keyword: '', status: 'ACTIVE' })
const pagination = ref({ page: 1, size: 20, total: 0 })

let debounceTimer = null
function debounceLoad() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(loadData, 400)
}

const rules = {
  studentNo: [{ required: true, message: '请输入学号' }],
  realName:  [{ required: true, message: '请输入姓名' }],
  gender:    [{ required: true, message: '请选择性别' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await studentApi.list({
      keyword: filters.value.keyword || null,
      status: filters.value.status,
      page: pagination.value.page,
      size: pagination.value.size,
    })
    students.value = res.data.records
    pagination.value.total = res.data.total
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = { keyword: '', status: 'ACTIVE' }
  loadData()
}

function openDialog(row = null) {
  form.value = row
    ? { id: row.id, studentNo: row.studentNo, realName: row.realName, gender: row.gender, major: row.major, className: row.className, phone: row.phone, roomId: row.roomId, bedNumber: row.bedNumber, checkInDate: row.checkInDate }
    : { gender: 'MALE' }
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.value.id) {
      await studentApi.update(form.value.id, form.value)
    } else {
      await studentApi.create(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function handleCheckout(row) {
  await ElMessageBox.confirm(`确认为「${row.realName}」办理退宿？`, '退宿确认', { type: 'warning' })
  await studentApi.checkout(row.id, {})
  ElMessage.success('退宿成功')
  loadData()
}

function openTransfer(row) {
  currentStudentId.value = row.id
  transferForm.value = { newRoomId: null, bedNumber: 1 }
  transferVisible.value = true
}

async function handleTransfer() {
  saving.value = true
  try {
    await studentApi.transfer(currentStudentId.value, transferForm.value)
    ElMessage.success('调换宿舍成功')
    transferVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const rRes = await roomApi.available()
  availableRooms.value = rRes.data
  loadData()
})
</script>
