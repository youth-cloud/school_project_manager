<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import NoticeFormDialog from '@/views/notice/components/NoticeFormDialog.vue'
import {
  createNoticeApi,
  deleteNoticeApi,
  getNoticeDetailApi,
  getNoticePageApi,
  updateNoticeApi,
  type NoticeFormData,
  type NoticeItem,
} from '@/api/notice'
import { useUserOptions } from '@/composables/useUserOptions'
import { useUserStore } from '@/stores/user'
import { getUserRealName } from '@/utils/userDisplay'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const tableData = ref<NoticeItem[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<NoticeItem | null>(null)
const deletingId = ref('')
const { userOptionMap, loadUserOptions } = useUserOptions()

const noticeForm = reactive<NoticeFormData>({
  title: '',
  content: '',
  targetRole: 'ALL',
  status: 1,
})

const canManageNotice = computed(() => {
  const roles = userStore.userInfo?.roles || []
  return roles.includes('ADMIN') || roles.includes('TEACHER')
})

const queryForm = reactive({
  title: '',
  targetRole: '',
  status: undefined as number | undefined,
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const fetchNoticeList = async () => {
  loading.value = true
  try {
    const res = await getNoticePageApi({
      current: pagination.current,
      size: pagination.size,
      title: queryForm.title || undefined,
      targetRole: queryForm.targetRole || undefined,
      status: queryForm.status,
    })

    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchNoticeList()
}

const handleReset = () => {
  queryForm.title = ''
  queryForm.targetRole = ''
  queryForm.status = undefined
  pagination.current = 1
  fetchNoticeList()
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchNoticeList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchNoticeList()
}

const getRoleLabel = (targetRole: string | null) => {
  if (!targetRole || targetRole === 'ALL') return '全体可见'
  if (targetRole === 'TEACHER') return '教师'
  if (targetRole === 'STUDENT') return '学生'
  return targetRole
}

const getRoleTagType = (targetRole: string | null) => {
  if (!targetRole || targetRole === 'ALL') return 'primary'
  if (targetRole === 'TEACHER') return 'success'
  if (targetRole === 'STUDENT') return 'warning'
  return 'info'
}

const getStatusLabel = (status: number) => {
  return status === 1 ? '已发布' : '草稿/停用'
}

const getStatusTagType = (status: number) => {
  return status === 1 ? 'success' : 'info'
}

const formatDateTime = (value: string | null) => {
  if (!value) return '--'
  return value.replace('T', ' ')
}

const getPublisherLabel = (id: string) => {
  return getUserRealName(id, userOptionMap.value)
}

const resetNoticeForm = () => {
  noticeForm.id = undefined
  noticeForm.title = ''
  noticeForm.content = ''
  noticeForm.targetRole = 'ALL'
  noticeForm.status = 1
  noticeForm.publishTime = ''
}

const handleCreate = () => {
  dialogMode.value = 'create'
  resetNoticeForm()
  dialogVisible.value = true
}

const handleEdit = (row: NoticeItem) => {
  dialogMode.value = 'edit'
  noticeForm.id = row.id
  noticeForm.title = row.title
  noticeForm.content = row.content || ''
  noticeForm.targetRole = row.targetRole || 'ALL'
  noticeForm.status = row.status
  noticeForm.publishTime = row.publishTime || ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await createNoticeApi(noticeForm)
      ElMessage.success('公告新增成功')
    } else {
      await updateNoticeApi(noticeForm)
      ElMessage.success('公告修改成功')
    }
    dialogVisible.value = false
    fetchNoticeList()
  } finally {
    submitting.value = false
  }
}

const handleView = async (row: NoticeItem) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getNoticeDetailApi(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const handleDelete = async (row: NoticeItem) => {
  await ElMessageBox.confirm(`确认删除公告“${row.title}”吗？删除后不可恢复。`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
  })

  deletingId.value = row.id
  try {
    await deleteNoticeApi(row.id)
    ElMessage.success('公告删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    fetchNoticeList()
  } finally {
    deletingId.value = ''
  }
}

onMounted(() => {
  Promise.all([loadUserOptions(), fetchNoticeList()])
})
</script>

<template>
  <div class="notice-page">
    <el-card class="notice-hero-card" shadow="never">
      <div class="hero-content">
        <div>
          <div class="hero-badge">Notice Management</div>
          <h1>公告管理</h1>
        </div>
      </div>
    </el-card>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="公告标题">
          <el-input
            v-model="queryForm.title"
            placeholder="请输入公告标题"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="目标角色">
          <el-select v-model="queryForm.targetRole" placeholder="请选择目标角色" clearable style="width: 180px">
            <el-option label="全体可见" value="ALL" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 160px">
            <el-option label="已发布" :value="1" />
            <el-option label="草稿/停用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button v-if="canManageNotice" type="success" :icon="Plus" @click="handleCreate">新建公告</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div class="table-title">公告列表</div>
          <el-tag type="info">共 {{ total }} 条</el-tag>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe class="notice-table">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="公告标题" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">{{ row.title }}</el-button>
          </template>
        </el-table-column>
        <el-table-column label="目标角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.targetRole)" effect="light">
              {{ getRoleLabel(row.targetRole) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布人" width="140">
          <template #default="{ row }">
            {{ getPublisherLabel(row.publisherId) }}
          </template>
        </el-table-column>
        <el-table-column label="发布时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column label="更新时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleView(row)">查看</el-button>
            <template v-if="canManageNotice">
              <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link :icon="Delete" :loading="deletingId === row.id" @click="handleDelete(row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :current-page="pagination.current"
          :page-size="pagination.size"
          :page-sizes="[10, 20, 30, 50]"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <NoticeFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :form-data="noticeForm"
      :submitting="submitting"
      @submit="handleSubmit"
    />

    <el-dialog v-model="detailVisible" title="公告详情" width="720px" destroy-on-close>
      <div v-loading="detailLoading" class="notice-detail" v-if="detailData">
        <div class="detail-header">
          <div>
            <h2>{{ detailData.title }}</h2>
            <p>发布时间：{{ formatDateTime(detailData.publishTime) }}</p>
          </div>
          <div class="detail-tags">
            <el-tag :type="getRoleTagType(detailData.targetRole)" effect="light">{{ getRoleLabel(detailData.targetRole) }}</el-tag>
            <el-tag :type="getStatusTagType(detailData.status)" effect="light">{{ getStatusLabel(detailData.status) }}</el-tag>
          </div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="发布人">{{ getPublisherLabel(detailData.publisherId) }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDateTime(detailData.publishTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDateTime(detailData.updateTime) }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-content">
          {{ detailData.content || '暂无公告内容' }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.notice-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.notice-hero-card,
.filter-card,
.table-card {
  border-radius: 20px;
  border: none;
  box-shadow: 0 14px 32px rgb(57 118 201 / 8%);
}

.notice-hero-card {
  background: linear-gradient(135deg, #edf6ff 0%, #f8fbff 60%, #ffffff 100%);
}

.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.hero-badge {
  display: inline-block;
  margin-bottom: 12px;
  padding: 6px 12px;
  border-radius: 999px;
  background: #dcebff;
  color: #2f6fdb;
  font-size: 12px;
  font-weight: 600;
}

.hero-content h1 {
  margin: 0 0 10px;
  color: #1f2d3d;
  font-size: 28px;
}

.hero-content p {
  margin: 0;
  color: #6b7a90;
  line-height: 1.8;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 0;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.table-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2d3d;
}

.table-subtitle {
  margin-top: 6px;
  color: #7b8ba1;
  font-size: 13px;
}

.notice-table {
  margin-top: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.notice-detail {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.detail-header h2 {
  margin: 0 0 8px;
  color: #1f2d3d;
  font-size: 24px;
}

.detail-header p {
  margin: 0;
  color: #7b8ba1;
}

.detail-tags {
  display: flex;
  gap: 8px;
}

.detail-content {
  min-height: 160px;
  padding: 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f7fbff 0%, #edf5ff 100%);
  color: #344256;
  line-height: 1.9;
  white-space: pre-wrap;
}
</style>
