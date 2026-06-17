<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Plus, Refresh, Search, Select, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import { useUserOptions } from '@/composables/useUserOptions'
import { getProjectTopicListApi, type ProjectTopicOption } from '@/api/projectTopic'
import {
  createTopicApplicationApi,
  getTopicApplicationDetailApi,
  getTopicApplicationPageApi,
  reviewTopicApplicationApi,
  type TopicApplicationCreateFormData,
  type TopicApplicationItem,
  type TopicApplicationReviewFormData,
} from '@/api/topicApplication'
import { getTrainingBatchListApi, type TrainingBatchOption } from '@/api/trainingBatch'
import { useUserStore } from '@/stores/user'
import { getStudentDisplayName, getTeacherDisplayName } from '@/utils/userDisplay'
import TopicApplicationApplyDialog from '@/views/topic-application/components/TopicApplicationApplyDialog.vue'
import TopicApplicationReviewDialog from '@/views/topic-application/components/TopicApplicationReviewDialog.vue'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<TopicApplicationItem[]>([])
const total = ref(0)

const applyDialogVisible = ref(false)
const reviewDialogVisible = ref(false)

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<TopicApplicationItem | null>(null)

const batchOptions = ref<TrainingBatchOption[]>([])
const topicOptions = ref<ProjectTopicOption[]>([])
const { studentOptions, teacherOptionMap, userOptionMap, loadRequestedOptions } = useUserOptions()

