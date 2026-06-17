<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import ReviewRecordFormDialog from '@/views/review-record/components/ReviewRecordFormDialog.vue'
import { useUserOptions } from '@/composables/useUserOptions'
import { getTeacherDisplayName } from '@/utils/userDisplay'
import {
  createReviewRecordApi,
  deleteReviewRecordApi,
  getReviewRecordDetailApi,
  getReviewRecordPageApi,
  updateReviewRecordApi,
  type ReviewRecordFormData,
  type ReviewRecordItem,
} from '@/api/reviewRecord'
import { getStageSubmissionListApi, type StageSubmissionItem } from '@/api/stageSubmission'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<ReviewRecordItem[]>([])
const total = ref(0)

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<ReviewRecordItem | null>(null)

const deletingId = ref('')
const submissionOptions = ref<StageSubmissionItem[]>([])
const { teacherOptionMap, loadTeacherOptions } = useUserOptions()

const queryForm = reactive({
  submissionId: '',
  reviewResult: '',
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const reviewForm = reactive<ReviewRecordFormData>({
  submissionId: '',
  reviewResult: 'APPROVED',
  score: null,
  comment: '',
})

const roles = computed(() => userStore.userInfo?.roles || [])
const currentUserId = computed(() => String(userStore.userInfo?.id || ''))
const isTeacher = computed(() => roles.value.includes('TEACHER'))
const isAdmin = computed(() => roles.value.includes('ADMIN'))

const roleHint = computed(() => {
  if (isAdmin.value) return '管理员可查看全部评审记录'
  if (isTeacher.value) return '教师可新增并维护自己评审的记录'
  return '学生可查看自己项目对应的评审记录'
})

const reviewResultMap: Record<string, string> = {
  APPROVED: '通过',
  PASS: '通过',
  REJECTED: '驳回',
  REJECT: '驳回',
  NEED_FIX: '需修改',
}

const normalizeReviewResult = (value: string) => {
  if (value === 'PASS') return 'APPROVED'
  if (value === 'REJECT') return 'REJECTED'
  return value
}

const getReviewResultLabel = (value: string) => reviewResultMap[value] || value || '--'
const getReviewResultTagType = (value: string) => {
  if (value === 'APPROVED' || value === 'PASS') return 'success'
  if (value === 'REJECTED' || value === 'REJECT') return 'danger'
  if (value === 'NEED_FIX') return 'warning'
  return 'info'
}

const formatDateTime = (value: string | null) => {
  if (!value) return '--'
  return value.replace('T', ' ')
}

const getSubmissionLabel = (id: string) => {
  const item = submissionOptions.value.find((option) => option.id === id)
  if (!item) return id
  const summary = item.summary?.trim() || '未填写摘要'
  return `V${item.versionNo} - ${summary}`
}

const getTeacherLabel = (id: string) => {
  return getTeacherDisplayName(id, teacherOptionMap.value)
}

const canManageRow = (row: ReviewRecordItem) => {
  return isTeacher.value && row.reviewerId === currentUserId.value
}

const fetchSubmissionOptions = async () => {
  const res = await getStageSubmissionListApi()
  submissionOptions.value = res.data
}

const fetchTeacherOptions = async () => {
  await loadTeacherOptions()
}

const fetchReviewRecordList = async () => {
  loading.value = true
  try {
    const res = await getReviewRecordPageApi({
      current: pagination.current,
      size: pagination.size,
      submissionId: queryForm.submissionId || undefined,
      reviewResult: queryForm.reviewResult ? normalizeReviewResult(queryForm.reviewResult) : undefined,
    })

    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchReviewRecordList()
}

const handleReset = () => {
  queryForm.submissionId = ''
  queryForm.reviewResult = ''
  pagination.current = 1
  fetchReviewRecordList()
}

const resetReviewForm = () => {
  reviewForm.id = undefined
  reviewForm.submissionId = ''
  reviewForm.reviewResult = 'APPROVED'
  reviewForm.score = null
  reviewForm.comment = ''
}

const handleCreate = async () => {
  if (!submissionOptions.value.length) {
    await fetchSubmissionOptions()
  }
  dialogMode.value = 'create'
  resetReviewForm()
  dialogVisible.value = true
}

const handleEdit = async (row: ReviewRecordItem) => {
  if (!submissionOptions.value.length) {
    await fetchSubmissionOptions()
  }
  dialogMode.value = 'edit'
  reviewForm.id = row.id
  reviewForm.submissionId = row.submissionId
  reviewForm.reviewResult = normalizeReviewResult(row.reviewResult)
  reviewForm.score = row.score
  reviewForm.comment = row.comment || ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!reviewForm.submissionId) {
    ElMessage.warning('请先选择阶段提交')
    return
  }

  if (!reviewForm.reviewResult.trim()) {
    ElMessage.warning('请先填写评审结果')
    return
  }

  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await createReviewRecordApi({
        ...reviewForm,
        reviewResult: normalizeReviewResult(reviewForm.reviewResult),
      })
      ElMessage.success('评审记录创建成功')
    } else {
      await updateReviewRecordApi({
        ...reviewForm,
        reviewResult: normalizeReviewResult(reviewForm.reviewResult),
      })
      ElMessage.success('评审记录修改成功')
    }
    dialogVisible.value = false
    fetchReviewRecordList()
  } finally {
    submitting.value = false
  }
}

