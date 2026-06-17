<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { useUserOptions } from '@/composables/useUserOptions'
import { getProjectGroupListApi, type ProjectGroupOption } from '@/api/projectGroup'
import {
  createProjectGroupMemberApi,
  deleteProjectGroupMemberApi,
  getProjectGroupMemberDetailApi,
  getProjectGroupMemberPageApi,
  type ProjectGroupMemberCreateFormData,
  type ProjectGroupMemberItem,
} from '@/api/projectGroupMember'
import { useUserStore } from '@/stores/user'
import { getStudentDisplayName } from '@/utils/userDisplay'
import ProjectGroupMemberJoinDialog from '@/views/project-group-member/components/ProjectGroupMemberJoinDialog.vue'

const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<ProjectGroupMemberItem[]>([])
const total = ref(0)

const dialogVisible = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<ProjectGroupMemberItem | null>(null)

const deletingId = ref('')
const groupOptions = ref<ProjectGroupOption[]>([])
const { studentOptions, userOptionMap, loadStudentOptions, loadRequestedOptions } = useUserOptions()

const queryForm = reactive({
  groupId: '',
  userId: '',
  isLeader: undefined as number | undefined,
  status: undefined as number | undefined,
})

const pagination = reactive({
  current: 1,
  size: 10,
})

const memberForm = reactive<ProjectGroupMemberCreateFormData>({
  groupId: '',
  isLeader: 0,
  status: 1,
})

const roles = computed(() => userStore.userInfo?.roles || [])
const currentUserId = computed(() => String(userStore.userInfo?.id || ''))
const isAdmin = computed(() => roles.value.includes('ADMIN'))
const isTeacher = computed(() => roles.value.includes('TEACHER'))
const isStudent = computed(() => roles.value.includes('STUDENT'))

const roleHint = computed(() => {
  if (isAdmin.value) return '管理员可查看和删除全部项目组成员'
  if (isTeacher.value) return '教师可查看和删除自己指导项目下的成员记录'
  return '学生可加入正式项目组，也可退出自己的成员记录'
})

const getGroupLabel = (id: string) => {
  const item = groupOptions.value.find((option) => option.id === id)
  if (!item) return id
  const projectName = item.projectName?.trim()
  return projectName ? `${item.groupName} - ${projectName}` : item.groupName
}

const getLeaderLabel = (value: number) => (value === 1 ? '组长' : '成员')
const getLeaderTagType = (value: number) => (value === 1 ? 'warning' : 'info')
const getStatusLabel = (value: number) => (value === 1 ? '启用' : '停用')
const getStatusTagType = (value: number) => (value === 1 ? 'success' : 'info')

const getStudentLabel = (id: string) => {
  return getStudentDisplayName(id, userOptionMap.value)
}

const formatDateTime = (value: string | null) => {
  if (!value) return '--'
  return value.replace('T', ' ')
}

const canJoin = computed(() => isStudent.value)

const canDeleteRow = (row: ProjectGroupMemberItem) => {
  if (isAdmin.value || isTeacher.value) return true
  return isStudent.value && row.userId === currentUserId.value
}

const deleteActionText = (row: ProjectGroupMemberItem) => {
  if (isStudent.value && row.userId === currentUserId.value) return '退出'
  return '删除'
}

const fetchGroupOptions = async () => {
  const res = await getProjectGroupListApi()
  groupOptions.value = res.data
}

const fetchStudentOptions = async () => {
  await loadStudentOptions()
}

const fetchUserOptions = async () => {
  await loadRequestedOptions(['user'])
}

