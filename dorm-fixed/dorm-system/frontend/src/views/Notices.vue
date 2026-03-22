<template>
  <div>
    <PageHeader title="公告管理" subtitle="发布宿舍通知与公告">
      <el-button type="primary" :icon="Plus" @click="openDialog()" v-if="auth.isManager">发布公告</el-button>
    </PageHeader>

    <el-row :gutter="16">
      <el-col :span="16">
        <el-card shadow="never">
          <el-table :data="notices" v-loading="loading" stripe>
            <el-table-column label="" width="60" align="center">
              <template #default="{ row }">
                <el-icon v-if="row.isPinned" color="#ef4444"><Top /></el-icon>
              </template>
            </el-table-column>
            <el-table-column label="标题" prop="title" min-width="180">
              <template #default="{ row }">
                <span @click="viewNotice(row)" style="cursor:pointer;color:#3b82f6">{{ row.title }}</span>
              </template>
            </el-table-column>
            <el-table-column label="分类" width="90" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="row.category === 'FEE' ? 'warning' : 'primary'">{{ row.category }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="作者" prop="authorName" width="90" align="center" />
            <el-table-column label="时间" width="110" align="center">
              <template #default="{ row }">{{ dayjs(row.createdAt).format('MM-DD HH:mm') }}</template>
            </el-table-column>
            <el-table-column label="操作" width="130" align="center" v-if="auth.isManager">
              <template #default="{ row }">
                <el-button size="small" :icon="Edit" @click="openDialog(row)">编辑</el-button>
                <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div style="display:flex;justify-content:flex-end;margin-top:16px">
            <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
              :total="pagination.total" layout="total, prev, pager, next" @current-change="loadData" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" v-if="currentNotice">
          <template #header>
            <span style="font-weight:600">{{ currentNotice.title }}</span>
          </template>
          <div style="font-size:13px;color:#6b7280;margin-bottom:12px">
            {{ currentNotice.authorName }} · {{ dayjs(currentNotice.createdAt).format('YYYY-MM-DD HH:mm') }}
          </div>
          <div style="font-size:14px;line-height:1.8;white-space:pre-wrap;color:#374151">{{ currentNotice.content }}</div>
        </el-card>
        <el-empty v-else description="点击标题查看详情" :image-size="80" />
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '发布公告'" width="540px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="公告标题" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width:100%">
            <el-option label="通知" value="NOTICE" />
            <el-option label="缴费" value="FEE" />
            <el-option label="活动" value="ACTIVITY" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="公告内容..." />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.isPinned" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { noticeApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Top } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import dayjs from 'dayjs'

const auth = useAuthStore()
const notices = ref([])
const loading = ref(false), saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const form = ref({})
const currentNotice = ref(null)
const pagination = ref({ page: 1, size: 10, total: 0 })

const rules = {
  title:   [{ required: true, message: '请输入标题' }],
  content: [{ required: true, message: '请输入内容' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await noticeApi.list({ page: pagination.value.page, size: pagination.value.size })
    notices.value = res.data.records
    pagination.value.total = res.data.total
    if (notices.value.length && !currentNotice.value) currentNotice.value = notices.value[0]
  } finally { loading.value = false }
}
function viewNotice(row) { currentNotice.value = row }
function openDialog(row = null) {
  form.value = row
    ? { id: row.id, title: row.title, content: row.content, category: row.category, isPinned: row.isPinned }
    : { category: 'NOTICE', isPinned: false }
  dialogVisible.value = true
}
async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.value.id) await noticeApi.update(form.value.id, form.value)
    else await noticeApi.create(form.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally { saving.value = false }
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除公告「${row.title}」？`, '警告', { type: 'warning' })
  await noticeApi.delete(row.id)
  ElMessage.success('删除成功')
  if (currentNotice.value?.id === row.id) currentNotice.value = null
  loadData()
}
onMounted(loadData)
</script>
