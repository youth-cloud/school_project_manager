<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Download, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { useUserOptions } from '@/composables/useUserOptions'
import { getProjectGroupListApi, type ProjectGroupOption } from '@/api/projectGroup'
import { getStageTaskListApi, type StageTaskItem } from '@/api/stageTask'
import StageSubmissionFormDialog from '@/views/stage-submission/components/StageSubmissionFormDialog.vue'
import SubmissionFileUploadDialog from '@/views/submission-file/components/SubmissionFileUploadDialog.vue'
import {
  createStageSubmissionApi,
  deleteStageSubmissionApi,
  getStageSubmissionDetailApi,
  getStageSubmissionPageApi,
  updateStageSubmissionApi,
  type StageSubmissionFormData,
  type StageSubmissionItem,
} from '@/api/stageSubmission'
import {
  deleteSubmissionFileApi,
  fetchSubmissionFileDownloadApi,
  fetchSubmissionFilePreviewApi,
  getSubmissionFilePageApi,
  uploadSubmissionFileApi,
  type SubmissionFileItem,
  type SubmissionFileUploadFormData,
} from '@/api/submissionFile'
import { getTrainingBatchListApi, type TrainingBatchOption } from '@/api/trainingBatch'
import { useUserStore } from '@/stores/user'
import { getStudentDisplayName, getTeacherDisplayName, getUserDisplayName } from '@/utils/userDisplay'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const tableData = ref<StageSubmissionItem[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<StageSubmissionItem | null>(null)
const attachmentLoading = ref(false)
const attachmentRows = ref<SubmissionFileItem[]>([])
const attachmentDialogVisible = ref(false)
const attachmentSubmitting = ref(false)
const deletingId = ref('')
const previewingFileId = ref('')
const downloadingFileId = ref('')
const deletingFileId = ref('')
const batchOptions = ref<TrainingBatchOption[]>([])
const groupOptions = ref<ProjectGroupOption[]>([])
const taskOptions = ref<StageTaskItem[]>([])
const { studentOptionMap, teacherOptionMap, userOptionMap, loadRequestedOptions } = useUserOptions()

const attachmentForm = reactive<SubmissionFileUploadFormData>({
  submissionId: '',
  bizType: '',
  file: null,
})

const submissionForm = reactive<StageSubmissionFormData>({
  taskId: '',
  batchId: '',
  groupId: '',
  versionNo: 1,
  summary: '',
  reportText: '',
  repoUrl: '',
  deployUrl: '',
  status: 1,
})

const queryForm = reactive({
  versionNo: undefined as number | undefined,
  summary: '',
  status: undefined as number | undefined,
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const currentUserId = computed(() => String(userStore.userInfo?.id || ''))
const roleHint = computed(() => {
  const roles = userStore.userInfo?.roles || []
  if (roles.includes('ADMIN')) return '管理员可查看并管理全部提交'
  if (roles.includes('TEACHER')) return '教师查看自己指导组的提交'
  return '学生可新增并管理自己的提交'
})
const canCreateSubmission = computed(() => (userStore.userInfo?.roles || []).includes('STUDENT'))
const isAdmin = computed(() => (userStore.userInfo?.roles || []).includes('ADMIN'))
const canUploadAttachment = computed(() => isAdmin.value || (userStore.userInfo?.roles || []).includes('STUDENT'))

const fetchStageSubmissionList = async () => {
  loading.value = true
  try {
    const res = await getStageSubmissionPageApi({
      current: pagination.current,
      size: pagination.size,
      versionNo: queryForm.versionNo,
      summary: queryForm.summary || undefined,
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
  fetchStageSubmissionList()
}

const handleReset = () => {
  queryForm.versionNo = undefined
  queryForm.summary = ''
  queryForm.status = undefined
  pagination.current = 1
  fetchStageSubmissionList()
}

const handleCreate = async () => {
  if (!batchOptions.value.length || !groupOptions.value.length || !taskOptions.value.length) {
    await loadOptions()
  }
  dialogMode.value = 'create'
  resetSubmissionForm()
  dialogVisible.value = true
}

const handleEdit = (row: StageSubmissionItem) => {
  dialogMode.value = 'edit'
  submissionForm.id = row.id
  submissionForm.taskId = row.taskId
  submissionForm.batchId = row.batchId
  submissionForm.groupId = row.groupId
  submissionForm.versionNo = row.versionNo
  submissionForm.summary = row.summary || ''
  submissionForm.reportText = row.reportText || ''
  submissionForm.repoUrl = row.repoUrl || ''
  submissionForm.deployUrl = row.deployUrl || ''
  submissionForm.status = row.status
  dialogVisible.value = true
}



const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchStageSubmissionList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchStageSubmissionList()
}

const formatDateTime = (value: string | null) => {
  if (!value) return '--'
  return value.replace('T', ' ')
}

const getStatusLabel = (status: number) => {
  return status === 1 ? '已提交' : '草稿/停用'
}

const getStatusTagType = (status: number) => {
  return status === 1 ? 'success' : 'info'
}

const getSummaryPreview = (value: string | null) => {
  if (!value) return '暂无提交摘要'
  if (value.length <= 40) return value
  return `${value.slice(0, 40)}...`
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await createStageSubmissionApi(submissionForm)
      ElMessage.success('阶段提交新增成功')
    } else {
      await updateStageSubmissionApi(submissionForm)
      ElMessage.success('阶段提交修改成功')
    }
    dialogVisible.value = false
    fetchStageSubmissionList()
  } finally {
    submitting.value = false
  }
}

const handleView = async (row: StageSubmissionItem) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  attachmentRows.value = []
  try {
    const [detailRes] = await Promise.all([
      getStageSubmissionDetailApi(row.id),
      fetchAttachmentList(row.id),
    ])
    detailData.value = detailRes.data
  } finally {
    detailLoading.value = false
  }
}

const handleDelete = async (row: StageSubmissionItem) => {
  await ElMessageBox.confirm(`确认删除这条阶段提交吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
  })

  deletingId.value = row.id
  try {
    await deleteStageSubmissionApi(row.id)
    ElMessage.success('阶段提交删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    fetchStageSubmissionList()
  } finally {
    deletingId.value = ''
  }
}

const getBatchLabel = (id: string) => batchOptions.value.find((item) => item.id === id)?.batchName || id
const getGroupLabel = (id: string) => groupOptions.value.find((item) => item.id === id)?.groupName || id
const getTaskLabel = (id: string) => taskOptions.value.find((item) => item.id === id)?.taskTitle || id
const getStudentLabel = (id: string) => {
  return getStudentDisplayName(id, userOptionMap.value)
}
const getTeacherLabel = (id: string) => getTeacherDisplayName(id, teacherOptionMap.value)
const getUserLabel = (id: string) => {
  return getUserDisplayName(id, userOptionMap.value, studentOptionMap.value)
}
const canManageRow = (row: StageSubmissionItem) => isAdmin.value || row.submitterId === currentUserId.value
const canDeleteAttachment = (row: SubmissionFileItem) => isAdmin.value || row.uploadUserId === currentUserId.value

const getAttachmentSizeLabel = (size: number) => {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(2)} KB`
  return `${(size / 1024 / 1024).toFixed(2)} MB`
}

const getAttachmentBizLabel = (bizType: string | null) => {
  const bizMap: Record<string, string> = {
    REPORT: '周报',
    SOURCE_CODE: '源码',
    PDF: 'PDF 文档',
    SCREENSHOT: '截图',
    DEMO: '演示说明',
    OTHER: '其他',
  }
  return bizMap[bizType || ''] || bizType || '--'
}

const loadOptions = async () => {
  const [batchRes, groupRes, taskRes] = await Promise.all([
    getTrainingBatchListApi(),
    getProjectGroupListApi(),
    getStageTaskListApi(),
    loadRequestedOptions(['student', 'teacher', 'user']),
  ])
  batchOptions.value = batchRes.data
  groupOptions.value = groupRes.data
  taskOptions.value = taskRes.data
}

const fetchAttachmentList = async (submissionId: string) => {
  attachmentLoading.value = true
  try {
    const res = await getSubmissionFilePageApi({
      current: 1,
      size: 50,
      submissionId,
    })
    attachmentRows.value = res.data.records
  } finally {
    attachmentLoading.value = false
  }
}

const resetSubmissionForm = () => {
  submissionForm.id = undefined
  submissionForm.taskId = ''
  submissionForm.batchId = ''
  submissionForm.groupId = ''
  submissionForm.versionNo = 1
  submissionForm.summary = ''
  submissionForm.reportText = ''
  submissionForm.repoUrl = ''
  submissionForm.deployUrl = ''
  submissionForm.status = 1
}

const resetAttachmentForm = () => {
  attachmentForm.submissionId = detailData.value?.id || ''
  attachmentForm.bizType = ''
  attachmentForm.file = null
}

onMounted(() => {
  Promise.allSettled([
    loadOptions(),
    fetchStageSubmissionList(),
  ])
})

const openAttachmentUpload = () => {
  if (!detailData.value) return
  resetAttachmentForm()
  attachmentDialogVisible.value = true
}

const handleAttachmentUpload = async () => {
  if (!attachmentForm.submissionId) {
    ElMessage.warning('请先选择目标阶段提交')
    return
  }
  if (!attachmentForm.file) {
    ElMessage.warning('请先选择要上传的文件')
    return
  }

  const formData = new FormData()
  formData.append('submissionId', attachmentForm.submissionId)
  if (attachmentForm.bizType.trim()) {
    formData.append('bizType', attachmentForm.bizType.trim())
  }
  formData.append('file', attachmentForm.file)

  attachmentSubmitting.value = true
  try {
    await uploadSubmissionFileApi(formData)
    ElMessage.success('提交附件上传成功')
    attachmentDialogVisible.value = false
    await fetchAttachmentList(attachmentForm.submissionId)
  } finally {
    attachmentSubmitting.value = false
  }
}

const headerString = (value: unknown) => (typeof value === 'string' ? value : undefined)

const parseBlobName = (header?: string) => {
  if (!header) return ''
  const utf8Matched = header.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Matched?.[1]) return decodeURIComponent(utf8Matched[1])
  const defaultMatched = header.match(/filename="?([^"]+)"?/i)
  return defaultMatched?.[1] || ''
}

const handleAttachmentPreview = async (row: SubmissionFileItem) => {
  previewingFileId.value = row.id
  try {
    const res = await fetchSubmissionFilePreviewApi(row.id)
    const contentType = headerString(res.headers['content-type']) || 'application/octet-stream'
    const blob = new Blob([res.data], { type: contentType })
    const url = URL.createObjectURL(blob)
    window.open(url, '_blank')
    setTimeout(() => URL.revokeObjectURL(url), 60_000)
  } finally {
    previewingFileId.value = ''
  }
}

const handleAttachmentDownload = async (row: SubmissionFileItem) => {
  downloadingFileId.value = row.id
  try {
    const res = await fetchSubmissionFileDownloadApi(row.id)
    const contentType = headerString(res.headers['content-type']) || 'application/octet-stream'
    const contentDisposition = headerString(res.headers['content-disposition'])
    const blob = new Blob([res.data], { type: contentType })
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = parseBlobName(contentDisposition) || row.originalName || row.fileName
    document.body.appendChild(anchor)
    anchor.click()
    document.body.removeChild(anchor)
    URL.revokeObjectURL(url)
  } finally {
    downloadingFileId.value = ''
  }
}

const handleAttachmentDelete = async (row: SubmissionFileItem) => {
  await ElMessageBox.confirm('确认删除这条提交附件吗？删除后会同时删除物理文件。', '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
  })

  deletingFileId.value = row.id
  try {
    await deleteSubmissionFileApi(row.id)
    ElMessage.success('提交附件删除成功')
    if (detailData.value) {
      await fetchAttachmentList(detailData.value.id)
    }
  } finally {
    deletingFileId.value = ''
  }
}
</script>

