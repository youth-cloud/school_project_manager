<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { useUserOptions } from '@/composables/useUserOptions'
import {
  createProjectTopicApi,
  deleteProjectTopicApi,
  getProjectTopicDetailApi,
  getProjectTopicPageApi,
  updateProjectTopicApi,
  type ProjectTopicFormData,
  type ProjectTopicItem,
} from '@/api/projectTopic'
import { getTrainingBatchListApi, type TrainingBatchOption } from '@/api/trainingBatch'
import { useUserStore } from '@/stores/user'
import { getTeacherDisplayName } from '@/utils/userDisplay'
import ProjectTopicFormDialog from '@/views/project-topic/components/ProjectTopicFormDialog.vue'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<ProjectTopicItem[]>([])
const total = ref(0)

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<ProjectTopicItem | null>(null)

const deletingId = ref('')
const batchOptions = ref<TrainingBatchOption[]>([])
const { teacherOptions, teacherOptionMap, loadTeacherOptions } = useUserOptions()

const queryForm = reactive({
  topicName: '',
  batchId: '',
  teacherId: '',
  difficultyLevel: undefined as number | undefined,
  status: undefined as number | undefined,
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const topicForm = reactive<ProjectTopicFormData>({
  batchId: '',
  topicName: '',
  topicDescription: '',
  difficultyLevel: null,
  techRequirements: '',
  maxMembers: null,
  status: 1,
})

const roles = computed(() => userStore.userInfo?.roles || [])
const currentUserId = computed(() => String(userStore.userInfo?.id || ''))
const isTeacher = computed(() => roles.value.includes('TEACHER'))
const isAdmin = computed(() => roles.value.includes('ADMIN'))
const isStudent = computed(() => roles.value.includes('STUDENT'))

const roleHint = computed(() => {
  if (isAdmin.value) return '管理员可查看和删除全部课题'
  if (isTeacher.value) return '教师可发布、修改和删除自己创建的课题'
  return '学生可查看启用中的课题并发起选题申请'
})

const difficultyMap: Record<number, string> = {
  1: '简单',
  2: '中等',
  3: '较难',
  4: '困难',
}

const getDifficultyLabel = (value: number | null) => {
  if (!value) return '--'
  return difficultyMap[value] || String(value)
}

const getDifficultyTagType = (value: number | null) => {
  if (value === 1) return 'success'
  if (value === 2) return 'primary'
  if (value === 3) return 'warning'
  if (value === 4) return 'danger'
  return 'info'
}

const getStatusLabel = (status: number) => (status === 1 ? '启用' : '停用')
const getStatusTagType = (status: number) => (status === 1 ? 'success' : 'info')

const formatDateTime = (value: string | null) => {
  if (!value) return '--'
  return value.replace('T', ' ')
}

const textPreview = (value: string | null, limit = 32) => {
  if (!value) return '暂无内容'
  return value.length > limit ? `${value.slice(0, limit)}...` : value
}

const getBatchLabel = (id: string) => {
  const item = batchOptions.value.find((option) => option.id === id)
  if (!item) return id
  return item.termName ? `${item.batchName} - ${item.termName}` : item.batchName
}

const getTeacherLabel = (id: string) => {
  return getTeacherDisplayName(id, teacherOptionMap.value)
}

const canEditRow = (row: ProjectTopicItem) => isTeacher.value && row.teacherId === currentUserId.value
const canDeleteRow = (row: ProjectTopicItem) => isAdmin.value || (isTeacher.value && row.teacherId === currentUserId.value)

const fetchBatchOptions = async () => {
  const res = await getTrainingBatchListApi()
  batchOptions.value = res.data
}

const fetchTeacherOptions = async () => {
  await loadTeacherOptions()
}

const fetchProjectTopicList = async () => {
  loading.value = true
  try {
    const res = await getProjectTopicPageApi({
      current: pagination.current,
      size: pagination.size,
      topicName: queryForm.topicName || undefined,
      batchId: queryForm.batchId || undefined,
      teacherId: queryForm.teacherId || undefined,
      difficultyLevel: queryForm.difficultyLevel,
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
  fetchProjectTopicList()
}

const handleReset = () => {
  queryForm.topicName = ''
  queryForm.batchId = ''
  queryForm.teacherId = ''
  queryForm.difficultyLevel = undefined
  queryForm.status = undefined
  pagination.current = 1
  fetchProjectTopicList()
}

const resetTopicForm = () => {
  topicForm.id = undefined
  topicForm.batchId = ''
  topicForm.topicName = ''
  topicForm.topicDescription = ''
  topicForm.difficultyLevel = null
  topicForm.techRequirements = ''
  topicForm.maxMembers = null
  topicForm.status = 1
}

const handleCreate = async () => {
  if (!batchOptions.value.length) {
    await fetchBatchOptions()
  }
  dialogMode.value = 'create'
  resetTopicForm()
  dialogVisible.value = true
}

const handleEdit = async (row: ProjectTopicItem) => {
  if (!batchOptions.value.length) {
    await fetchBatchOptions()
  }
  dialogMode.value = 'edit'
  topicForm.id = row.id
  topicForm.batchId = row.batchId
  topicForm.topicName = row.topicName
  topicForm.topicDescription = row.topicDescription || ''
  topicForm.difficultyLevel = row.difficultyLevel
  topicForm.techRequirements = row.techRequirements || ''
  topicForm.maxMembers = row.maxMembers
  topicForm.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!topicForm.batchId) {
    ElMessage.warning('请先选择所属批次')
    return
  }

  if (!topicForm.topicName.trim()) {
    ElMessage.warning('请先填写课题名称')
    return
  }

  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await createProjectTopicApi(topicForm)
      ElMessage.success('课题创建成功')
    } else {
      await updateProjectTopicApi(topicForm)
      ElMessage.success('课题修改成功')
    }
    dialogVisible.value = false
    fetchProjectTopicList()
  } finally {
    submitting.value = false
  }
}

const handleView = async (row: ProjectTopicItem) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await getProjectTopicDetailApi(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const handleDelete = async (row: ProjectTopicItem) => {
  await ElMessageBox.confirm('确认删除这条课题记录吗？', '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
  })

  deletingId.value = row.id
  try {
    await deleteProjectTopicApi(row.id)
    ElMessage.success('课题删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    fetchProjectTopicList()
  } finally {
    deletingId.value = ''
  }
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchProjectTopicList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchProjectTopicList()
}

onMounted(() => {
  Promise.allSettled([
    fetchBatchOptions(),
    fetchTeacherOptions(),
    fetchProjectTopicList(),
  ])
})
</script>

<template>
  <div class="project-topic-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div class="hero-main">
          <div class="hero-badge">Project Topic Management</div>
          <h1>课题管理</h1>
        </div>
        <div class="hero-side">
          <div class="hero-side-label">当前视角</div>
          <div class="hero-side-value">{{ roleHint }}</div>
          <div class="hero-side-meta">当前列表共 {{ total }} 条课题记录</div>
        </div>
      </div>
    </el-card>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="课题名称">
          <el-input
            v-model="queryForm.topicName"
            clearable
            placeholder="请输入课题名称"
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

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

        <el-form-item label="教师">
          <el-select
            v-model="queryForm.teacherId"
            clearable
            filterable
            placeholder="请选择教师"
            style="width: 220px"
          >
            <el-option
              v-for="item in teacherOptions"
              :key="item.id"
              :label="getTeacherLabel(item.id)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="难度">
          <el-select
            v-model="queryForm.difficultyLevel"
            clearable
            placeholder="请选择难度"
            style="width: 160px"
          >
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="较难" :value="3" />
            <el-option label="困难" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="queryForm.status"
            clearable
            placeholder="请选择状态"
            style="width: 160px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button v-if="isTeacher" type="success" :icon="Plus" @click="handleCreate">新建课题</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div>
            <div class="table-title">课题列表</div>
          </div>
          <div class="header-tags">
            <el-tag type="info">共 {{ total }} 条</el-tag>
            <el-tag type="primary">{{ roleHint }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe class="project-topic-table">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="课题名称" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              {{ row.topicName }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="所属批次" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getBatchLabel(row.batchId) }}
          </template>
        </el-table-column>
        <el-table-column label="教师" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getTeacherLabel(row.teacherId) }}
          </template>
        </el-table-column>
        <el-table-column label="难度" width="100">
          <template #default="{ row }">
            <el-tag :type="getDifficultyTagType(row.difficultyLevel)" effect="light">
              {{ getDifficultyLabel(row.difficultyLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxMembers" label="最大人数" width="100" />
        <el-table-column prop="selectedCount" label="已选人数" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="课题简介" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            {{ textPreview(row.topicDescription) }}
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
            <el-button v-if="canEditRow(row)" type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="canDeleteRow(row)"
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

    <ProjectTopicFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :form-data="topicForm"
      :batch-options="batchOptions"
      :submitting="submitting"
      @submit="handleSubmit"
    />

    <el-dialog v-model="detailVisible" title="课题详情" width="860px" destroy-on-close>
      <div v-loading="detailLoading" class="detail-panel detail-loading-panel">
        <template v-if="detailData">
        <div class="detail-header">
          <div>
            <h2>{{ detailData.topicName }}</h2>
            <p>{{ getBatchLabel(detailData.batchId) }} - 教师：{{ getTeacherLabel(detailData.teacherId) }}</p>
          </div>
          <div class="detail-tags">
            <el-tag :type="getDifficultyTagType(detailData.difficultyLevel)" effect="light">
              {{ getDifficultyLabel(detailData.difficultyLevel) }}
            </el-tag>
            <el-tag :type="getStatusTagType(detailData.status)" effect="light">
              {{ getStatusLabel(detailData.status) }}
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="所属批次">
            {{ getBatchLabel(detailData.batchId) }}
          </el-descriptions-item>
          <el-descriptions-item label="教师">
            {{ getTeacherLabel(detailData.teacherId) }}
          </el-descriptions-item>
          <el-descriptions-item label="最大人数">
            {{ detailData.maxMembers ?? '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="已选人数">
            {{ detailData.selectedCount ?? '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(detailData.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(detailData.updateTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-block">
          <div class="detail-block-title">课题简介</div>
          <div class="detail-block-content">{{ detailData.topicDescription || '暂无简介' }}</div>
        </div>

        <div class="detail-block">
          <div class="detail-block-title">技术要求</div>
          <div class="detail-block-content">{{ detailData.techRequirements || '暂无技术要求' }}</div>
        </div>
        </template>
        <el-skeleton v-else :rows="7" animated />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.project-topic-page {
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
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 1024px) {
  .hero-content {
    flex-direction: column;
  }

  .hero-side {
    min-width: 0;
  }
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
