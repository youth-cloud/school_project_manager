<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import StageTaskFormDialog from '@/views/stage-task/components/StageTaskFormDialog.vue'
import { useUserOptions } from '@/composables/useUserOptions'
import { getTrainingBatchListApi, type TrainingBatchOption } from '@/api/trainingBatch'
import { getTeacherDisplayName } from '@/utils/userDisplay'
import {
  createStageTaskApi,
  deleteStageTaskApi,
  getStageTaskDetailApi,
  getStageTaskPageApi,
  updateStageTaskApi,
  type StageTaskFormData,
  type StageTaskItem,
} from '@/api/stageTask'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const tableData = ref<StageTaskItem[]>([])
const total = ref(0)
const batchOptions = ref<TrainingBatchOption[]>([])
const { teacherOptionMap, loadTeacherOptions } = useUserOptions()
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<StageTaskItem | null>(null)
const deletingId = ref('')

const stageTaskForm = reactive<StageTaskFormData>({
  batchId: '',
  taskTitle: '',
  taskDescription: '',
  stageNo: 1,
  deadline: '',
  needReport: 1,
  needSourceCode: 1,
  needPdf: 1,
  needScreenshot: 0,
  needDemoUrl: 0,
  status: 1,
})

const queryForm = reactive({
  batchId: '',
  taskTitle: '',
  stageNo: undefined as number | undefined,
  status: undefined as number | undefined,
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const canEditStageTask = computed(() => {
  const roles = userStore.userInfo?.roles || []
  return roles.includes('TEACHER')
})

const canDeleteStageTask = computed(() => {
  const roles = userStore.userInfo?.roles || []
  return roles.includes('ADMIN') || roles.includes('TEACHER')
})

const roleHint = computed(() => {
  if (canEditStageTask.value) return '教师可发布和编辑'
  if (canDeleteStageTask.value) return '管理员可删除'
  return '当前账号只读'
})

const fetchStageTaskList = async () => {
  loading.value = true
  try {
    const res = await getStageTaskPageApi({
      current: pagination.current,
      size: pagination.size,
      batchId: queryForm.batchId || undefined,
      taskTitle: queryForm.taskTitle || undefined,
      stageNo: queryForm.stageNo,
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
  fetchStageTaskList()
}

const handleReset = () => {
  queryForm.batchId = ''
  queryForm.taskTitle = ''
  queryForm.stageNo = undefined
  queryForm.status = undefined
  pagination.current = 1
  fetchStageTaskList()
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchStageTaskList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchStageTaskList()
}

const formatDateTime = (value: string | null) => {
  if (!value) return '--'
  return value.replace('T', ' ')
}

const getStatusLabel = (status: number) => {
  return status === 1 ? '启用' : '停用'
}

const getStatusTagType = (status: number) => {
  return status === 1 ? 'success' : 'info'
}

const getNeedLabel = (value: number) => {
  return value === 1 ? '需要' : '不需要'
}

const getNeedTagType = (value: number) => {
  return value === 1 ? 'primary' : 'info'
}

const getBatchLabel = (batchId: string) => {
  return batchOptions.value.find((item) => item.id === batchId)?.batchName || batchId
}

const getTeacherLabel = (teacherId: string) => {
  return getTeacherDisplayName(teacherId, teacherOptionMap.value)
}

const loadBatchOptions = async () => {
  const [batchRes] = await Promise.all([getTrainingBatchListApi(), loadTeacherOptions()])
  batchOptions.value = batchRes.data
}

const resetStageTaskForm = () => {
  stageTaskForm.id = undefined
  stageTaskForm.batchId = ''
  stageTaskForm.taskTitle = ''
  stageTaskForm.taskDescription = ''
  stageTaskForm.stageNo = 1
  stageTaskForm.deadline = ''
  stageTaskForm.needReport = 1
  stageTaskForm.needSourceCode = 1
  stageTaskForm.needPdf = 1
  stageTaskForm.needScreenshot = 0
  stageTaskForm.needDemoUrl = 0
  stageTaskForm.status = 1
}

const handleCreate = () => {
  dialogMode.value = 'create'
  resetStageTaskForm()
  dialogVisible.value = true
}

const handleEdit = (row: StageTaskItem) => {
  dialogMode.value = 'edit'
  stageTaskForm.id = row.id
  stageTaskForm.batchId = row.batchId
  stageTaskForm.taskTitle = row.taskTitle
  stageTaskForm.taskDescription = row.taskDescription || ''
  stageTaskForm.stageNo = row.stageNo
  stageTaskForm.deadline = row.deadline || ''
  stageTaskForm.needReport = row.needReport
  stageTaskForm.needSourceCode = row.needSourceCode
  stageTaskForm.needPdf = row.needPdf
  stageTaskForm.needScreenshot = row.needScreenshot
  stageTaskForm.needDemoUrl = row.needDemoUrl
  stageTaskForm.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await createStageTaskApi(stageTaskForm)
      ElMessage.success('阶段任务新增成功')
    } else {
      await updateStageTaskApi(stageTaskForm)
      ElMessage.success('阶段任务修改成功')
    }
    dialogVisible.value = false
    fetchStageTaskList()
  } finally {
    submitting.value = false
  }
}

const handleView = async (row: StageTaskItem) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getStageTaskDetailApi(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const handleDelete = async (row: StageTaskItem) => {
  await ElMessageBox.confirm(`确认删除阶段任务“${row.taskTitle}”吗？删除后不可恢复。`, '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
  })

  deletingId.value = row.id
  try {
    await deleteStageTaskApi(row.id)
    ElMessage.success('阶段任务删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    fetchStageTaskList()
  } finally {
    deletingId.value = ''
  }
}

onMounted(async () => {
  await loadBatchOptions()
  fetchStageTaskList()
})
</script>

<template>
  <div class="stage-task-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div class="hero-main">
          <div class="hero-badge">Stage Task Management</div>
          <h1>阶段任务管理</h1>
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
        <el-form-item label="所属批次">
          <el-select v-model="queryForm.batchId" placeholder="请选择实训批次" clearable filterable style="width: 240px">
            <el-option
              v-for="item in batchOptions"
              :key="item.id"
              :label="getBatchLabel(item.id)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="任务标题">
          <el-input
            v-model="queryForm.taskTitle"
            placeholder="请输入任务标题"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="阶段序号">
          <el-input-number v-model="queryForm.stageNo" :min="1" placeholder="阶段序号" controls-position="right" />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 160px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button v-if="canEditStageTask" type="success" :icon="Plus" @click="handleCreate">新建阶段任务</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div>
            <div class="table-title">阶段任务列表</div>
          </div>
          <div class="header-tags">
            <el-tag type="info">共 {{ total }} 条</el-tag>
            <el-tag v-if="canEditStageTask" type="success">教师可发布和编辑</el-tag>
            <el-tag v-else-if="canDeleteStageTask" type="primary">管理员可删除</el-tag>
            <el-tag v-else type="warning">当前账号只读查看</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe class="stage-task-table">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="任务标题" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">{{ row.taskTitle }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="stageNo" label="阶段序号" width="100" />
        <el-table-column label="截止时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.deadline) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="任务要求" min-width="340">
          <template #default="{ row }">
            <div class="requirement-tags">
              <el-tag size="small" :type="getNeedTagType(row.needReport)" effect="light">周报：{{ getNeedLabel(row.needReport) }}</el-tag>
              <el-tag size="small" :type="getNeedTagType(row.needSourceCode)" effect="light">源码：{{ getNeedLabel(row.needSourceCode) }}</el-tag>
              <el-tag size="small" :type="getNeedTagType(row.needPdf)" effect="light">PDF：{{ getNeedLabel(row.needPdf) }}</el-tag>
              <el-tag size="small" :type="getNeedTagType(row.needScreenshot)" effect="light">截图：{{ getNeedLabel(row.needScreenshot) }}</el-tag>
              <el-tag size="small" :type="getNeedTagType(row.needDemoUrl)" effect="light">演示链接：{{ getNeedLabel(row.needDemoUrl) }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="发布教师" width="140">
          <template #default="{ row }">
            {{ getTeacherLabel(row.teacherId) }}
          </template>
        </el-table-column>
        <el-table-column label="所属批次" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getBatchLabel(row.batchId) }}
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
            <el-button v-if="canEditStageTask" type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="canDeleteStageTask"
              type="danger"
              link
              :icon="Delete"
              :loading="deletingId === row.id"
              @click="handleDelete(row)"
            >删除</el-button>
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

    <StageTaskFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :form-data="stageTaskForm"
      :batch-options="batchOptions"
      :submitting="submitting"
      @submit="handleSubmit"
    />

    <el-dialog v-model="detailVisible" title="阶段任务详情" width="760px" destroy-on-close>
      <div v-if="detailData" v-loading="detailLoading" class="stage-task-detail">
        <div class="detail-header">
          <div>
            <h2>{{ detailData.taskTitle }}</h2>
            <p>所属批次：{{ getBatchLabel(detailData.batchId) }}</p>
          </div>
          <div class="detail-tags">
            <el-tag :type="getStatusTagType(detailData.status)" effect="light">{{ getStatusLabel(detailData.status) }}</el-tag>
            <el-tag type="primary" effect="light">第{{ detailData.stageNo }} 阶段</el-tag>
          </div>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="发布教师">{{ getTeacherLabel(detailData.teacherId) }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ formatDateTime(detailData.deadline) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDateTime(detailData.updateTime) }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-requirements">
          <el-tag :type="getNeedTagType(detailData.needReport)" effect="light">周报：{{ getNeedLabel(detailData.needReport) }}</el-tag>
          <el-tag :type="getNeedTagType(detailData.needSourceCode)" effect="light">源码：{{ getNeedLabel(detailData.needSourceCode) }}</el-tag>
          <el-tag :type="getNeedTagType(detailData.needPdf)" effect="light">PDF：{{ getNeedLabel(detailData.needPdf) }}</el-tag>
          <el-tag :type="getNeedTagType(detailData.needScreenshot)" effect="light">截图：{{ getNeedLabel(detailData.needScreenshot) }}</el-tag>
          <el-tag :type="getNeedTagType(detailData.needDemoUrl)" effect="light">演示链接：{{ getNeedLabel(detailData.needDemoUrl) }}</el-tag>
        </div>

        <div class="detail-content">
          {{ detailData.taskDescription || '暂无任务说明' }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.stage-task-page {
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
    linear-gradient(135deg, #eef6ff 0%, #f8fbff 58%, #ffffff 100%);
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

.stage-task-table {
  margin-top: 4px;
}

.requirement-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.stage-task-detail {
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

.detail-tags,
.detail-requirements {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
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

@media (max-width: 1024px) {
  .hero-content {
    flex-direction: column;
  }

  .hero-side {
    min-width: 0;
  }
}
</style>
