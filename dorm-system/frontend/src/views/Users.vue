<template>
  <div>
    <PageHeader title="用户管理" subtitle="系统账号与权限管理">
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增用户</el-button>
    </PageHeader>

    <el-card shadow="never">
      <el-table :data="users" v-loading="loading" stripe border>
        <el-table-column label="用户名"   prop="username"  width="130" />
        <el-table-column label="姓名"     prop="realName"  width="100" />
        <el-table-column label="角色"     width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="roleMap[row.role]?.type" size="small">{{ roleMap[row.role]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="手机号"   prop="phone"     width="130" />
        <el-table-column label="邮箱"     prop="email"     min-width="160" />
        <el-table-column label="状态"     width="90"  align="center">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'" size="small">{{ row.isActive ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160" align="center">
          <template #default="{ row }">{{ dayjs(row.createdAt).format('YYYY-MM-DD HH:mm') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :icon="Edit" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" :type="row.isActive ? 'warning' : 'success'" @click="toggleActive(row)">
              {{ row.isActive ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="440px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" show-password placeholder="默认 123456" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width:100%">
            <el-option label="系统管理员" value="ADMIN" />
            <el-option label="宿管"       value="MANAGER" />
            <el-option label="学生"       value="STUDENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
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
import { userApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Plus, Edit } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import dayjs from 'dayjs'

const users = ref([])
const loading = ref(false), saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const form = ref({})

const roleMap = {
  ADMIN:   { label: '管理员', type: 'danger'  },
  MANAGER: { label: '宿管',   type: 'warning' },
  STUDENT: { label: '学生',   type: 'primary' },
}
const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  realName: [{ required: true, message: '请输入姓名'   }],
  role:     [{ required: true, message: '请选择角色'   }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await userApi.list()
    users.value = res.data
  } finally { loading.value = false }
}
function openDialog(row = null) {
  form.value = row
    ? { id: row.id, username: row.username, realName: row.realName, role: row.role, phone: row.phone, email: row.email }
    : { role: 'STUDENT' }
  dialogVisible.value = true
}
async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.value.id) await userApi.update(form.value.id, form.value)
    else await userApi.create(form.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally { saving.value = false }
}
async function toggleActive(row) {
  await userApi.update(row.id, { ...row, isActive: !row.isActive })
  ElMessage.success(row.isActive ? '已禁用' : '已启用')
  loadData()
}
onMounted(loadData)
</script>