const fetchMemberList = async () => {
  loading.value = true
  try {
    const res = await getProjectGroupMemberPageApi({
      current: pagination.current,
      size: pagination.size,
      groupId: queryForm.groupId || undefined,
      userId: queryForm.userId || undefined,
      isLeader: queryForm.isLeader,
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
  fetchMemberList()
}

const handleReset = () => {
  queryForm.groupId = ''
  queryForm.userId = ''
  queryForm.isLeader = undefined
  queryForm.status = undefined
  pagination.current = 1
  fetchMemberList()
}

const resetMemberForm = () => {
  memberForm.groupId = ''
  memberForm.isLeader = 0
  memberForm.status = 1
}

const handleCreate = async () => {
  if (!groupOptions.value.length) {
    await fetchGroupOptions()
  }
  resetMemberForm()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  memberForm.groupId = memberForm.groupId.trim()
  memberForm.isLeader = 0

  if (!memberForm.groupId) {
    ElMessage.warning('请先输入项目组 ID')
    return
  }

  submitting.value = true
  try {
    await createProjectGroupMemberApi(memberForm)
    ElMessage.success('加入项目组成功')
    dialogVisible.value = false
    fetchMemberList()
  } finally {
    submitting.value = false
  }
}

const handleView = async (row: ProjectGroupMemberItem) => {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await getProjectGroupMemberDetailApi(row.id)
    detailData.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const handleDelete = async (row: ProjectGroupMemberItem) => {
  const actionText = deleteActionText(row)

  await ElMessageBox.confirm(`确认${actionText}该项目组成员吗？操作后将立即生效。`, `${actionText}确认`, {
    type: 'warning',
    confirmButtonText: `确认${actionText}`,
    cancelButtonText: '取消',
  })

  deletingId.value = row.id
  try {
    await deleteProjectGroupMemberApi(row.id)
    ElMessage.success(`项目组成员${actionText}成功`)
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    fetchMemberList()
  } finally {
    deletingId.value = ''
  }
}

const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchMemberList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchMemberList()
}

onMounted(() => {
  Promise.allSettled([
    fetchGroupOptions(),
    fetchStudentOptions(),
    fetchUserOptions(),
    fetchMemberList(),
  ])
})
</script>

<template>
  <div class="project-group-member-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div class="hero-main">
          <div class="hero-badge">Project Group Member Management</div>
          <h1>项目组成员管理</h1>
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
        <el-form-item label="目标项目组">
          <el-select
            v-model="queryForm.groupId"
            clearable
            filterable
            placeholder="请选择项目组"
            style="width: 280px"
          >
            <el-option
              v-for="item in groupOptions"
              :key="item.id"
              :label="getGroupLabel(item.id)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="成员">
          <el-select v-model="queryForm.userId" clearable filterable placeholder="请选择成员" style="width: 240px">
            <el-option v-for="item in studentOptions" :key="item.id" :label="getStudentLabel(item.id)" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="身份">
          <el-select
            v-model="queryForm.isLeader"
            clearable
            placeholder="请选择身份"
            style="width: 140px"
          >
            <el-option label="组长" :value="1" />
            <el-option label="成员" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="queryForm.status"
            clearable
            placeholder="请选择状态"
            style="width: 140px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button v-if="canJoin" type="success" :icon="Plus" @click="handleCreate">加入项目组</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <div>
            <div class="table-title">成员列表</div>
          </div>
          <div class="header-tags">
            <el-tag type="info">共 {{ total }} 条</el-tag>
            <el-tag type="primary">{{ roleHint }}</el-tag>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe class="project-group-member-table">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="目标项目组" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              {{ getGroupLabel(row.groupId) }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="成员" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ getStudentLabel(row.userId) }}</template>
        </el-table-column>
        <el-table-column label="身份" width="100">
          <template #default="{ row }">
            <el-tag :type="getLeaderTagType(row.isLeader)" effect="light">
              {{ getLeaderLabel(row.isLeader) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="加入时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.joinTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="canDeleteRow(row)"
              type="danger"
              link
              :icon="Delete"
              :loading="deletingId === row.id"
              @click="handleDelete(row)"
            >
              {{ deleteActionText(row) }}
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

    <ProjectGroupMemberJoinDialog
      v-model="dialogVisible"
      :form-data="memberForm"
      :submitting="submitting"
      @submit="handleSubmit"
    />

    <el-dialog v-model="detailVisible" title="项目组成员详情" width="760px" destroy-on-close>
      <div v-loading="detailLoading" class="detail-panel detail-loading-panel">
        <template v-if="detailData">
        <div class="detail-header">
          <div>
            <h2>{{ getGroupLabel(detailData.groupId) }}</h2>
            <p>成员：{{ getStudentLabel(detailData.userId) }}</p>
          </div>
          <div class="detail-tags">
            <el-tag :type="getLeaderTagType(detailData.isLeader)" effect="light">
              {{ getLeaderLabel(detailData.isLeader) }}
            </el-tag>
            <el-tag :type="getStatusTagType(detailData.status)" effect="light">
              {{ getStatusLabel(detailData.status) }}
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="目标项目组">
            {{ getGroupLabel(detailData.groupId) }}
          </el-descriptions-item>
          <el-descriptions-item label="成员">
            {{ getStudentLabel(detailData.userId) }}
          </el-descriptions-item>
          <el-descriptions-item label="成员身份">
            {{ getLeaderLabel(detailData.isLeader) }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ getStatusLabel(detailData.status) }}
          </el-descriptions-item>
          <el-descriptions-item label="加入时间" :span="2">
            {{ formatDateTime(detailData.joinTime) }}
          </el-descriptions-item>
        </el-descriptions>
        </template>
        <el-skeleton v-else :rows="6" animated />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.project-group-member-page {
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

@media (max-width: 1024px) {
  .hero-content {
    flex-direction: column;
  }

  .hero-side {
    min-width: 0;
  }
}
</style>
