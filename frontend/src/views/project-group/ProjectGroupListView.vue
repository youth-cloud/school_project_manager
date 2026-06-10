<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { useUserOptions } from '@/composables/useUserOptions'
import {
  createProjectGroupApi,
  deleteProjectGroupApi,
  getProjectGroupDetailApi,
  getProjectGroupPageApi,
  updateProjectGroupApi,
  type ProjectGroupFormData,
  type ProjectGroupItem,
} from '@/api/projectGroup'
import { getProjectTopicListApi, type ProjectTopicOption } from '@/api/projectTopic'
import { getTrainingBatchListApi, type TrainingBatchOption } from '@/api/trainingBatch'
import { useUserStore } from '@/stores/user'
import { getStudentDisplayName, getTeacherDisplayName } from '@/utils/userDisplay'
import ProjectGroupFormDialog from '@/views/project-group/components/ProjectGroupFormDialog.vue'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<ProjectGroupItem[]>([])
const total = ref(0)

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<ProjectGroupItem | null>(null)

const deletingId = ref('')
const batchOptions = ref<TrainingBatchOption[]>([])
const topicOptions = ref<ProjectTopicOption[]>([])
const { studentOptions, teacherOptions, teacherOptionMap, userOptionMap, loadRequestedOptions } = useUserOptions()

