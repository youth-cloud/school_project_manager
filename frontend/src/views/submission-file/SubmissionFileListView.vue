<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Download, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { useUserOptions } from '@/composables/useUserOptions'
import { getStudentDisplayName, getTeacherDisplayName, getUserDisplayName } from '@/utils/userDisplay'
import SubmissionFileUploadDialog from '@/views/submission-file/components/SubmissionFileUploadDialog.vue'
import {
  deleteSubmissionFileApi,
  fetchSubmissionFileDownloadApi,
  fetchSubmissionFilePreviewApi,
  getSubmissionFileDetailApi,
  getSubmissionFilePageApi,
  uploadSubmissionFileApi,
  type SubmissionFileItem,
  type SubmissionFileUploadFormData,
} from '@/api/submissionFile'
import { getStageSubmissionListApi, type StageSubmissionItem } from '@/api/stageSubmission'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const rows = ref<SubmissionFileItem[]>([])
const total = ref(0)
const subs = ref<StageSubmissionItem[]>([])
const { studentOptionMap, teacherOptionMap, userOptionMap, loadRequestedOptions } = useUserOptions()
const dlg = ref(false)
const detail = ref(false)
const detailLoading = ref(false)
const detailData = ref<SubmissionFileItem | null>(null)
const deleting = ref('')
const previewing = ref('')
const downloading = ref('')

const q = reactive({
  submissionId: '',
  fileType: '',
  bizType: '',
})

const p = reactive({
  current: 1,
  size: 10,
})

const f = reactive<SubmissionFileUploadFormData>({
  submissionId: '',
  bizType: '',
  file: null,
})

const roles = computed(() => userStore.userInfo?.roles || [])
const uid = computed(() => String(userStore.userInfo?.id || ''))
const isAdmin = computed(() => roles.value.includes('ADMIN'))
const canUpload = computed(() => isAdmin.value || roles.value.includes('STUDENT'))
const roleHint = computed(() => {
  if (isAdmin.value) return '管理员可查看、上传并管理全部附件'
  if (roles.value.includes('TEACHER')) return '教师可查看自己指导组提交下的附件'
  return '学生可上传并管理自己上传的附件'
})

const subLabel = (id: string) => {
  const item = subs.value.find((entry) => entry.id === id)
  return item ? `V${item.versionNo} · ${item.summary?.trim() || '未填写摘要'}` : id
}

const bizMap: Record<string, string> = {
  REPORT: '周报',
  SOURCE_CODE: '源码',
  PDF: 'PDF 文档',
  SCREENSHOT: '截图',
  DEMO: '演示说明',
  OTHER: '其他',
}

const biz = (v: string | null) => bizMap[v || ''] || v || '--'
const time = (v: string | null) => (v ? v.replace('T', ' ') : '--')
const size = (n: number) => (n < 1024 ? `${n} B` : n < 1048576 ? `${(n / 1024).toFixed(2)} KB` : `${(n / 1048576).toFixed(2)} MB`)
const getStudentLabel = (id: string) => getStudentDisplayName(id, userOptionMap.value)
const getTeacherLabel = (id: string) => getTeacherDisplayName(id, teacherOptionMap.value)
const userLabel = (id: string) => {
  return getUserDisplayName(id, userOptionMap.value, studentOptionMap.value)
}
const canDelete = (row: SubmissionFileItem) => isAdmin.value || row.uploadUserId === uid.value

