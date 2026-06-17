<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { useUserOptions } from '@/composables/useUserOptions'
import { getProjectGroupListApi, type ProjectGroupOption } from '@/api/projectGroup'
import { getTrainingBatchListApi, type TrainingBatchOption } from '@/api/trainingBatch'
import { getStudentDisplayName } from '@/utils/userDisplay'
import {
  createWeeklyReportApi,
  deleteWeeklyReportApi,
  getWeeklyReportDetailApi,
  getWeeklyReportPageApi,
  updateWeeklyReportApi,
  type WeeklyReportFormData,
  type WeeklyReportItem,
} from '@/api/weeklyReport'
import { useUserStore } from '@/stores/user'
import WeeklyReportFormDialog from '@/views/weekly-report/components/WeeklyReportFormDialog.vue'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<WeeklyReportItem[]>([])
const total = ref(0)

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<WeeklyReportItem | null>(null)

const deletingId = ref('')
const batchOptions = ref<TrainingBatchOption[]>([])
const groupOptions = ref<ProjectGroupOption[]>([])
const { studentOptions, userOptionMap, loadRequestedOptions } = useUserOptions()

const queryForm = reactive({
  batchId: '',
  groupId: '',
  studentId: '',
  weekIndex: undefined as number | undefined,
  status: undefined as number | undefined,
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const weeklyReportForm = reactive<WeeklyReportFormData>({
  batchId: '',
  groupId: '',
  weekIndex: 1,
  completedWork: '',
  problemDesc: '',
  nextPlan: '',
  status: 1,
})

const roles = computed(() => userStore.userInfo?.roles || [])
const currentUserId = computed(() => String(userStore.userInfo?.id || ''))
const isStudent = computed(() => roles.value.includes('STUDENT'))
const isAdmin = computed(() => roles.value.includes('ADMIN'))
const isTeacher = computed(() => roles.value.includes('TEACHER'))

const roleHint = computed(() => {
  if (isAdmin.value) return '管理员可查看全部周报'
  if (isTeacher.value) return '教师可查看自己指导项目组的周报'
  return '学生可查看同项目组周报，并维护自己的周报'
})

const filteredGroupOptions = computed(() => {
  if (!queryForm.batchId) return groupOptions.value
  return groupOptions.value.filter((item) => item.batchId === queryForm.batchId)
})

const canManageRow = (row: WeeklyReportItem) => isStudent.value && row.studentId === currentUserId.value

const getBatchLabel = (id: string) => {
  const item = batchOptions.value.find((option) => option.id === id)
  if (!item) return id
  return item.termName ? `${item.batchName} - ${item.termName}` : item.batchName
}

const getGroupLabel = (id: string) => {
  const item = groupOptions.value.find((option) => option.id === id)
  if (!item) return id
  return item.projectName ? `${item.groupName} - ${item.projectName}` : item.groupName
}

const getStudentLabel = (id: string) => {
  return getStudentDisplayName(id, userOptionMap.value)
}

const formatDateTime = (value: string | null) => {
  if (!value) return '--'
  return value.replace('T', ' ')
}

const getStatusLabel = (status: number) => (status === 1 ? '已提交' : '草稿/停用')
const getStatusTagType = (status: number) => (status === 1 ? 'success' : 'info')

const textPreview = (value: string | null, limit = 32) => {
  if (!value) return '暂无内容'
  return value.length > limit ? `${value.slice(0, limit)}...` : value
}

const fetchOptions = async () => {
  const [batchRes, groupRes] = await Promise.all([
    getTrainingBatchListApi(),
    getProjectGroupListApi(),
    loadRequestedOptions(['student', 'user']),
  ])
  batchOptions.value = batchRes.data
  groupOptions.value = groupRes.data
}

const fetchWeeklyReportList = async () => {
  loading.value = true
  try {
    const res = await getWeeklyReportPageApi({
      current: pagination.current,
      size: pagination.size,
      batchId: queryForm.batchId || undefined,
      groupId: queryForm.groupId || undefined,
      studentId: queryForm.studentId || undefined,
      weekIndex: queryForm.weekIndex,
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
  fetchWeeklyReportList()
}

const handleReset = () => {
  queryForm.batchId = ''
  queryForm.groupId = ''
  queryForm.studentId = ''
  queryForm.weekIndex = undefined
  queryForm.status = undefined
  pagination.current = 1
  fetchWeeklyReportList()
}

const resetWeeklyReportForm = () => {
  weeklyReportForm.id = undefined
  weeklyReportForm.batchId = ''
  weeklyReportForm.groupId = ''
  weeklyReportForm.weekIndex = 1
  weeklyReportForm.completedWork = ''
  weeklyReportForm.problemDesc = ''
  weeklyReportForm.nextPlan = ''
  weeklyReportForm.status = 1
}

const handleCreate = async () => {
  if (!batchOptions.value.length || !groupOptions.value.length) {
    await fetchOptions()
  }
  dialogMode.value = 'create'
  resetWeeklyReportForm()
  dialogVisible.value = true
}

const handleEdit = async (row: WeeklyReportItem) => {
  if (!batchOptions.value.length || !groupOptions.value.length) {
    await fetchOptions()
  }
  dialogMode.value = 'edit'
  weeklyReportForm.id = row.id
  weeklyReportForm.batchId = row.batchId
  weeklyReportForm.groupId = row.groupId
  weeklyReportForm.weekIndex = row.weekIndex
  weeklyReportForm.completedWork = row.completedWork || ''
  weeklyReportForm.problemDesc = row.problemDesc || ''
  weeklyReportForm.nextPlan = row.nextPlan || ''
  weeklyReportForm.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!weeklyReportForm.batchId) {
    ElMessage.warning('请先选择所属批次')
    return
  }

  if (!weeklyReportForm.groupId) {
    ElMessage.warning('请先选择项目组')
    return
  }

  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await createWeeklyReportApi(weeklyReportForm)
      ElMessage.success('周报创建成功')
    } else {
      await updateWeeklyReportApi(weeklyReportForm)
      ElMessage.success('周报修改成功')
    }
    dialogVisible.value = false
    fetchWeeklyReportList()
  } finally {
    submitting.value = false
  }
}

const handleView = async (row: WeeklyReportItem) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getWeeklyReportDetailApi(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const handleDelete = async (row: WeeklyReportItem) => {
  await ElMessageBox.confirm('确认删除这条周报记录吗？', '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
  })

  deletingId.value = row.id
  try {
    await deleteWeeklyReportApi(row.id)
    ElMessage.success('周报删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    fetchWeeklyReportList()
  } finally {
    deletingId.value = ''
  }
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchWeeklyReportList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchWeeklyReportList()
}

onMounted(async () => {
  await fetchOptions()
  await fetchWeeklyReportList()
})
</script>

<template>
  <div class="weekly-report-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div class="hero-main">
          <div class="hero-badge">Weekly Report Management</div>
          <h1>周报管理</h1>
        </div>
        <div class="hero-side">
          <div class="hero-side-label">当前视角</div>
          <div class="hero-side-value">{{ roleHint }}</div>
          <div class="hero-side-meta">当前列表共 {{ total }} 条周报记录</div>
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

        <el-form-item label="项目组">
          <el-select
            v-model="queryForm.groupId"
            clearable
            filterable
            placeholder="请选择项目组"
            style="width: 250px"
          >
            <el-option
              v-for="item in filteredGroupOptions"
              :key="item.id"
              :label="getGroupLabel(item.id)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="学生">
          <el-select
            v-model="queryForm.studentId"
            clearable
            filterable
            placeholder="请选择学生"
            style="width: 240px"
          >
            <el-option
              v-for="item in studentOptions"
              :key="item.id"
              :label="`${item.realName}（${item.username}）`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="周次">
          <el-input-number
            v-model="queryForm.weekIndex"
            :min="1"
            controls-position="right"
            placeholder="周次"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" clearable placeholder="请选择状态" style="width: 160px">
            <el-option label="已提交" :value="1" />
            <el-option label="草稿/停用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button v-if="isStudent" type="success" :icon="Plus" @click="handleCreate">新建周报</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div>
            <div class="table-title">周报列表</div>
          </div>
          <div class="header-tags">
            <el-tag type="info">共 {{ total }} 条</el-tag>
            <el-tag type="primary">{{ roleHint }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe class="weekly-report-table">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="所属批次" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              {{ getBatchLabel(row.batchId) }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="项目组" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getGroupLabel(row.groupId) }}
          </template>
        </el-table-column>
        <el-table-column label="学生" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getStudentLabel(row.studentId) }}
          </template>
        </el-table-column>
        <el-table-column prop="weekIndex" label="周次" width="80" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="完成情况" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ textPreview(row.completedWork) }}
          </template>
        </el-table-column>
        <el-table-column label="下周计划" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ textPreview(row.nextPlan) }}
          </template>
        </el-table-column>
        <el-table-column label="提交时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleView(row)">查看</el-button>
            <el-button v-if="canManageRow(row)" type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
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

    <WeeklyReportFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :form-data="weeklyReportForm"
      :batch-options="batchOptions"
      :group-options="groupOptions"
      :submitting="submitting"
      @submit="handleSubmit"
    />

    <el-dialog v-model="detailVisible" title="周报详情" width="820px" destroy-on-close>
      <div v-if="detailData" v-loading="detailLoading" class="detail-panel">
        <div class="detail-header">
          <div>
            <h2>{{ getBatchLabel(detailData.batchId) }} - 第{{ detailData.weekIndex }} 周</h2>
            <p>学生：{{ getStudentLabel(detailData.studentId) }} - 项目组：{{ getGroupLabel(detailData.groupId) }}</p>
          </div>
          <div class="detail-tags">
            <el-tag :type="getStatusTagType(detailData.status)" effect="light">
              {{ getStatusLabel(detailData.status) }}
            </el-tag>
            <el-tag type="primary" effect="light">
              分数：{{ detailData.score ?? '--' }}
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="所属批次">
            {{ getBatchLabel(detailData.batchId) }}
          </el-descriptions-item>
          <el-descriptions-item label="项目组">
            {{ getGroupLabel(detailData.groupId) }}
          </el-descriptions-item>
          <el-descriptions-item label="学生">
            {{ getStudentLabel(detailData.studentId) }}
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ formatDateTime(detailData.submitTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="教师评语">
            {{ detailData.teacherComment || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="评审时间">
            {{ formatDateTime(detailData.reviewTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-block">
          <div class="detail-block-title">完成情况</div>
          <div class="detail-block-content">{{ detailData.completedWork || '暂无内容' }}</div>
        </div>

        <div class="detail-block">
          <div class="detail-block-title">存在问题</div>
          <div class="detail-block-content">{{ detailData.problemDesc || '暂无内容' }}</div>
        </div>

        <div class="detail-block">
          <div class="detail-block-title">下周计划</div>
          <div class="detail-block-content">{{ detailData.nextPlan || '暂无内容' }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.weekly-report-page {
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

@media (max-width: 1024px) {
  .hero-content {
    flex-direction: column;
  }

  .hero-side {
    min-width: 0;
  }
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