const queryForm = reactive({
  groupName: '',
  batchId: '',
  topicId: '',
  leaderId: '',
  teacherId: '',
  status: undefined as number | undefined,
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const groupForm = reactive<ProjectGroupFormData>({
  batchId: '',
  topicId: '',
  groupName: '',
  leaderId: '',
  projectName: '',
  projectDescription: '',
  repoUrl: '',
  deployUrl: '',
  status: 1,
})

const roles = computed(() => userStore.userInfo?.roles || [])
const currentUserId = computed(() => String(userStore.userInfo?.id || ''))
const isTeacher = computed(() => roles.value.includes('TEACHER'))
const isAdmin = computed(() => roles.value.includes('ADMIN'))
const isStudent = computed(() => roles.value.includes('STUDENT'))

const roleHint = computed(() => {
  if (isAdmin.value) return '管理员可查看和删除全部正式项目组'
  if (isTeacher.value) return '教师可维护自己指导的正式项目组'
  return '学生只查看自己所在的正式项目组'
})

const filteredTopicOptions = computed(() => {
  if (!queryForm.batchId) return topicOptions.value
  return topicOptions.value.filter((item) => item.batchId === queryForm.batchId)
})

const getBatchLabel = (id: string) => {
  const item = batchOptions.value.find((option) => option.id === id)
  if (!item) return id
  return item.termName ? `${item.batchName} · ${item.termName}` : item.batchName
}

const getTopicLabel = (id: string) => {
  const item = topicOptions.value.find((option) => option.id === id)
  return item?.topicName || id
}

const getStudentLabel = (id: string) => {
  return getStudentDisplayName(id, userOptionMap.value)
}

const getTeacherLabel = (id: string) => {
  return getTeacherDisplayName(id, teacherOptionMap.value)
}

const getStatusLabel = (status: number) => (status === 1 ? '启用' : '停用')
const getStatusTagType = (status: number) => (status === 1 ? 'success' : 'info')

const formatDateTime = (value: string | null) => {
  if (!value) return '--'
  return value.replace('T', ' ')
}

const textPreview = (value: string | null, limit = 28) => {
  if (!value) return '暂无内容'
  return value.length > limit ? `${value.slice(0, limit)}...` : value
}

const canEditRow = (row: ProjectGroupItem) => isTeacher.value && row.teacherId === currentUserId.value
const canDeleteRow = (row: ProjectGroupItem) => isAdmin.value || (isTeacher.value && row.teacherId === currentUserId.value)
const canViewGroupId = (row: ProjectGroupItem | null) => {
  if (!row) return false
  return row.teacherId === currentUserId.value || row.leaderId === currentUserId.value
}

const handleCopyGroupId = async (groupId: string) => {
  try {
    await navigator.clipboard.writeText(groupId)
    ElMessage.success('项目组 ID 已复制')
  } catch {
    ElMessage.error('复制失败，请手动复制项目组 ID')
  }
}

const fetchOptions = async () => {
  const [batchRes, topicRes] = await Promise.all([
    getTrainingBatchListApi(),
    getProjectTopicListApi(),
    loadRequestedOptions(['student', 'teacher', 'user']),
  ])
  batchOptions.value = batchRes.data
  topicOptions.value = topicRes.data
}

const fetchProjectGroupList = async () => {
  loading.value = true
  try {
    const res = await getProjectGroupPageApi({
      current: pagination.current,
      size: pagination.size,
      groupName: queryForm.groupName || undefined,
      batchId: queryForm.batchId || undefined,
      topicId: queryForm.topicId || undefined,
      leaderId: queryForm.leaderId || undefined,
      teacherId: queryForm.teacherId || undefined,
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
  fetchProjectGroupList()
}

const handleReset = () => {
  queryForm.groupName = ''
  queryForm.batchId = ''
  queryForm.topicId = ''
  queryForm.leaderId = ''
  queryForm.teacherId = ''
  queryForm.status = undefined
  pagination.current = 1
  fetchProjectGroupList()
}

const resetGroupForm = () => {
  groupForm.id = undefined
  groupForm.batchId = ''
  groupForm.topicId = ''
  groupForm.groupName = ''
  groupForm.leaderId = ''
  groupForm.projectName = ''
  groupForm.projectDescription = ''
  groupForm.repoUrl = ''
  groupForm.deployUrl = ''
  groupForm.status = 1
}

const handleCreate = async () => {
  if (!batchOptions.value.length || !topicOptions.value.length) {
    await fetchOptions()
  }
  dialogMode.value = 'create'
  resetGroupForm()
  dialogVisible.value = true
}

const handleEdit = async (row: ProjectGroupItem) => {
  if (!batchOptions.value.length || !topicOptions.value.length) {
    await fetchOptions()
  }
  dialogMode.value = 'edit'
  groupForm.id = row.id
  groupForm.batchId = row.batchId
  groupForm.topicId = row.topicId
  groupForm.groupName = row.groupName
  groupForm.leaderId = row.leaderId
  groupForm.projectName = row.projectName || ''
  groupForm.projectDescription = row.projectDescription || ''
  groupForm.repoUrl = row.repoUrl || ''
  groupForm.deployUrl = row.deployUrl || ''
  groupForm.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!groupForm.batchId) {
    ElMessage.warning('请先选择所属批次')
    return
  }

  if (!groupForm.topicId) {
    ElMessage.warning('请先选择目标课题')
    return
  }

  if (!groupForm.groupName.trim()) {
    ElMessage.warning('请先填写项目组名称')
    return
  }

  if (!groupForm.leaderId.trim()) {
    ElMessage.warning('请先选择组长')
    return
  }

  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await createProjectGroupApi(groupForm)
      ElMessage.success('项目组创建成功')
    } else {
      await updateProjectGroupApi(groupForm)
      ElMessage.success('项目组修改成功')
    }
    dialogVisible.value = false
    fetchProjectGroupList()
  } finally {
    submitting.value = false
  }
}

const handleView = async (row: ProjectGroupItem) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await getProjectGroupDetailApi(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const handleDelete = async (row: ProjectGroupItem) => {
  await ElMessageBox.confirm('确认删除这个正式项目组吗？', '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
  })

  deletingId.value = row.id
  try {
    await deleteProjectGroupApi(row.id)
    ElMessage.success('项目组删除成功')
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    fetchProjectGroupList()
  } finally {
    deletingId.value = ''
  }
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchProjectGroupList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchProjectGroupList()
}

onMounted(() => {
  Promise.allSettled([
    fetchOptions(),
    fetchProjectGroupList(),
  ])
})
</script>

<template>
  <div class="project-group-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div>
          <div class="hero-badge">Project Group Management</div>
          <h1>正式项目组</h1>
        </div>
      </div>
    </el-card>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="项目组名称">
          <el-input
            v-model="queryForm.groupName"
            clearable
            placeholder="请输入项目组名称"
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

        <el-form-item label="目标课题">
          <el-select
            v-model="queryForm.topicId"
            clearable
            filterable
            placeholder="请选择课题"
            style="width: 250px"
          >
            <el-option
              v-for="item in filteredTopicOptions"
              :key="item.id"
              :label="getTopicLabel(item.id)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="组长">
          <el-select v-model="queryForm.leaderId" clearable filterable placeholder="请选择组长" style="width: 220px">
            <el-option v-for="item in studentOptions" :key="item.id" :label="getStudentLabel(item.id)" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="教师">
          <el-select v-model="queryForm.teacherId" clearable filterable placeholder="请选择教师" style="width: 220px">
            <el-option v-for="item in teacherOptions" :key="item.id" :label="getTeacherLabel(item.id)" :value="item.id" />
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
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div>
            <div class="table-title">项目组列表</div>
          </div>
          <div class="header-tags">
            <el-tag type="info">共 {{ total }} 条</el-tag>
            <el-tag type="primary">{{ roleHint }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe class="project-group-table">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="项目组名称" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              {{ row.groupName }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="所属批次" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getBatchLabel(row.batchId) }}
          </template>
        </el-table-column>
        <el-table-column label="目标课题" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getTopicLabel(row.topicId) }}
          </template>
        </el-table-column>
        <el-table-column label="组长" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ getStudentLabel(row.leaderId) }}</template>
        </el-table-column>
        <el-table-column label="教师" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ getTeacherLabel(row.teacherId) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="项目名称" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ textPreview(row.projectName) }}
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
          :current-page="pagination.current"
          :page-size="pagination.size"
          :page-sizes="[10, 20, 30, 50]"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <ProjectGroupFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :form-data="groupForm"
      :batch-options="batchOptions"
      :topic-options="topicOptions"
      :student-options="studentOptions"
      :submitting="submitting"
      @submit="handleSubmit"
    />

    <el-dialog v-model="detailVisible" title="项目组详情" width="900px" destroy-on-close>
      <div v-loading="detailLoading" class="detail-panel detail-loading-panel">
        <template v-if="detailData">
        <div class="detail-header">
          <div>
            <h2>{{ detailData.groupName }}</h2>
            <p>{{ getBatchLabel(detailData.batchId) }} · {{ getTopicLabel(detailData.topicId) }}</p>
          </div>
          <div class="detail-tags">
            <el-tag :type="getStatusTagType(detailData.status)" effect="light">
              {{ getStatusLabel(detailData.status) }}
            </el-tag>
          </div>
        </div>

        <div v-if="canViewGroupId(detailData)" class="group-id-panel">
          <div>
            <div class="group-id-label">项目组 ID</div>
            <div class="group-id-value">{{ detailData.id }}</div>
          </div>
          <el-button type="primary" plain @click="handleCopyGroupId(detailData.id)">复制 ID</el-button>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item v-if="canViewGroupId(detailData)" label="项目组ID">
            {{ detailData.id }}
          </el-descriptions-item>
          <el-descriptions-item label="所属批次">
            {{ getBatchLabel(detailData.batchId) }}
          </el-descriptions-item>
          <el-descriptions-item label="目标课题">
            {{ getTopicLabel(detailData.topicId) }}
          </el-descriptions-item>
          <el-descriptions-item label="组长">
            {{ getStudentLabel(detailData.leaderId) }}
          </el-descriptions-item>
          <el-descriptions-item label="教师">
            {{ getTeacherLabel(detailData.teacherId) }}
          </el-descriptions-item>
          <el-descriptions-item label="仓库地址">
            {{ detailData.repoUrl || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="部署地址">
            {{ detailData.deployUrl || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(detailData.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(detailData.updateTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-block">
          <div class="detail-block-title">项目名称</div>
          <div class="detail-block-content">{{ detailData.projectName || '暂无项目名称' }}</div>
        </div>

        <div class="detail-block">
          <div class="detail-block-title">项目简介</div>
          <div class="detail-block-content">{{ detailData.projectDescription || '暂无项目简介' }}</div>
        </div>
        </template>
        <el-skeleton v-else :rows="7" animated />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.project-group-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero-card,
.filter-card,
.table-card {
  border-radius: 20px;
  border: none;
  box-shadow: 0 14px 32px rgb(57 118 201 / 8%);
}

.hero-card {
  background: linear-gradient(135deg, #eef7ff 0%, #f8fbff 58%, #ffffff 100%);
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
  min-height: 240px;
}

.group-id-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, #f7fbff 0%, #edf5ff 100%);
  border: 1px solid #dcebff;
}

.group-id-label {
  margin-bottom: 6px;
  color: #6b7a90;
  font-size: 13px;
}

.group-id-value {
  color: #1f2d3d;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.3px;
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