const loadOptions = async () => {
  const [submissionRes] = await Promise.all([
    getStageSubmissionListApi(),
    loadRequestedOptions(['student', 'teacher', 'user']),
  ])
  subs.value = submissionRes.data
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getSubmissionFilePageApi({
      current: p.current,
      size: p.size,
      submissionId: q.submissionId || undefined,
      fileType: q.fileType || undefined,
      bizType: q.bizType || undefined,
    })
    rows.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const search = () => {
  p.current = 1
  fetchList()
}

const reset = () => {
  q.submissionId = ''
  q.fileType = ''
  q.bizType = ''
  p.current = 1
  fetchList()
}

const openCreate = async () => {
  if (!subs.value.length) {
    await loadOptions()
  }
  f.submissionId = ''
  f.bizType = ''
  f.file = null
  dlg.value = true
}

const submit = async () => {
  if (!f.submissionId) return ElMessage.warning('请先选择目标阶段提交')
  if (!f.file) return ElMessage.warning('请先选择要上传的文件')

  const formData = new FormData()
  formData.append('submissionId', f.submissionId)
  if (f.bizType.trim()) formData.append('bizType', f.bizType.trim())
  formData.append('file', f.file)

  submitting.value = true
  try {
    await uploadSubmissionFileApi(formData)
    ElMessage.success('提交附件上传成功')
    dlg.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

const view = async (row: SubmissionFileItem) => {
  detail.value = true
  detailLoading.value = true
  try {
    detailData.value = (await getSubmissionFileDetailApi(row.id)).data
  } finally {
    detailLoading.value = false
  }
}

const headerString = (value: unknown) => (typeof value === 'string' ? value : undefined)

const blobName = (header?: string) => {
  if (!header) return ''
  const utf8Matched = header.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Matched?.[1]) return decodeURIComponent(utf8Matched[1])
  const defaultMatched = header.match(/filename="?([^"]+)"?/i)
  return defaultMatched?.[1] || ''
}

const preview = async (row: SubmissionFileItem) => {
  previewing.value = row.id
  try {
    const res = await fetchSubmissionFilePreviewApi(row.id)
    const contentType = headerString(res.headers['content-type']) || 'application/octet-stream'
    const blob = new Blob([res.data], { type: contentType })
    const url = URL.createObjectURL(blob)
    window.open(url, '_blank')
    setTimeout(() => URL.revokeObjectURL(url), 60_000)
  } finally {
    previewing.value = ''
  }
}

const download = async (row: SubmissionFileItem) => {
  downloading.value = row.id
  try {
    const res = await fetchSubmissionFileDownloadApi(row.id)
    const contentType = headerString(res.headers['content-type']) || 'application/octet-stream'
    const contentDisposition = headerString(res.headers['content-disposition'])
    const blob = new Blob([res.data], { type: contentType })
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = blobName(contentDisposition) || row.originalName || row.fileName
    document.body.appendChild(anchor)
    anchor.click()
    document.body.removeChild(anchor)
    URL.revokeObjectURL(url)
  } finally {
    downloading.value = ''
  }
}

const del = async (row: SubmissionFileItem) => {
  await ElMessageBox.confirm('确认删除这条提交附件吗？删除后会同时删除物理文件。', '删除确认', {
    type: 'warning',
  })
  deleting.value = row.id
  try {
    await deleteSubmissionFileApi(row.id)
    ElMessage.success('提交附件删除成功')
    if (rows.value.length === 1 && p.current > 1) p.current -= 1
    fetchList()
  } finally {
    deleting.value = ''
  }
}

const handleCurrentChange = (page: number) => {
  p.current = page
  fetchList()
}

const handleSizeChange = (sizeValue: number) => {
  p.size = sizeValue
  p.current = 1
  fetchList()
}

onMounted(async () => {
  await loadOptions()
  await fetchList()
})
</script>

<template>
  <div class="submission-file-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div class="hero-main">
          <div class="hero-badge">Submission File Management</div>
          <h1>提交附件管理</h1>
        </div>
        <div class="hero-side">
          <div class="hero-side-label">当前视角</div>
          <div class="hero-side-value">{{ roleHint }}</div>
          <div class="hero-side-meta">共 {{ total }} 条记录</div>
        </div>
      </div>
    </el-card>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="所属提交">
          <el-select v-model="q.submissionId" filterable clearable style="width: 280px">
            <el-option v-for="item in subs" :key="item.id" :label="subLabel(item.id)" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件类型">
          <el-input v-model="q.fileType" clearable style="width: 160px" @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-input v-model="q.bizType" clearable style="width: 180px" @keyup.enter="search" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
          <el-button v-if="canUpload" type="success" :icon="Plus" @click="openCreate">上传附件</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div class="table-title">附件列表</div>
          <div class="header-tags">
            <el-tag type="info">共 {{ total }} 条</el-tag>
            <el-tag type="primary">{{ roleHint }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="rows" v-loading="loading" stripe class="submission-file-table">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="原始文件名" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="view(row)">{{ row.originalName }}</el-button>
          </template>
        </el-table-column>
        <el-table-column label="所属提交" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ subLabel(row.submissionId) }}</template>
        </el-table-column>
        <el-table-column prop="fileType" label="文件类型" width="100" />
        <el-table-column label="业务类型" width="120">
          <template #default="{ row }">{{ biz(row.bizType) }}</template>
        </el-table-column>
        <el-table-column label="文件大小" width="110">
          <template #default="{ row }">{{ size(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column label="上传人" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ userLabel(row.uploadUserId) }}</template>
        </el-table-column>
        <el-table-column label="上传时间" min-width="180">
          <template #default="{ row }">{{ time(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="270" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="view(row)">查看</el-button>
            <el-button type="primary" link :icon="View" :loading="previewing === row.id" @click="preview(row)">预览</el-button>
            <el-button type="success" link :icon="Download" :loading="downloading === row.id" @click="download(row)">下载</el-button>
            <el-button v-if="canDelete(row)" type="danger" link :icon="Delete" :loading="deleting === row.id" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :current-page="p.current"
          :page-size="p.size"
          :page-sizes="[10, 20, 30, 50]"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <SubmissionFileUploadDialog
      v-model="dlg"
      :form-data="f"
      :submission-options="subs"
      :submitting="submitting"
      @submit="submit"
    />

    <el-dialog v-model="detail" title="附件详情" width="720px">
      <div v-if="detailData" v-loading="detailLoading" class="detail-panel">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="原始文件名" :span="2">{{ detailData.originalName }}</el-descriptions-item>
          <el-descriptions-item label="所属提交">{{ subLabel(detailData.submissionId) }}</el-descriptions-item>
          <el-descriptions-item label="上传人">{{ userLabel(detailData.uploadUserId) }}</el-descriptions-item>
          <el-descriptions-item label="文件类型">{{ detailData.fileType }}</el-descriptions-item>
          <el-descriptions-item label="业务类型">{{ biz(detailData.bizType) }}</el-descriptions-item>
          <el-descriptions-item label="文件大小">{{ size(detailData.fileSize) }}</el-descriptions-item>
          <el-descriptions-item label="上传时间">{{ time(detailData.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="文件路径" :span="2">{{ detailData.filePath }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-actions">
          <el-button type="primary" @click="preview(detailData)">预览</el-button>
          <el-button type="success" @click="download(detailData)">下载</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.submission-file-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.hero-card,
.filter-card,
.table-card {
  border-radius: 24px;
  border: 1px solid rgba(120, 148, 196, 0.14);
  box-shadow: 0 18px 38px rgb(57 118 201 / 8%);
}

.hero-card {
  background:
    radial-gradient(circle at top right, rgba(116, 166, 255, 0.18), transparent 24%),
    linear-gradient(135deg, #eef7ff 0%, #f8fbff 58%, #ffffff 100%);
}

.hero-content {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 20px;
}

.hero-main {
  max-width: 760px;
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
  margin: 0;
  color: #1f2d3d;
  font-size: 30px;
}

.hero-side {
  min-width: 240px;
  padding: 20px 22px;
  border-radius: 20px;
  border: 1px solid rgba(120, 148, 196, 0.12);
  background: rgba(255, 255, 255, 0.72);
}

.hero-side-label {
  color: #7b8ba1;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-side-value {
  margin-top: 10px;
  color: #1f2d3d;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.5;
}

.hero-side-meta {
  margin-top: 10px;
  color: #7b8ba1;
  line-height: 1.7;
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

.header-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.submission-file-table {
  margin-top: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.detail-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-actions {
  display: flex;
  gap: 10px;
}

@media (max-width: 1024px) {
  .hero-content {
    flex-direction: column;
  }

  .hero-side {
    min-width: 0;
  }
}
</style>