const queryForm = reactive({
  batchId: '',
  topicId: '',
  studentId: '',
  status: '',
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const applyForm = reactive<TopicApplicationCreateFormData>({
  batchId: '',
  topicId: '',
  applyReason: '',
})

const reviewForm = reactive<TopicApplicationReviewFormData>({
  id: undefined,
  status: 'APPROVED',
  reviewComment: '',
})

const roles = computed(() => userStore.userInfo?.roles || [])
const isStudent = computed(() => roles.value.includes('STUDENT'))
const isTeacher = computed(() => roles.value.includes('TEACHER'))
const isAdmin = computed(() => roles.value.includes('ADMIN'))
const pageCount = computed(() => Math.max(1, Math.ceil(total.value / pagination.size)))

const roleHint = computed(() => {
  if (isAdmin.value) return '管理员可查看全部选题申请'
  if (isTeacher.value) return '教师可查看自己课题下的申请'
  return '学生可提交并查看自己的选题申请'
})

const filteredTopicOptions = computed(() => {
  if (!queryForm.batchId) return topicOptions.value
  return topicOptions.value.filter((item) => item.batchId === queryForm.batchId)
})

const getBatchLabel = (id: string) => {
  const item = batchOptions.value.find((option) => option.id === id)
  if (!item) return id
  return item.termName ? `${item.batchName} - ${item.termName}` : item.batchName
}

const getTopicLabel = (id: string) => {
  const item = topicOptions.value.find((option) => option.id === id)
  return item?.topicName || id
}

const difficultyMap: Record<number, string> = {
  1: '简单',
  2: '中等',
  3: '较难',
  4: '困难',
}

const getTopicDifficulty = (id: string) => {
  const item = topicOptions.value.find((option) => option.id === id)
  if (!item?.difficultyLevel) return '--'
  return difficultyMap[item.difficultyLevel] || String(item.difficultyLevel)
}

const getStudentLabel = (id: string) => {
  return getStudentDisplayName(id, userOptionMap.value)
}

const getTeacherLabel = (id: string | null) => {
  return getTeacherDisplayName(id, teacherOptionMap.value)
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    CANCELED: '已取消',
  }
  return map[status] || status || '--'
}

const getStatusTagType = (status: string) => {
  if (status === 'PENDING') return 'warning'
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'info'
}

const formatDateTime = (value: string | null) => {
  if (!value) return '--'
  return value.replace('T', ' ')
}

const textPreview = (value: string | null, limit = 28) => {
  if (!value) return '暂无内容'
  return value.length > limit ? `${value.slice(0, limit)}...` : value
}

const canReviewRow = (row: TopicApplicationItem) => isTeacher.value && row.status === 'PENDING'

const fetchOptions = async () => {
  const [batchRes, topicRes] = await Promise.all([
    getTrainingBatchListApi(),
    getProjectTopicListApi(),
    loadRequestedOptions(['student', 'teacher', 'user']),
  ])
  batchOptions.value = batchRes.data
  topicOptions.value = topicRes.data
}

const fetchTopicApplicationList = async () => {
  loading.value = true
  try {
    const res = await getTopicApplicationPageApi({
      current: pagination.current,
      size: pagination.size,
      batchId: queryForm.batchId || undefined,
      topicId: queryForm.topicId || undefined,
      studentId: queryForm.studentId || undefined,
      status: queryForm.status || undefined,
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchTopicApplicationList()
}

const handleReset = () => {
  queryForm.batchId = ''
  queryForm.topicId = ''
  queryForm.studentId = ''
  queryForm.status = ''
  pagination.current = 1
  fetchTopicApplicationList()
}

const resetApplyForm = () => {
  applyForm.batchId = ''
  applyForm.topicId = ''
  applyForm.applyReason = ''
}

const resetReviewForm = () => {
  reviewForm.id = undefined
  reviewForm.status = 'APPROVED'
  reviewForm.reviewComment = ''
}

const handleCreate = async () => {
  if (!batchOptions.value.length || !topicOptions.value.length) {
    await fetchOptions()
  }
  resetApplyForm()
  applyDialogVisible.value = true
}

const handleApplySubmit = async () => {
  if (!applyForm.batchId) {
    ElMessage.warning('请先选择所属批次')
    return
  }
  if (!applyForm.topicId) {
    ElMessage.warning('请先选择目标课题')
    return
  }

  submitting.value = true
  try {
    await createTopicApplicationApi(applyForm)
    ElMessage.success('选题申请提交成功')
    applyDialogVisible.value = false
    fetchTopicApplicationList()
  } finally {
    submitting.value = false
  }
}

const handleReview = (row: TopicApplicationItem) => {
  resetReviewForm()
  reviewForm.id = row.id
  reviewDialogVisible.value = true
}

const handleReviewSubmit = async () => {
  if (!reviewForm.id) {
    ElMessage.warning('请先选择申请记录')
    return
  }

  submitting.value = true
  try {
    await reviewTopicApplicationApi(reviewForm)
    ElMessage.success('选题申请审核成功')
    reviewDialogVisible.value = false
    fetchTopicApplicationList()
  } finally {
    submitting.value = false
  }
}

const handleView = async (row: TopicApplicationItem) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await getTopicApplicationDetailApi(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchTopicApplicationList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchTopicApplicationList()
}

onMounted(() => {
  Promise.allSettled([
    fetchOptions(),
    fetchTopicApplicationList(),
  ])
})
</script>

<template>
  <div class="topic-application-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div class="hero-main">
          <div class="hero-badge">Topic Application Management</div>
          <h1>选题申请管理</h1>
        </div>
        <div class="hero-side">
          <div class="hero-side-label">当前视角</div>
          <div class="hero-side-value">{{ roleHint }}</div>
          <div class="hero-side-meta">当前列表共 {{ total }} 条选题申请记录</div>
        </div>
      </div>
    </el-card>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="所属批次">
          <el-select
            v-model="queryForm.batchId"
            clearable
            filterable
            placeholder="请选择实训批次"
            style="width: 250px"
          >
            <el-option
              v-for="item in batchOptions"
              :key="item.id"
              :label="getBatchLabel(item.id)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="目标课题">
          <el-select
            v-model="queryForm.topicId"
            clearable
            filterable
            placeholder="请选择课题"
            style="width: 260px"
          >
            <el-option
              v-for="item in filteredTopicOptions"
              :key="item.id"
              :label="getTopicLabel(item.id)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="学生">
          <el-select v-model="queryForm.studentId" clearable filterable placeholder="请选择学生" style="width: 240px">
            <el-option v-for="item in studentOptions" :key="item.id" :label="getStudentLabel(item.id)" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" clearable placeholder="请选择状态" style="width: 160px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已取消" value="CANCELED" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button v-if="isStudent" type="success" :icon="Plus" @click="handleCreate">发起申请</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div>
            <div class="table-title">申请列表</div>
          </div>
          <div class="header-tags">
            <el-tag type="info">共 {{ total }} 条</el-tag>
            <el-tag type="primary">{{ roleHint }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe class="topic-application-table">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="所属批次" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              {{ getBatchLabel(row.batchId) }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="目标课题" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getTopicLabel(row.topicId) }}
          </template>
        </el-table-column>
        <el-table-column label="难度" width="80">
          <template #default="{ row }">
            {{ getTopicDifficulty(row.topicId) }}
          </template>
        </el-table-column>
        <el-table-column label="学生" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ getStudentLabel(row.studentId) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请理由" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ textPreview(row.applyReason) }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="canReviewRow(row)"
              type="success"
              link
              :icon="Select"
              @click="handleReview(row)"
            >
              审核
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <div class="pagination-summary">
          <span>共 {{ total }} 条</span>
          <span>第 {{ pagination.current }} / {{ pageCount }} 页</span>
        </div>
        <div v-if="pageCount > 1" class="pagination-quick-actions">
          <el-button plain @click="handleCurrentChange(pagination.current - 1)" :disabled="pagination.current <= 1">
            上一页
          </el-button>
          <el-button plain @click="handleCurrentChange(pagination.current + 1)" :disabled="pagination.current >= pageCount">
            下一页
          </el-button>
        </div>
        <el-pagination
          background
          layout="sizes, prev, pager, next, jumper"
          :total="total"
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :pager-count="5"
          :page-sizes="[10, 20, 30, 50]"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <TopicApplicationApplyDialog
      v-model="applyDialogVisible"
      :form-data="applyForm"
      :batch-options="batchOptions"
      :topic-options="topicOptions"
      :submitting="submitting"
      @submit="handleApplySubmit"
    />

    <TopicApplicationReviewDialog
      v-model="reviewDialogVisible"
      :form-data="reviewForm"
      :submitting="submitting"
      @submit="handleReviewSubmit"
    />

    <el-dialog v-model="detailVisible" title="选题申请详情" width="820px" destroy-on-close>
      <div v-loading="detailLoading" class="detail-panel detail-loading-panel">
        <template v-if="detailData">
        <div class="detail-header">
          <div>
            <h2>{{ getTopicLabel(detailData.topicId) }}</h2>
            <p>{{ getBatchLabel(detailData.batchId) }} - 学生：{{ getStudentLabel(detailData.studentId) }}</p>
          </div>
          <div class="detail-tags">
            <el-tag :type="getStatusTagType(detailData.status)" effect="light">
              {{ getStatusLabel(detailData.status) }}
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="所属批次">
            {{ getBatchLabel(detailData.batchId) }}
          </el-descriptions-item>
          <el-descriptions-item label="目标课题">
            {{ getTopicLabel(detailData.topicId) }}
          </el-descriptions-item>
          <el-descriptions-item label="学生">
            {{ getStudentLabel(detailData.studentId) }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(detailData.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="审核教师">
            {{ getTeacherLabel(detailData.reviewerId) }}
          </el-descriptions-item>
          <el-descriptions-item label="审核时间">
            {{ formatDateTime(detailData.reviewTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-block">
          <div class="detail-block-title">申请理由</div>
          <div class="detail-block-content">{{ detailData.applyReason || '暂无申请理由' }}</div>
        </div>

        <div class="detail-block">
          <div class="detail-block-title">审核意见</div>
          <div class="detail-block-content">{{ detailData.reviewComment || '暂无审核意见' }}</div>
        </div>
        </template>
        <el-skeleton v-else :rows="6" animated />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.topic-application-page {
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

.pagination-wrapper {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  margin-top: 20px;
}

.pagination-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  color: #6b7a90;
  font-size: 13px;
  font-weight: 600;
}

.pagination-quick-actions {
  display: flex;
  gap: 10px;
}

.detail-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-loading-panel {
  min-height: 220px;
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
  flex-wrap: wrap;
}

.detail-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-block-title {
  font-size: 15px;
  font-weight: 700;
  color: #1f2d3d;
}

@media (max-width: 1024px) {
  .hero-content {
    flex-direction: column;
  }

  .hero-side {
    min-width: 0;
  }
}

.detail-block-content {
  min-height: 96px;
  padding: 16px 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f7fbff 0%, #edf5ff 100%);
  color: #344256;
  line-height: 1.9;
  white-space: pre-wrap;
}
</style>