const handleView = async (row: ReviewRecordItem) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await getReviewRecordDetailApi(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const handleDelete = async (row: ReviewRecordItem) => {
  await ElMessageBox.confirm('确认删除这条评审记录吗？', '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
  })

  deletingId.value = row.id
  try {
    await deleteReviewRecordApi(row.id)
    ElMessage.success('评审记录删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    fetchReviewRecordList()
  } finally {
    deletingId.value = ''
  }
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchReviewRecordList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchReviewRecordList()
}

onMounted(() => {
  Promise.allSettled([
    fetchSubmissionOptions(),
    fetchTeacherOptions(),
    fetchReviewRecordList(),
  ])
})
</script>

<template>
  <div class="review-record-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div class="hero-main">
          <div class="hero-badge">Review Record Management</div>
          <h1>评审记录管理</h1>
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
        <el-form-item label="阶段提交">
          <el-select
            v-model="queryForm.submissionId"
            filterable
            clearable
            placeholder="请选择阶段提交"
            style="width: 280px"
          >
            <el-option
              v-for="item in submissionOptions"
              :key="item.id"
              :label="getSubmissionLabel(item.id)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="评审结果">
          <el-select
            v-model="queryForm.reviewResult"
            clearable
            placeholder="请选择评审结果"
            style="width: 180px"
          >
            <el-option label="通过" value="APPROVED" />
            <el-option label="驳回" value="REJECTED" />
            <el-option label="需修改" value="NEED_FIX" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button v-if="isTeacher" type="success" :icon="Plus" @click="handleCreate">新建评审记录</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div>
            <div class="table-title">评审记录列表</div>
          </div>
          <div class="header-tags">
            <el-tag type="info">共 {{ total }} 条</el-tag>
            <el-tag type="primary">{{ roleHint }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe class="review-record-table">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="阶段提交" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              {{ getSubmissionLabel(row.submissionId) }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="评审结果" width="120">
          <template #default="{ row }">
            <el-tag :type="getReviewResultTagType(row.reviewResult)" effect="light">
              {{ getReviewResultLabel(row.reviewResult) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="100">
          <template #default="{ row }">
            {{ row.score ?? '--' }}
          </template>
        </el-table-column>
        <el-table-column label="评审教师" width="140">
          <template #default="{ row }">
            {{ getTeacherLabel(row.reviewerId) }}
          </template>
        </el-table-column>
        <el-table-column label="评审时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.reviewTime) }}
          </template>
        </el-table-column>
        <el-table-column label="评审意见" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.comment || '暂无内容' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="canManageRow(row)"
              type="primary"
              link
              :icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="canManageRow(row)"
              type="danger"
              link
              :icon="Delete"
              :loading="deletingId === row.id"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
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

    <ReviewRecordFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :form-data="reviewForm"
      :submission-options="submissionOptions"
      :submitting="submitting"
      @submit="handleSubmit"
    />

    <el-dialog v-model="detailVisible" title="评审记录详情" width="760px" destroy-on-close>
      <div v-loading="detailLoading" class="detail-panel detail-loading-panel">
        <template v-if="detailData">
          <div class="detail-header">
            <div>
              <h2>{{ getSubmissionLabel(detailData.submissionId) }}</h2>
              <p>评审教师：{{ getTeacherLabel(detailData.reviewerId) }}</p>
            </div>
            <div class="detail-tags">
              <el-tag :type="getReviewResultTagType(detailData.reviewResult)" effect="light">
                {{ getReviewResultLabel(detailData.reviewResult) }}
              </el-tag>
              <el-tag type="primary" effect="light">
                分数：{{ detailData.score ?? '--' }}
              </el-tag>
            </div>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="阶段提交">
              {{ getSubmissionLabel(detailData.submissionId) }}
            </el-descriptions-item>
            <el-descriptions-item label="评审教师">
              {{ getTeacherLabel(detailData.reviewerId) }}
            </el-descriptions-item>
            <el-descriptions-item label="评审时间">
              {{ formatDateTime(detailData.reviewTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(detailData.createTime) }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="detail-comment">
            {{ detailData.comment || '暂无评审意见' }}
          </div>
        </template>
        <el-skeleton v-else :rows="6" animated />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.review-record-page {
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
  justify-content: flex-end;
  margin-top: 20px;
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

.detail-comment {
  min-height: 140px;
  padding: 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f7fbff 0%, #edf5ff 100%);
  color: #344256;
  line-height: 1.9;
  white-space: pre-wrap;
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