<template>
  <div class="stage-submission-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div class="hero-main">
          <div class="hero-badge">Stage Submission Management</div>
          <h1>阶段提交管理</h1>
        </div>
        <div class="hero-side">
          <div class="hero-side-label">当前视角</div>
          <div class="hero-side-value">{{ roleHint }}</div>
          <div class="hero-side-meta">当前列表共 {{ total }} 条阶段提交记录</div>
        </div>
      </div>
    </el-card>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="版本号">
          <el-input-number v-model="queryForm.versionNo" :min="1" controls-position="right" placeholder="版本号" />
        </el-form-item>

        <el-form-item label="提交摘要">
          <el-input
            v-model="queryForm.summary"
            placeholder="请输入提交摘要关键词"
            clearable
            style="width: 260px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 160px">
            <el-option label="已提交" :value="1" />
            <el-option label="草稿/停用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button v-if="canCreateSubmission" type="success" :icon="Plus" @click="handleCreate">新建阶段提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div>
            <div class="table-title">阶段提交列表</div>
          </div>
          <div class="header-tags">
            <el-tag type="info">共 {{ total }} 条</el-tag>
            <el-tag type="primary">{{ roleHint }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe class="stage-submission-table">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="提交摘要" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">{{ getSummaryPreview(row.summary) }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="versionNo" label="版本号" width="90" />
        <el-table-column label="阶段任务" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ getTaskLabel(row.taskId) }}</template>
        </el-table-column>
        <el-table-column label="所属批次" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ getBatchLabel(row.batchId) }}</template>
        </el-table-column>
        <el-table-column label="项目组" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ getGroupLabel(row.groupId) }}</template>
        </el-table-column>
        <el-table-column label="提交人" width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getStudentLabel(row.submitterId) }}
          </template>
        </el-table-column>
        <el-table-column label="提交状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.submitTime) }}
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
            <el-button v-if="canManageRow(row)" type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="canManageRow(row)" type="danger" link :icon="Delete" :loading="deletingId === row.id" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 30, 50]"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <StageSubmissionFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :form-data="submissionForm"
      :batch-options="batchOptions"
      :group-options="groupOptions"
      :task-options="taskOptions"
      :submitting="submitting"
      @submit="handleSubmit"
    />

    <el-dialog v-model="detailVisible" title="阶段提交详情" width="880px" destroy-on-close>
      <div v-loading="detailLoading" class="submission-detail detail-loading-panel">
        <template v-if="detailData">
          <div class="detail-header">
            <div>
              <h2>{{ detailData.summary || '阶段提交详情' }}</h2>
              <p>{{ getTaskLabel(detailData.taskId) }} - {{ getBatchLabel(detailData.batchId) }}</p>
            </div>
            <div class="detail-tags">
              <el-tag :type="getStatusTagType(detailData.status)" effect="light">{{ getStatusLabel(detailData.status) }}</el-tag>
              <el-tag type="primary" effect="light">V{{ detailData.versionNo }}</el-tag>
            </div>
          </div>

          <div class="detail-overview">
            <div class="overview-card">
              <div class="overview-label">项目组</div>
              <div class="overview-value">{{ getGroupLabel(detailData.groupId) }}</div>
            </div>
            <div class="overview-card">
              <div class="overview-label">提交人</div>
              <div class="overview-value">{{ getStudentLabel(detailData.submitterId) }}</div>
            </div>
            <div class="overview-card">
              <div class="overview-label">提交时间</div>
              <div class="overview-value">{{ formatDateTime(detailData.submitTime) }}</div>
            </div>
            <div class="overview-card">
              <div class="overview-label">最后更新</div>
              <div class="overview-value">{{ formatDateTime(detailData.updateTime) }}</div>
            </div>
          </div>

          <div class="section-card">
            <div class="section-title">提交说明</div>
            <div class="detail-content">{{ detailData.reportText || '暂无提交说明' }}</div>
          </div>

          <div class="section-card">
            <div class="section-title">提交链接</div>
            <div class="detail-links" v-if="detailData.repoUrl || detailData.deployUrl">
              <el-link v-if="detailData.repoUrl" :href="detailData.repoUrl" target="_blank" type="primary">仓库地址</el-link>
              <el-link v-if="detailData.deployUrl" :href="detailData.deployUrl" target="_blank" type="success">部署地址</el-link>
            </div>
            <div v-else class="link-empty">当前未填写仓库地址或部署地址</div>
          </div>

          <div class="section-card attachment-panel">
            <div class="attachment-header">
              <div class="attachment-meta">
                <div class="attachment-title">提交附件</div>
                <div class="attachment-subtitle">共 {{ attachmentRows.length }} 个附件，可在这里直接上传、预览和下载</div>
              </div>
              <el-button v-if="canUploadAttachment" type="primary" plain :icon="Plus" @click="openAttachmentUpload">
                上传附件
              </el-button>
            </div>

            <el-table :data="attachmentRows" v-loading="attachmentLoading" stripe>
              <el-table-column label="原始文件名" min-width="220" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ row.originalName }}
                </template>
              </el-table-column>
              <el-table-column label="业务类型" width="120">
                <template #default="{ row }">
                  {{ getAttachmentBizLabel(row.bizType) }}
                </template>
              </el-table-column>
              <el-table-column prop="fileType" label="文件类型" width="100" />
              <el-table-column label="文件大小" width="110">
                <template #default="{ row }">
                  {{ getAttachmentSizeLabel(row.fileSize) }}
                </template>
              </el-table-column>
              <el-table-column label="上传人" min-width="180" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ getUserLabel(row.uploadUserId) }}
                </template>
              </el-table-column>
              <el-table-column label="上传时间" min-width="180">
                <template #default="{ row }">
                  {{ formatDateTime(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="260" fixed="right">
                <template #default="{ row }">
                  <el-button
                    type="primary"
                    link
                    :icon="View"
                    :loading="previewingFileId === row.id"
                    @click="handleAttachmentPreview(row)"
                  >
                    预览
                  </el-button>
                  <el-button
                    type="success"
                    link
                    :icon="Download"
                    :loading="downloadingFileId === row.id"
                    @click="handleAttachmentDownload(row)"
                  >
                    下载
                  </el-button>
                  <el-button
                    v-if="canDeleteAttachment(row)"
                    type="danger"
                    link
                    :icon="Delete"
                    :loading="deletingFileId === row.id"
                    @click="handleAttachmentDelete(row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
              <template #empty>
                <el-empty description="暂无附件，点击右上角可直接上传" />
              </template>
            </el-table>
          </div>
        </template>
        <el-skeleton v-else animated :rows="8" />
      </div>
    </el-dialog>

    <SubmissionFileUploadDialog
      v-model="attachmentDialogVisible"
      :form-data="attachmentForm"
      :submission-options="detailData ? [detailData] : []"
      :submitting="attachmentSubmitting"
      @submit="handleAttachmentUpload"
    />
  </div>
</template>

<style scoped lang="scss">
.stage-submission-page {
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
  margin: 0 0 10px;
  color: #1f2d3d;
  font-size: 30px;
}

.hero-content p {
  margin: 0;
  max-width: 640px;
  color: #6b7a90;
  line-height: 1.8;
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

.filter-panel-head {
  margin-bottom: 14px;
}

.filter-title {
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 700;
}

.filter-subtitle {
  margin-top: 6px;
  color: #7b8ba1;
  font-size: 13px;
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

.header-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.stage-submission-table {
  margin-top: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.submission-detail {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-loading-panel {
  min-height: 280px;
}

.detail-overview {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.overview-card,
.section-card {
  border-radius: 18px;
  border: 1px solid #e4eefb;
  background: linear-gradient(180deg, #fbfdff 0%, #f6faff 100%);
}

.overview-card {
  padding: 18px;
}

.overview-label {
  margin-bottom: 8px;
  color: #7b8ba1;
  font-size: 13px;
}

.overview-value {
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.6;
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

@media (max-width: 1024px) {
  .hero-content {
    flex-direction: column;
  }

  .hero-side {
    min-width: 0;
  }
}

.detail-header p {
  margin: 0;
  color: #7b8ba1;
}

.detail-tags,
.detail-links {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.section-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px;
}

.section-title {
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 700;
}

.detail-content {
  min-height: 140px;
  padding: 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f7fbff 0%, #edf5ff 100%);
  color: #344256;
  line-height: 1.9;
  white-space: pre-wrap;
}

.attachment-panel {
  gap: 16px;
}

.attachment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.attachment-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.attachment-title {
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 700;
}

.attachment-subtitle,
.link-empty {
  color: #7b8ba1;
  font-size: 13px;
}
</style>
